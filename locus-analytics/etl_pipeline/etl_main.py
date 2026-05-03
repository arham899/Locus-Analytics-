from db import get_connection
from scraper import fetch_listings
from cleaner import clean_price, clean_text, clean_property_type, generate_id
import hashlib
import subprocess

conn = get_connection()
cur = conn.cursor()

listings = fetch_listings()

cur.execute("""
INSERT INTO etl_job (job_name, status, progress_percent, records_extracted)
VALUES (%s, %s, %s, %s)
RETURNING job_id
""", ("Property_ETL", "RUNNING", 0, len(listings)))

job_id = cur.fetchone()[0]

for i, item in enumerate(listings):

    title = clean_text(item["title"])
    price = clean_price(item["price"])
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
        print("Skipping duplicate listing...")
        continue

    property_id = generate_id(title, price)

    # Insert / update property
    cur.execute("""
    INSERT INTO property (property_id, city, locality, property_type, area, price, url_hash)
    VALUES (%s, %s, %s, %s, %s, %s, %s)
    ON CONFLICT (property_id) DO UPDATE SET
        price = EXCLUDED.price,
        updated_at = CURRENT_TIMESTAMP
    """, (property_id, "Lahore", title, ptype, 10, price, url_hash))

    # Progress tracking
    progress = int((i + 1) / len(listings) * 100)

    cur.execute("""
    UPDATE etl_job
    SET progress_percent = %s,
        updated_at = CURRENT_TIMESTAMP
    WHERE job_id = %s
    """, (progress, job_id))

# Final job update
cur.execute("""
UPDATE etl_job
SET status = 'SUCCESS',
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
subprocess.run(["python", "../ml/retrain_model.py"])