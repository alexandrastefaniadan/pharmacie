-- =====================================================================
-- V8 — Treatments (recipes of medications suggested for a condition)
-- =====================================================================
-- A treatment is a named bundle of medications that the pharmacist can
-- suggest to a client. Each treatment is strictly either HUMAN or
-- VETERINARY; the medications inside must share the same usage type
-- (enforced at the service layer).
--
-- Soft-delete + optimistic-locking + audit columns match the medication
-- table — same conventions everywhere.
-- =====================================================================

CREATE TABLE treatment (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),

    name         VARCHAR(255) NOT NULL,
    description  TEXT,                       -- patient-facing explanation
    notes        TEXT,                       -- internal pharmacist notes
    usage_type   VARCHAR(20)  NOT NULL
        CHECK (usage_type IN ('HUMAN', 'VETERINARY')),

    -- audit
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(80),
    updated_by   VARCHAR(80),
    deleted_at   TIMESTAMPTZ,                -- soft delete
    version      BIGINT       NOT NULL DEFAULT 0
);

-- Active-name uniqueness scoped per usage type: a treatment named
-- "Rhume" can exist in HUMAN and another in VETERINARY independently.
CREATE UNIQUE INDEX ux_treatment_name_active
    ON treatment (lower(name), usage_type)
    WHERE deleted_at IS NULL;

CREATE INDEX ix_treatment_usage_type
    ON treatment (usage_type)
    WHERE deleted_at IS NULL;

-- Trigram index for the same fuzzy / contains free-text search we use
-- on medications.
CREATE INDEX ix_treatment_name_trgm
    ON treatment USING gin (name gin_trgm_ops);


-- ---------------------------------------------------------------------
-- Many-to-many: treatment ↔ medication
-- ---------------------------------------------------------------------
-- `position` preserves the order the pharmacist entered the meds in
-- (1st-line, 2nd-line, accompanying). ON DELETE RESTRICT prevents
-- losing a medication from a treatment if it gets hard-deleted.
CREATE TABLE treatment_medication (
    treatment_id  UUID    NOT NULL REFERENCES treatment(id)  ON DELETE CASCADE,
    medication_id UUID    NOT NULL REFERENCES medication(id) ON DELETE RESTRICT,
    position      INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (treatment_id, medication_id)
);
CREATE INDEX ix_treatment_medication_med ON treatment_medication (medication_id);


-- ---------------------------------------------------------------------
-- Many-to-many: treatment ↔ indication (optional)
-- ---------------------------------------------------------------------
-- Lets us later implement "search by symptom → suggested treatments"
-- without a schema change.
CREATE TABLE treatment_indication (
    treatment_id  UUID    NOT NULL REFERENCES treatment(id) ON DELETE CASCADE,
    indication_id INTEGER NOT NULL REFERENCES indication(id),
    PRIMARY KEY (treatment_id, indication_id)
);
CREATE INDEX ix_treatment_indication_ind ON treatment_indication (indication_id);

