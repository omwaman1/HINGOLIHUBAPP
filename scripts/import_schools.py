"""
School Import Script for HelloHingoli
Imports schools from JustDial JSON to database with R2 image upload
"""

import json
import re
import time
import requests
import hashlib
import hmac
from datetime import datetime
from urllib.parse import quote
import pymysql

# ========== CONFIGURATION ==========
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
SUBCATEGORY_ID = 150128  # Schools
ADMIN_USER_ID = 1
DEFAULT_IMAGE = 'https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=640'

# ========== HELPER FUNCTIONS ==========

def sanitize_name(name):
    """Convert name to URL-safe slug"""
    name = name.lower()
    name = re.sub(r'[^a-z0-9\s-]', '', name)
    name = re.sub(r'\s+', '-', name)
    name = name[:50]
    return name

def clean_address(address):
    """Remove city/state suffix from address"""
    if not address:
        return ''
    # Remove common suffixes
    patterns = [
        r',?\s*Hingoli$',
        r',?\s*Maharashtra$',
        r',?\s*Basmath$',
    ]
    cleaned = address
    for pattern in patterns:
        cleaned = re.sub(pattern, '', cleaned, flags=re.IGNORECASE)
    return cleaned.strip().rstrip(',').strip()

def extract_phone(phone_str):
    """Extract valid 10-digit phone number"""
    if not phone_str or phone_str == 'Show Number':
        return None
    # Extract digits
    digits = re.sub(r'\D', '', phone_str)
    # Remove leading 0 or 91
    if digits.startswith('91') and len(digits) > 10:
        digits = digits[2:]
    if digits.startswith('0') and len(digits) > 10:
        digits = digits[1:]
    if len(digits) == 10:
        return digits
    return None

def get_s3_client():
    """Get boto3 S3 client configured for R2"""
    import boto3
    return boto3.client(
        's3',
        endpoint_url=R2_CONFIG['endpoint'],
        aws_access_key_id=R2_CONFIG['access_key'],
        aws_secret_access_key=R2_CONFIG['secret_key'],
        region_name='auto'
    )

def upload_to_r2(image_url, filename):
    """Download image and upload to R2 using boto3"""
    try:
        # Download image
        response = requests.get(image_url, timeout=30)
        if response.status_code != 200:
            print(f"    Failed to download: {image_url}")
            return None
        
        image_data = response.content
        content_type = response.headers.get('Content-Type', 'image/jpeg')
        
        # Upload to R2 using boto3
        s3 = get_s3_client()
        s3.put_object(
            Bucket=R2_CONFIG['bucket'],
            Key=filename,
            Body=image_data,
            ContentType=content_type
        )
        
        public_url = f"{R2_CONFIG['public_url']}/{filename}"
        return public_url
            
    except Exception as e:
        print(f"    Upload error: {e}")
        return None

def get_next_listing_id(cursor):
    """Get next sequential listing ID"""
    cursor.execute("SELECT COALESCE(MAX(listing_id), 0) + 1 FROM listings")
    return cursor.fetchone()[0]

def check_duplicate(cursor, title):
    """Check if listing with same title exists"""
    cursor.execute("SELECT listing_id FROM listings WHERE title = %s LIMIT 1", (title,))
    result = cursor.fetchone()
    return result[0] if result else None

# ========== MAIN IMPORT FUNCTION ==========

def import_schools(json_path, dry_run=False):
    """Import schools from JSON file to database"""
    
    # Load data
    with open(json_path, 'r', encoding='utf-8') as f:
        schools = json.load(f)
    
    print(f"Loaded {len(schools)} schools from {json_path}")
    
    # Connect to database
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    stats = {'imported': 0, 'skipped_duplicate': 0, 'errors': 0}
    seen_titles = set()
    
    try:
        for i, school in enumerate(schools):
            title = school.get('name', '').strip()
            
            if not title:
                print(f"[{i+1}] Skipping: no name")
                continue
            
            # Skip duplicates within import file
            if title in seen_titles:
                print(f"[{i+1}] Skipping duplicate in file: {title}")
                stats['skipped_duplicate'] += 1
                continue
            seen_titles.add(title)
            
            # Check database duplicates
            existing_id = check_duplicate(cursor, title)
            if existing_id:
                print(f"[{i+1}] Skipping (exists #{existing_id}): {title}")
                stats['skipped_duplicate'] += 1
                continue
            
            print(f"\n[{i+1}] Processing: {title}")
            
            # Clean data
            address = clean_address(school.get('address', ''))
            phone = extract_phone(school.get('phone', ''))
            rating = float(school.get('rating') or 0)
            review_count = int(school.get('review_count') or 0)
            images = school.get('images', [])
            
            # Upload images to R2
            timestamp = int(time.time())
            name_slug = sanitize_name(title)
            
            main_image_url = DEFAULT_IMAGE
            gallery_urls = []
            
            if images:
                # Main image
                main_filename = f"listings/schools/{name_slug}_{timestamp}.webp"
                print(f"    Uploading main image...")
                uploaded_main = upload_to_r2(images[0], main_filename)
                if uploaded_main:
                    main_image_url = uploaded_main
                    print(f"    -> {uploaded_main}")
                
                # Gallery images
                for idx, img_url in enumerate(images[1:4], 1):  # Max 3 gallery
                    gallery_filename = f"listings/schools/{name_slug}_{timestamp}_{idx}.webp"
                    print(f"    Uploading gallery {idx}...")
                    uploaded = upload_to_r2(img_url, gallery_filename)
                    if uploaded:
                        gallery_urls.append(uploaded)
            
            if dry_run:
                print(f"    [DRY RUN] Would insert: {title}")
                stats['imported'] += 1
                continue
            
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
            
            # Insert gallery images
            for idx, gallery_url in enumerate(gallery_urls):
                cursor.execute("""
                    INSERT INTO listing_images (listing_id, image_url, sort_order, image_type)
                    VALUES (%s, %s, %s, 'gallery')
                """, (listing_id, gallery_url, idx))
            
            conn.commit()
            stats['imported'] += 1
            print(f"    -> Inserted as #{listing_id}")
            
    except Exception as e:
        print(f"\nERROR: {e}")
        stats['errors'] += 1
        conn.rollback()
        raise
        
    finally:
        cursor.close()
        conn.close()
    
    print(f"\n{'='*50}")
    print(f"Import Complete!")
    print(f"  Imported: {stats['imported']}")
    print(f"  Skipped (duplicates): {stats['skipped_duplicate']}")
    print(f"  Errors: {stats['errors']}")
    print('='*50)
    
    return stats


if __name__ == "__main__":
    import sys
    
    json_path = 'hingoli_all_schools.json'
    dry_run = '--dry-run' in sys.argv
    
    if dry_run:
        print("=== DRY RUN MODE (no database changes) ===\n")
    
    import_schools(json_path, dry_run=dry_run)
