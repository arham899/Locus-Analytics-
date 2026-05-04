import requests
from bs4 import BeautifulSoup
import time
import random

# Basic user-agent rotation
USER_AGENTS = [
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0 Safari/537.36",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 Version/17.0 Safari/605.1.15",
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/118.0 Safari/537.36"
]

CITY_URLS = {
    "Lahore": "https://www.zameen.com/Homes/Lahore-1-1.html",
    "Karachi": "https://www.zameen.com/Homes/Karachi-2-1.html",
    "Islamabad": "https://www.zameen.com/Homes/Islamabad-3-1.html"
}


def get_headers():
    return {
        "User-Agent": random.choice(USER_AGENTS),
        "Accept-Language": "en-US,en;q=0.9"
    }


def fetch_listings(city="Lahore", pages=1):
    listings = []
    current_delay = 2.0

    base_url = CITY_URLS.get(city, CITY_URLS["Lahore"])

    for page in range(1, pages + 1):
        url = f"{base_url}?page={page}"
        print(f"  [PAGE {page}/{pages}] Requesting: {url}")

        try:
            response = requests.get(url, headers=get_headers(), timeout=10)

            if response.status_code == 429:
                print("[WARN] Rate limited! Sleeping...")
                time.sleep(10)
                current_delay += 1.0
                continue

            if response.status_code != 200:
                print(f"[WARN] Failed page {page}: {response.status_code}")
                continue

            time.sleep(current_delay)

            soup = BeautifulSoup(response.text, "html.parser")

            # More specific: each listing card
            cards = soup.find_all("article")

            for card in cards:
                try:
                    # ✅ Title
                    title_tag = card.find("h2")
                    title = title_tag.get_text(strip=True) if title_tag else None

                    # ✅ Price (Zameen uses specific spans for price)
                    price_tag = card.find("span", {"aria-label": "Price"})
                    if not price_tag:
                        price_tag = card.select_one("[data-testid='listing-price']")

                    price = price_tag.get_text(strip=True) if price_tag else None

                    # ✅ Property type (usually inside description/meta)
                    type_tag = card.select_one("[aria-label='Type']")
                    if not type_tag:
                        # fallback: infer from title
                        if title:
                            t = title.lower()

                            if "house" in t or "bungalow" in t:
                                property_type = "house"
                            elif "flat" in t or "apartment" in t:
                                property_type = "apartment"
                            elif "plot" in t:
                                property_type = "plot"
                            else:
                                property_type = "unknown"
                        else:
                            property_type = "unknown"
                    else:
                        property_type = type_tag.get_text(strip=True).lower()

                    # ✅ Area
                    area_tag = card.find("span", {"aria-label": "Area"})
                    if not area_tag:
                        area_tag = card.select_one("[data-testid='listing-area']")
                    area = area_tag.get_text(strip=True) if area_tag else None

                    if title and price:
                        print(f"    - Extracted: {title[:40]}... | {price} | Area: {area}")
                        listings.append({
                            "title": title,
                            "price": price,
                            "area": area,
                            "type": property_type,
                            "url": card.find("a")["href"] if card.find("a") else None
                        })

                except Exception as e:
                    print(f"[ERROR] Parsing listing failed: {e}")

            time.sleep(random.uniform(1.5, 3.0))

        except Exception as e:
            print(f"[ERROR] Request failed: {e}")

    return listings

if __name__ == "__main__":
    data = fetch_listings(pages=2)
    for d in data[:5]:
        print(d)