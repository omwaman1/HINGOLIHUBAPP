---
description: Scrape business data from JustDial and import into HelloHingoli database
---

# JustDial Business Scraping Workflow

This workflow scrapes businesses from JustDial for Hingoli and imports them into the HelloHingoli database.

## Prerequisites
- Python 3.x installed
- Chrome browser installed
- Required packages: `selenium`, `pymysql`, `requests`

// turbo
1. Install Python dependencies:
```powershell
python -m pip install selenium pymysql requests beautifulsoup4
```

## Step 1: Configure the Scraper

2. Open `c:\Users\Meeting\Desktop\MH\scripts\justdial_selenium.py`

3. Modify the categories to scrape (around line 213):
```python
categories = [
    "Schools",
    "Hospitals",
    "Restaurants",
    "Hotels",
    "Coaching-Classes",
    # Add more categories here
]
```

4. Set `max_items` for how many to extract per category:
```python
max_items = 100  # Change as needed
```

## Step 2: Run the Scraper

// turbo
5. Navigate to scripts folder and run scraper:
```powershell
cd c:\Users\Meeting\Desktop\MH\scripts
python justdial_selenium.py
```

6. **Wait for browser to open** - Chrome will launch and navigate to JustDial pages. Do not close it.

7. **Output files created**:
   - `hingoli_all_schools.json` - Full data with images
   - `hingoli_all_schools.csv` - Spreadsheet format

## Step 3: Configure the Import Script

8. Open `c:\Users\Meeting\Desktop\MH\scripts\import_schools.py`

9. Update these settings as needed:
```python
CATEGORY_ID = 150090      # Parent category (Education)
SUBCATEGORY_ID = 150128   # Subcategory (Schools)
ADMIN_USER_ID = 1         # Owner user ID
DEFAULT_IMAGE = 'https://images.unsplash.com/...'  # Fallback image
```

10. Find correct category IDs:
```powershell
C:\xampp\mysql\bin\mysql.exe -h gateway01.ap-southeast-1.prod.aws.tidbcloud.com -P 4000 -u "39rSBGEWyaX8SaD.root" -p"lOUBAGjTSM0SvHIt" --ssl hellohingoli -e "SELECT category_id, name, parent_id, listing_type FROM categories WHERE name LIKE '%keyword%';"
```

## Step 4: Test with Dry Run

// turbo
11. Test import without database changes:
```powershell
cd c:\Users\Meeting\Desktop\MH\scripts
chcp 65001 ; python import_schools.py --dry-run
```

12. Review output - check for duplicate handling, image uploads, etc.

## Step 5: Run Actual Import

// turbo
13. Run the import (actually inserts to database):
```powershell
cd c:\Users\Meeting\Desktop\MH\scripts
chcp 65001 ; python import_schools.py
```

## Step 6: Verify in Database

// turbo
14. Count imported listings:
```powershell
C:\xampp\mysql\bin\mysql.exe -h gateway01.ap-southeast-1.prod.aws.tidbcloud.com -P 4000 -u "39rSBGEWyaX8SaD.root" -p"lOUBAGjTSM0SvHIt" --ssl hellohingoli -e "SELECT COUNT(*) FROM listings WHERE subcategory_id = 150128;"
```

// turbo
15. Check gallery images:
```powershell
C:\xampp\mysql\bin\mysql.exe -h gateway01.ap-southeast-1.prod.aws.tidbcloud.com -P 4000 -u "39rSBGEWyaX8SaD.root" -p"lOUBAGjTSM0SvHIt" --ssl hellohingoli -e "SELECT COUNT(*) FROM listing_images WHERE listing_id >= 36;"
```

---

## Data Extraction Details

### Fields Extracted from JustDial
| Field | CSS Selector |
|-------|--------------|
| Name | `span.lng_cont_name` |
| Address | `div.locatcity` |
| Phone | `span.callcontent` |
| Rating | `li.resultbox_totalrate` |
| Reviews | `li.resultbox_countrate` |
| Images | `img[src*="jdmagicbox.com"]` |

### R2 Image Upload
- **Path**: `listings/{category}/{business-name}_{timestamp}.webp`
- **Public URL**: `https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/...`

### Database Tables Updated
1. `listings` - Main listing record
2. `business_listings` - Business details (name, phone)
3. `listing_images` - Gallery images

---

## Adapting for New Categories

16. Duplicate import script for new category:
```powershell
copy import_schools.py import_hospitals.py
```

17. Update these values in new file:
```python
CATEGORY_ID = 150091      # New parent category
SUBCATEGORY_ID = 150140   # New subcategory
DEFAULT_IMAGE = '...'     # Category-appropriate default
json_path = 'hingoli_hospitals.json'  # New data file
```

18. Update R2 path in `upload_to_r2` function:
```python
main_filename = f"listings/hospitals/{name_slug}_{timestamp}.webp"
```

---

## Troubleshooting

### JustDial Blocking Requests
- Use Selenium version (not requests-based)
- Add delays between pages
- Use realistic user-agent

### Database Connection Issues
- Use PyMySQL (not mysql-connector-python)
- Ensure SSL config: `'ssl': {'ssl': {}}`

### Images Not Loading
- Check JustDial URL is valid
- Some listings have no images - uses default

### Console Encoding Issues
- Run: `chcp 65001` before Python commands
