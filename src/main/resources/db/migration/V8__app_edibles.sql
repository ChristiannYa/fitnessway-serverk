-- ------------
-- af/ae UPDATE
-- ------------
ALTER TABLE app_foods
    RENAME TO app_edibles;

ALTER TABLE app_edibles
    ADD COLUMN edible_type edible_type;

UPDATE app_edibles
    SET edible_type = 'food';

ALTER TABLE app_edibles
    ALTER COLUMN edible_type SET NOT NULL;

-- SEQUENCES
ALTER SEQUENCE app_foods_id_seq
    RENAME TO app_edibles_id_seq;

-- INDEXES
ALTER INDEX app_foods_pkey
      RENAME TO app_edibles_pkey;

ALTER INDEX app_foods_name_brand_amount_per_serving_serving_unit_key
      RENAME TO app_edibles_name_brand_amount_per_serving_serving_unit_key;

ALTER INDEX idx_app_foods_brand_lower
      RENAME TO idx_app_edibles_brand_lower;

ALTER INDEX idx_app_foods_created_by
      RENAME TO idx_app_edibles_created_by;

ALTER INDEX idx_app_foods_name_lower
        RENAME TO idx_app_edibles_name_lower;

ALTER INDEX idx_app_foods_name_trgm
      RENAME TO idx_app_edibles_name_trgm;

-- CHECK CONSTRAINTS
ALTER TABLE app_edibles
    RENAME CONSTRAINT app_foods_amount_per_serving_check
    TO app_edibles_amount_per_serving_check;

-- FK CONSTRAINTS
ALTER TABLE app_edibles
    RENAME CONSTRAINT app_foods_created_by_fkey
    TO app_edibles_created_by_fkey;

ALTER TABLE user_favorite_app_foods
    RENAME CONSTRAINT user_favorite_app_foods_app_food_id_fkey
    TO user_favorite_app_edibles_app_edible_id_fkey;

-- --------------
-- afn/aen UPDATE
-- --------------
ALTER TABLE app_food_nutrients
    RENAME TO app_edible_nutrients;

ALTER TABLE app_edible_nutrients
    RENAME COLUMN app_food_id TO app_edible_id;

-- INDEXES
ALTER INDEX app_food_nutrients_pkey
      RENAME TO app_edible_nutrients_pkey;

ALTER INDEX idx_app_food_nutrients_food_id
      RENAME TO idx_app_edible_nutrients_edible_id;

-- CHECK CONSTRAINTS
ALTER TABLE app_edible_nutrients
    RENAME CONSTRAINT app_food_nutrients_amount_check
    TO app_edible_nutrients_amount_check;

-- FK CONSTRAINTS
ALTER TABLE app_edible_nutrients
    RENAME CONSTRAINT app_food_nutrients_app_food_id_fkey
    TO app_edible_nutrients_app_edible_id_fkey;

ALTER TABLE app_edible_nutrients
    RENAME CONSTRAINT app_food_nutrients_nutrient_id_fkey
    TO app_edible_nutrients_nutrient_id_fkey;

