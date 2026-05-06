"""
LOCUS Analytics — Bulk Data Generator
Generates 500 realistic property listings and inserts them into PostgreSQL.
"""
import hashlib
import random
import uuid
from datetime import date, timedelta

import psycopg2

DB_HOST = "localhost"
DB_PORT = 5432
DB_NAME = "locus_analytics"
DB_USER = "postgres"
DB_PASS = "password"

CITY_DATA = {
    "Karachi": {
        "lat": 24.8607, "lon": 67.0011,
        "localities": [
            "DHA Phase 1", "DHA Phase 2", "DHA Phase 5", "DHA Phase 6",
            "DHA Phase 8", "Clifton", "Bahria Town Karachi",
            "Gulshan-e-Iqbal", "Malir Cantt", "PECHS",
            "North Nazimabad", "Federal B Area", "Korangi",
            "Nazimabad", "Scheme 33",
        ],
        "price_range": {
            "house":     (8_000_000, 120_000_000),
            "apartment": (3_000_000, 50_000_000),
            "plot":      (5_000_000, 80_000_000),
            "commercial":(10_000_000, 200_000_000),
        },
    },
    "Lahore": {
        "lat": 31.5204, "lon": 74.3587,
        "localities": [
            "DHA Phase 1", "DHA Phase 2", "DHA Phase 5", "DHA Phase 6",
            "Gulberg", "Model Town", "Johar Town",
            "Bahria Town Lahore", "Wapda Town", "Garden Town",
            "Iqbal Town", "Cantt", "Valencia Town",
        ],
        "price_range": {
            "house":     (7_000_000, 100_000_000),
            "apartment": (2_500_000, 40_000_000),
            "plot":      (4_000_000, 70_000_000),
            "commercial":(8_000_000, 150_000_000),
        },
    },
    "Islamabad": {
        "lat": 33.6844, "lon": 73.0479,
        "localities": [
            "F-6", "F-7", "F-8", "F-10", "F-11",
            "G-9", "G-10", "E-11",
        ],
        "price_range": {
            "house":     (12_000_000, 150_000_000),
            "apartment": (4_000_000, 60_000_000),
            "plot":      (8_000_000, 100_000_000),
            "commercial":(15_000_000, 250_000_000),
        },
    },
}

PROPERTY_TYPES = ["house", "apartment", "plot", "commercial"]
PROPERTY_WEIGHTS = [0.45, 0.30, 0.15, 0.10]

RECORDS_TO_GENERATE = 500


def generate_records():
    records = []
    for _ in range(RECORDS_TO_GENERATE):
        city = random.choices(list(CITY_DATA.keys()), weights=[0.40, 0.35, 0.25])[0]
        info = CITY_DATA[city]
        locality = random.choice(info["localities"])
        prop_type = random.choices(PROPERTY_TYPES, weights=PROPERTY_WEIGHTS)[0]

        # Area in sq.ft. (realistic ranges)
        if prop_type == "house":
            area = random.randint(900, 6000)
        elif prop_type == "apartment":
            area = random.randint(500, 2500)
        elif prop_type == "plot":
            area = random.choice([1125, 2250, 4500, 9000])  # 5M, 10M, 1K, 2K
        else:
            area = random.randint(1000, 8000)

        # Price in PKR
        pmin, pmax = info["price_range"][prop_type]
        price = random.randint(pmin, pmax)

        # Bedrooms / Bathrooms
        if prop_type in ("plot", "commercial"):
            bedrooms = 0
            bathrooms = 0
        else:
            if area < 800:
                bedrooms = random.choice([1, 2])
            elif area < 1500:
                bedrooms = random.choice([2, 3])
            elif area < 2500:
                bedrooms = random.choice([3, 4])
            elif area < 4000:
                bedrooms = random.choice([4, 5])
            else:
                bedrooms = random.choice([5, 6])
            bathrooms = max(1, bedrooms - random.choice([0, 1]))

        # Listing date (random within past 2 years)
        days_ago = random.randint(0, 730)
        listing_date = str(date.today() - timedelta(days=days_ago))

        # Coordinates
        lat = round(info["lat"] + random.uniform(-0.05, 0.05), 6)
        lon = round(info["lon"] + random.uniform(-0.05, 0.05), 6)

        # URL hash for dedup
        key = f"{city}|{locality}|{prop_type}|{area}|{price}|{uuid.uuid4().hex[:6]}"
        url_hash = hashlib.md5(key.encode()).hexdigest()

        records.append((
            f"gen-{uuid.uuid4().hex[:12]}",
            city, locality, prop_type,
            area, price, bedrooms, bathrooms,
            listing_date, lat, lon, url_hash
        ))

    return records


def main():
    conn = psycopg2.connect(
        host=DB_HOST, port=DB_PORT, dbname=DB_NAME,
        user=DB_USER, password=DB_PASS
    )

    records = generate_records()

    sql = """
        INSERT INTO property (
            property_id, city, locality, property_type,
            area, price, bedrooms, bathrooms, listing_date,
            latitude, longitude, url_hash
        )
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        ON CONFLICT (url_hash) DO NOTHING
    """

    with conn.cursor() as cur:
        cur.executemany(sql, records)

    conn.commit()

    # Check total count
    with conn.cursor() as cur:
        cur.execute("SELECT COUNT(*) FROM property")
        total = cur.fetchone()[0]
        cur.execute("SELECT city, COUNT(*) FROM property GROUP BY city ORDER BY city")
        breakdown = cur.fetchall()

    conn.close()

    print(f"\n✅ Inserted {len(records)} new records")
    print(f"📊 Total properties in DB: {total}")
    print("\nBreakdown by city:")
    for city, count in breakdown:
        print(f"  {city}: {count}")


if __name__ == "__main__":
    main()
