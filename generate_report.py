"""Generate the LOCUS Analytics SRS/Design report PDF."""
from reportlab.lib.pagesizes import LETTER
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_JUSTIFY
from reportlab.lib.units import inch
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, PageBreak, Image,
    Table, TableStyle, ListFlowable, ListItem, KeepTogether
)
import os

OUT = r"c:\Users\muhammad arham\OneDrive\Desktop\locus analytics\LOCUS_Analytics_SRS_Design_Document.pdf"
ASSETS = r"c:\Users\muhammad arham\OneDrive\Desktop\locus analytics\Locus-Analytics-"

styles = getSampleStyleSheet()
H1 = ParagraphStyle('H1', parent=styles['Heading1'], fontSize=18, spaceAfter=12, textColor=colors.HexColor('#0B3D91'))
H2 = ParagraphStyle('H2', parent=styles['Heading2'], fontSize=14, spaceAfter=8, textColor=colors.HexColor('#0B3D91'))
H3 = ParagraphStyle('H3', parent=styles['Heading3'], fontSize=12, spaceAfter=6, textColor=colors.HexColor('#222'))
BODY = ParagraphStyle('Body', parent=styles['BodyText'], fontSize=10.5, leading=15, alignment=TA_JUSTIFY, spaceAfter=8)
TITLE = ParagraphStyle('Title', parent=styles['Title'], fontSize=28, alignment=TA_LEFT, leading=34, spaceAfter=20)
META = ParagraphStyle('Meta', parent=styles['BodyText'], fontSize=12, alignment=TA_LEFT, spaceAfter=10)
CODE = ParagraphStyle('Code', parent=styles['Code'], fontSize=9, leading=12, leftIndent=12, backColor=colors.HexColor('#F4F4F4'))

doc = SimpleDocTemplate(OUT, pagesize=LETTER,
                        leftMargin=0.9*inch, rightMargin=0.9*inch,
                        topMargin=0.9*inch, bottomMargin=0.9*inch,
                        title="LOCUS Analytics - SRS & Design Document")

story = []

def P(text, style=BODY):
    story.append(Paragraph(text, style))

def bullets(items):
    flow = ListFlowable(
        [ListItem(Paragraph(i, BODY), leftIndent=10) for i in items],
        bulletType='bullet', start='•', leftIndent=18
    )
    story.append(flow)
    story.append(Spacer(1, 6))

# ---------------- COVER PAGE ----------------
story.append(Spacer(1, 1.2*inch))
story.append(Paragraph("Software Requirements and<br/>Design Document", TITLE))
story.append(Spacer(1, 0.4*inch))
story.append(Paragraph("for", META))
story.append(Paragraph("<b>LOCUS ANALYTICS</b><br/><i>Real-Estate Analytics Platform for Pakistan</i>", H1))
story.append(Spacer(1, 0.6*inch))
story.append(Paragraph("<b>Prepared by</b><br/>"
                       "Fasih Ul Mubashir (24i-0517) — System Engineer<br/>"
                       "Arham Manzoor (24i-0640) — Lead Data Scientist<br/>"
                       "Ayaan Aman (24i-0663) — Frontend Architect", META))
story.append(Spacer(1, 0.3*inch))
story.append(Paragraph("<b>SCOPE:</b> JavaFX desktop application covering FMV estimation, "
                       "rental yield, ROI, price trends, heatmaps, investment clusters, "
                       "and PDF reporting for the Karachi, Lahore, and Islamabad markets.", META))
story.append(Paragraph("<b>DATE:</b> May 7, 2026", META))
story.append(PageBreak())

# ---------------- TOC ----------------
P("Table of Contents", H1)
toc_items = [
    "1. Introduction",
    "    1.1 Purpose",
    "    1.2 Product Scope",
    "    1.3 Title",
    "    1.4 Objectives",
    "    1.5 Problem Statement",
    "2. Overall Description",
    "    2.1 Product Perspective",
    "    2.2 Product Functions",
    "    2.3 List of Use Cases",
    "    2.4 Extended Use Cases",
    "    2.5 Use Case Diagram",
    "3. Other Nonfunctional Requirements",
    "    3.1 Performance Requirements",
    "    3.2 Safety Requirements",
    "    3.3 Security Requirements",
    "    3.4 Software Quality Attributes",
    "    3.5 Business Rules",
    "    3.6 Operating Environment",
    "    3.7 User Interfaces",
    "4. Domain Model",
    "5. System Sequence Diagram",
    "6. Sequence Diagram",
    "7. Class Diagram",
    "8. Package Diagram",
    "9. Deployment Diagram",
]
for t in toc_items:
    P(t, ParagraphStyle('toc', parent=BODY, spaceAfter=2))
story.append(PageBreak())

