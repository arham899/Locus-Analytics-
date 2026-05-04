import psycopg2

conn = psycopg2.connect(
    host="localhost",
    port=5432,
    dbname="locus_analytics",
    user="postgres",
    password="password"
)

cur = conn.cursor()
cur.execute("SELECT column_name FROM information_schema.columns WHERE table_name = 'etl_job'")
columns = cur.fetchall()
print("Columns in etl_job:")
for col in columns:
    print(f" - {col[0]}")
conn.close()
