"""
LOCUS Analytics — ETL Scraper Pipeline
Author: Fasih Ul Mubashir (24i-0517)

Two modes controlled by REAL_SCRAPING flag:

  REAL_SCRAPING = False  (default)
      Generates realistic synthetic data — no internet needed.
      Good for development and demos.

  REAL_SCRAPING = True
      Calls zameen_scraper.scrape_zameen() which fetches live listings
      from zameen.com using BeautifulSoup.
      Requires:  pip install requests beautifulsoup4 lxml

Both modes produce the same dict format and share the same
clean_data(), load_data(), dedup, progress tracking, and resume logic.

Usage:
    python run_etl.py [JOB_ID]

    JOB_ID is passed by ETLServiceImpl (Java) when starting this script.
    If omitted a new UUID is generated locally.
"""

import hashlib
import random
import sys
import time
import uuid
from datetime import date

import psycopg2

# ─────────────────────────────────────────────────────────────────────
# CONFIG — mirrors config.properties
# ─────────────────────────────────────────────────────────────────────
DB_HOST  = "localhost"
DB_PORT  = 5432
DB_NAME  = "locus_analytics"
DB_USER  = "postgres"
DB_PASS  = "password"

# ── Mode switch ──────────────────────────────────────────────────────
# Set to True to scrape live data from zameen.com.
# Requires:  pip install requests beautifulsoup4 lxml
REAL_SCRAPING = True

# Delay between each simulated HTTP request (rate limiting)
REQUEST_DELAY_SEC = 1.0

# How many records to generate per run (simulation mode only)
RECORDS_TO_GENERATE = random.randint(20, 50)

# Real scraper limits (used when REAL_SCRAPING = True)
REAL_MAX_LISTINGS = 2000
REAL_MAX_PAGES    = 5

# Progress update interval (write to DB every N records)
PROGRESS_INTERVAL = 10

# ─────────────────────────────────────────────────────────────────────
# GEOGRAPHY: city-centre coords + locality offsets
# ─────────────────────────────────────────────────────────────────────
CITY_DATA = {
    "Karachi": {
        "lat": 24.8607, "lon": 67.0011,
        "localities": [
            "DHA Phase 6", "DHA Phase 5", "Clifton", "Gulshan-e-Iqbal",
            "North Nazimabad", "Malir Cantt", "Scheme 33", "Korangi",
            "Bahria Town Karachi", "Nazimabad",
        ],
        "price_min": 8_000_000, "price_max": 120_000_000,
    },
    "Lahore": {
        "lat": 31.5204, "lon": 74.3587,
        "localities": [
            "DHA Phase 5", "DHA Phase 6", "Bahria Town", "Gulberg",
            "Johar Town", "Model Town", "Wapda Town", "Lake City",
            "Cantt", "Faisal Town",
        ],
        "price_min": 7_000_000, "price_max": 100_000_000,
    },
    "Islamabad": {
        "lat": 33.6844, "lon": 73.0479,
        "localities": [
            "Sector F-8", "Sector E-7", "Sector F-10", "Sector G-9",
            "DHA Phase 2", "Bahria Town Islamabad", "Sector I-8",
            "Sector F-6", "Sector H-13", "Sector G-11",
        ],
        "price_min": 12_000_000, "price_max": 150_000_000,
    },
}

PROPERTY_TYPES = ["house", "apartment", "plot", "commercial"]
PROPERTY_WEIGHTS = [0.50, 0.25, 0.20, 0.05]


# ─────────────────────────────────────────────────────────────────────
# DATABASE
# ─────────────────────────────────────────────────────────────────────

def get_connection():
    return psycopg2.connect(
        host=DB_HOST, port=DB_PORT, dbname=DB_NAME,
        user=DB_USER, password=DB_PASS
    )


def load_existing_hashes(conn):
    """Returns the set of url_hashes already in the property table."""
    with conn.cursor() as cur:
        cur.execute("SELECT url_hash FROM property WHERE url_hash IS NOT NULL")
        return {row[0] for row in cur.fetchall()}


