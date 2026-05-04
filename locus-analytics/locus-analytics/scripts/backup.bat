@echo off
REM LOCUS Analytics — PostgreSQL Backup Script (Windows)
REM Author: Fasih Ul Mubashir (24i-0517)
REM
REM Creates a timestamped pg_dump and keeps only the last 4 backups.
REM Schedule with Task Scheduler to run weekly.

setlocal enabledelayedexpansion

set DB_NAME=locus_analytics
set DB_USER=postgres
set DB_HOST=localhost
set PGPASSWORD=password
set KEEP=4

REM Resolve backups folder relative to this script
set SCRIPT_DIR=%~dp0
set BACKUP_DIR=%SCRIPT_DIR%..\backups

if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

REM Build timestamp: YYYYMMDD_HHMMSS
for /f "tokens=1-3 delims=/" %%a in ("%DATE%") do (
    set MM=%%a
    set DD=%%b
    set YYYY=%%c
)
for /f "tokens=1-3 delims=:." %%a in ("%TIME: =0%") do (
    set HH=%%a
    set MIN=%%b
    set SEC=%%c
)
set TIMESTAMP=%YYYY%%MM%%DD%_%HH%%MIN%%SEC%
set OUTFILE=%BACKUP_DIR%\%DB_NAME%_%TIMESTAMP%.sql

echo [BACKUP] Dumping %DB_NAME% to %OUTFILE%
pg_dump -U %DB_USER% -h %DB_HOST% -d %DB_NAME% --no-owner --no-privileges -f "%OUTFILE%"

if %ERRORLEVEL% neq 0 (
    echo [ERROR] pg_dump failed.
    exit /b 1
)

echo [BACKUP] Done.

REM Rotation: delete oldest files beyond KEEP count
set COUNT=0
for /f %%f in ('dir /b /o-d "%BACKUP_DIR%\%DB_NAME%_*.sql" 2^>nul') do (
    set /a COUNT+=1
    if !COUNT! gtr %KEEP% (
        echo [BACKUP] Removing old backup: %%f
        del "%BACKUP_DIR%\%%f"
    )
)

echo [BACKUP] Kept last %KEEP% backups.
endlocal
