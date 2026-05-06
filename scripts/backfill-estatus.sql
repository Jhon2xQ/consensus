-- Backfill script for making electoral_processes.estatus NOT NULL
--
-- This migration:
-- 1. Updates rows with NULL estatus to their computed correct state
-- 2. Alters the column to NOT NULL
--
-- Run this BEFORE deploying the code change (ddl-auto: update will
-- add the NOT NULL constraint, but won't fix existing NULL rows).
--
-- Usage:
--   psql -U <user> -d <database> -f scripts/backfill-estatus.sql

BEGIN;

-- Set estatus to 'NONE' for all rows that currently have NULL
UPDATE electoral_processes
SET estatus = 'NONE'
WHERE estatus IS NULL;

-- Add NOT NULL constraint (safe now that all rows have a value)
ALTER TABLE electoral_processes
ALTER COLUMN estatus SET NOT NULL;

COMMIT;
