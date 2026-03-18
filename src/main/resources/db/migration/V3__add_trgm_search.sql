CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_app_foods_name_trgm ON app_foods USING GIN (LOWER(name) gin_trgm_ops);