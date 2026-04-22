import csv
import random
import json
import uuid
from datetime import datetime, timedelta

# ----------------------------
# STEP 2 — Geography
# ----------------------------

CITIES = {
    "Karachi": 0.40,
    "Islamabad": 0.30,
    "Lahore": 0.30
}

LOCALITIES = {
    "Karachi": [
        "Clifton", "DHA Phase 5", "DHA Phase 6", "DHA Phase 8",
        "Bahria Town Karachi", "Gulshan-e-Iqbal", "Nazimabad",
        "Malir Cantt", "PECHS", "Scheme 33", "North Nazimabad",
        "Korangi", "Federal B Area", "Gulistan-e-Jauhar",
        "Shah Faisal Colony"
    ],
    "Islamabad": [
        "F-6", "F-7", "F-8", "F-10", "F-11",
        "G-6", "G-7", "G-8", "G-9", "G-10",
        "Bahria Town", "DHA Phase 2", "E-11",
        "Blue Area", "PWD Housing Society"
    ],
    "Lahore": [
        "Gulberg", "DHA Phase 1", "DHA Phase 5", "DHA Phase 6",
        "Johar Town", "Model Town", "Garden Town",
        "Bahria Town Lahore", "Wapda Town", "Iqbal Town",
        "Valencia Town", "Lake City", "Cantt",
        "Askari 10", "Paragon City"
    ]
}

# ----------------------------
# STEP 3 — Property Types
# ----------------------------

PROPERTY_TYPES = {
    "house": 0.50,
    "apartment": 0.25,
    "plot": 0.20,
    "commercial": 0.05
}

# ----------------------------
# STEP 4 — Area Bands
# ----------------------------

AREA_BANDS = {
    "apartment": (500, 1800),
    "house": (900, 4500),
    "plot": (500, 5000),
    "commercial": (800, 8000)
}

# ----------------------------
# STEP 5 — Load Price Map
# ----------------------------

with open("locality_price_map.json", "r") as f:
    PRICE_MAP = json.load(f)

# ----------------------------
# STEP 6 — Date Generator
# ----------------------------

def random_date():
    today = datetime.today()
    days_back = random.randint(0, 3 * 365)
    return (today - timedelta(days=days_back)).date()

# ----------------------------
# STEP 7 — Coordinates
# ----------------------------

CITY_COORDS = {
    "Karachi": (24.8607, 67.0011),
    "Lahore": (31.5204, 74.3587),
    "Islamabad": (33.6844, 73.0479)
}

def generate_coords(city):
    lat, lon = CITY_COORDS[city]
    return (
        lat + random.uniform(-0.2, 0.2),
        lon + random.uniform(-0.2, 0.2)
    )

# ----------------------------
# STEP 9 — Weighted Choice
# ----------------------------

def weighted_choice(weight_map):
    items = list(weight_map.keys())
    weights = list(weight_map.values())
    return random.choices(items, weights=weights, k=1)[0]

# ----------------------------
# STEP 10 — Core Generator
# ----------------------------

def generate_property():
    # City
    city = weighted_choice(CITIES)

    # Locality
    locality = random.choice(LOCALITIES[city])

    # Property Type
    property_type = weighted_choice(PROPERTY_TYPES)

    # Area
    min_a, max_a = AREA_BANDS[property_type]

    # Add slight bias toward mid-range values
    area = round(random.triangular(min_a, max_a, (min_a + max_a) / 2), 2)

    # Bedrooms & Bathrooms (correlated)
    if property_type in ["plot", "commercial"]:
        bedrooms = None
        bathrooms = None
    else:
        if area < 900:
            bedrooms = random.randint(1, 2)
            bathrooms = random.randint(1, 2)
        elif area < 1800:
            bedrooms = random.randint(2, 4)
            bathrooms = random.randint(1, 3)
        elif area < 3000:
            bedrooms = random.randint(3, 5)
            bathrooms = random.randint(2, 4)
        else:
            bedrooms = random.randint(4, 7)
            bathrooms = random.randint(3, 6)

    # Price (core realism)
    base_price_per_sqft = 10000
    #multiplier = PRICE_MAP.get(city, {}).get(locality, 1.0)
    multiplier = PRICE_MAP.get(city, {}).get(locality)

    if multiplier is None:
        multiplier = 1.0

    price = area * base_price_per_sqft * multiplier
    price *= random.uniform(0.85, 1.15)
    price = round(price, 2)

    # Coordinates
    lat, lon = generate_coords(city)

    # Listing Date
    listing_date = random_date()

    # IDs
    property_id = str(uuid.uuid4())
    url_hash = str(uuid.uuid4()).replace("-", "")[:12]

    # Derived
    price_per_sqft = round(price / area, 2)

    return [
        property_id,
        city,
        locality,
        property_type,
        area,
        price,
        bedrooms,
        bathrooms,
        listing_date,
        lat,
        lon,
        price_per_sqft,
        url_hash
    ]

# ----------------------------
# STEP 11 — CSV Writer
# ----------------------------

def main():
    with open("seed_properties.csv", "w", newline="") as f:
        writer = csv.writer(f)

        writer.writerow([
            "property_id", "city", "locality", "property_type",
            "area", "price", "bedrooms", "bathrooms",
            "listing_date", "latitude", "longitude",
            "price_per_sqft", "url_hash"
        ])

        for _ in range(800):
            writer.writerow(generate_property())

    print(" seed_properties.csv generated with 800 rows.")

# ----------------------------
# RUN
# ----------------------------

if __name__ == "__main__":
    main()