# ---------------- 1. INTRODUCTION ----------------
P("1. Introduction", H1)

P("1.1 Purpose", H2)
P("This Software Requirements Specification (SRS) describes the requirements and design of "
  "<b>LOCUS Analytics v1.0</b>, a JavaFX-based desktop platform for real-estate analytics in Pakistan. "
  "The document covers the entire system: the JavaFX presentation layer, the service and DAO layers, "
  "the PostgreSQL persistence layer, the Python ETL pipeline, and the Ridge-regression ML pricing model. "
  "It is intended for the development team, evaluators, and future maintainers.")

P("1.2 Product Scope", H2)
P("LOCUS Analytics enables property analysts and system administrators to ingest listings from public "
  "sources, estimate fair market value, calculate rental yield and ROI, visualise price trends and density "
  "heatmaps, identify investment clusters via K-Means, and produce branded PDF valuation reports. The "
  "product is delivered as a single Maven-built fat JAR backed by a PostgreSQL 15 database and supporting "
  "Python utilities (ETL, seeding, model retraining). The benefits are: (i) data-driven valuations replacing "
  "anecdotal pricing, (ii) a unified analyst workstation for the Karachi/Lahore/Islamabad markets, and "
  "(iii) automated, auditable PDF deliverables for clients.")

P("1.3 Title", H2)
P("<b>LOCUS Analytics — A Desktop Real-Estate Intelligence and Valuation Platform for Pakistan.</b> "
  "The aim is to provide property analysts an integrated workbench for valuation, yield analysis, and "
  "investment-cluster discovery using a transparent, retrainable ML model.")

P("1.4 Objectives", H2)
bullets([
    "Estimate Fair Market Value (FMV) of any residential property using a Ridge-regression model "
    "trained on locality, area, bedrooms, bathrooms, property type, and amenities.",
    "Compute rental yield and ROI for any listing or hypothetical investment scenario.",
    "Visualise locality-level price trends across configurable time ranges.",
    "Render a Google-Maps heatmap of listing density and price intensity.",
    "Discover investment clusters using K-Means over yield, growth, and price-per-sqft.",
    "Generate professional, branded PDF valuation reports for clients.",
    "Run an ETL pipeline that scrapes/synthesises listings, deduplicates, and loads into PostgreSQL.",
    "Allow administrators to manage users, listings, and ML/ETL system configuration.",
])

P("1.5 Problem Statement", H2)
P("Real-estate decisions in Pakistan are dominated by anecdotal pricing, fragmented portals, and a lack "
  "of analytical tooling. Buyers, sellers, and analysts rarely have access to consistent fair-market valuations "
  "or rental-yield/ROI calculators tied to a verifiable dataset. Existing portals show listings but not "
  "analytics; spreadsheets used by analysts are error-prone and non-reproducible.")
P("LOCUS Analytics addresses this by combining a curated PostgreSQL dataset, a transparent ML pricing model, "
  "and an analyst-grade JavaFX UI. Office automation replaces the current manual valuation workflow: a report "
  "that previously required several hours of spreadsheet work is generated as a branded PDF in seconds. "
  "<b>Feasibility:</b> the technology stack (Java 17, JavaFX, PostgreSQL, Python, scikit-learn) is open-source; "
  "the team has the required Java, Python, and DBMS skills; the dataset is bootstrapped via a deterministic "
  "seed script (800 realistic properties) and extended via the ETL pipeline, so no third-party data licence "
  "is needed for the prototype.")

story.append(PageBreak())

# ---------------- 2. OVERALL DESCRIPTION ----------------
P("2. Overall Description", H1)

P("2.1 Product Perspective", H2)
P("LOCUS Analytics is a new, self-contained product — not a follow-on of any prior system. It is composed of "
  "three cooperating subsystems that share a single PostgreSQL database:")
bullets([
    "<b>JavaFX Desktop Client</b> — the analyst-facing application (login, 14 screens, controllers, services).",
    "<b>Python ETL Pipeline</b> — extracts/synthesises listings, cleans them, and loads them into "
    "the <i>property</i> table while emitting progress to the <i>etl_job</i> table.",
    "<b>Python ML Module</b> — trains/retrains the Ridge regression model and serialises it to "
    "<i>ml/model.json</i>, which the Java <i>LinearRegressionPredictor</i> hot-reloads.",
])
P("External interfaces: (i) Google Maps JavaScript API for the heatmap WebView, (ii) PostgreSQL JDBC, "
  "(iii) the local filesystem for the generated <i>valuation-report-*.pdf</i> artefacts and the "
  "<i>backups/</i> directory.")

