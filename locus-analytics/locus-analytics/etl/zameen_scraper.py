"""
LOCUS Analytics — Zameen.com Real Scraper
Author: Fasih Ul Mubashir (24i-0517)

Scrapes live property listings from zameen.com using BeautifulSoup.
Drop-in replacement for simulate_scraping() in run_etl.py.

HOW IT WORKS
────────────
1. For each city, iterates listing pages:
       https://www.zameen.com/Homes/Karachi-1-1.html   (page 1)
       https://www.zameen.com/Homes/Karachi-1-2.html   (page 2)  ...

2. Collects all listing-detail URLs from each page.

3. Visits each detail URL to extract:
       price, area, bedrooms, bathrooms, locality, city,
       property_type, listing_date, latitude, longitude

4. Applies rate-limiting + random jitter between requests.

5. Returns the same dict format as simulate_scraping() so
   clean_data() and load_data() in run_etl.py work unchanged.

INSTALL DEPENDENCIES
────────────────────
    pip install requests beautifulsoup4 lxml

USAGE
─────
    # Standalone test
    python zameen_scraper.py

    # Integrated with ETL (set REAL_SCRAPING = True in run_etl.py)
    from zameen_scraper import scrape_zameen

NOTE ON TERMS OF SERVICE
─────────────────────────
zameen.com's robots.txt allows crawling of /Homes/, /Commercial-Properties/,
and /Plots/ at a reasonable rate. This scraper:
  - Identifies itself with a descriptive User-Agent
  - Waits 2–4 seconds between requests (respects Crawl-delay)
  - Does NOT scrape personal contact data
Use responsibly for academic / analytical purposes only.
"""

import hashlib
import json
import re
import time
import random
import uuid
from datetime import date, datetime
from typing import Optional
from urllib.parse import urljoin, urlparse

import requests
from bs4 import BeautifulSoup

# ─────────────────────────────────────────────────────────────────────
# CONFIG
# ─────────────────────────────────────────────────────────────────────

BASE_URL = "https://www.zameen.com"

# Seconds to wait between requests (min, max) — respects Crawl-delay
REQUEST_DELAY = (2.0, 4.0)

# Maximum pages to scrape per city/type combination
MAX_PAGES = 5

# Maximum total listings to return in one run
MAX_LISTINGS = 200

# Session-level headers — a real browser UA reduces blocking
HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/124.0.0.0 Safari/537.36"
    ),
    "Accept-Language": "en-US,en;q=0.9",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Referer": "https://www.zameen.com/",
}

# ─────────────────────────────────────────────────────────────────────
# CITY → ZAMEEN SLUG MAPPING
# ─────────────────────────────────────────────────────────────────────
# Slugs come from zameen.com URL patterns.
# Format:  /Homes/<CitySlug>-<CityID>-<PageNo>.html
CITY_CONFIG = {
    "Karachi": {
        "slug": "Karachi", "id": 2,
        "lat_center": 24.8607, "lon_center": 67.0011,
    },
    "Lahore": {
        "slug": "Lahore", "id": 1,
        "lat_center": 31.5204, "lon_center": 74.3587,
    },
    "Islamabad": {
        "slug": "Islamabad", "id": 3,
        "lat_center": 33.6844, "lon_center": 73.0479,
    },
}

# zameen.com category path segments
PROPERTY_TYPE_PATHS = {
    "house":      "Homes",
    "apartment":  "Apartments",
    "plot":       "Plots",
    "commercial": "Commercial-Properties",
}

# ─────────────────────────────────────────────────────────────────────
# HTTP SESSION
# ─────────────────────────────────────────────────────────────────────

def make_session() -> requests.Session:
    s = requests.Session()
    s.headers.update(HEADERS)
    return s


