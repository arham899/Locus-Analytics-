from db import get_connection

conn = get_connection()
cur = conn.cursor()

cur.execute("""
INSERT INTO property (
    property_id,
    city,
    locality,
    property_type,
    area,
    price
)
VALUES (%s, %s, %s, %s, %s, %s)
""", (
    "P_ETL_1",
    "Lahore",
    "DHA",
    "House",
    10,
    12000000
))

conn.commit()

cur.close()
conn.close()

print("Inserted via ETL!")