P("2.2 Product Functions", H2)
P("At a high level, the product offers:")
bullets([
    "Authentication with role-based access (analyst vs. admin), BCrypt password hashing.",
    "FMV estimation backed by a hot-reloadable Ridge regression model.",
    "Rental-yield and ROI calculators with input validation.",
    "Property search with multi-criteria filters and pagination.",
    "Side-by-side property comparison.",
    "Price-trend charts at the locality/city level.",
    "Density and price heatmap rendered through a Google Maps WebView.",
    "K-Means investment-cluster discovery with configurable parameters.",
    "Branded PDF valuation report generation and persistence.",
    "ETL pipeline orchestration with live progress, deduplication, and resume support.",
    "Admin-only listing management, user management, and system configuration.",
])

P("2.3 List of Use Cases", H2)
uc_data = [
    ["#", "Use Case", "Primary Actor", "Screen"],
    ["UC-1", "Estimate Fair Market Value", "Property Analyst", "FMV Estimate"],
    ["UC-2", "Calculate Rental Yield", "Property Analyst", "Rental Yield"],
    ["UC-3", "Calculate ROI", "Property Analyst", "ROI Calculator"],
    ["UC-4", "Search Properties", "Property Analyst", "Search"],
    ["UC-5", "Compare Properties", "Property Analyst", "Compare"],
    ["UC-6", "View Price Trends", "Property Analyst", "Price Trends"],
    ["UC-7", "Render Property Heatmap", "Property Analyst", "Heatmap"],
    ["UC-8", "Generate Valuation Report (PDF)", "Property Analyst", "Reports"],
    ["UC-9", "Identify Investment Clusters", "Property Analyst", "Clusters"],
    ["UC-10", "Run ETL Pipeline", "System Administrator", "ETL Dashboard"],
    ["UC-11", "Manage Listings", "System Administrator", "Listings"],
    ["UC-12", "System Configuration", "System Administrator", "Config"],
    ["UC-13", "Authenticate User", "All Users", "Login"],
]
t = Table(uc_data, colWidths=[0.55*inch, 2.5*inch, 1.6*inch, 1.6*inch])
t.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#0B3D91')),
    ('TEXTCOLOR', (0,0), (-1,0), colors.white),
    ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
    ('FONTSIZE', (0,0), (-1,-1), 9),
    ('GRID', (0,0), (-1,-1), 0.4, colors.grey),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor('#F4F6FA')]),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
]))
story.append(t)
story.append(Spacer(1, 12))

P("2.4 Extended Use Cases", H2)

P("UC-1: Estimate Fair Market Value (extended)", H3)
P("<b>Primary Actor:</b> Property Analyst &nbsp; <b>Stakeholders:</b> Client, System Admin<br/>"
  "<b>Preconditions:</b> User is authenticated; <i>ml/model.json</i> exists and has been loaded.<br/>"
  "<b>Trigger:</b> Analyst opens the FMV Estimate screen and submits a property profile.")
P("<b>Main Success Scenario:</b>")
bullets([
    "Analyst enters city, locality, area (sqft), bedrooms, bathrooms, type, and amenity flags.",
    "<i>InputValidator</i> rejects out-of-range values (e.g., area &lt; 100 sqft).",
    "<i>ValuationServiceImpl</i> assembles a feature vector and calls <i>LinearRegressionPredictor</i>.",
    "Predictor returns the FMV in PKR; the service persists a <i>Valuation</i> row.",
    "Controller renders the FMV plus a confidence band based on locality variance.",
])
P("<b>Extensions:</b> (a) Model file missing → service raises a friendly error and the UI shows a retry banner. "
  "(b) Locality unknown to the model → fall back to city-level mean encoding.")

P("UC-8: Generate Valuation Report (extended)", H3)
P("<b>Primary Actor:</b> Property Analyst<br/>"
  "<b>Preconditions:</b> A <i>Valuation</i> record exists for the target property.")
P("<b>Main Success Scenario:</b>")
bullets([
    "Analyst selects a stored valuation and clicks <i>Generate PDF</i>.",
    "<i>ValuationReportServiceImpl</i> aggregates property facts, FMV, comparables, and trend data.",
    "<i>ReportPdfServiceImpl</i> builds a branded PDF (logo, charts, tables) into <i>generated-reports/</i>.",
    "A <i>ValuationReport</i> row is inserted with the file path and SHA-256 hash.",
    "The UI offers <i>Open</i> and <i>Reveal in folder</i> actions.",
])
P("<b>Extensions:</b> (a) PDF write fails (disk full) → DAO transaction is rolled back; UI shows the I/O error.")

P("UC-10: Run ETL Pipeline (extended)", H3)
P("<b>Primary Actor:</b> System Administrator<br/>"
  "<b>Preconditions:</b> Admin is logged in; the database is reachable.")