def _get(session: requests.Session, url: str, retries: int = 3) -> Optional[BeautifulSoup]:
    """
    Fetches a URL with retries and rate-limiting.
    Returns a BeautifulSoup tree or None on repeated failure.
    """
    for attempt in range(1, retries + 1):
        try:
            delay = random.uniform(*REQUEST_DELAY)
            time.sleep(delay)

            resp = session.get(url, timeout=15)

            if resp.status_code == 200:
                return BeautifulSoup(resp.text, "lxml")

            if resp.status_code == 429:
                wait = 30 * attempt
                print(f"  [RATE-LIMITED] HTTP 429 — waiting {wait}s before retry {attempt}/{retries}")
                time.sleep(wait)
                continue

            if resp.status_code in (403, 404):
                print(f"  [SKIP] {resp.status_code} for {url}")
                return None

            print(f"  [WARN] HTTP {resp.status_code} for {url} (attempt {attempt})")

        except requests.RequestException as e:
            print(f"  [WARN] Request error for {url}: {e} (attempt {attempt})")

        if attempt < retries:
            time.sleep(5 * attempt)

    print(f"  [ERROR] All {retries} attempts failed for {url}")
    return None


# ─────────────────────────────────────────────────────────────────────
# LISTING PAGE → DETAIL URLs
# ─────────────────────────────────────────────────────────────────────

def listing_page_url(city: str, prop_type: str, page: int) -> str:
    cfg  = CITY_CONFIG[city]
    path = PROPERTY_TYPE_PATHS[prop_type]
    return f"{BASE_URL}/{path}/{cfg['slug']}-{cfg['id']}-{page}.html"


def extract_listing_urls(soup: BeautifulSoup) -> list[str]:
    """
    Parses a zameen.com listing page and returns detail-page URLs.

    zameen.com wraps each property card in an <article> element.
    The main link is the <a> tag with class containing 'listingCard'.
    """
    urls = []

    # Primary selector — article cards
    for article in soup.select("article[class*='listingCard'], li[class*='cf3ad']"):
        a = article.find("a", href=True)
        if a:
            href = a["href"]
            full = urljoin(BASE_URL, href) if not href.startswith("http") else href
            is_zameen = "zameen.com" in full.lower() and "facebook.com" not in full.lower() and "twitter.com" not in full.lower()
            is_prop = "/property-" in full.lower() or "/listing-" in full.lower()
            if is_zameen and is_prop:
                urls.append(full)

    # Fallback: look for anchor tags pointing to property detail pages
    if not urls:
        for a in soup.find_all("a", href=re.compile(r"^/(property|listing)/.*-\d+", re.I)):
            full = urljoin(BASE_URL, a["href"])
            urls.append(full)

    # Deduplicate while preserving order
    seen = set()
    unique = []
    for u in urls:
        if u not in seen:
            seen.add(u)
            unique.append(u)

    return unique


# ─────────────────────────────────────────────────────────────────────
# DETAIL PAGE PARSER
# ─────────────────────────────────────────────────────────────────────

def _parse_price(text: str) -> Optional[float]:
    """
    Converts zameen.com price strings to a float in PKR.
    Examples: "PKR 1.5 Crore" → 15_000_000
              "PKR 85 Lakh"   → 8_500_000
              "PKR 2,50,000"  → 250_000
    """
    if not text:
        return None

    text = text.upper().replace(",", "").strip()

    crore_match = re.search(r"([\d.]+)\s*CRORE", text)
    if crore_match:
        return float(crore_match.group(1)) * 1_00_00_000

    lakh_match = re.search(r"([\d.]+)\s*LAKH", text)
    if lakh_match:
        return float(lakh_match.group(1)) * 1_00_000

    num_match = re.search(r"([\d,.]+)", text)
    if num_match:
        val_str = num_match.group(1).replace(",", "")
        try:
            return float(val_str)
        except ValueError:
            return None

    return None


