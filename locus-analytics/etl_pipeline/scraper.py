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

BASE_URL = "https://www.zameen.com/Homes/Lahore-1-1.html"


def get_headers():
    return {
        "User-Agent": random.choice(USER_AGENTS),
        "Accept-Language": "en-US,en;q=0.9"
    }


def fetch_listings(pages=1):
    listings = []

    for page in range(1, pages + 1):
        url = f"{BASE_URL}?page={page}"

        try:
            response = requests.get(url, headers=get_headers(), timeout=10)

            if response.status_code != 200:
                print(f"[WARN] Failed page {page}: {response.status_code}")
                continue

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

                    if title and price:
                        listings.append({
                            "title": title,
                            "price": price,
                            "type": property_type
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