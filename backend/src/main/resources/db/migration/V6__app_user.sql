-- =====================================================================
-- V6 — Application users (authentication)
-- =====================================================================
-- A minimal users table used by Spring Security. Passwords are stored as
-- bcrypt hashes (60 chars). One default admin user is seeded at startup
-- from the environment variables ADMIN_USERNAME / ADMIN_PASSWORD (see
-- AdminUserSeeder). No password is ever committed to git.
-- =====================================================================

CREATE TABLE app_user (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(60)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'ADMIN',
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Username lookups are case-insensitive at the application level; keep the
-- index on the raw column to enforce uniqueness, plus a functional index for
-- the lookup path.
CREATE INDEX ix_app_user_lower_username ON app_user (LOWER(username));