def update_etl_job(conn, job_id, stage, progress, extracted=0,
                   cleaned=0, loaded=0, errors=0, status="running"):
    """Upserts the etl_job row so the Java UI can poll progress."""
    sql = """
        INSERT INTO etl_job (
            job_id, run_date, status, current_stage, progress_percent,
            records_extracted, records_cleaned, records_loaded, errors,
            created_at, updated_at
        )
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s,
                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (job_id) DO UPDATE SET
            status           = EXCLUDED.status,
            current_stage    = EXCLUDED.current_stage,
            progress_percent = EXCLUDED.progress_percent,
            records_extracted = EXCLUDED.records_extracted,
            records_cleaned  = EXCLUDED.records_cleaned,
            records_loaded   = EXCLUDED.records_loaded,
            errors           = EXCLUDED.errors,
            updated_at       = CURRENT_TIMESTAMP
    """
    with conn.cursor() as cur:
        cur.execute(sql, (
            job_id, date.today(), status, stage, progress,
            extracted, cleaned, loaded, errors
        ))
    conn.commit()


# ─────────────────────────────────────────────────────────────────────
# EXTRACT
# ─────────────────────────────────────────────────────────────────────

def _url_hash(city, locality, prop_type, area, price):
    """Deterministic hash for deduplication (simulates URL hash from a real scraper)."""
    key = f"{city}|{locality}|{prop_type}|{int(area)}|{int(price)}"
    return hashlib.md5(key.encode()).hexdigest()


def _coords(city_info):
    """Generates lat/lon near the city centre with a realistic random offset."""
    lat = city_info["lat"] + random.uniform(-0.05, 0.05)
    lon = city_info["lon"] + random.uniform(-0.05, 0.05)
    return round(lat, 6), round(lon, 6)


def simulate_scraping(n, existing_hashes):
    """
    Generates n simulated property listings.
    Applies rate limiting (REQUEST_DELAY_SEC between each record).
    Skips records whose url_hash is already in the DB (resume-on-failure).
    """
    print(f"[EXTRACT] Scraping {n} listings with {REQUEST_DELAY_SEC}s delay each ...")
    records = []
    skipped = 0

    for i in range(n):
        city = random.choices(
            list(CITY_DATA.keys()), weights=[0.40, 0.30, 0.30]
        )[0]
        city_info = CITY_DATA[city]
        locality  = random.choice(city_info["localities"])
        prop_type = random.choices(PROPERTY_TYPES, weights=PROPERTY_WEIGHTS)[0]

        area  = random.randint(600, 5000)
        price = random.randint(city_info["price_min"], city_info["price_max"])
        lat, lon = _coords(city_info)

        bedrooms  = random.randint(1, 6) if prop_type != "plot" else 0
        bathrooms = random.randint(1, bedrooms) if bedrooms > 0 else 0

        h = _url_hash(city, locality, prop_type, area, price)

        if h in existing_hashes:
            skipped += 1
        else:
            records.append({
                "property_id": f"etl-{uuid.uuid4().hex[:12]}",
                "city":         city,
                "locality":     locality,
                "property_type": prop_type,
                "area":         area,
                "price":        price,
                "bedrooms":     bedrooms,
                "bathrooms":    bathrooms,
                "listing_date": str(date.today()),
                "latitude":     lat,
                "longitude":    lon,
                "url_hash":     h,
            })

        # Rate limiting
        time.sleep(REQUEST_DELAY_SEC)

        if (i + 1) % 10 == 0:
            print(f"  [{i+1}/{n}] extracted so far ...")

    print(f"[EXTRACT] Done. {len(records)} new, {skipped} skipped (already in DB).")
    return records


# ─────────────────────────────────────────────────────────────────────
# TRANSFORM
# ─────────────────────────────────────────────────────────────────────

def clean_data(records):
    """Removes invalid records and normalises field values."""
    print("[TRANSFORM] Cleaning ...")
    cleaned = []
    for r in records:
        if r["price"] <= 0 or r["area"] <= 0:
            continue
        if r["bedrooms"] < 0 or r["bathrooms"] < 0:
            continue
        # Normalise property type to lowercase
        r["property_type"] = r["property_type"].lower()
        cleaned.append(r)

    print(f"[TRANSFORM] {len(cleaned)}/{len(records)} records passed cleaning.")
    return cleaned


# ─────────────────────────────────────────────────────────────────────
# LOAD
# ─────────────────────────────────────────────────────────────────────

