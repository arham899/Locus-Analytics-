## Setup Instructions

1. Install PostgreSQL
2. Create DB:
   CREATE DATABASE locus_analytics;

3. Run schema.sql

4. Copy:
   src/main/resources/config.properties.template
   → config.properties

5. Fill DB credentials

6. Run:
   TestConnection.java

Expected Output:
DB Connected Successfully!