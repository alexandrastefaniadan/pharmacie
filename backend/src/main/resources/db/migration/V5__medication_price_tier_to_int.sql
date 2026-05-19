-- =====================================================================
-- V5 — Widen medication.price_tier from SMALLINT to INTEGER
-- =====================================================================
-- The JPA entity exposes priceTier as `int`, which Hibernate validates
-- against INTEGER (not SMALLINT). Widening the column avoids a custom
-- @Column(columnDefinition = ...) hack and keeps the entity portable.
-- The CHECK constraint (0..5) is preserved.
-- =====================================================================

ALTER TABLE medication
    ALTER COLUMN price_tier TYPE INTEGER USING price_tier::integer;

