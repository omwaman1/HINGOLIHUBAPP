"""
Import Hingoli Electronics Shops
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

CATEGORY_ID = 150003  # Mobiles & Electronics (parent)
ADMIN_USER_ID = 1
DEFAULT_IMAGE = 'https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=640'

# Smart categorization mappings
SUBCATEGORY_MAP = {
    'mobile': 150027,      # Mobile Phones
    'phone': 150027,
    'jio': 150027,
    'mi store': 150027,
    'laptop': 150029,      # Laptops
    'computer': 150030,    # Desktop Computers
    'desktop': 150030,
    'camera': 150031,      # Cameras
    'photo': 150031,
    'tv': 150032,          # TV & Video
    'video': 150032,
    'audio': 150033,       # Audio & Speakers
    'speaker': 150033,
    'printer': 150036,     # Printers & Scanners
    'gaming': 150035,      # Gaming
}
DEFAULT_SUBCATEGORY = 150034  # Computer Accessories (general)

def get_subcategory(title):
    """Smart categorization based on business name"""
    title_lower = title.lower()
    for keyword, subcat_id in SUBCATEGORY_MAP.items():
        if keyword in title_lower:
            return subcat_id
    return DEFAULT_SUBCATEGORY

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

def import_electronics():
    with open('hingoli_electronics.json', 'r', encoding='utf-8') as f:
        shops = json.load(f)
    
    # Filter only Hingoli and Basmath
    valid = [s for s in shops if 'hingoli' in s.get('address', '').lower() or 'basmath' in s.get('address', '').lower()]
    
    print(f"Found {len(valid)} Hingoli/Basmath electronics shops out of {len(shops)} total")
    
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    imported = 0
    
    for shop in valid:
        title = shop.get('name', '').strip()
        title = re.sub(r'[^\x00-\x7F]+', '', title).strip()
        if not title or check_duplicate(cursor, title):
            continue
        
        print(f"\nImporting: {title}")
        
        address = clean_address(shop.get('address', ''))
        if not address:
            address = 'Hingoli'
        
        phone = extract_phone(shop.get('phone', ''))
        rating = float(shop.get('rating') or 0)
        review_count = int(shop.get('review_count') or 0)
        images = [img for img in shop.get('images', []) if 'jdmagicbox.com' in img and 'icontent' not in img]
        
        timestamp = int(time.time())
        name_slug = sanitize_name(title)
        
        main_image_url = DEFAULT_IMAGE
        if images:
            filename = f"listings/electronics/{name_slug}_{timestamp}.webp"
            uploaded = upload_to_r2(images[0], filename)
            if uploaded:
                main_image_url = uploaded
        
        listing_id = get_next_listing_id(cursor)
        subcategory_id = get_subcategory(title)
        
        cursor.execute("""
            INSERT INTO listings 
            (listing_id, listing_type, title, description, category_id, subcategory_id,
             location, city, state, country, main_image_url, user_id, status,
             is_verified, avg_rating, review_count)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """, (listing_id, 'business', title, address, CATEGORY_ID, subcategory_id,
            address, 'Hingoli', 'Maharashtra', 'India', main_image_url, ADMIN_USER_ID, 
            'active', 1, rating, review_count))
        
        cursor.execute("""
            INSERT INTO business_listings (listing_id, business_name, business_phone)
            VALUES (%s, %s, %s)
        """, (listing_id, title, phone))
        
        conn.commit()
        imported += 1
        print(f"    -> #{listing_id} (subcat: {subcategory_id})")
        time.sleep(0.2)
    
    cursor.close()
    conn.close()
    print(f"\n{'='*50}")
    print(f"Imported {imported} electronics shops")
    print('='*50)

if __name__ == "__main__":
    import_electronics()
