ALTER TABLE app_edibles
ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC',
ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE app_edibles
ALTER COLUMN updated_at DROP NOT NULL;