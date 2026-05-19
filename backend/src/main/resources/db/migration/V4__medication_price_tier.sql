-- =====================================================================
-- V4 — Add a manual "price tier" indicator to medications
-- =====================================================================
-- A simple 0..5 scale used purely for visual ranking in the UI (dollar
-- icons next to the name). 0 means "not rated" and is the default; the
-- pharmacist sets the value manually when creating or editing a med.
-- We never store the actual price.
-- =====================================================================

ALTER TABLE medication
    ADD COLUMN price_tier SMALLINT NOT NULL DEFAULT 0
        CHECK (price_tier BETWEEN 0 AND 5);

-- Helps the default "most expensive first" sort.
CREATE INDEX ix_medication_price_tier
    ON medication (price_tier DESC)
    WHERE deleted_at IS NULL;

