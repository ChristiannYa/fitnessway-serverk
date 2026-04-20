-- ---
-- New
-- ---
-- Will be used for future app_foods-related schema as well in the future
CREATE TYPE edible_type AS ENUM('food', 'supplement');

-- ------------
-- uf/ue update
-- ------------
ALTER TABLE user_foods
    RENAME TO user_edibles;

ALTER TABLE user_edibles
    ADD COLUMN edible_type edible_type;

UPDATE user_edibles
    SET edible_type = 'food';

ALTER TABLE user_edibles
    ALTER COLUMN edible_type SET NOT NULL;

-- uf/ue sequences
ALTER SEQUENCE user_foods_id_seq
    RENAME TO user_edibles_id_seq;

-- uf/ue indexes
ALTER INDEX user_foods_pkey
    RENAME TO user_edibles_pkey;

ALTER INDEX idx_user_foods_created_at
    RENAME TO idx_user_edibles_created_at;

ALTER INDEX idx_user_foods_last_logged_at
    RENAME TO idx_user_edibles_last_logged_at;

-- uf/ue constraints
ALTER TABLE user_edibles
    RENAME CONSTRAINT user_foods_user_id_name_brand_amount_per_serving_serving_un_key TO user_edibles_user_id_name_brand_amount_per_serving_serving_un_key;

ALTER TABLE user_edibles
    RENAME CONSTRAINT user_foods_amount_per_serving_check TO user_edibles_amount_per_serving_check;

ALTER TABLE user_edibles
    RENAME CONSTRAINT user_foods_brand_check TO user_edibles_brand_check;

ALTER TABLE user_edibles
    RENAME CONSTRAINT user_foods_name_check TO user_edibles_name_check;

ALTER TABLE user_edibles
    RENAME CONSTRAINT user_foods_user_id_fkey TO user_edibles_user_id_fkey;

-- --------------
-- ufn/uen update
-- --------------
ALTER TABLE user_food_nutrients
    RENAME TO user_edible_nutrients;

ALTER TABLE user_edible_nutrients
    RENAME COLUMN user_food_id TO user_edible_id;

-- ufn/uen indexes
ALTER INDEX user_food_nutrients_user_food_id_nutrient_id_key
    RENAME TO user_edible_nutrients_user_edible_id_nutrient_id_key;

-- ufn/uen constraints
ALTER TABLE user_edible_nutrients
    RENAME CONSTRAINT user_food_nutrients_amount_check TO user_edible_nutrients_amount_check;

ALTER TABLE user_edible_nutrients
    RENAME CONSTRAINT user_food_nutrients_nutrient_id_fkey TO user_edible_nutrients_nutrient_id_fkey;

ALTER TABLE user_edible_nutrients
    RENAME CONSTRAINT user_food_nutrients_user_food_id_fkey TO user_edible_nutrients_user_edible_id_fkey;

-- --------------
-- ufs/ues update
-- --------------
ALTER TABLE user_food_snapshots
    RENAME TO user_edible_snapshots;

ALTER TABLE user_edible_snapshots
    RENAME COLUMN original_food_id TO original_edible_id;

ALTER TABLE user_edible_snapshots
    RENAME COLUMN food_status TO snapshot_status;

ALTER TYPE food_status
    RENAME TO user_edible_snapshot_status;

-- ufs/ues sequences
ALTER SEQUENCE user_food_snapshots_id_seq
    RENAME TO user_edible_snapshots_id_seq;

-- ufs/ues indexes
ALTER INDEX user_food_snapshots_pkey
    RENAME TO user_edible_snapshots_pkey;

-- ufs/ues constraints
ALTER TABLE user_edible_snapshots
    RENAME CONSTRAINT user_food_snapshots_user_id_fkey TO user_edible_snapshots_user_id_fkey;

-- --------------
-- ufl/uel update
-- --------------
ALTER TABLE user_food_logs
    RENAME TO user_edible_logs;

ALTER TABLE user_edible_logs
    RENAME COLUMN food_id TO edible_id;

ALTER TABLE user_edible_logs
    RENAME COLUMN food_snapshot_id TO edible_snapshot_id;

ALTER TYPE food_log_category
    RENAME TO log_category;

ALTER TYPE food_source
    RENAME TO log_source;

-- ufl/uel sequences
ALTER SEQUENCE user_food_logs_id_seq
    RENAME TO user_edible_logs_id_seq;

-- ufl/uel indexes
ALTER INDEX user_food_logs_pkey
    RENAME TO user_edible_logs_pkey;

ALTER INDEX idx_user_food_logs_app_source_logged
    RENAME TO idx_user_edible_logs_app_source_logged;

ALTER INDEX idx_user_food_logs_user_source_logged
    RENAME TO idx_user_edible_logs_user_source_logged;

-- ufl/uel constraints
ALTER TABLE user_edible_logs
    RENAME CONSTRAINT user_food_logs_servings_check TO user_edible_logs_servings_check;

ALTER TABLE user_edible_logs
    RENAME CONSTRAINT user_food_logs_food_snapshot_id_fkey TO user_edible_logs_edible_snapshot_id_fkey;

ALTER TABLE user_edible_logs
    RENAME CONSTRAINT user_food_logs_user_id_fkey TO user_edible_logs_user_id_fkey;

-- ---------------
-- uni/uni update
-- ---------------
ALTER TABLE user_nutrient_intake
    RENAME COLUMN food_log_id TO edible_log_id;

-- uni indexes
ALTER INDEX idx_nutrient_intake_food_log
    RENAME TO idx_nutrient_intake_edible_log;

-- uni constraints
ALTER TABLE user_nutrient_intake
    RENAME CONSTRAINT user_nutrient_intake_food_log_id_fkey TO user_nutrient_intake_edible_log_id_fkey;