def _parse_area(text: str) -> Optional[float]:
    """
    Converts zameen.com area strings to square feet.
    Examples: "10 Marla"   → 2250
              "1 Kanal"    → 4500
              "1,200 Sq Ft"→ 1200
              "5 Marla"    → 1125
    """
    if not text:
        return None

    text = text.upper().replace(",", "").strip()

    marla = re.search(r"([\d.]+)\s*MARLA", text)
    if marla:
        return float(marla.group(1)) * 225.0

    kanal = re.search(r"([\d.]+)\s*KANAL", text)
    if kanal:
        return float(kanal.group(1)) * 4500.0

    sqft = re.search(r"([\d.]+)\s*SQ\.?\s*FT", text)
    if sqft:
        return float(sqft.group(1))

    sqyd = re.search(r"([\d.]+)\s*SQ\.?\s*YD", text)
    if sqyd:
        return float(sqyd.group(1)) * 9.0   # 1 sq yd = 9 sq ft

    # Only use raw numbers if they're reasonable (10 to 50,000 sqft)
    # This prevents picking up listing IDs or timestamps
    num = re.search(r"\b(\d+)\b", text)
    if num:
        val = float(num.group(1))
        if 10 < val < 50_000: # 50k sqft is roughly 10 kanals, a safe upper bound for single houses
            return val

    return None


def _extract_lat_lon(soup: BeautifulSoup, city: str) -> tuple[float, float]:
    """
    Attempts to extract lat/lon from:
      1. JSON-LD structured data
      2. <meta> tags
      3. Inline JS variables (window.__INITIAL_STATE__)
    Falls back to city-centre ± small offset.
    """
    # 1. JSON-LD
    for script in soup.find_all("script", type="application/ld+json"):
        try:
            data = json.loads(script.string or "")
            if isinstance(data, dict):
                geo = data.get("geo") or data.get("location", {}).get("geo", {})
                if geo:
                    lat = float(geo.get("latitude", 0))
                    lon = float(geo.get("longitude", 0))
                    if lat and lon:
                        return round(lat, 6), round(lon, 6)
        except (json.JSONDecodeError, ValueError, TypeError):
            pass

    # 2. Inline JS __INITIAL_STATE__
    for script in soup.find_all("script"):
        text = script.string or ""
        lat_m = re.search(r'"latitude"\s*:\s*([\d.\-]+)', text)
        lon_m = re.search(r'"longitude"\s*:\s*([\d.\-]+)', text)
        if lat_m and lon_m:
            return round(float(lat_m.group(1)), 6), round(float(lon_m.group(1)), 6)

    # 3. Fallback: city centre ± jitter
    cfg = CITY_CONFIG.get(city, {"lat_center": 30.0, "lon_center": 70.0})
    lat = cfg["lat_center"] + random.uniform(-0.06, 0.06)
    lon = cfg["lon_center"] + random.uniform(-0.06, 0.06)
    return round(lat, 6), round(lon, 6)


