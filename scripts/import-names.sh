#!/usr/bin/env bash
#
# Bulk-import medication names into the Pharmacie DB.
#
# Reads one name per line from the file passed as argument (or names.txt by
# default) and inserts each name as a new medication, skipping any that
# already exist (case-insensitive match on the name column).
#
# Usage:
#   ./scripts/import-names.sh                       # uses ./names.txt
#   ./scripts/import-names.sh path/to/your-list.txt
#
# Assumes the Postgres container from docker-compose.yml is running.
# Override with env vars if your setup differs:
#   PGHOST=localhost PGPORT=5432 PGUSER=pharmacie PGPASSWORD=pharmacie PGDATABASE=pharmacie
#
set -euo pipefail

FILE="${1:-names.txt}"
if [[ ! -f "$FILE" ]]; then
  echo "❌ File not found: $FILE" >&2
  exit 1
fi

: "${PGHOST:=localhost}"
: "${PGPORT:=5432}"
: "${PGUSER:=pharmacie}"
: "${PGPASSWORD:=pharmacie}"
: "${PGDATABASE:=pharmacie}"
export PGHOST PGPORT PGUSER PGPASSWORD PGDATABASE

echo "→ Importing names from '$FILE' into ${PGUSER}@${PGHOST}:${PGPORT}/${PGDATABASE}…"

# We do the work in a single transaction with a temp staging table so the
# import is atomic and we get clean before/after counts.
psql -v ON_ERROR_STOP=1 <<SQL
BEGIN;

CREATE TEMP TABLE _import(name text) ON COMMIT DROP;

\copy _import(name) FROM '$FILE'

-- Clean up: trim, drop blanks, dedupe within the file itself.
DELETE FROM _import WHERE COALESCE(BTRIM(name), '') = '';
UPDATE _import SET name = BTRIM(name);

WITH inserted AS (
    INSERT INTO medication (name, data_source)
    SELECT DISTINCT name, 'IMPORT'
    FROM _import i
    WHERE NOT EXISTS (
        SELECT 1 FROM medication m
        WHERE LOWER(m.name) = LOWER(i.name)
          AND m.deleted_at IS NULL
    )
    RETURNING 1
)
SELECT
    (SELECT count(*) FROM _import)                       AS rows_in_file,
    (SELECT count(DISTINCT LOWER(name)) FROM _import)    AS distinct_names,
    (SELECT count(*) FROM inserted)                      AS newly_inserted;

COMMIT;
SQL

echo "✅ Done."

