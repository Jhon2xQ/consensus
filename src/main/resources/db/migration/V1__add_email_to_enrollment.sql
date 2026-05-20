-- ============================================================
-- V1: Add email to enrollment — Two-phase enrollment support
-- ============================================================
-- Two-phase enrollment flow:
--   Phase 1 — Creator registers voter email (userId=null, commitment=null)
--   Phase 2 — User claims enrollment by JWT email match
--
-- This migration:
--   1. Adds the email column (NOT NULL after backfill)
--   2. Makes userId and commitment nullable (for phase-1 enrollments)
--   3. Replaces UNIQUE(processId, userId) with UNIQUE(processId, email)
--   4. Keeps UNIQUE(processId, commitment) — unchanged
--
-- For development environments, ddl-auto: update handles the column
-- addition. Run this script manually when ddl-auto is set to 'none'
-- or 'validate' (e.g., staging/production).
-- ============================================================

-- Step 1: Add email column (nullable initially)
ALTER TABLE enrollments
    ADD COLUMN IF NOT EXISTS email VARCHAR(255);

-- Step 2: Backfill existing rows with a placeholder email
-- In development, this ensures all existing rows have a unique email.
-- In production starting fresh, this is a no-op (no rows exist).
UPDATE enrollments
SET email = 'migrated-' || id || '@consensus.local'
WHERE email IS NULL;

-- Step 3: Make email NOT NULL after backfill
ALTER TABLE enrollments
    ALTER COLUMN email SET NOT NULL;

-- Step 4: Make userId nullable (was NOT NULL, now accepts null for phase-1)
ALTER TABLE enrollments
    ALTER COLUMN user_id DROP NOT NULL;

-- Step 5: Make commitment nullable (was NOT NULL, now accepts null for phase-1)
ALTER TABLE enrollments
    ALTER COLUMN commitment DROP NOT NULL;

-- Step 6: Drop the old UNIQUE(process_id, user_id) constraint
-- PostgreSQL auto-names: {tablename}_{columnname}_key
-- Hibernate naming: UK_xxxxx (check your DB for exact name)
-- Uncomment and adjust the constraint name for your environment:
-- ALTER TABLE enrollments DROP CONSTRAINT IF EXISTS enrollments_electoral_process_id_user_id_key;

-- Step 7: Add the new UNIQUE(process_id, email) constraint
-- This is automatically created by Hibernate ddl-auto: update.
-- For manual environments, uncomment:
-- ALTER TABLE enrollments ADD CONSTRAINT enrollments_electoral_process_id_email_key UNIQUE (electoral_process_id, email);

-- Note: UNIQUE(process_id, commitment) is preserved — no change needed.
