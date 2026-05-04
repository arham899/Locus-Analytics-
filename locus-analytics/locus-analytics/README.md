# LOCUS Analytics

A JavaFX desktop application for real-estate analytics in Pakistan (Karachi, Lahore, Islamabad).
Covers FMV estimation (ML), rental yield, ROI, price trends, heatmaps, investment clusters, and PDF reports.

---

## Team

| Member | Roll | Role |
|---|---|---|
| Fasih Ul Mubashir | 24i-0517 | System Engineer — Backend, DB, ETL, Deployment |
| Arham Manzoor | 24i-0640 | Lead Data Scientist — Domain Model, Services, ML |
| Ayaan Aman | 24i-0663 | Frontend Architect — JavaFX UI, Charts, Maps, PDF |

---

## Prerequisites

| Tool | Version |
|---|---|
| Java JDK | 17 or higher |
| Apache Maven | 3.8+ |
| PostgreSQL | 15+ |
| Python | 3.10+ |

---

## Setup — Step by Step

### 1. Clone and open

```bash
git clone <repo-url>
cd locus-analytics
```

### 2. Configure PostgreSQL

```sql
-- Run in psql or pgAdmin
CREATE DATABASE locus_analytics;
```

### 3. Run the schema

```bash
psql -U postgres -d locus_analytics -f schema.sql
```

This creates all tables, indexes, triggers, and the audit-log function.

### 4. Edit config.properties

```bash
cp src/main/resources/config.properties src/main/resources/config.properties.bak
```

Open `src/main/resources/config.properties` and fill in:

```properties
# PostgreSQL
pg.db.url=jdbc:postgresql://localhost:5432/locus_analytics
pg.db.username=postgres
pg.db.password=YOUR_PASSWORD

# Google Maps (for heatmap)
google.maps.api.key=YOUR_GOOGLE_MAPS_KEY

# ML model path (relative to project root)
ml.model.path=ml/model.json
```

### 5. Seed the database

```bash
cd data
pip install psycopg2-binary pandas numpy
python generate_seed.py
```

Expected output: `[SEED] 800 properties inserted.`

### 6. Train the ML model

```bash
cd ml
pip install -r requirements.txt     # scikit-learn pandas numpy psycopg2-binary
python train_model.py
```

Expected output:
```
Test R2:  0.87xx
Test MAE: PKR 2,xxx,xxx
[EXPORT] Model saved to ml/model.json
```

The `ml/model.json` file must exist before the app starts.

### 7. Seed users

Run this SQL to create a default admin and analyst:

```sql
-- Password: admin123  (BCrypt hash)
INSERT INTO app_user (user_id, name, email, password_hash, role)
VALUES (
  'admin-001',
  'System Admin',
  'admin@locus.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh7y',
  'admin'
);

-- Password: analyst123  (BCrypt hash)
INSERT INTO app_user (user_id, name, email, password_hash, role)
VALUES (
  'analyst-001',
  'Property Analyst',
  'analyst@locus.com',
  '$2a$10$8Kp8HvKkJnq2Xt1QFvCo4.AoQxm5UUBdL3qqz.rvSwv.9fDiTZCnS',
  'analyst'
);
```

### 8. Build the application

```bash
mvn clean package -DskipTests
```

Produces: `target/locus-analytics-1.0-SNAPSHOT.jar`

### 9. Run the application

```bash
java -jar target/locus-analytics-1.0-SNAPSHOT.jar
```

Or via Maven:

```bash
mvn javafx:run
```

Login with `admin@locus.com` / `admin123`.

---

## Running the ETL Pipeline

The ETL can be triggered from inside the app (Admin → Run ETL Pipeline), or manually:

```bash
cd etl
pip install psycopg2-binary
python run_etl.py
```

The script:
- Generates realistic property listings for Karachi, Lahore, Islamabad
- Skips records already in the database (deduplication via `url_hash`)
- Writes progress to the `etl_job` table so the UI progress bar updates in real time
- Supports resume: re-running after a crash skips already-loaded records

After ETL, retrain the model:

```bash
cd ml
python retrain_model.py
```

The Java app hot-reloads `ml/model.json` automatically — no restart needed.

---

## Running Unit Tests

```bash
mvn test
```

Tests are in `src/test/java/com/locus/service/` — 7 service classes, 63 test cases.
No database required (tests use in-memory stubs).

---

## Project Structure

```
locus-analytics/
├── schema.sql                      # PostgreSQL DDL (run once)
├── src/
│   ├── main/
│   │   ├── java/com/locus/
│   │   │   ├── config/             # DBConnection (HikariCP)
│   │   │   ├── dao/                # DAO interfaces + implementations
│   │   │   ├── exception/          # ValidationException
│   │   │   ├── ml/                 # LinearRegressionPredictor
│   │   │   ├── model/              # POJOs + DTOs
│   │   │   ├── service/            # Service interfaces + implementations
│   │   │   └── ui/                 # JavaFX controllers, FXML, CSS
│   │   └── resources/
│   │       ├── config.properties   # DB + API credentials
│   │       └── fxml/               # 14 screen FXML files
│   └── test/
│       └── java/com/locus/service/ # JUnit 5 unit tests
├── data/
│   └── generate_seed.py            # Seed 800 realistic properties
├── etl/
│   └── run_etl.py                  # ETL scraper + loader
├── ml/
│   ├── train_model.py              # Initial Ridge regression training
│   ├── retrain_model.py            # Post-ETL retraining with comparison
│   └── model.json                  # Trained model artifact (generated)
└── scripts/
    ├── backup.sh                   # Linux/macOS weekly backup
    └── backup.bat                  # Windows weekly backup
```

---

## Database Backup

```bash
# Linux / macOS
bash scripts/backup.sh

# Windows
scripts\backup.bat
```

Backups are stored in `backups/` (last 4 weeks retained automatically).

---

## Use Cases Supported

| # | Use Case | Screen |
|---|---|---|
| UC-1 | Estimate Fair Market Value | FMV Estimate |
| UC-2 | Calculate Rental Yield | Rental Yield |
| UC-3 | Calculate ROI | ROI Calculator |
| UC-4 | Search Properties | Search |
| UC-5 | Compare Properties | Compare |
| UC-6 | View Price Trends | Price Trends |
| UC-7 | Property Heatmap | Heatmap |
| UC-8 | Generate Valuation Report (PDF) | Reports |
| UC-9 | Identify Investment Clusters | Clusters |
| UC-10 | Run ETL Pipeline | ETL Dashboard (Admin) |
| UC-11 | Manage Listings | Listings (Admin) |
| UC-12 | System Configuration | Config (Admin) |
