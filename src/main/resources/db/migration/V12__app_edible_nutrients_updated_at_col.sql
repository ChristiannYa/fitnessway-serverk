ALTER TABLE app_edible_nutrients
    ADD COLUMN created_at TIMESTAMPTZ,
    ADD COLUMN updated_at TIMESTAMPTZ;

UPDATE app_edible_nutrients
SET created_at = '2026-06-08 00:00:00+00';