from db import get_connection
from scraper import fetch_listings
from cleaner import clean_price, clean_text, clean_property_type, generate_id, clean_area
import hashlib
import subprocess

conn = get_connection()
cur = conn.cursor()

cities = ["Lahore", "Karachi", "Islamabad"]
all_listings = []

import time

PAGES_PER_CITY = 20
print(f"Starting extraction for all cities ({PAGES_PER_CITY} pages each)...")

for city in cities:
    print(f"\n>>> [CITY: {city.upper()}] Working on deep extraction...")
    city_listings = fetch_listings(city=city, pages=PAGES_PER_CITY)
    for item in city_listings:
        item["city_name"] = city
    all_listings.extend(city_listings)
    print(f"DONE: Extracted {len(city_listings)} listings from {city}.")

# Generate a unique job_id (schema expects VARCHAR)
job_id = "JOB_" + hashlib.md5(str(time.time()).encode()).hexdigest()[:8]

cur.execute("""
INSERT INTO etl_job (job_id, run_date, status, progress_percent, records_extracted)
VALUES (%s, CURRENT_DATE, %s, %s, %s)
""", (job_id, "running", 0, len(all_listings)))

print(f"\n>>> [DATABASE] Loading {len(all_listings)} records into PostgreSQL...")

for i, item in enumerate(all_listings):
    city = item["city_name"]
    title = clean_text(item["title"])
    price = clean_price(item["price"])
    area = clean_area(item.get("area"))
    ptype = clean_property_type(item["type"])
    url = item.get("url")

    # Skip if URL missing
    if not url:
        continue

    # Generate URL hash (for deduplication + resume support)
    url_hash = hashlib.md5(url.encode()).hexdigest()

    # Resume-on-failure check (skip already processed records)
    cur.execute(
        "SELECT 1 FROM property WHERE url_hash = %s",
        (url_hash,)
    )
    if cur.fetchone():
        # Optional: reduce noise by not printing every skip
        continue

    property_id = generate_id(title, price)

    # Insert / update property
    cur.execute("""
    INSERT INTO property (property_id, city, locality, property_type, area, price, url_hash)
    VALUES (%s, %s, %s, %s, %s, %s, %s)
    ON CONFLICT (property_id) DO UPDATE SET
        price = EXCLUDED.price,
        area = EXCLUDED.area,
        updated_at = CURRENT_TIMESTAMP
    """, (property_id, city, title, ptype, area, price, url_hash))

    # Progress tracking
    progress = int((i + 1) / len(all_listings) * 100)
    if (i + 1) % 10 == 0 or (i + 1) == len(all_listings):
        print(f"    [DB PROGRESS] {i + 1}/{len(all_listings)} records processed ({progress}%)...")
        # Commit every 10 records so data is actually "loading" live
        conn.commit()

    cur.execute("""
    UPDATE etl_job
    SET progress_percent = %s,
        updated_at = CURRENT_TIMESTAMP
    WHERE job_id = %s
    """, (progress, job_id))

# Final job update
cur.execute("""
UPDATE etl_job
SET status = 'success',
    progress_percent = 100,
    updated_at = CURRENT_TIMESTAMP
WHERE job_id = %s
""", (job_id,))

conn.commit()
cur.close()
conn.close()

print("ETL pipeline executed successfully with full tracking!")

# Trigger ML retraining (Phase 3 requirement)
print("Triggering ML retraining...")
subprocess.run(["python", "../locus-analytics/ml/retrain_model.py"])
