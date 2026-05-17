-- =====================================================================
-- V3 — Medication ↔ AgeGroup becomes many-to-many
-- =====================================================================
-- Business reason: a single medication (e.g. a syrup) can target both
-- children and adults. The previous single-FK design forced one or the
-- other. We also drop two age groups that are not useful in practice for
-- this pharmacy (ADOLESCENT, ELDERLY).
-- =====================================================================

-- 1) New join table
CREATE TABLE medication_age_group (
    medication_id UUID    NOT NULL REFERENCES medication(id) ON DELETE CASCADE,
    age_group_id  INTEGER NOT NULL REFERENCES age_group(id),
    PRIMARY KEY (medication_id, age_group_id)
);
CREATE INDEX ix_med_ag_age_group ON medication_age_group (age_group_id);

-- 2) Preserve existing data: every row that had a single age_group_id
--    becomes one row in the new join table.
INSERT INTO medication_age_group (medication_id, age_group_id)
SELECT id, age_group_id
FROM medication
WHERE age_group_id IS NOT NULL;

-- 3) Drop the old column + its supporting index
DROP INDEX IF EXISTS ix_medication_age_group;
ALTER TABLE medication DROP COLUMN age_group_id;

-- 4) Remove unused age groups. Any join rows that reference them are
--    removed first to avoid an FK violation.
DELETE FROM medication_age_group
WHERE age_group_id IN (
    SELECT id FROM age_group WHERE code IN ('ADOLESCENT', 'ELDERLY')
);
DELETE FROM age_group WHERE code IN ('ADOLESCENT', 'ELDERLY');