P("<b>Main Success Scenario:</b>")
bullets([
    "Admin clicks <i>Run ETL</i>; <i>ETLServiceImpl</i> spawns the Python <i>run_etl.py</i> process.",
    "Python writes per-batch progress into the <i>etl_job</i> row created by the service.",
    "The JavaFX progress bar polls <i>ETLJobDAOImpl.findLatest()</i> and animates.",
    "On completion, the row is marked <i>SUCCESS</i> and the model can be retrained.",
])
P("<b>Extensions:</b> (a) Crash mid-run → resume on next invocation skips already-loaded <i>url_hash</i> values. "
  "(b) Network/DB error → row marked <i>FAILED</i> with the captured stderr.")

P("2.5 Use Case Diagram", H2)
P("The diagram below summarises actors and use cases. Two actors interact with the system: the "
  "<b>Property Analyst</b> (analytical use cases UC-1 to UC-9) and the <b>System Administrator</b> "
  "(operational use cases UC-10 to UC-12). Both inherit from the abstract <b>User</b> actor that "
  "performs UC-13 (Authenticate).")

uc_diagram = [
    ["Actor", "→", "Use Cases"],
    ["User (abstract)", "→", "UC-13 Authenticate"],
    ["Property Analyst", "→", "UC-1 Estimate FMV, UC-2 Rental Yield, UC-3 ROI, UC-4 Search, "
                              "UC-5 Compare, UC-6 Price Trends, UC-7 Heatmap, UC-8 Generate Report, "
                              "UC-9 Identify Clusters"],
    ["System Administrator", "→", "UC-10 Run ETL, UC-11 Manage Listings, UC-12 Configure System "
                                  "(plus all Analyst use cases)"],
]
t = Table(uc_diagram, colWidths=[1.6*inch, 0.3*inch, 4.3*inch])
t.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#0B3D91')),
    ('TEXTCOLOR', (0,0), (-1,0), colors.white),
    ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
    ('FONTSIZE', (0,0), (-1,-1), 9),
    ('GRID', (0,0), (-1,-1), 0.4, colors.grey),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
]))
story.append(t)
story.append(Spacer(1, 8))
P("<i>«include»</i> relationships: every analyst/admin use case includes UC-13 (Authenticate). "
  "<i>«extend»</i>: UC-8 (Generate Report) extends UC-1 when the analyst chooses to export an FMV result.")

story.append(PageBreak())

# ---------------- 3. NONFUNCTIONAL ----------------
P("3. Other Nonfunctional Requirements", H1)

P("3.1 Performance Requirements", H2)
bullets([
    "FMV estimation must complete in &lt; 200 ms on the analyst desktop after the model is loaded.",
    "Property search with up to four filters over 50,000 rows must return the first page in &lt; 500 ms "
    "(supported by indexes on <i>city</i>, <i>locality</i>, and <i>price</i>).",
    "PDF report generation must complete in &lt; 5 s for a single property with up to 10 comparables.",
    "ETL throughput target: ≥ 200 listings/second insert rate on the seeded dataset.",
    "Application cold-start (login screen visible) must be &lt; 4 s on a 4-core / 8-GB machine.",
    "Heatmap initial render &lt; 3 s for 5,000 points.",
])

P("3.2 Safety Requirements", H2)
bullets([
    "All destructive admin actions (delete listing, purge ETL job) require an explicit confirmation dialog.",
    "Database backups are produced weekly via <i>scripts/backup.sh</i>/<i>.bat</i> with 4-week retention; "
    "this protects against accidental data loss.",
    "PDF report generation never overwrites existing files — each report is named by UUID.",
    "The ML model is loaded into memory at startup and on file-change; a malformed model file falls back "
    "to the previously-loaded version rather than crashing the app.",
])

P("3.3 Security Requirements", H2)
bullets([
    "Passwords are stored as BCrypt hashes (cost factor 10) in <i>app_user.password_hash</i>.",
    "Role-based access control: analyst vs. admin; admin-only screens are gated by <i>SceneManager</i>.",
    "All DAO writes go through PreparedStatements — no string-concatenated SQL.",
    "Every write operation emits an <i>audit_log</i> row (actor, action, target, timestamp) via a "
    "PostgreSQL trigger.",
    "Database credentials and the Google Maps API key live in <i>config.properties</i> and are excluded "
    "from version control.",
    "The Google Maps WebView is sandboxed and cannot navigate outside the configured map domain.",
])

