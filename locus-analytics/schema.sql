-- Locus Analytics — PostgreSQL Schema
-- Author: Fasih
-- All tables are mirrored in Oracle (see schema_oracle.sql)

DROP FUNCTION IF EXISTS update_timestamp CASCADE;

DROP TABLE IF EXISTS valuation_report CASCADE;
DROP TABLE IF EXISTS roi_analysis CASCADE;
DROP TABLE IF EXISTS rental_analysis CASCADE;
DROP TABLE IF EXISTS valuation CASCADE;
DROP TABLE IF EXISTS property CASCADE;
DROP TABLE IF EXISTS investment_cluster CASCADE;
DROP TABLE IF EXISTS etl_job CASCADE;
DROP TABLE IF EXISTS system_configuration CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;

CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 1. Users
CREATE TABLE app_user (
    user_id VARCHAR PRIMARY KEY,

    name VARCHAR NOT NULL,
    email VARCHAR UNIQUE NOT NULL,

    role VARCHAR NOT NULL CHECK (role IN ('admin', 'analyst')),
    certification_level VARCHAR,
    access_level VARCHAR,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Property
CREATE TABLE property (
    property_id VARCHAR PRIMARY KEY,

    city VARCHAR NOT NULL,
    locality VARCHAR NOT NULL,
    property_type VARCHAR NOT NULL,

    area NUMERIC NOT NULL CHECK (area > 0),
    price NUMERIC NOT NULL CHECK (price > 0),

    bedrooms INT CHECK (bedrooms >= 0),
    bathrooms INT CHECK (bathrooms >= 0),

    listing_date DATE,

    latitude NUMERIC,
    longitude NUMERIC,

    price_per_sqft NUMERIC GENERATED ALWAYS AS (price / NULLIF(area, 0)) STORED,

    url_hash VARCHAR UNIQUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_property_city ON property(city);
CREATE INDEX idx_property_locality ON property(locality);
CREATE INDEX idx_property_city_locality ON property(city, locality);
CREATE INDEX idx_property_date ON property(listing_date);
CREATE INDEX idx_property_type ON property(property_type);
CREATE INDEX idx_property_city_date ON property(city, listing_date);
CREATE INDEX idx_property_city_locality_type ON property(city, locality, property_type);

-- 3. Valuation
CREATE TABLE valuation (
    valuation_id VARCHAR PRIMARY KEY,

    property_id VARCHAR NOT NULL REFERENCES property(property_id) ON DELETE CASCADE,

    estimation_fmv NUMERIC CHECK (estimation_fmv > 0),
    confidence_lower_limit NUMERIC,
    confidence_upper_limit NUMERIC,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_valuation_property ON valuation(property_id);

-- 4. Rental Analysis
CREATE TABLE rental_analysis (
    analysis_id VARCHAR PRIMARY KEY,

    property_id VARCHAR NOT NULL REFERENCES property(property_id) ON DELETE CASCADE,

    annual_rent NUMERIC CHECK (annual_rent >= 0),
    annual_expenses NUMERIC CHECK (annual_expenses >= 0),

    gross_yield NUMERIC,
    net_yield NUMERIC,

    analyst_id VARCHAR REFERENCES app_user(user_id) ON DELETE SET NULL,

    analysis_date DATE NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rental_property ON rental_analysis(property_id);

-- 5. ROI Analysis
CREATE TABLE roi_analysis (
    analysis_id VARCHAR PRIMARY KEY,

    property_id VARCHAR NOT NULL REFERENCES property(property_id) ON DELETE CASCADE,

    analysis_date DATE NOT NULL,

    purchase_price NUMERIC CHECK (purchase_price > 0),
    purchase_date DATE,

    cumulative_rental_income NUMERIC DEFAULT 0,
    total_expenses NUMERIC DEFAULT 0,

    total_return NUMERIC,
    roi_percentage NUMERIC,
    annualized_roi NUMERIC,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_roi_property ON roi_analysis(property_id);

-- 6. Valuation Report
CREATE TABLE valuation_report (
    report_id VARCHAR PRIMARY KEY,

    property_id VARCHAR NOT NULL REFERENCES property(property_id) ON DELETE CASCADE,
    analyst_id VARCHAR REFERENCES app_user(user_id) ON DELETE SET NULL,

    generation_date DATE NOT NULL,

    included_sections TEXT,
    analyst_notes TEXT,

    pdf_file_path VARCHAR,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Investment Cluster
CREATE TABLE investment_cluster (
    cluster_id VARCHAR PRIMARY KEY,

    city VARCHAR NOT NULL,
    locality VARCHAR,

    investment_score NUMERIC,
    price_appreciation NUMERIC,
    listing_volume_growth NUMERIC,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. ETL Jobs
CREATE TABLE etl_job (
    job_id VARCHAR PRIMARY KEY,

    admin_id VARCHAR REFERENCES app_user(user_id) ON DELETE SET NULL,

    run_date DATE NOT NULL,

    records_extracted INT CHECK (records_extracted >= 0),
    records_cleaned INT CHECK (records_cleaned >= 0),
    records_loaded INT CHECK (records_loaded >= 0),

    errors INT DEFAULT 0,

    status VARCHAR CHECK (status IN ('success', 'failed', 'running')),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. System Configuration
CREATE TABLE system_configuration (
    config_id VARCHAR PRIMARY KEY,

    admin_id VARCHAR REFERENCES app_user(user_id) ON DELETE SET NULL,

    db_host VARCHAR,
    google_maps_api_key VARCHAR,
    zameen_scrape_interval VARCHAR,
    model_file_path VARCHAR,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER trg_user BEFORE UPDATE ON app_user
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_property BEFORE UPDATE ON property
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_valuation BEFORE UPDATE ON valuation
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_rental BEFORE UPDATE ON rental_analysis
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_roi BEFORE UPDATE ON roi_analysis
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_report BEFORE UPDATE ON valuation_report
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_cluster BEFORE UPDATE ON investment_cluster
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_etl BEFORE UPDATE ON etl_job
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_config BEFORE UPDATE ON system_configuration
FOR EACH ROW EXECUTE FUNCTION update_timestamp();