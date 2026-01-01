"""
Fix R2 Images Script
Re-uploads images for existing school listings using boto3
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

DEFAULT_IMAGE = 'https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=640'

def sanitize_name(name):
    name = name.lower()
    name = re.sub(r'[^a-z0-9\s-]', '', name)
    name = re.sub(r'\s+', '-', name)
    return name[:50]

def get_s3_client():
    return boto3.client(
        's3',
        endpoint_url=R2_CONFIG['endpoint'],
        aws_access_key_id=R2_CONFIG['access_key'],
        aws_secret_access_key=R2_CONFIG['secret_key'],
        region_name='auto'
    )

def upload_to_r2(image_url, filename):
    """Download and upload to R2"""
    try:
        response = requests.get(image_url, timeout=30)
        if response.status_code != 200:
            print(f"    Download failed: {response.status_code}")
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

def fix_images():
    # Load original JSON with JustDial URLs
    with open('hingoli_all_schools.json', 'r', encoding='utf-8') as f:
        schools = json.load(f)
    
    print(f"Loaded {len(schools)} schools")
    
    # Connect to database
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    updated = 0
    
    for school in schools:
        title = school.get('name', '').strip()
        images = school.get('images', [])
        
        if not title or not images:
            continue
        
        # Find listing in database
        cursor.execute("SELECT listing_id FROM listings WHERE title = %s", (title,))
        result = cursor.fetchone()
        
        if not result:
            continue
        
        listing_id = result[0]
        print(f"\n[{listing_id}] {title}")
        
        # Upload main image
        timestamp = int(time.time())
        name_slug = sanitize_name(title)
        
        main_filename = f"listings/schools/{name_slug}_{timestamp}.webp"
        print(f"    Uploading main image...")
        main_url = upload_to_r2(images[0], main_filename)
        
        if main_url:
            cursor.execute("UPDATE listings SET main_image_url = %s WHERE listing_id = %s", 
                          (main_url, listing_id))
            print(f"    -> {main_url}")
            updated += 1
        else:
            # Use default
            cursor.execute("UPDATE listings SET main_image_url = %s WHERE listing_id = %s",
                          (DEFAULT_IMAGE, listing_id))
            print(f"    -> Using default image")
        
        # Upload gallery images
        cursor.execute("DELETE FROM listing_images WHERE listing_id = %s", (listing_id,))
        
        for idx, img_url in enumerate(images[1:4], 1):
            gallery_filename = f"listings/schools/{name_slug}_{timestamp}_{idx}.webp"
            print(f"    Uploading gallery {idx}...")
            gallery_url = upload_to_r2(img_url, gallery_filename)
            
            if gallery_url:
                cursor.execute("""
                    INSERT INTO listing_images (listing_id, image_url, sort_order, image_type)
                    VALUES (%s, %s, %s, 'gallery')
                """, (listing_id, gallery_url, idx-1))
        
        conn.commit()
        time.sleep(0.5)  # Rate limit
    
    cursor.close()
    conn.close()
    
    print(f"\n{'='*50}")
    print(f"Updated {updated} listings with R2 images")
    print('='*50)

if __name__ == "__main__":
    fix_images()