P("3.4 Software Quality Attributes", H2)
bullets([
    "<b>Maintainability:</b> strict layering (UI → Service → DAO → DB) with interface/impl separation; "
    "service stubs enable test isolation.",
    "<b>Testability:</b> 63 JUnit-5 unit tests across 7 service classes, runnable without a database.",
    "<b>Portability:</b> single fat JAR built with the Maven Shade plugin; runs on Windows, Linux, macOS "
    "with JDK 17+.",
    "<b>Reliability:</b> ETL is idempotent (deduplication via <i>url_hash</i>) and resumable after a crash.",
    "<b>Usability:</b> keyboard navigation, consistent CSS theming, and a fixed top navigation bar.",
    "<b>Reusability:</b> service interfaces are decoupled from JavaFX so they can be reused by a future "
    "REST/CLI front-end.",
])

P("3.5 Business Rules", H2)
bullets([
    "<b>BR-1:</b> Only <i>admin</i> users may run the ETL pipeline or modify system configuration.",
    "<b>BR-2:</b> Only <i>admin</i> users may delete or hard-edit a property listing; analysts may "
    "create valuations but not mutate source listings.",
    "<b>BR-3:</b> A valuation older than 30 days is flagged as <i>STALE</i> on the report.",
    "<b>BR-4:</b> A listing with area &lt; 100 sqft or price &lt; PKR 100,000 is rejected by ETL cleaning.",
    "<b>BR-5:</b> Generated PDF reports are immutable; corrections require a new report.",
    "<b>BR-6:</b> Every authenticated session lasts at most 8 hours, then a re-login is required.",
])

P("3.6 Operating Environment", H2)
bullets([
    "<b>OS:</b> Windows 10/11, Ubuntu 22.04 LTS, macOS 13+ (any host with a JVM 17 runtime).",
    "<b>JVM:</b> OpenJDK 17 or higher with JavaFX 21 modules.",
    "<b>Database:</b> PostgreSQL 15+ on the same host or a reachable network host (Oracle 19c also "
    "supported via <i>schema_oracle.sql</i>).",
    "<b>Python:</b> 3.10+ with <i>scikit-learn</i>, <i>pandas</i>, <i>numpy</i>, <i>psycopg2-binary</i>.",
    "<b>Hardware:</b> minimum 4-core CPU, 8 GB RAM, 1 GB free disk for backups and reports.",
    "<b>Network:</b> outbound HTTPS for Google Maps tiles only; the rest is local.",
])

P("3.7 User Interfaces", H2)
P("The UI is a JavaFX application with 14 FXML screens following a fixed-navbar layout. The brand palette "
  "uses navy (#0B3D91) as primary, white as background, and accent green for positive metrics. Standard "
  "controls present on every screen: top navigation bar with the LOCUS logo, screen title, user badge, and "
  "<b>Help</b> + <b>Logout</b> buttons. Forms validate on blur; error messages appear in a red banner below "
  "the form. Long-running operations (ETL, PDF generation) display a determinate progress bar driven by "
  "<i>UiAnimationHelper</i>. Keyboard shortcuts: <i>Ctrl+L</i> logout, <i>Ctrl+R</i> refresh, "
  "<i>Esc</i> close modal. Detailed UI specifications live in <i>BrandAssets.java</i> and the FXML "
  "stylesheet.")

story.append(PageBreak())

# ---------------- 4. DOMAIN MODEL ----------------
P("4. Domain Model", H1)
P("The domain model captures the conceptual entities of the property-analytics domain and their "
  "relationships, independent of any storage or UI concern. The principal concepts are:")

dom_table = [
    ["Concept", "Description", "Key Attributes"],
    ["User", "Authenticated actor of the system.", "userId, name, email, role, passwordHash"],
    ["PropertyAnalyst", "User who performs valuations and analyses.", "(specialisation of User)"],
    ["SystemAdministrator", "User who runs ETL and manages config.", "(specialisation of User)"],
    ["Property", "A single real-estate listing.", "propertyId, city, locality, area, beds, baths, price, type"],
    ["Valuation", "An FMV estimate produced for a property.", "valuationId, propertyId, fmv, generatedAt"],
    ["RentalAnalysis", "Yield computation for a property.", "annualRent, grossYield, netYield"],
    ["ROIAnalysis", "ROI scenario computation.", "investment, holdingYears, expectedROI"],
    ["InvestmentCluster", "K-Means cluster of localities/properties.", "clusterId, centroid, members"],
    ["ValuationReport", "Generated PDF artefact.", "reportId, valuationId, filePath, sha256"],
    ["ETLJob", "A run of the ETL pipeline.", "jobId, startedAt, status, processed, failed"],
    ["AuditLog", "Append-only record of writes.", "logId, actor, action, target, ts"],
    ["SystemConfiguration", "Tunable runtime parameters.", "key, value, updatedBy"],
]
t = Table(dom_table, colWidths=[1.5*inch, 2.6*inch, 2.2*inch])
t.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#0B3D91')),
    ('TEXTCOLOR', (0,0), (-1,0), colors.white),
    ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
    ('FONTSIZE', (0,0), (-1,-1), 8.5),
    ('GRID', (0,0), (-1,-1), 0.4, colors.grey),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor('#F4F6FA')]),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
]))
story.append(t)
story.append(Spacer(1, 10))

