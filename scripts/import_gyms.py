"""
Import Hingoli Gyms
"""

import json
import re
import time
import requests
import pymysql
import boto3

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

CATEGORY_ID = 150089  # Healthcare (parent)
SUBCATEGORY_ID = 150122  # Gym (using existing ID or create new)
ADMIN_USER_ID = 1
DEFAULT_IMAGE = 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=640'

def sanitize_name(name):
    name = name.lower()
    name = re.sub(r'[^a-z0-9\s-]', '', name)
    name = re.sub(r'\s+', '-', name)
    return name[:50]

def clean_address(address):
    if not address:
        return ''
    patterns = [r',?\s*Basmath$', r',?\s*Hingoli$', r',?\s*Maharashtra$']
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
    return boto3.client('s3', endpoint_url=R2_CONFIG['endpoint'],
        aws_access_key_id=R2_CONFIG['access_key'],
        aws_secret_access_key=R2_CONFIG['secret_key'], region_name='auto')

def upload_to_r2(image_url, filename):
    try:
        if 'jdmagicbox.com/images/icontent' in image_url:
            return None
        response = requests.get(image_url, timeout=30)
        if response.status_code != 200:
            return None
        s3 = get_s3_client()
        s3.put_object(Bucket=R2_CONFIG['bucket'], Key=filename, Body=response.content,
            ContentType=response.headers.get('Content-Type', 'image/jpeg'))
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

def import_gyms():
    with open('hingoli_gyms.json', 'r', encoding='utf-8') as f:
        gyms = json.load(f)
    
    # Filter only Hingoli and Basmath gyms
    valid_gyms = []
    for g in gyms:
        addr = g.get('address', '').lower()
        if 'hingoli' in addr or 'basmath' in addr:
            valid_gyms.append(g)
    
    print(f"Found {len(valid_gyms)} Hingoli/Basmath gyms out of {len(gyms)} total")
    
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    imported = 0
    
    for gym in valid_gyms:
        title = gym.get('name', '').strip()
        title = re.sub(r'[^\x00-\x7F]+', '', title).strip()
        if not title or check_duplicate(cursor, title):
            continue
        
        print(f"\nImporting: {title}")
        
        address = clean_address(gym.get('address', ''))
        if not address:
            address = 'Hingoli'
        
        phone = extract_phone(gym.get('phone', ''))
        rating = float(gym.get('rating') or 0)
        review_count = int(gym.get('review_count') or 0)
        images = [img for img in gym.get('images', []) if 'jdmagicbox.com/v2/comp' in img or 'jdmagicbox.com/comp' in img]
        
        timestamp = int(time.time())
        name_slug = sanitize_name(title)
        
        main_image_url = DEFAULT_IMAGE
        if images:
            filename = f"listings/gyms/{name_slug}_{timestamp}.webp"
            uploaded = upload_to_r2(images[0], filename)
            if uploaded:
                main_image_url = uploaded
        
        listing_id = get_next_listing_id(cursor)
        
        cursor.execute("""
            INSERT INTO listings 
            (listing_id, listing_type, title, description, category_id, subcategory_id,
             location, city, state, country, main_image_url, user_id, status,
             is_verified, avg_rating, review_count)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """, (listing_id, 'business', title, address, CATEGORY_ID, SUBCATEGORY_ID,
            address, 'Hingoli', 'Maharashtra', 'India', main_image_url, ADMIN_USER_ID, 
            'active', 1, rating, review_count))
        
        cursor.execute("""
            INSERT INTO business_listings (listing_id, business_name, business_phone)
            VALUES (%s, %s, %s)
        """, (listing_id, title, phone))
        
        conn.commit()
        imported += 1
        print(f"    -> #{listing_id}")
        time.sleep(0.3)
    
    cursor.close()
    conn.close()
    print(f"\n{'='*50}")
    print(f"Imported {imported} gyms")
    print('='*50)

if __name__ == "__main__":
    import_gyms()
