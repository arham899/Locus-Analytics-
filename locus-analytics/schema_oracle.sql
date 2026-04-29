-- Locus Analytics — Oracle Schema
-- Full mirror of PostgreSQL schema (same tables, same data)
-- Author: Fasih
-- Use this DDL to set up Oracle as an alternative/backup database

-- ─────────────────────────────────────────────
-- 1. Users
-- ─────────────────────────────────────────────
CREATE TABLE app_user (
    user_id             VARCHAR2(255) PRIMARY KEY,
    name                VARCHAR2(255) NOT NULL,
    email               VARCHAR2(255) UNIQUE NOT NULL,
    password_hash       VARCHAR2(255),
    role                VARCHAR2(50)  NOT NULL,
    certification_level VARCHAR2(100),
    access_level        VARCHAR2(100),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_role CHECK (role IN ('admin', 'analyst'))
);

-- ─────────────────────────────────────────────
-- 2. Property
-- ─────────────────────────────────────────────
CREATE TABLE property (
    property_id   VARCHAR2(255) PRIMARY KEY,
    city          VARCHAR2(255) NOT NULL,
    locality      VARCHAR2(255) NOT NULL,
    property_type VARCHAR2(100) NOT NULL,
    area          NUMBER        NOT NULL,
    price         NUMBER        NOT NULL,
    bedrooms      NUMBER(10),
    bathrooms     NUMBER(10),
    listing_date  DATE,
    latitude      NUMBER,
    longitude     NUMBER,
    price_per_sqft NUMBER GENERATED ALWAYS AS (price / NULLIF(area, 0)) VIRTUAL,
    url_hash      VARCHAR2(255) UNIQUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_area  CHECK (area > 0),
    CONSTRAINT chk_price CHECK (price > 0),
    CONSTRAINT chk_bed   CHECK (bedrooms >= 0),
    CONSTRAINT chk_bath  CHECK (bathrooms >= 0)
);

CREATE INDEX idx_property_city               ON property(city);
CREATE INDEX idx_property_locality           ON property(locality);
CREATE INDEX idx_property_city_locality      ON property(city, locality);
CREATE INDEX idx_property_date               ON property(listing_date);
CREATE INDEX idx_property_type               ON property(property_type);
CREATE INDEX idx_property_city_date          ON property(city, listing_date);
CREATE INDEX idx_property_city_locality_type ON property(city, locality, property_type);

-- ─────────────────────────────────────────────
-- 3. Valuation
-- ─────────────────────────────────────────────
CREATE TABLE valuation (
    valuation_id           VARCHAR2(255) PRIMARY KEY,
    property_id            VARCHAR2(255) NOT NULL REFERENCES property(property_id) ON DELETE CASCADE,
    estimation_fmv         NUMBER,
    confidence_lower_limit NUMBER,
    confidence_upper_limit NUMBER,
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_fmv CHECK (estimation_fmv > 0)
);

CREATE INDEX idx_valuation_property ON valuation(property_id);

-- ─────────────────────────────────────────────
-- 4. Rental Analysis
-- ─────────────────────────────────────────────
CREATE TABLE rental_analysis (
    analysis_id     VARCHAR2(255) PRIMARY KEY,
    property_id     VARCHAR2(255) NOT NULL REFERENCES property(property_id) ON DELETE CASCADE,
    annual_rent     NUMBER,
    annual_expenses NUMBER,
    gross_yield     NUMBER,
    net_yield       NUMBER,
    analyst_id      VARCHAR2(255) REFERENCES app_user(user_id),
    analysis_date   DATE NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_rent     CHECK (annual_rent >= 0),
    CONSTRAINT chk_expenses CHECK (annual_expenses >= 0)
);

CREATE INDEX idx_rental_property ON rental_analysis(property_id);