P("Key associations: <i>User</i> ◊— produces —◊ <i>Valuation</i>; <i>Property</i> 1—* <i>Valuation</i>; "
  "<i>Valuation</i> 1—1 <i>ValuationReport</i>; <i>Property</i> *—* <i>InvestmentCluster</i> "
  "(via cluster membership); <i>SystemAdministrator</i> 1—* <i>ETLJob</i>; every write produces "
  "exactly one <i>AuditLog</i> entry.")

# include the existing domain_model.png
dom_img = os.path.join(ASSETS, "domain_model.png")
if os.path.exists(dom_img):
    P("<i>Figure 4.1 — Domain Model (rendered)</i>", H3)
    try:
        img = Image(dom_img, width=6.5*inch, height=4.3*inch, kind='proportional')
        story.append(img)
    except Exception as e:
        P(f"[domain_model.png could not be embedded: {e}]")

story.append(PageBreak())

# ---------------- 5. SSD ----------------
P("5. System Sequence Diagram", H1)
P("The SSD below describes the black-box interaction between the <b>Property Analyst</b> actor and the "
  "<b>LOCUS Analytics System</b> for the central use case <b>UC-1: Estimate Fair Market Value</b>. Only "
  "system-boundary events are shown.")

ssd = [
    ["Step", "Actor → System", "System → Actor"],
    ["1", "login(email, password)", ""],
    ["2", "", "sessionToken, role"],
    ["3", "openFmvScreen()", ""],
    ["4", "", "screen ready"],
    ["5", "submitFmvRequest(city, locality, area, beds, baths, type, amenities)", ""],
    ["6", "", "fmvAmount, confidenceBand, valuationId"],
    ["7", "requestPdfReport(valuationId)", ""],
    ["8", "", "reportFilePath"],
    ["9", "logout()", ""],
    ["10", "", "ack"],
]
t = Table(ssd, colWidths=[0.55*inch, 3.1*inch, 2.65*inch])
t.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#0B3D91')),
    ('TEXTCOLOR', (0,0), (-1,0), colors.white),
    ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
    ('FONTSIZE', (0,0), (-1,-1), 9),
    ('GRID', (0,0), (-1,-1), 0.4, colors.grey),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor('#F4F6FA')]),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
]))
story.append(t)
story.append(Spacer(1, 8))
P("Notation: each numbered row is one message crossing the system boundary. Steps 1–2, 5–6, 7–8, and 9–10 "
  "are synchronous request/response pairs; the system performs internal work (model lookup, DB write, "
  "PDF rendering) entirely as a black box.")

story.append(PageBreak())

# ---------------- 6. SEQUENCE DIAGRAM ----------------
P("6. Sequence Diagram", H1)
P("The white-box sequence diagram below expands UC-1 (Estimate FMV) into the actual collaboration between "
  "the JavaFX controller, service, predictor, and DAO classes.")

seq = [
    ["#", "Caller", "Callee", "Message"],
    ["1", "FMVEstimateController", "InputValidator", "validate(form)"],
    ["2", "InputValidator", "FMVEstimateController", "ok / ValidationException"],
    ["3", "FMVEstimateController", "ValuationServiceImpl", "estimateFMV(featureDto)"],
    ["4", "ValuationServiceImpl", "LinearRegressionPredictor", "predict(features)"],
    ["5", "LinearRegressionPredictor", "ValuationServiceImpl", "fmv (PKR)"],
    ["6", "ValuationServiceImpl", "ValuationDAOImpl", "save(Valuation)"],
    ["7", "ValuationDAOImpl", "DBConnection", "getConnection()"],
    ["8", "ValuationDAOImpl", "PostgreSQL", "INSERT INTO valuation ..."],
    ["9", "PostgreSQL", "ValuationDAOImpl", "valuationId"],
    ["10", "ValuationDAOImpl", "AuditLogDAOImpl", "log(actor, 'CREATE_VALUATION', valuationId)"],
    ["11", "ValuationServiceImpl", "FMVEstimateController", "ValuationResult{fmv, band, id}"],
    ["12", "FMVEstimateController", "JavaFX Scene", "render(result)"],
]
t = Table(seq, colWidths=[0.4*inch, 1.7*inch, 1.7*inch, 2.5*inch])
t.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#0B3D91')),
    ('TEXTCOLOR', (0,0), (-1,0), colors.white),
    ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
    ('FONTSIZE', (0,0), (-1,-1), 8.5),
    ('GRID', (0,0), (-1,-1), 0.4, colors.grey),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor('#F4F6FA')]),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
]))
story.append(t)
story.append(Spacer(1, 8))
P("The model strictly traverses UI → Service → DAO → DB. Audit logging is invoked synchronously after each "
  "write, ensuring every mutation is recorded before the controller returns.")

