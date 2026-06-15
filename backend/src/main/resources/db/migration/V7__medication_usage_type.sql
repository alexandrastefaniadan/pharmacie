-- =====================================================================
-- V7 — Distinguish human medications from veterinary ones
-- =====================================================================
-- A single enum-backed column on `medication`. Existing rows are kept
-- as HUMAN via the DEFAULT — no data migration is needed.
--
-- The active-name uniqueness is widened to include usage_type so the
-- same commercial name can exist both as a human product and a vet
-- product (different SKUs, but pharmacies do see this in practice).
-- =====================================================================

ALTER TABLE medication
    ADD COLUMN usage_type VARCHAR(20) NOT NULL DEFAULT 'HUMAN'
        CHECK (usage_type IN ('HUMAN', 'VETERINARY'));

-- Helpful for the very common "filter by usage type" path.
CREATE INDEX ix_medication_usage_type
    ON medication (usage_type) WHERE deleted_at IS NULL;

-- Replace the old "one name per active medication" index with a
-- per-usage-type one. Two products with the same name are still
-- forbidden inside HUMAN, or inside VETERINARY — but the same name
-- can now exist once in each bucket.
DROP INDEX IF EXISTS ux_medication_name_active;
CREATE UNIQUE INDEX ux_medication_name_active
    ON medication (lower(name), usage_type)
    WHERE deleted_at IS NULL;