INSERT_SQL = """
    INSERT INTO property (
        property_id, city, locality, property_type,
        area, price, bedrooms, bathrooms, listing_date,
        latitude, longitude, url_hash
    )
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    ON CONFLICT (url_hash) DO NOTHING
"""


def load_data(conn, records, job_id, total_extracted, total_cleaned):
    """Inserts cleaned records; updates etl_job progress every PROGRESS_INTERVAL rows."""
    print("[LOAD] Inserting into PostgreSQL ...")
    loaded = 0
    errors = 0

    with conn.cursor() as cur:
        for i, r in enumerate(records):
            try:
                cur.execute(INSERT_SQL, (
                    r["property_id"], r["city"], r["locality"], r["property_type"],
                    r["area"], r["price"], r["bedrooms"], r["bathrooms"],
                    r["listing_date"], r["latitude"], r["longitude"], r["url_hash"]
                ))
                loaded += 1
            except Exception as e:
                errors += 1
                print(f"  [WARN] Failed to insert {r['property_id']}: {e}")
                conn.rollback()
                continue

            # Periodic progress flush
            if (i + 1) % PROGRESS_INTERVAL == 0:
                conn.commit()
                pct = int(((i + 1) / len(records)) * 100)
                update_etl_job(conn, job_id, "Loading", pct,
                               total_extracted, total_cleaned, loaded, errors)
                print(f"  [{i+1}/{len(records)}] loaded so far ...")

    conn.commit()
    print(f"[LOAD] Complete. {loaded} inserted, {errors} errors.")
    return loaded, errors


# ─────────────────────────────────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────────────────────────────────

def main():
    job_id = sys.argv[1] if len(sys.argv) > 1 else str(uuid.uuid4())

    print("=" * 55)
    print("  LOCUS Analytics — ETL Pipeline")
    print(f"  Job ID: {job_id}")
    print("=" * 55 + "\n")

    conn = None
    try:
        conn = get_connection()

        # ── Mark job as started ──────────────────────────
        update_etl_job(conn, job_id, "Extracting", 0)

        # ── Resume: load already-processed hashes ────────
        existing_hashes = load_existing_hashes(conn)
        print(f"[RESUME] {len(existing_hashes)} url_hashes already in DB — will skip duplicates.\n")

        # ── EXTRACT ──────────────────────────────────────
        if REAL_SCRAPING:
            from zameen_scraper import scrape_zameen
            raw = scrape_zameen(
                existing_hashes=existing_hashes,
                max_listings=300,
                max_pages=2,
            )
            extracted = len(raw)
        else:
            raw = simulate_scraping(RECORDS_TO_GENERATE, existing_hashes)
            extracted = len(raw) + (RECORDS_TO_GENERATE - len(raw))  # includes skipped

        update_etl_job(conn, job_id, "Transforming", 33, extracted)

        # ── TRANSFORM ────────────────────────────────────
        cleaned = clean_data(raw)
        update_etl_job(conn, job_id, "Loading", 66, extracted, len(cleaned))

        # ── LOAD ─────────────────────────────────────────
        loaded, errors = load_data(conn, cleaned, job_id, extracted, len(cleaned))

        # ── Mark job complete ────────────────────────────
        status = "success" if errors == 0 else "failed"
        update_etl_job(conn, job_id, "Done", 100,
                       extracted, len(cleaned), loaded, errors, status)

        print(f"  [OK] ETL complete — {loaded} new properties loaded.")
        print(f"  Errors: {errors}")
        print(f"{'=' * 55}\n")

        # ── RETRAIN ML MODEL ─────────────────────────────
        print("[ML] Triggering model retraining...")
        import subprocess
        import os
        # Path to retrain_model.py is in the same directory
        ml_script = os.path.join(os.path.dirname(__file__), "..", "ml", "retrain_model.py")
        subprocess.run(["python", ml_script])
        print("[ML] Retraining complete. model.json has been updated.")

    except Exception as e:
        print(f"\n[FAILED] ETL pipeline error: {e}")
        if conn:
            try:
                update_etl_job(conn, job_id, "Failed", 0, status="failed")
            except Exception:
                pass
        sys.exit(1)

    finally:
        if conn:
            conn.close()


if __name__ == "__main__":
    main()
