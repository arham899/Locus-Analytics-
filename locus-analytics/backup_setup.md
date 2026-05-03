
# Database Backup Automation (Windows)

## 1. Create Backup Folder

Create this folder on your Desktop:

```
locus_backups
```

---

## 2. Create Backup Script

Open Notepad and paste the following:

```bat
@echo off

set PGPASSWORD=password

set BACKUP_DIR=C:\Users\YOUR_USERNAME\Desktop\locus_backups

set FILE_NAME=locus_backup_%date:~-4,4%%date:~-10,2%%date:~-7,2%.sql

"C:\Program Files\PostgreSQL\15\bin\pg_dump.exe" -U postgres -h localhost -d locus_analytics -F c -f "%BACKUP_DIR%\%FILE_NAME%"

for /f "skip=4 eol=: delims=" %%F in ('dir /b /o-d /a-d "%BACKUP_DIR%\*.sql"') do del "%BACKUP_DIR%\%%F"
```

Save it as:

```
db_backup.bat
```

(Select **Save as type: All Files**)

---

## 3. Schedule the Backup

1. Open **Task Scheduler**
2. Click **Create Basic Task**
3. Name:

```
LOCUS DB Backup
```

4. Trigger:

* Select **Weekly**

5. Action:

* Select **Start a Program**

6. Program/script:

```
C:\Users\YOUR_USERNAME\Desktop\db_backup.bat
```

7. Click **Finish**
