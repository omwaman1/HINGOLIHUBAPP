"""
JustDial Business Data Extractor - Improved Version
Uses specific CSS selectors for accurate data extraction
"""

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options
import json
import time
import csv
import random
import re

class JustDialScraper:
    def __init__(self, headless=False):
        """Initialize Chrome browser"""
        options = Options()
        if headless:
            options.add_argument('--headless=new')
        options.add_argument('--no-sandbox')
        options.add_argument('--disable-dev-shm-usage')
        options.add_argument('--disable-blink-features=AutomationControlled')
        options.add_experimental_option('excludeSwitches', ['enable-automation'])
        options.add_argument('--window-size=1920,1080')
        options.add_argument('user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36')
        
        self.driver = webdriver.Chrome(options=options)
        self.driver.execute_script("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})")
        self.wait = WebDriverWait(self.driver, 10)
        
    def scroll_page(self, times=5):
        """Scroll to load more results"""
        for _ in range(times):
            self.driver.execute_script("window.scrollBy(0, 800)")
            time.sleep(0.5)
    
    def extract_listings(self, max_items=3):
        """Extract listings from current page"""
        businesses = []
        time.sleep(3)
        
        # Scroll more to load all results
        print("Scrolling to load all results...")
        self.scroll_page(15)
        time.sleep(2)
        
        # Find all result boxes
        listings = self.driver.find_elements(By.CSS_SELECTOR, 'div[class*="resultbox_info"]')
        
        if not listings:
            # Try parent resultbox
            listings = self.driver.find_elements(By.CSS_SELECTOR, 'div[class*="resultbox "]')
        
        print(f"Found {len(listings)} listing elements")
        
        for i, listing in enumerate(listings[:max_items]):
            try:
                business = self.extract_business_details(listing)
                if business and business.get('name'):
                    businesses.append(business)
                    print(f"\n  [{i+1}] {business['name']}")
                    print(f"      Address: {business['address']}")
                    print(f"      Phone: {business['phone']}")
                    print(f"      Rating: {business['rating']}")
                    print(f"      Images: {len(business.get('images', []))}")
            except Exception as e:
                print(f"  Error extracting listing {i+1}: {e}")
                continue
        
        return businesses
    
    def extract_business_details(self, element):
        """Extract detailed info from a listing element"""
        business = {
            'name': '',
            'address': '',
            'phone': '',
            'rating': '',
            'review_count': '',
            'url': '',
            'images': [],
            'timing': ''
        }
        
        # Get parent resultbox for full data
        try:
            parent = element.find_element(By.XPATH, './ancestor::div[contains(@class, "resultbox ")]')
        except:
            parent = element
        
        # === BUSINESS NAME ===
        name_selectors = [
            'span.lng_cont_name',
            'a.lng_cont_name', 
            'h2.resultbox_title_anchor span',
            'a[class*="resultbox_title"]'
        ]
        for selector in name_selectors:
            try:
                name_el = parent.find_element(By.CSS_SELECTOR, selector)
                business['name'] = name_el.text.strip()
                if business['name']:
                    break
            except:
                continue
        
        # === FULL ADDRESS ===
        # Using: div.locatcity.font15.fw400.color111
        address_selectors = [
            'div.locatcity',
            'div[class*="locatcity"]',
            'span.cont.paddr',
            'div.address-info span'
        ]
        for selector in address_selectors:
            try:
                addr_el = parent.find_element(By.CSS_SELECTOR, selector)
                business['address'] = addr_el.text.strip()
                if business['address']:
                    break
            except:
                continue
        
        # === PHONE NUMBER ===
        # Using: span.callcontent.callNowAnchor
        phone_selectors = [
            'span.callcontent',
            'span[class*="callcontent"]',
            'a.contact-info',
            'span.mobilesv'
        ]
        for selector in phone_selectors:
            try:
                phone_el = parent.find_element(By.CSS_SELECTOR, selector)
                phone_text = phone_el.text.strip()
                # Extract numbers only
                phone_nums = re.findall(r'\d{10,}', phone_text)
                if phone_nums:
                    business['phone'] = phone_nums[0]
                    break
                elif phone_text:
                    business['phone'] = phone_text
                    break
            except:
                continue
        
        # === RATING ===
        # Using: li.resultbox_totalrate (contains rating like "4.9")
        rating_selectors = [
            'li.resultbox_totalrate',
            'li[class*="resultbox_totalrate"]',
            'span.green-box',
            'span[class*="rating"]'
        ]
        for selector in rating_selectors:
            try:
                rating_el = parent.find_element(By.CSS_SELECTOR, selector)
                rating_text = rating_el.text.strip()
                # Extract rating number (like 4.9)
                rating_match = re.search(r'(\d+\.?\d*)', rating_text)
                if rating_match:
                    business['rating'] = rating_match.group(1)
                    break
            except:
                continue
        
        # === REVIEW COUNT ===
        # Using: li.resultbox_countrate (contains "19 Ratings")
        try:
            review_el = parent.find_element(By.CSS_SELECTOR, 'li.resultbox_countrate, li[class*="resultbox_countrate"]')
            review_text = review_el.text.strip()
            review_match = re.search(r'(\d+)', review_text)
            if review_match:
                business['review_count'] = review_match.group(1)
        except:
            pass
        
        # === IMAGES (max 4) ===
        # From carousel: img[src*="jdmagicbox.com"]
        try:
            images = parent.find_elements(By.CSS_SELECTOR, 'img[src*="jdmagicbox.com"]')
            for img in images[:4]:  # Max 4 images
                src = img.get_attribute('src')
                if src and 'jdmagicbox' in src and 'data:image' not in src:
                    # Get higher resolution version
                    src = re.sub(r'\?w=\d+', '?w=640', src)
                    if src not in business['images']:
                        business['images'].append(src)
                        if len(business['images']) >= 4:
                            break
        except:
            pass
        
        # === URL ===
        try:
            link = parent.find_element(By.CSS_SELECTOR, 'a[href*="/Hingoli/"]')
            business['url'] = link.get_attribute('href')
        except:
            pass
        
        # === TIMING ===
        try:
            timing_el = parent.find_element(By.CSS_SELECTOR, 'span[class*="opentxt"], div[class*="open"]')
            business['timing'] = timing_el.text.strip()
        except:
            pass
        
        return business
    
    def search_category(self, city, category, max_items=3):
        """Search for businesses in a category"""
        url = f"https://www.justdial.com/{city}/{category}"
        print(f"\nOpening: {url}")
        
        self.driver.get(url)
        time.sleep(3)
        
        businesses = self.extract_listings(max_items)
        return businesses
    
    def save_results(self, businesses, filename_base):
        """Save to CSV and JSON"""
        if not businesses:
            print("No businesses to save")
            return
        
        # CSV
        csv_file = f"{filename_base}.csv"
        keys = ['name', 'address', 'phone', 'rating', 'review_count', 'timing', 'url', 'images', 'source_category']
        with open(csv_file, 'w', newline='', encoding='utf-8') as f:
            writer = csv.DictWriter(f, fieldnames=keys, extrasaction='ignore')
            writer.writeheader()
            for b in businesses:
                row = b.copy()
                row['images'] = ' | '.join(b.get('images', []))  # Convert list to string
                writer.writerow(row)
        print(f"\n[OK] Saved to {csv_file}")
        
        # JSON
        json_file = f"{filename_base}.json"
        with open(json_file, 'w', encoding='utf-8') as f:
            json.dump(businesses, f, indent=2, ensure_ascii=False)
        print(f"[OK] Saved to {json_file}")
    
    def close(self):
        """Close browser"""
        self.driver.quit()


def main():
    print("="*60)
    print("JustDial Business Extractor - Hingoli (Electronics)")
    print("="*60)
    
    # Extract Electronics
    categories = ["Electronics-Shops"]
    max_items = 100  # Extract all (up to 100)
    
    scraper = JustDialScraper(headless=False)
    all_businesses = []
    
    try:
        for category in categories:
            print(f"\n{'='*50}")
            print(f"Category: {category} (extracting up to {max_items} items)")
            print('='*50)
            
            businesses = scraper.search_category("Hingoli", category, max_items)
            
            for b in businesses:
                b['source_category'] = category
            
            all_businesses.extend(businesses)
            print(f"\nExtracted {len(businesses)} businesses from {category}")
        
        # Save results
        scraper.save_results(all_businesses, 'hingoli_electronics')
        
        print(f"\n{'='*60}")
        print(f"TOTAL: {len(all_businesses)} schools extracted")
        print("="*60)
        
    finally:
        scraper.close()


if __name__ == "__main__":
    main()
