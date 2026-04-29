"""
LOCUS Analytics — ETL Scraper Pipeline (Simulation)
Author: Fasih

Simulates scraping a real estate website and loading data into the PostgreSQL database.
"""

import psycopg2
import time
import random
import uuid
from datetime import date

# CONFIG (Matches config.properties)
DB_HOST = "localhost"
DB_PORT = 5432
DB_NAME = "locus_analytics"
DB_USER = "postgres"
DB_PASS = "password"

def get_db_connection():
    return psycopg2.connect(
        host=DB_HOST, port=DB_PORT, dbname=DB_NAME,
        user=DB_USER, password=DB_PASS
    )

def simulate_scraping():
    print("[ETL] Connecting to target real estate portal...")
    time.sleep(1)
    print("[ETL] Extracting property listings for Karachi, Lahore, Islamabad...")
    
    extracted_properties = []
    cities = ["Karachi", "Lahore", "Islamabad"]
    types = ["house", "apartment"]
    
    # Simulate extracting 5 to 15 properties
    num_to_extract = random.randint(5, 15)
    for _ in range(num_to_extract):
        city = random.choice(cities)
        prop_type = random.choice(types)
        
        # Dummy data based on city
        if city == "Karachi":
            locality = random.choice(["DHA Phase 6", "Clifton", "Gulshan-e-Iqbal"])
            price = random.randint(15_000_000, 80_000_000)
        elif city == "Lahore":
            locality = random.choice(["DHA Phase 5", "Bahria Town", "Gulberg"])
            price = random.randint(12_000_000, 70_000_000)
        else:
            locality = random.choice(["Sector F-8", "Sector E-7", "DHA Phase 2"])
            price = random.randint(20_000_000, 100_000_000)
            
        area = random.randint(800, 4000)
        
        extracted_properties.append({
            "id": f"scraped-{uuid.uuid4().hex[:8]}",
            "city": city,
            "locality": locality,
            "type": prop_type,
            "price": price,
            "area": area,
            "bedrooms": random.randint(2, 6),
            "bathrooms": random.randint(2, 6)
        })
        time.sleep(0.2) # Simulate network delay
        
    print(f"[ETL] Extracted {len(extracted_properties)} listings.")
    return extracted_properties

def clean_data(properties):
    print("[ETL] Cleaning data (removing outliers, normalizing names)...")
    time.sleep(1)
    cleaned = [p for p in properties if p["price"] > 0 and p["area"] > 0]
    print(f"[ETL] Data cleaning complete. {len(cleaned)} records ready for load.")
    return cleaned

def load_data(properties):
    print("[ETL] Loading data into PostgreSQL...")
    conn = get_db_connection()
    cursor = conn.cursor()
    
    insert_sql = """
        INSERT INTO property 
        (property_id, city, locality, property_type, area, price, bedrooms, bathrooms, listing_date)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
    """
    
    loaded_count = 0
    today = str(date.today())
    
    for p in properties:
        try:
            cursor.execute(insert_sql, (
                p["id"], p["city"], p["locality"], p["type"], p["area"], 
                p["price"], p["bedrooms"], p["bathrooms"], today
            ))
            loaded_count += 1
        except Exception as e:
            print(f"[ETL-ERROR] Failed to load {p['id']}: {e}")
            conn.rollback()
            
    conn.commit()
    cursor.close()
    conn.close()
    
    print(f"[ETL] Load complete. Successfully inserted {loaded_count} new properties.")

def main():
    print("=" * 50)
    print("  LOCUS Analytics - ETL Scraper Job")
    print("=" * 50)
    
    try:
        raw_data = simulate_scraping()
        clean_data_list = clean_data(raw_data)
        load_data(clean_data_list)
        print("\n[OK] ETL Pipeline finished successfully.")
    except Exception as e:
        print(f"\n[FAILED] ETL Pipeline failed: {e}")

if __name__ == "__main__":
    main()
