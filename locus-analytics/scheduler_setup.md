
# ETL Scheduler Setup (Windows Task Scheduler)

## Steps:

1. Press **Windows Key** → search **Task Scheduler** → open it

2. Click **Create Basic Task**

3. Name it:

   ```
   LOCUS Analytics ETL Pipeline
   ```

4. Choose trigger:

   * Select **Daily** (or Weekly)
   * Set your preferred time

5. Action:

   * Choose **Start a Program**

6. Fill in:

   * **Program/script:**

     ```
     python
     ```
   * **Add arguments:**

     ```
     C:\path\to\etl_pipeline\etl_main.py
     ```
   * **Start in:**

     ```
     C:\path\to\etl_pipeline
     ```

7. Click **Finish**
