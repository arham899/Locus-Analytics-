#!/usr/bin/env bash
# LOCUS Analytics — PostgreSQL Backup Script (Linux / macOS)
# Author: Fasih Ul Mubashir (24i-0517)
#
# Creates a timestamped pg_dump and keeps only the last 4 backups.
# Schedule with cron: 0 2 * * 0  /path/to/scripts/backup.sh

set -euo pipefail

DB_NAME="locus_analytics"
DB_USER="postgres"
BACKUP_DIR="$(cd "$(dirname "$0")/.." && pwd)/backups"
KEEP=4

mkdir -p "$BACKUP_DIR"

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
OUTFILE="$BACKUP_DIR/${DB_NAME}_${TIMESTAMP}.sql.gz"

echo "[BACKUP] Dumping $DB_NAME → $OUTFILE"
PGPASSWORD="${PGPASSWORD:-password}" pg_dump \
    -U "$DB_USER" \
    -h localhost \
    -d "$DB_NAME" \
    --no-owner \
    --no-privileges \
    | gzip > "$OUTFILE"

echo "[BACKUP] Done. $(du -sh "$OUTFILE" | cut -f1)"

# Rotation: keep only the $KEEP most recent files
EXISTING=$(ls -1t "$BACKUP_DIR"/${DB_NAME}_*.sql.gz 2>/dev/null | wc -l)
if [ "$EXISTING" -gt "$KEEP" ]; then
    ls -1t "$BACKUP_DIR"/${DB_NAME}_*.sql.gz \
        | tail -n +"$((KEEP + 1))" \
        | xargs rm -f
    echo "[BACKUP] Rotation: kept last $KEEP backups, removed $((EXISTING - KEEP)) old files."
fi

echo "[BACKUP] Backup list:"
ls -lh "$BACKUP_DIR"/${DB_NAME}_*.sql.gz 2>/dev/null || echo "  (none)"
