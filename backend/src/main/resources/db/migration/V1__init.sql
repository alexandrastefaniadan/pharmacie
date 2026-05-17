-- =====================================================================
-- V1 — Initial schema
-- =====================================================================
-- Conventions:
--   * snake_case for tables and columns
--   * UUID primary keys for business entities
--   * SERIAL primary keys for lookup tables
--   * soft delete via deleted_at on business entities
--   * audit columns created_at / updated_at on every business table
--   * @Version (optimistic locking) via "version" column
-- =====================================================================

-- Required PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";   -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pg_trgm";    -- trigram fuzzy search on med names


-- ---------------------------------------------------------------------
-- Lookup tables (reference data, editable by ADMIN through the UI later)
-- ---------------------------------------------------------------------

-- Pharmaceutical form (forme galénique): tablet, syrup, cream, ovule, ...
CREATE TABLE pharmaceutical_form (
    id        SERIAL       PRIMARY KEY,
    code      VARCHAR(40)  NOT NULL UNIQUE,
    label_fr  VARCHAR(80)  NOT NULL,
    sort_order INTEGER     NOT NULL DEFAULT 100
);

-- Target population (nourrisson, enfant, adulte, ...)
CREATE TABLE age_group (
    id        SERIAL       PRIMARY KEY,
    code      VARCHAR(20)  NOT NULL UNIQUE,
    label_fr  VARCHAR(60)  NOT NULL,
    sort_order INTEGER     NOT NULL DEFAULT 100
);

-- Therapeutic class (antibiotique, antalgique, vitamine, ...)
CREATE TABLE therapeutic_class (
    id        SERIAL        PRIMARY KEY,
    code      VARCHAR(40)   NOT NULL UNIQUE,
    label_fr  VARCHAR(120)  NOT NULL,
    sort_order INTEGER      NOT NULL DEFAULT 100
);

-- Indication / usage (toux, fièvre, cicatrisation, ...)
CREATE TABLE indication (
    id        SERIAL        PRIMARY KEY,
    code      VARCHAR(60)   NOT NULL UNIQUE,
    label_fr  VARCHAR(120)  NOT NULL,
    sort_order INTEGER      NOT NULL DEFAULT 100
);


-- ---------------------------------------------------------------------
-- Core business entity: medication
-- ---------------------------------------------------------------------
CREATE TABLE medication (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),

    -- core info
    name            VARCHAR(255) NOT NULL,
    inn             VARCHAR(255),                 -- DCI / International Nonproprietary Name
    dosage          VARCHAR(80),                  -- e.g. "500 mg", "5 mg/ml"
    description     TEXT,
    is_parapharmacy BOOLEAN      NOT NULL DEFAULT FALSE,

    -- single-value relationships
    form_id         INTEGER      REFERENCES pharmaceutical_form(id),
    age_group_id    INTEGER      REFERENCES age_group(id),

    -- hooks for future imports / enrichment (DMP, Sobrus, barcode scanner)
    barcode         VARCHAR(20),
    external_cip    VARCHAR(20),
    data_source     VARCHAR(20)  NOT NULL DEFAULT 'MANUAL',  -- MANUAL | SOBRUS | DMP

    -- audit
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by      VARCHAR(80),
    updated_by      VARCHAR(80),
    deleted_at      TIMESTAMPTZ,                  -- soft delete
    version         BIGINT       NOT NULL DEFAULT 0
);

-- Uniqueness only on active rows (allows re-creating a name after a soft delete)
CREATE UNIQUE INDEX ux_medication_name_active
    ON medication (lower(name))
    WHERE deleted_at IS NULL;

-- Helpful indexes for listing / filtering
CREATE INDEX ix_medication_form          ON medication (form_id)       WHERE deleted_at IS NULL;
CREATE INDEX ix_medication_age_group     ON medication (age_group_id)  WHERE deleted_at IS NULL;
CREATE INDEX ix_medication_barcode       ON medication (barcode)       WHERE barcode IS NOT NULL;
CREATE INDEX ix_medication_external_cip  ON medication (external_cip)  WHERE external_cip IS NOT NULL;

-- Trigram index for fast fuzzy / "contains" search on name
CREATE INDEX ix_medication_name_trgm
    ON medication USING gin (name gin_trgm_ops);


-- ---------------------------------------------------------------------
-- Many-to-many: medication ↔ therapeutic_class
-- ---------------------------------------------------------------------
CREATE TABLE medication_therapeutic_class (
    medication_id        UUID    NOT NULL REFERENCES medication(id) ON DELETE CASCADE,
    therapeutic_class_id INTEGER NOT NULL REFERENCES therapeutic_class(id),
    PRIMARY KEY (medication_id, therapeutic_class_id)
);
CREATE INDEX ix_med_tc_class ON medication_therapeutic_class (therapeutic_class_id);


-- ---------------------------------------------------------------------
-- Many-to-many: medication ↔ indication
-- ---------------------------------------------------------------------
CREATE TABLE medication_indication (
    medication_id UUID    NOT NULL REFERENCES medication(id) ON DELETE CASCADE,
    indication_id INTEGER NOT NULL REFERENCES indication(id),
    PRIMARY KEY (medication_id, indication_id)
);
CREATE INDEX ix_med_ind_indication ON medication_indication (indication_id);