story.append(PageBreak())

# ---------------- 7. CLASS DIAGRAM ----------------
P("7. Class Diagram", H1)
P("The class diagram of LOCUS Analytics is organised around four logical groups: <b>model</b>, <b>dao</b>, "
  "<b>service</b>, and <b>ui.controller</b>. The table below summarises the principal classes; "
  "<i>«interface»</i> stereotypes are noted where applicable.")

cls = [
    ["Layer", "Class / Interface", "Key Members"],
    ["model", "User, PropertyAnalyst, SystemAdministrator", "userId, name, email, role; role-specific behaviour"],
    ["model", "Property", "propertyId, city, locality, area, beds, baths, price, type"],
    ["model", "Valuation", "valuationId, propertyId, fmv, confidence, generatedAt"],
    ["model", "ValuationReport", "reportId, valuationId, filePath, sha256"],
    ["model", "RentalAnalysis, ROIAnalysis", "yield, growth, ROI inputs/outputs"],
    ["model", "InvestmentCluster", "clusterId, centroid, members[]"],
    ["model", "ETLJob, AuditLog, SystemConfiguration", "operational metadata"],
    ["model.dto", "SearchFilter, PagedResult, TrendPoint, HeatmapPoint, ClusterParams, ROIInput, "
                  "TimeRange, LocalityMetric, ComparisonResult, TrendStatistics", "transport DTOs"],
    ["dao «interface»", "PropertyDAO, ValuationDAO, ValuationReportDAO, RentalAnalysisDAO, "
                        "ROIAnalysisDAO, InvestmentClusterDAO, ETLJobDAO, AuditLogDAO, UserDAO, "
                        "SystemConfigurationDAO", "CRUD + query methods"],
    ["dao.impl", "*DAOImpl (10 classes)", "JDBC implementations using HikariCP"],
    ["config", "DBConnection", "HikariCP DataSource, getConnection()"],
    ["ml", "LinearRegressionPredictor", "loadModel(path), predict(features), hot-reload watcher"],
    ["service «interface»", "AuthenticationService, ValuationService, RentalYieldService, ROIService, "
                            "SearchService, CompareService, PriceTrendService, HeatmapService, "
                            "InvestmentClusterService, ListingManagementService, ETLService, "
                            "ConfigurationService, ValuationReportService, ReportPdfService", "domain ops"],
    ["service.impl", "*ServiceImpl + *ServiceStub (parallel hierarchy)", "real impls and in-memory stubs"],
    ["service.validation", "InputValidator", "form-level validation + ValidationException"],
    ["ui", "LocusAnalyticsApp, Main, SceneManager, ServiceRegistry, BrandAssets", "JavaFX bootstrap"],
    ["ui.controller", "LoginController, MainController, ServiceAwareController, UiNavigationBridge",
                      "shell/navigation"],
    ["ui.controller.screen", "AbstractScreenController + 14 screen controllers (FMV, RentalYield, ROI, "
                              "Search, Compare, PriceTrend, Heatmap, Report, Cluster, ETL, Listings, "
                              "Config, Intro)", "one controller per FXML"],
]
t = Table(cls, colWidths=[1.3*inch, 2.5*inch, 2.5*inch])
t.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#0B3D91')),
    ('TEXTCOLOR', (0,0), (-1,0), colors.white),
    ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
    ('FONTSIZE', (0,0), (-1,-1), 8.2),
    ('GRID', (0,0), (-1,-1), 0.4, colors.grey),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor('#F4F6FA')]),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
]))
story.append(t)
story.append(Spacer(1, 8))
P("<b>Key relationships:</b> "
  "(i) every <i>*ServiceImpl</i> depends on one or more <i>*DAO</i> interfaces; "
  "(ii) every <i>*DAOImpl</i> depends on <i>DBConnection</i>; "
  "(iii) every screen controller extends <i>AbstractScreenController</i> and is wired through "
  "<i>ServiceRegistry</i>; "
  "(iv) <i>PropertyAnalyst</i> and <i>SystemAdministrator</i> generalise <i>User</i>; "
  "(v) <i>ValuationServiceImpl</i> aggregates <i>LinearRegressionPredictor</i>; "
  "(vi) <i>ValuationReportServiceImpl</i> composes <i>ReportPdfServiceImpl</i>.")

story.append(PageBreak())

# ---------------- 8. PACKAGE DIAGRAM ----------------
P("8. Package Diagram", H1)
P("The Java source is organised under the root package <b>com.locus</b> and follows a strict downward "
  "dependency rule: an upper layer may only depend on the layers below it.")

