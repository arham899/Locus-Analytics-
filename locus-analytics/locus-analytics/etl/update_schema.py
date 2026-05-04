import psycopg2

conn = psycopg2.connect(
    host="localhost",
    port=5432,
    dbname="locus_analytics",
    user="postgres",
    password="password"
)

cur = conn.cursor()
try:
    print("Updating etl_job table...")
    cur.execute("ALTER TABLE etl_job ADD COLUMN IF NOT EXISTS current_stage VARCHAR DEFAULT 'idle'")
    cur.execute("ALTER TABLE etl_job ADD COLUMN IF NOT EXISTS progress_percent INT DEFAULT 0")
    
    print("Updating system_configuration table (audit log compatibility)...")
    cur.execute("ALTER TABLE system_configuration ADD COLUMN IF NOT EXISTS admin_id VARCHAR REFERENCES app_user(user_id) ON DELETE SET NULL")
    
    print("Creating audit_log table if missing...")
    cur.execute("""
        CREATE TABLE IF NOT EXISTS audit_log (
            audit_id  VARCHAR PRIMARY KEY,
            admin_id  VARCHAR REFERENCES app_user(user_id) ON DELETE SET NULL,
            table_name VARCHAR NOT NULL,
            field_name VARCHAR,
            old_value  TEXT,
            new_value  TEXT,
            changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)
    
    conn.commit()
    print("Database schema updated successfully.")
except Exception as e:
    conn.rollback()
    print(f"Error updating schema: {e}")
finally:
    conn.close()
