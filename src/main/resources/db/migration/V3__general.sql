-- Update enums
ALTER TYPE nutrient_unit RENAME TO serving_unit;
ALTER TYPE user_pending_food_status RENAME TO app_food_pending_status;

-- Set proper type for nutrients name
ALTER TABLE nutrients ALTER COLUMN name TYPE VARCHAR(50);
ALTER TABLE nutrients DROP CONSTRAINT nutrients_name_check;
ALTER TABLE nutrients ADD CONSTRAINT nutrients_name_check CHECK (length(name) > 0);

-- Set proper type for nutrients symbol
ALTER TABLE nutrients ALTER COLUMN symbol TYPE VARCHAR(4);
ALTER TABLE nutrients DROP CONSTRAINT nutrients_symbol_check;