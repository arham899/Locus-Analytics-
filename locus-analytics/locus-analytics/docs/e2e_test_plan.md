# LOCUS Analytics — End-to-End Test Plan
**Author:** Ayaan Aman (24i-0663)  
**Phase:** 3 — Reports, Polish, Testing

---

## Test Environment Setup

- App running via `mvn javafx:run` or `java -jar target/locus-analytics-*.jar`
- PostgreSQL running with seed data loaded (≥ 800 properties)
- `ml/model.json` present (run `python ml/train_model.py` first)
- Two test accounts: `admin@locus.com` / `admin123` and `analyst@locus.com` / `analyst123`

---

## Login Screen

| # | Steps | Expected | Pass/Fail |
|---|---|---|---|
| L-1 | Enter valid admin credentials → click Login | Navigates to main screen, sidebar shows Admin menus | |
| L-2 | Enter valid analyst credentials → click Login | Navigates to main screen, sidebar does **not** show Admin menus | |
| L-3 | Enter wrong password → click Login | Error message "Invalid credentials" shown, no navigation | |
| L-4 | Leave email blank → click Login | Validation error on email field | |
| L-5 | Leave password blank → click Login | Validation error on password field | |

---

## UC-1 — Estimate Fair Market Value

| # | Steps | Expected | Pass/Fail |
|---|---|---|---|
| FMV-1 | City=Karachi, Locality=DHA Phase 5, Type=house, Area=2000, Beds=3, Baths=3 → Submit | FMV displayed (PKR > 0), confidence interval shown, comparables table populated | |
| FMV-2 | Submit with blank locality | Field-level error on locality field | |
| FMV-3 | Submit with Area=0 | Validation error on area | |
| FMV-4 | City=London (invalid) | City validation error | |
| FMV-5 | Locality not in model (e.g. "Unknown Area") | Error: locality not supported | |
| FMV-6 | Click "Generate Report" after a valid estimate | Navigates to Report screen with property pre-filled | |

---

## UC-2 — Calculate Rental Yield

| # | Steps | Expected | Pass/Fail |
|---|---|---|---|
| RY-1 | Property Value=10M, Monthly Rent=50K, Expenses=0 → Submit | Gross Yield=6.0%, Net Yield=6.0% displayed | |
| RY-2 | Same as RY-1 but Expenses=120K/yr | Net Yield=4.8%, Gross still 6.0% | |
| RY-3 | Monthly Rent=100K on Value=1M (annual rent > value) | Validation error | |
| RY-4 | Property Value=0 | Validation error | |
| RY-5 | Use "Select from previous valuations" dropdown | Property value auto-filled from last FMV estimate | |
| RY-6 | Submit valid form | City average comparison bar chart visible | |

---

## UC-3 — Calculate ROI

| # | Steps | Expected | Pass/Fail |
|---|---|---|---|
| ROI-1 | Purchase=10M, Date=3yr ago, Current=14M, Rental=2M, Expenses=0.5M → Submit | Total Return=5.5M, ROI%=55, positive annualized ROI shown | |
| ROI-2 | Current Value < Purchase Price | Negative ROI displayed in red | |
| ROI-3 | Purchase date = tomorrow (future) | Validation error on purchase date | |
| ROI-4 | Click "Auto-estimate current value" button | Calls FMV service, populates current value field | |
| ROI-5 | Purchase date = 3 months ago | Warning banner: "Holding period < 1 year" visible | |
| ROI-6 | Submit valid form | Pie chart (capital vs rental income) and value growth line chart displayed | |

---

## UC-4 — Search Properties

| # | Steps | Expected | Pass/Fail |
|---|---|---|---|
| S-1 | No filters → Search | TableView populated with properties, pagination controls visible | |
| S-2 | City=Lahore, Type=apartment → Search | Only Lahore apartments returned | |
| S-3 | Min Price=50M, Max Price=10M → Search | Validation error (min > max) | |
| S-4 | Filter yields no results | Empty state message shown | |
| S-5 | Change page → click Next | Next page of results loaded | |
| S-6 | Click column header (Price) | Table sorted by price | |
| S-7 | Double-click a row | Property detail dialog opens | |
| S-8 | Select 2 rows → click "Add to Compare" | Navigates to Compare screen with those 2 properties pre-filled | |

---

## UC-5 — Compare Properties

| # | Steps | Expected | Pass/Fail |
|---|---|---|---|
| C-1 | Enter 2 valid property IDs → Compare | Side-by-side table with BEST (green) and WORST (red) cells | |
| C-2 | Enter 4 property IDs → Compare | All 4 columns shown | |
| C-3 | Enter 1 property ID → Compare | Validation error: need at least 2 | |
| C-4 | Enter 5 property IDs → Compare | Validation error: max 4 | |
| C-5 | Property with no FMV → Compare | N/A shown with "Run Estimate" link | |
| C-6 | Click "Export to Report" | Navigates to Report screen with selected properties | |

---

## UC-6 — Price Trends

| # | Steps | Expected | Pass/Fail |
|---|---|---|---|
| PT-1 | City=Islamabad, Locality=Sector F-8, Type=house, Range=1Y → Submit | Line chart with monthly data points, statistics card shown | |
| PT-2 | Locality with < 6 data points | Fallback message: "Using city-level data" displayed | |
| PT-3 | Time range=5Y | Chart spans 5 years of data | |
| PT-4 | Custom date range with from > to | Validation error | |
| PT-5 | Toggle residential vs commercial overlay | Two series appear on same chart | |
| PT-6 | Click chart data point | Zoom / tooltip shows monthly detail | |

