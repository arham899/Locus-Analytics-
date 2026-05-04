from db import get_connection

def view_latest_data():
    try:
        conn = get_connection()
        cur = conn.cursor()

        # 1. Check total counts
        cur.execute("SELECT COUNT(*) FROM property;")
        total_properties = cur.fetchone()[0]
        
        cur.execute("SELECT COUNT(*) FROM etl_job;")
        total_jobs = cur.fetchone()[0]

        print("="*50)
        print("DATABASE SUMMARY")
        print("="*50)
        print(f"Total Properties in DB: {total_properties}")
        print(f"Total ETL Jobs Run:     {total_jobs}")
        print("-" * 50)

        # 2. Show latest 10 properties
        print("\nLATEST 10 PROPERTIES EXTRACTED:")
        print(f"{'City':<12} | {'Area (M)':<8} | {'Price':<15} | {'Locality'}")
        print("-" * 70)
        
        cur.execute("""
            SELECT city, area, price, locality 
            FROM property 
            ORDER BY created_at DESC 
            LIMIT 10;
        """)
        
        for row in cur.fetchall():
            city, area, price, locality = row
            # Format price with commas
            formatted_price = f"{int(price):,}"
            print(f"{city:<12} | {float(area):<8.1f} | {formatted_price:<15} | {locality[:40]}")

        # 3. Show latest job status
        print("\nLATEST JOB STATUS:")
        cur.execute("""
            SELECT job_id, status, records_extracted, updated_at 
            FROM etl_job 
            ORDER BY updated_at DESC 
            LIMIT 1;
        """)
        job = cur.fetchone()
        if job:
            print(f"ID: {job[0]} | Status: {job[1]} | Records: {job[2]} | Time: {job[3]}")

        cur.close()
        conn.close()
    except Exception as e:
        print(f"Error connecting to database: {e}")

if __name__ == "__main__":
    view_latest_data()