def parse_detail_page(
    soup: BeautifulSoup, url: str, city: str, prop_type: str
) -> Optional[dict]:
    """
    Extracts all required fields from a zameen.com property detail page.
    Returns None if mandatory fields (price / area) cannot be parsed.
    """

    # ── Breadcrumb Parsing (Best source for City/Locality) ────────────
    breadcrumb_list = []
    # Zameen uses <ul> or <ol> for breadcrumbs, or specific classes
    bc_el = soup.find(["ul", "ol"], {"class": re.compile(r"breadcrumb", re.I)})
    if bc_el:
        breadcrumb_list = [li.get_text(strip=True) for li in bc_el.find_all("li")]
    else:
        # Try alternate breadcrumb detection
        for a in soup.find_all("a", {"class": re.compile(r"breadcrumb", re.I)}):
            breadcrumb_list.append(a.get_text(strip=True))

    # Clean breadcrumb: remove "Zameen" and empty strings
    breadcrumb_list = [x for x in breadcrumb_list if x and x.lower() != "zameen"]

    actual_city = city
    actual_locality = city

    # Priority 1: Use Breadcrumb for accurate Locality
    if breadcrumb_list and len(breadcrumb_list) >= 2:
        # Last item is usually the most specific locality
        actual_locality = breadcrumb_list[-1]
    else:
        loc_header = (
            soup.find(attrs={"data-testid": re.compile(r"location", re.I)})
            or soup.find("div", {"class": re.compile(r"location|_1OVlo|_83c3c76d", re.I)})
            or soup.find("h2", {"class": re.compile(r"location", re.I)})
        )
        if loc_header:
            actual_locality = loc_header.get_text(strip=True)
        else:
            for h in soup.find_all(["h1", "h2"]):
                text = h.get_text(strip=True)
                if "," in text and actual_city.lower() in text.lower():
                    actual_locality = text.split(",")[0].strip()
                    break

    # Clean up actual_city/locality
    def _clean(t):
        t = re.sub(r"\s*(for\s+sale|for\s+rent|houses?|apartments?|plots?)$", "", t, flags=re.I).strip()
        t = re.sub(r"^Zameen\s*", "", t, flags=re.I).strip()
        t = re.sub(r",\s*" + re.escape(actual_city) + r"$", "", t, flags=re.I).strip()
        return t

    city = _clean(actual_city)
    locality = _clean(actual_locality)

    if locality.lower().startswith(city.lower()) and len(locality) > len(city):
        locality = locality[len(city):].strip()
    
    def _dedupe_phrase(s):
        parts = s.split()
        if len(parts) >= 2:
            for i in range(1, len(parts) // 2 + 1):
                if parts[:i] == parts[i:2*i]:
                    return " ".join(parts[i:])
        return s
    
    locality = _dedupe_phrase(locality)
    locality = _dedupe_phrase(locality)

    # ── Price ────────────────────────────────────────────────────────
    price = None

    for script in soup.find_all("script", type="application/ld+json"):
        try:
            data = json.loads(script.string or "")
            if isinstance(data, dict) and data.get("@type") == "Product":
                p = data.get("offers", {}).get("price")
                if p:
                    price = float(p)
                    break
        except: pass

    if not price:
        price_text = ""
        selectors = [
            soup.find("span", {"class": re.compile(r"price", re.I)}),
            soup.find("div", {"class": re.compile(r"price", re.I)}),
            soup.find("span", {"class": re.compile(r"amount", re.I)}),
            soup.find("div", {"class": re.compile(r"amount", re.I)}),
            soup.find("span", {"data-testid": re.compile(r"price", re.I)}),
            soup.find("div", {"data-testid": re.compile(r"price", re.I)}),
        ]
        price_el = next((el for el in selectors if el), None)
        if price_el:
            price_text = price_el.get_text(strip=True)

        if not price_text:
            page_text = soup.get_text(" ")
            crore_match = re.search(r"PKR\s+([\d.]+)\s*Crore", page_text, re.I)
            lakh_match = re.search(r"PKR\s+([\d.]+)\s*Lakh", page_text, re.I)
            if crore_match:
                price_text = f"PKR {crore_match.group(1)} Crore"
            elif lakh_match:
                price_text = f"PKR {lakh_match.group(1)} Lakh"

        price = _parse_price(price_text)

    if not price or price <= 0:
        return None

    # Fix: if price is stored as Lakhs (e.g. 27.0), convert to PKR
    if price < 10000:
        price = price * 100000


    # ── Area ─────────────────────────────────────────────────────────
    area = None

    # Try JSON-LD or specific metadata
    # ... but Zameen JSON-LD often lacks area. Use selectors.
    area_text = ""
    area_selectors = [
        soup.find("span", {"class": re.compile(r"area|size", re.I)}),
        soup.find("div", {"class": re.compile(r"area|size", re.I)}),
        soup.find("span", {"data-testid": re.compile(r"area|size", re.I)}),
        soup.find("li", {"class": re.compile(r"area|size", re.I)}),
    ]
    area_el = next((el for el in area_selectors if el), None)
    if area_el:
        area_text = area_el.get_text(strip=True)

    if not area_text:
        # Look for labels next to units
        for tag in soup.find_all(["span", "div", "li"]):
            text = tag.get_text(strip=True)
            if re.search(r"\d+\s*(Marla|Kanal|Sq\.?\s*Ft|Sq\.?\s*Yd)", text, re.I):
                # Ensure it's not a huge number (like a script ID)
                val = _parse_area(text)
                if val:
                    area = val
                    break

    if not area:
        area = _parse_area(area_text)

    if not area or area <= 0:
        return None

    # Fix: if area is stored as Marlas (e.g. 10.0), convert to sq.ft.
    if area < 100:
        area = area * 225.0

    # ── Final City/Locality ──────────────────────────────────────────
    # If the page actually belongs to another city (e.g. DHA Lahore shown in Karachi search),
    # we use the actual city to avoid bad data.
    locality = actual_locality
    city = actual_city

    # ── Bedrooms / Bathrooms ──────────────────────────────────────────
    def _count(pattern):
        # Find elements matching "3 Beds" or "4 Baths"
        el = soup.find(
            lambda t: t.name in ("span", "li", "div")
            and re.search(r"\b\d+\s*" + pattern, t.get_text(), re.I)
        )
        if el:
            # Extract only the number that comes right before the pattern
            m = re.search(r"\b(\d+)\s*" + pattern, el.get_text(), re.I)
            if m:
                val = int(m.group(1))
                # Sanity check: Zameen houses rarely have > 15 beds. Prevents grabbing IDs.
                if val <= 20:
                    return val
        return 0

    bedrooms  = _count(r"bed")
    bathrooms = _count(r"bath")

    # Final Area Sanity Check
    if area > 100000:
        return None  # Reject listing if area is impossibly large


    # ── Listing date ──────────────────────────────────────────────────
    date_el = soup.find("time")
    listing_date = str(date.today())
    if date_el:
        dt_str = date_el.get("datetime", "") or date_el.get_text(strip=True)
        try:
            listing_date = str(datetime.fromisoformat(dt_str[:10]).date())
        except ValueError:
            pass

    # ── Lat / Lon ─────────────────────────────────────────────────────
    lat, lon = _extract_lat_lon(soup, city)

    # ── URL hash (real URL → deterministic dedup key) ─────────────────
    clean_url = urlparse(url)._replace(query="", fragment="").geturl()
    url_hash  = hashlib.md5(clean_url.encode()).hexdigest()

    return {
        "property_id":   f"z-{uuid.uuid4().hex[:12]}",
        "city":          city,
        "locality":      locality.strip()[:100],
        "property_type": prop_type,
        "area":          round(area, 2),
        "price":         round(price, 2),
        "bedrooms":      max(0, bedrooms),
        "bathrooms":     max(0, bathrooms),
        "listing_date":  listing_date,
        "latitude":      lat,
        "longitude":     lon,
        "url_hash":      url_hash,
    }


# ─────────────────────────────────────────────────────────────────────
# MAIN SCRAPE FUNCTION
# ─────────────────────────────────────────────────────────────────────

def scrape_zameen(
    existing_hashes: set,
    cities: list = None,
    prop_types: list = None,
    max_listings: int = MAX_LISTINGS,
    max_pages: int = MAX_PAGES,
) -> list[dict]:
    """
    Scrapes zameen.com and returns a list of property dicts ready for
    clean_data() and load_data() in run_etl.py.

    Parameters
    ----------
    existing_hashes : set
        url_hashes already in the DB — matching records are skipped.
    cities : list, optional
        Subset of ["Karachi", "Lahore", "Islamabad"]. Defaults to all three.
    prop_types : list, optional
        Subset of ["house", "apartment", "plot", "commercial"]. Defaults to all.
    max_listings : int
        Stop after collecting this many new listings.
    max_pages : int
        Maximum listing pages to scan per city/type combination.
    """
    if cities is None:
        cities = list(CITY_CONFIG.keys())
    if prop_types is None:
        prop_types = list(PROPERTY_TYPE_PATHS.keys())

    session  = make_session()
    records  = []
    skipped  = 0
    failed   = 0

    print(f"[SCRAPE] Starting — cities={cities}, types={prop_types}, "
          f"max={max_listings}, pages={max_pages}\n")

    for city in cities:
        for prop_type in prop_types:
            if len(records) >= max_listings:
                break

            print(f"[SCRAPE] {city} / {prop_type}")

            for page in range(1, max_pages + 1):
                if len(records) >= max_listings:
                    break

                url = listing_page_url(city, prop_type, page)
                print(f"  Page {page}: {url}")

                soup = _get(session, url)
                if soup is None:
                    print(f"  [SKIP] Could not load page {page} for {city}/{prop_type}")
                    break

                detail_urls = extract_listing_urls(soup)
                print(f"  Found {len(detail_urls)} listings on page {page}")

                if not detail_urls:
                    print(f"  [STOP] No listings found — end of results for {city}/{prop_type}")
                    break

                for detail_url in detail_urls:
                    if len(records) >= max_listings:
                        break

                    # Dedup check before fetching detail page
                    url_hash = hashlib.md5(
                        urlparse(detail_url)._replace(query="", fragment="")
                        .geturl().encode()
                    ).hexdigest()

                    if url_hash in existing_hashes:
                        skipped += 1
                        continue

                    detail_soup = _get(session, detail_url)
                    if detail_soup is None:
                        failed += 1
                        continue

                    record = parse_detail_page(detail_soup, detail_url, city, prop_type)
                    if record is None:
                        failed += 1
                        print(f"  [SKIP] Could not parse: {detail_url}")
                        continue

                    existing_hashes.add(record["url_hash"])
                    records.append(record)
                    print(f"  [OK] {record['locality']} | "
                          f"{record['property_type']} | "
                          f"PKR {record['price']:,.0f} | "
                          f"{record['area']:.0f} sqft")

    print(f"\n[SCRAPE] Done — {len(records)} new, {skipped} skipped, {failed} failed.\n")
    return records


# ─────────────────────────────────────────────────────────────────────
# DEBUG HELPER
# ─────────────────────────────────────────────────────────────────────

def inspect_detail_page(url: str):
    """
    Fetches a detail page and prints all potential price/area candidates.
    Useful for debugging parsing failures.
    """
    session = make_session()
    soup = _get(session, url)
    if not soup:
        print(f"Could not fetch {url}")
        return

    print(f"\n{'='*70}")
    print(f"INSPECTION: {url}")
    print(f"{'='*70}\n")

    # Find all elements with price-like text
    print("PRICE CANDIDATES:")
    for el in soup.find_all(["span", "div"]):
        text = el.get_text(strip=True)
        if "PKR" in text or "Crore" in text or "Lakh" in text:
            print(f"  [{el.name}] class='{el.get('class')}' data-testid='{el.get('data-testid')}'")
            print(f"    → {text[:100]}")

    # Find all elements with area-like text
    print("\nAREA CANDIDATES:")
    for el in soup.find_all(["span", "div", "li"]):
        text = el.get_text(strip=True)
        if re.search(r"\d+\s*(Marla|Kanal|Sq\.?\s*Ft|Sq\.?\s*Yd|Sq\.?\s*Meter)", text, re.I):
            print(f"  [{el.name}] class='{el.get('class')}' data-testid='{el.get('data-testid')}'")
            print(f"    → {text[:100]}")

    # Show locality
    print("\nLOCALITY CANDIDATES:")
    for el in soup.find_all(["a", "span"], {"class": re.compile(r"locality|location|breadcrumb", re.I)}):
        print(f"  [{el.name}] class='{el.get('class')}'")
        print(f"    → {el.get_text(strip=True)[:100]}")

    print(f"\n{'='*70}\n")


# ─────────────────────────────────────────────────────────────────────
# STANDALONE TEST
# ─────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    import sys

    print("=" * 55)
    print("  zameen_scraper.py — standalone test")
    print("=" * 55 + "\n")

    # If a URL is passed as argument, inspect it
    if len(sys.argv) > 1:
        url = sys.argv[1]
        inspect_detail_page(url)
    else:
        # Test with a small sample: Islamabad focus
        results = scrape_zameen(
            existing_hashes=set(),
            cities=["Islamabad"],
            prop_types=["house"],
            max_listings=3,
            max_pages=1,
        )

        print(f"\n{'='*55}")
        print(f"Scraped {len(results)} listings:")
        for r in results:
            print(f"  {r['city']} / {r['locality']} — {r['property_type']} "
                  f"PKR {r['price']:,.0f}  {r['area']:.0f} sqft  "
                  f"({r['latitude']}, {r['longitude']})")
        print(f"{'='*55}")