-- ─────────────────────────────────────────────
-- 5. ROI Analysis
-- ─────────────────────────────────────────────
CREATE TABLE roi_analysis (
    analysis_id             VARCHAR2(255) PRIMARY KEY,
    property_id             VARCHAR2(255) NOT NULL REFERENCES property(property_id) ON DELETE CASCADE,
    analysis_date           DATE NOT NULL,
    purchase_price          NUMBER,
    purchase_date           DATE,
    cumulative_rental_income NUMBER DEFAULT 0,
    total_expenses          NUMBER DEFAULT 0,
    total_return            NUMBER,
    roi_percentage          NUMBER,
    annualized_roi          NUMBER,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_purchase CHECK (purchase_price > 0)
);

CREATE INDEX idx_roi_property ON roi_analysis(property_id);

-- ─────────────────────────────────────────────
-- 6. Valuation Report
-- ─────────────────────────────────────────────
CREATE TABLE valuation_report (
    report_id          VARCHAR2(255) PRIMARY KEY,
    property_id        VARCHAR2(255) NOT NULL REFERENCES property(property_id) ON DELETE CASCADE,
    analyst_id         VARCHAR2(255) REFERENCES app_user(user_id),
    generation_date    DATE NOT NULL,
    included_sections  CLOB,
    analyst_notes      CLOB,
    pdf_file_path      VARCHAR2(500),
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────
-- 7. Investment Cluster
-- ─────────────────────────────────────────────
CREATE TABLE investment_cluster (
    cluster_id             VARCHAR2(255) PRIMARY KEY,
    city                   VARCHAR2(255) NOT NULL,
    locality               VARCHAR2(255),
    investment_score       NUMBER,
    price_appreciation     NUMBER,
    listing_volume_growth  NUMBER,
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────
-- 8. ETL Jobs
-- ─────────────────────────────────────────────
CREATE TABLE etl_job (
    job_id            VARCHAR2(255) PRIMARY KEY,
    admin_id          VARCHAR2(255) REFERENCES app_user(user_id),
    run_date          DATE NOT NULL,
    records_extracted NUMBER(10) DEFAULT 0,
    records_cleaned   NUMBER(10) DEFAULT 0,
    records_loaded    NUMBER(10) DEFAULT 0,
    errors            NUMBER(10) DEFAULT 0,
    status            VARCHAR2(50),
    current_stage     VARCHAR2(100),
    progress_percent  NUMBER(3) DEFAULT 0,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_status CHECK (status IN ('success', 'failed', 'running', 'SUCCESS', 'FAILED', 'RUNNING'))
);

-- ─────────────────────────────────────────────
-- 9. System Configuration
-- ─────────────────────────────────────────────
CREATE TABLE system_configuration (
    config_id              VARCHAR2(255) PRIMARY KEY,
    admin_id               VARCHAR2(255) REFERENCES app_user(user_id),
    db_host                VARCHAR2(255),
    google_maps_api_key    VARCHAR2(255),
    zameen_scrape_interval VARCHAR2(100),
    model_file_path        VARCHAR2(500),
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────
-- Triggers for updated_at
-- ─────────────────────────────────────────────
CREATE OR REPLACE TRIGGER trg_user BEFORE UPDATE ON app_user
FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE OR REPLACE TRIGGER trg_property BEFORE UPDATE ON property
FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE OR REPLACE TRIGGER trg_valuation BEFORE UPDATE ON valuation
FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE OR REPLACE TRIGGER trg_rental BEFORE UPDATE ON rental_analysis
FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE OR REPLACE TRIGGER trg_roi BEFORE UPDATE ON roi_analysis
FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE OR REPLACE TRIGGER trg_report BEFORE UPDATE ON valuation_report
FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE OR REPLACE TRIGGER trg_cluster BEFORE UPDATE ON investment_cluster
FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE OR REPLACE TRIGGER trg_etl BEFORE UPDATE ON etl_job
FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE OR REPLACE TRIGGER trg_config BEFORE UPDATE ON system_configuration
FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/
