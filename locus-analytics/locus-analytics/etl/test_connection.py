import requests
from bs4 import BeautifulSoup

url = "https://www.zameen.com/Homes/Karachi-1-1.html"
headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    "Accept-Language": "en-US,en;q=0.9",
}

print(f"Testing URL: {url}")
try:
    resp = requests.get(url, headers=headers, timeout=10)
    print(f"Status Code: {resp.status_code}")
    if resp.status_code == 200:
        soup = BeautifulSoup(resp.text, "lxml")
        links = soup.find_all("a", href=True)
        property_links = [l['href'] for l in links if "/Property/" in l['href']]
        print(f"Found {len(property_links)} property links.")
        if property_links:
            print(f"First link: {property_links[0]}")
    else:
        print("Failed to load page.")
except Exception as e:
    print(f"Error: {e}")