pkg = [
    ["Package", "Responsibility", "Depends On"],
    ["com.locus.ui", "JavaFX bootstrap, scene management, brand assets.", "ui.controller, service"],
    ["com.locus.ui.controller", "Shell controllers (Login, Main, Navigation).", "ui.controller.screen, service"],
    ["com.locus.ui.controller.screen", "One controller per FXML screen (14 screens).", "service, model.dto"],
    ["com.locus.service", "Service interfaces — the domain API.", "model, model.dto"],
    ["com.locus.service.impl", "Production + stub implementations.", "service, dao, ml, exception"],
    ["com.locus.service.validation", "Input validators.", "model.dto, exception"],
    ["com.locus.ml", "LinearRegressionPredictor (consumes ml/model.json).", "(stdlib only)"],
    ["com.locus.dao", "DAO interfaces.", "model, model.dto, exception"],
    ["com.locus.dao.impl", "JDBC implementations using HikariCP.", "dao, config"],
    ["com.locus.config", "DBConnection (HikariCP DataSource).", "(stdlib + HikariCP)"],
    ["com.locus.model (+ .dto)", "POJOs and DTOs.", "(none)"],
    ["com.locus.exception", "ValidationException, DataAccessException.", "(none)"],
]
t = Table(pkg, colWidths=[2.0*inch, 2.7*inch, 1.7*inch])
t.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#0B3D91')),
    ('TEXTCOLOR', (0,0), (-1,0), colors.white),
    ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
    ('FONTSIZE', (0,0), (-1,-1), 8.5),
    ('GRID', (0,0), (-1,-1), 0.4, colors.grey),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor('#F4F6FA')]),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
]))
story.append(t)
story.append(Spacer(1, 8))
P("External Python packages — <b>etl/</b> (run_etl.py, zameen_scraper.py) and <b>ml/</b> "
  "(train_model.py, retrain_model.py) — sit outside the Java module graph and communicate solely "
  "through the PostgreSQL database and the <i>ml/model.json</i> artefact. The <b>data/</b> package "
  "(generate_seed.py) is invoked once at setup.")

story.append(PageBreak())

# ---------------- 9. DEPLOYMENT ----------------
P("9. Deployment Diagram", H1)
P("LOCUS Analytics is deployed on a single workstation (the analyst's machine) plus a PostgreSQL "
  "database that may be local or networked. The deployment view is summarised below; a detailed PNG "
  "rendering of the schema (foreign keys and indexes) is referenced from the assets folder.")

dep = [
    ["Node", "Artefact", "Notes"],
    ["Analyst Workstation (Win/Linux/macOS)",
     "locus-analytics-1.0-SNAPSHOT.jar (fat JAR)",
     "JavaFX 21 client, JDK 17 runtime"],
    ["Analyst Workstation",
     "Python 3.10 + run_etl.py / train_model.py / retrain_model.py",
     "invoked by ETLServiceImpl as subprocess"],
    ["Analyst Workstation (filesystem)",
     "ml/model.json, generated-reports/, backups/, config.properties",
     "hot-reloaded model; PDF outputs; weekly DB backups"],
    ["Database Server",
     "PostgreSQL 15+ (database: locus_analytics)",
     "schema.sql provisions tables, indexes, audit triggers"],
    ["External Service",
     "Google Maps JavaScript API",
     "consumed by the Heatmap WebView only"],
]
t = Table(dep, colWidths=[2.0*inch, 2.3*inch, 2.2*inch])
t.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#0B3D91')),
    ('TEXTCOLOR', (0,0), (-1,0), colors.white),
    ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
    ('FONTSIZE', (0,0), (-1,-1), 8.5),
    ('GRID', (0,0), (-1,-1), 0.4, colors.grey),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor('#F4F6FA')]),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
]))
story.append(t)
story.append(Spacer(1, 10))
P("<b>Communication links:</b> "
  "(1) JavaFX client ↔ PostgreSQL via JDBC (TCP/5432); "
  "(2) JavaFX client ↔ Python ETL/ML via OS process spawn + the shared <i>etl_job</i> table; "
  "(3) JavaFX WebView ↔ Google Maps over HTTPS; "
  "(4) Backup scripts ↔ filesystem (<i>backups/</i>) on a weekly cron/Task Scheduler trigger.")

P("<b>Build &amp; release:</b> a single <i>mvn clean package</i> produces the shaded JAR; deployment is a "
  "copy-and-run, with <i>config.properties</i> as the only host-specific file. Database upgrades are "
  "applied via versioned SQL scripts (<i>schema.sql</i> and <i>fix_bad_data.sql</i>).")

# ----------- BUILD -----------
doc.build(story)
print(f"[OK] PDF written to {OUT}")
