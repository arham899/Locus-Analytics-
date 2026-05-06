-- ================================================================
-- LOCUS Analytics — Fix Bad Property Data
-- Targets rows by suspicious value ranges, not by ID prefix
-- ================================================================

-- STEP 1: Fix prices — values < 10000 are in Lakhs
-- Real PKR prices are 500,000+ (5 Lakh minimum)
-- Values like 30, 85, 105, 3475 are clearly in Lakhs
UPDATE property
SET price = price * 100000
WHERE price < 10000;

-- STEP 2: Fix area — values < 100 are in Marla (1 Marla = 225 sq.ft.)
-- Real sq.ft. values are 200+ minimum
UPDATE property
SET area = area * 225.0
WHERE area < 100;

-- STEP 3: Fix NULL bedrooms — estimate from area
UPDATE property
SET bedrooms = CASE
    WHEN area < 700  THEN 1
    WHEN area < 1200 THEN 2
    WHEN area < 2000 THEN 3
    WHEN area < 3500 THEN 4
    ELSE 5
END
WHERE bedrooms IS NULL
  AND property_type NOT IN ('plot', 'commercial');

-- Set bedrooms to 0 for plots/commercial that are NULL
UPDATE property
SET bedrooms = 0
WHERE bedrooms IS NULL;

-- STEP 4: Fix NULL bathrooms — estimate from bedrooms
UPDATE property
SET bathrooms = GREATEST(1, bedrooms - 1)
WHERE bathrooms IS NULL
  AND bedrooms > 0;

-- Set bathrooms to 0 where bedrooms is 0
UPDATE property
SET bathrooms = 0
WHERE bathrooms IS NULL;

-- STEP 5: Verify (price_per_sqft auto-recalculates — it's a generated column)
SELECT property_id, city, locality, property_type, 
       price, area, bedrooms, bathrooms, price_per_sqft
FROM property
ORDER BY RANDOM()
LIMIT 15;

