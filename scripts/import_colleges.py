"""
Import Hingoli Colleges
Imports only colleges that are actually in Hingoli
"""

import json
import re
import time
import requests
import pymysql
import boto3

# Configuration
DB_CONFIG = {
    'host': 'gateway01.ap-southeast-1.prod.aws.tidbcloud.com',
    'port': 4000,
    'user': '39rSBGEWyaX8SaD.root',
    'password': 'lOUBAGjTSM0SvHIt',
    'database': 'hellohingoli',
    'ssl': {'ssl': {}}
}

R2_CONFIG = {
    'endpoint': 'https://62b435cd6e08605f2c7c1aadedc6a591.r2.cloudflarestorage.com',
    'access_key': '6d12f3c5c7a0b68722e46063c8befec4',
    'secret_key': 'fd01dd18c77b8de4ccbf036b4dfafdaa062c5a32685dc8b482ac40d0a9d50d60',
    'bucket': 'hello-hingoli-bucket',
    'public_url': 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev'
}

CATEGORY_ID = 150090  # Education (parent)
SUBCATEGORY_ID = 150129  # Colleges
ADMIN_USER_ID = 1
DEFAULT_IMAGE = 'https://images.unsplash.com/photo-1562774053-701939374585?w=640'  # College image

def sanitize_name(name):
    name = name.lower()
    name = re.sub(r'[^a-z0-9\s-]', '', name)
    name = re.sub(r'\s+', '-', name)
    return name[:50]

def clean_address(address):
    if not address:
        return ''
    patterns = [r',?\s*Hingoli$', r',?\s*Maharashtra$']
    cleaned = address
    for pattern in patterns:
        cleaned = re.sub(pattern, '', cleaned, flags=re.IGNORECASE)
    return cleaned.strip().rstrip(',').strip()

def extract_phone(phone_str):
    if not phone_str or phone_str == 'Show Number':
        return None
    digits = re.sub(r'\D', '', phone_str)
    if digits.startswith('91') and len(digits) > 10:
        digits = digits[2:]
    if len(digits) == 10:
        return digits
    return None

def get_s3_client():
    return boto3.client(
        's3',
        endpoint_url=R2_CONFIG['endpoint'],
        aws_access_key_id=R2_CONFIG['access_key'],
        aws_secret_access_key=R2_CONFIG['secret_key'],
        region_name='auto'
    )

def upload_to_r2(image_url, filename):
    try:
        # Skip JustDial badge images
        if 'jdmagicbox.com/images/icontent' in image_url:
            return None
            
        response = requests.get(image_url, timeout=30)
        if response.status_code != 200:
            return None
        
        s3 = get_s3_client()
        s3.put_object(
            Bucket=R2_CONFIG['bucket'],
            Key=filename,
            Body=response.content,
            ContentType=response.headers.get('Content-Type', 'image/jpeg')
        )
        return f"{R2_CONFIG['public_url']}/{filename}"
    except Exception as e:
        print(f"    Upload error: {e}")
        return None

def get_next_listing_id(cursor):
    cursor.execute("SELECT COALESCE(MAX(listing_id), 0) + 1 FROM listings")
    return cursor.fetchone()[0]

def check_duplicate(cursor, title):
    cursor.execute("SELECT listing_id FROM listings WHERE title = %s LIMIT 1", (title,))
    result = cursor.fetchone()
    return result[0] if result else None

def import_colleges():
    # Load data
    with open('hingoli_colleges.json', 'r', encoding='utf-8') as f:
        all_colleges = json.load(f)
    
    # Filter only Hingoli colleges
    hingoli_colleges = [c for c in all_colleges if 'hingoli' in c.get('address', '').lower()]
    
    print(f"Found {len(hingoli_colleges)} Hingoli colleges out of {len(all_colleges)} total")
    
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    imported = 0
    
    for college in hingoli_colleges:
        title = college.get('name', '').strip()
        
        # Check duplicate
        if check_duplicate(cursor, title):
            print(f"Skipping duplicate: {title}")
            continue
        
        print(f"\nImporting: {title}")
        
        address = clean_address(college.get('address', ''))
        phone = extract_phone(college.get('phone', ''))
        rating = float(college.get('rating') or 0)
        review_count = int(college.get('review_count') or 0)
        images = [img for img in college.get('images', []) if 'jdmagicbox.com/v2/comp' in img or 'jdmagicbox.com/comp' in img]
        
        # Upload images
        timestamp = int(time.time())
        name_slug = sanitize_name(title)
        
        main_image_url = DEFAULT_IMAGE
        if images:
            filename = f"listings/colleges/{name_slug}_{timestamp}.webp"
            print(f"    Uploading main image...")
            uploaded = upload_to_r2(images[0], filename)
            if uploaded:
                main_image_url = uploaded
                print(f"    -> {uploaded}")
        
        # Get next ID
        listing_id = get_next_listing_id(cursor)
        
        # Insert listing
        cursor.execute("""
            INSERT INTO listings 
            (listing_id, listing_type, title, description, category_id, subcategory_id,
             location, city, state, country, main_image_url, user_id, status,
             is_verified, avg_rating, review_count)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """, (
            listing_id, 'business', title, address,
            CATEGORY_ID, SUBCATEGORY_ID,
            address, 'Hingoli', 'Maharashtra', 'India',
            main_image_url, ADMIN_USER_ID, 'active',
            1, rating, review_count
        ))
        
        # Insert business_listings
        cursor.execute("""
            INSERT INTO business_listings (listing_id, business_name, business_phone)
            VALUES (%s, %s, %s)
        """, (listing_id, title, phone))
        
        # Upload gallery images
        for idx, img_url in enumerate(images[1:4], 1):
            gallery_filename = f"listings/colleges/{name_slug}_{timestamp}_{idx}.webp"
            print(f"    Uploading gallery {idx}...")
            gallery_url = upload_to_r2(img_url, gallery_filename)
            if gallery_url:
                cursor.execute("""
                    INSERT INTO listing_images (listing_id, image_url, sort_order, image_type)
                    VALUES (%s, %s, %s, 'gallery')
                """, (listing_id, gallery_url, idx-1))
        
        conn.commit()
        imported += 1
        print(f"    -> Inserted as #{listing_id}")
    
    cursor.close()
    conn.close()
    
    print(f"\n{'='*50}")
    print(f"Imported {imported} Hingoli colleges")
    print('='*50)

if __name__ == "__main__":
    import_colleges()
