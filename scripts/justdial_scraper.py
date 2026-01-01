"""
JustDial Business Data Extractor
Extracts business listings from JustDial for Hingoli city
"""

import requests
from bs4 import BeautifulSoup
import json
import time
import re
import csv
from urllib.parse import urljoin, quote
import random

class JustDialScraper:
    def __init__(self):
        self.base_url = "https://www.justdial.com"
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8',
            'Accept-Language': 'en-US,en;q=0.9',
            'Accept-Encoding': 'gzip, deflate, br',
            'Connection': 'keep-alive',
            'Upgrade-Insecure-Requests': '1',
            'Cache-Control': 'max-age=0',
        })
        
    def decode_phone(self, encoded_chars):
        """Decode JustDial's obfuscated phone numbers"""
        # JustDial uses character substitution for phone numbers
        decode_map = {
            'acb': '0', 'bc': '1', 'cb': '2', 'dc': '3',
            'ed': '4', 'fe': '5', 'gf': '6', 'hg': '7',
            'ih': '8', 'ji': '9'
        }
        phone = ''
        for char_class in encoded_chars:
            for key, value in decode_map.items():
                if key in char_class:
                    phone += value
                    break
        return phone
    
    def extract_business_from_detail_page(self, url):
        """Extract detailed info from a single business page"""
        try:
            time.sleep(random.uniform(1, 3))  # Polite delay
            response = self.session.get(url, timeout=15)
            response.raise_for_status()
            
            soup = BeautifulSoup(response.content, 'html.parser')
            
            business = {
                'url': url,
                'name': '',
                'address': '',
                'phone': '',
                'rating': '',
                'reviews': '',
                'categories': [],
                'timing': '',
                'photos': []
            }
            
            # Business name
            name_tag = soup.find('h1', class_='fn') or soup.find('span', class_='fn')
            if name_tag:
                business['name'] = name_tag.get_text(strip=True)
            
            # Address
            address_tag = soup.find('span', class_='address') or soup.find('span', class_='cont')
            if address_tag:
                business['address'] = address_tag.get_text(strip=True)
            
            # Rating
            rating_tag = soup.find('span', class_='rating') or soup.find('span', class_='rate-count')
            if rating_tag:
                business['rating'] = rating_tag.get_text(strip=True)
            
            # Photos
            photo_tags = soup.find_all('img', class_='photo') or soup.find_all('img', {'data-src': True})
            for img in photo_tags[:5]:  # Limit to 5 photos
                src = img.get('data-src') or img.get('src')
                if src and 'justdial' in src:
                    business['photos'].append(src)
            
            return business
            
        except Exception as e:
            print(f"Error extracting {url}: {e}")
            return None
    
    def search_businesses(self, city, category, max_pages=3):
        """Search for businesses in a category"""
        businesses = []
        
        # Format search URL
        search_url = f"{self.base_url}/{city}/{category.replace(' ', '-')}"
        
        for page in range(1, max_pages + 1):
            print(f"Scraping page {page}...")
            
            try:
                if page > 1:
                    url = f"{search_url}/page-{page}"
                else:
                    url = search_url
                    
                time.sleep(random.uniform(2, 4))
                response = self.session.get(url, timeout=15)
                
                if response.status_code != 200:
                    print(f"Failed to fetch page {page}: {response.status_code}")
                    break
                    
                soup = BeautifulSoup(response.content, 'html.parser')
                
                # Find business listings
                listings = soup.find_all('li', class_='cntanr') or soup.find_all('div', class_='store-details')
                
                if not listings:
                    print(f"No listings found on page {page}")
                    break
                
                for listing in listings:
                    business = self.extract_listing(listing)
                    if business:
                        businesses.append(business)
                        print(f"Found: {business['name']}")
                
            except Exception as e:
                print(f"Error on page {page}: {e}")
                break
        
        return businesses
    
    def extract_listing(self, listing):
        """Extract info from a listing card"""
        try:
            business = {
                'name': '',
                'address': '',
                'phone': '',
                'rating': '',
                'category': '',
                'detail_url': ''
            }
            
            # Name and URL
            name_link = listing.find('a', class_='store-name') or listing.find('span', class_='lng_cont_name')
            if name_link:
                business['name'] = name_link.get_text(strip=True)
                if name_link.get('href'):
                    business['detail_url'] = urljoin(self.base_url, name_link['href'])
            
            # Address
            addr = listing.find('span', class_='cont') or listing.find('span', class_='loc')
            if addr:
                business['address'] = addr.get_text(strip=True)
            
            # Rating
            rating = listing.find('span', class_='rating') or listing.find('span', class_='green-box')
            if rating:
                business['rating'] = rating.get_text(strip=True)
            
            return business if business['name'] else None
            
        except Exception as e:
            print(f"Error extracting listing: {e}")
            return None
    
    def save_to_csv(self, businesses, filename):
        """Save businesses to CSV file"""
        if not businesses:
            print("No businesses to save")
            return
            
        keys = businesses[0].keys()
        with open(filename, 'w', newline='', encoding='utf-8') as f:
            writer = csv.DictWriter(f, fieldnames=keys)
            writer.writeheader()
            writer.writerows(businesses)
        print(f"Saved {len(businesses)} businesses to {filename}")
    
    def save_to_json(self, businesses, filename):
        """Save businesses to JSON file"""
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump(businesses, f, indent=2, ensure_ascii=False)
        print(f"Saved {len(businesses)} businesses to {filename}")


def main():
    scraper = JustDialScraper()
    
    # Categories to search in Hingoli
    categories = [
        "CBSE-Schools",
        "Schools",
        "Coaching-Classes",
        "Hospitals",
        "Restaurants",
        "Hotels"
    ]
    
    all_businesses = []
    
    for category in categories:
        print(f"\n{'='*50}")
        print(f"Searching: {category}")
        print('='*50)
        
        businesses = scraper.search_businesses("Hingoli", category, max_pages=2)
        
        for b in businesses:
            b['source_category'] = category
            all_businesses.append(b)
        
        print(f"Found {len(businesses)} in {category}")
    
    # Save results
    if all_businesses:
        scraper.save_to_csv(all_businesses, 'hingoli_businesses.csv')
        scraper.save_to_json(all_businesses, 'hingoli_businesses.json')
        
        print(f"\n{'='*50}")
        print(f"Total: {len(all_businesses)} businesses extracted")
        print(f"Saved to: hingoli_businesses.csv and hingoli_businesses.json")
    else:
        print("\nNo businesses found. JustDial may be blocking requests.")
        print("Try using a VPN or the Selenium version for browser automation.")


if __name__ == "__main__":
    main()