---

## UC-7 — Property Heatmap

| # | Steps | Expected | Pass/Fail |
|---|---|---|---|
| HM-1 | City=Karachi, Metric=Price per sq.ft., Type=house → Load | Google Map loads with heatmap layer | |
| HM-2 | Hover over a cluster | Tooltip shows locality name, avg price, listing count | |
| HM-3 | No internet / API key invalid | Offline fallback table shown with notice | |
| HM-4 | Change metric to Rental Demand → Load | Heatmap refreshes with new weights | |
| HM-5 | Zoom and pan map | Map interaction works smoothly | |

---

## UC-8 — Valuation Report

| # | Steps | Expected | Pass/Fail |
|---|---|---|---|
| R-1 | Select property with FMV, Rental, ROI all computed; tick all sections; add notes → Generate | PDF preview shown with all sections | |
| R-2 | No previous valuations for user | Message: "No valuations found — run FMV estimate first" | |
| R-3 | Tick only FMV + Comparables sections | PDF has only those 2 sections; others show "Not calculated" | |
| R-4 | Click Save → choose path | File saved; success notification shown | |
| R-5 | Click Print | Print dialog opens | |
| R-6 | Price trend section with no data for locality | Section shows city-level trend with note | |

---

## UC-9 — Investment Clusters

| # | Steps | Expected | Pass/Fail |
|---|---|---|---|
| IC-1 | City=Lahore, Period=3Y, Min listings=5 → Identify | Ranked table with investment scores 0–100 | |
| IC-2 | Scores normalized — top locality = 100 | Highest score is 100.00 | |
| IC-3 | Click a row | Drill-down panel shows price appreciation, volume growth, rental trend | |
| IC-4 | Map markers visible | Markers on map correspond to ranked localities | |
| IC-5 | No internet | Map hidden; table shown with "Map unavailable" notice | |
| IC-6 | Invalid city | Validation error | |

---

## UC-10 — ETL Pipeline (Admin only)

| # | Steps | Expected | Pass/Fail |
|---|---|---|---|
| E-1 | Login as analyst → check sidebar | ETL Dashboard menu item NOT visible | |
| E-2 | Login as admin → open ETL Dashboard | Last run summary shown (date, records, status) | |
| E-3 | Click "Start Pipeline" | Button disables; progress bar appears; stage label updates (Extracting → Transforming → Loading) | |
| E-4 | Wait for completion | Summary dialog shows extracted/cleaned/loaded/errors counts | |
| E-5 | Click "Start Pipeline" while job running | Button remains disabled | |

---

## UC-11 — Manage Listings (Admin only)

| # | Steps | Expected | Pass/Fail |
|---|---|---|---|
| ML-1 | Open Manage Listings as admin | All properties listed in searchable table | |
| ML-2 | Click "Add Listing" → fill form → Save | New property appears in table | |
| ML-3 | Add with price=0 | Validation error on price field | |
| ML-4 | Select a row → click Edit → change price → Save | Updated price reflected in table | |
| ML-5 | Select a row → click Delete → confirm | Row removed from table | |
| ML-6 | Click Delete without selecting a row | Delete button is disabled | |

---

## UC-12 — System Configuration (Admin only)

| # | Steps | Expected | Pass/Fail |
|---|---|---|---|
| SC-1 | Open Config as admin | Current values loaded in all tabs | |
| SC-2 | Change DB host to blank → Save | Validation error | |
| SC-3 | Change Scrape Interval to "0" → Save | Validation error (must be > 0) | |
| SC-4 | Update a valid value → Save | Success notification; audit log entry created | |
| SC-5 | Login as analyst → check sidebar | Config menu NOT visible | |

---

## Cross-UC Flows

| # | Scenario | Expected | Pass/Fail |
|---|---|---|---|
| X-1 | Search → select 2 properties → Compare → Export to Report → Generate PDF | Data flows correctly between all 4 screens | |
| X-2 | Estimate FMV → Calculate Rental Yield using that FMV → Calculate ROI → Generate Report with all sections | All 3 computations visible in final PDF | |
| X-3 | Run ETL → retrain model (`retrain_model.py`) → run FMV estimate → model uses updated predictions | New model loaded (hot-reload log visible in console) | |

---

## Role-Based Access Control

| # | Action | Admin | Analyst |
|---|---|---|---|
| RBAC-1 | ETL Dashboard visible | ✓ | ✗ |
| RBAC-2 | Manage Listings visible | ✓ | ✗ |
| RBAC-3 | System Config visible | ✓ | ✗ |
| RBAC-4 | FMV Estimate accessible | ✓ | ✓ |
| RBAC-5 | Reports accessible | ✓ | ✓ |

---

## Error / Edge Case Paths

| # | Scenario | Expected | Pass/Fail |
|---|---|---|---|
| ERR-1 | Kill DB connection mid-operation | User-friendly error dialog, no crash | |
| ERR-2 | Google Maps API quota exceeded | Offline fallback table shown | |
| ERR-3 | Submit all forms with empty inputs | All mandatory fields show validation errors | |
| ERR-4 | Navigate rapidly between screens | No stale data from previous screen shown | |
| ERR-5 | Resize window to minimum | UI remains usable (ScrollPane / responsive layout) | |
