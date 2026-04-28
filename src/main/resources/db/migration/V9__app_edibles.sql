-- --------------
-- upf/upe UPDATE
-- --------------
ALTER TYPE app_food_pending_status
    RENAME TO app_edible_pending_status;

ALTER TABLE user_pending_foods
    RENAME TO user_pending_edibles;

ALTER TABLE user_pending_edibles
    ADD COLUMN edible_type edible_type;

UPDATE user_pending_edibles
    SET edible_type = 'food';

ALTER TABLE user_pending_edibles
    ALTER COLUMN edible_type SET NOT NULL;

-- SEQUENCES
ALTER SEQUENCE user_pending_foods_id_seq
    RENAME TO user_pending_edibles_id_seq;

-- INDEXES
ALTER INDEX user_pending_foods_pkey
      RENAME TO user_pending_edibles_pkey;

ALTER INDEX idx_user_pending_foods_user_date
      RENAME TO idx_user_pending_edibles_user_date;

ALTER INDEX user_pending_foods_submitted_by_name_brand_amount_per_servi_key
      RENAME TO user_pending_edibles_submitted_by_name_brand_amount_per_servi_key;

-- CHECK CONSTRAINTS
ALTER TABLE user_pending_edibles
    RENAME CONSTRAINT user_pending_foods_amount_per_serving_check
    TO user_pending_edibles_amount_per_serving_check;

ALTER TABLE user_pending_edibles
    RENAME CONSTRAINT user_pending_foods_rejection_reason_check
    TO user_pending_edibles_rejection_reason_check;

-- FK CONSTRAINTS
ALTER TABLE user_pending_edibles
    RENAME CONSTRAINT user_pending_foods_reviewed_by_fkey
    TO user_pending_edibles_reviewed_by_fkey;

ALTER TABLE user_pending_edibles
    RENAME CONSTRAINT user_pending_foods_submitted_by_fkey
    TO user_pending_edibles_submitted_by_fkey;

-- ----------------
-- upfn/upen UPDATE
-- ----------------
ALTER TABLE user_pending_food_nutrients
    RENAME TO user_pending_edible_nutrients;

ALTER TABLE user_pending_edible_nutrients
    RENAME COLUMN pending_food_id TO pending_edible_id;

-- INDEXES
ALTER INDEX user_pending_food_nutrients_pending_food_id_nutrient_id_key
      RENAME TO user_pending_edible_nutrients_pending_edible_id_nutrient_id_key;

-- CHECK CONSTRAINTS
ALTER TABLE user_pending_edible_nutrients
    RENAME CONSTRAINT user_pending_food_nutrients_amount_check
    TO user_pending_edible_nutrients_amount_check;

-- FK CONSTRAINTS
ALTER TABLE user_pending_edible_nutrients
    RENAME CONSTRAINT user_pending_food_nutrients_nutrient_id_fkey
    TO user_pending_edible_nutrients_nutrient_id_fkey;

ALTER TABLE user_pending_edible_nutrients
    RENAME CONSTRAINT user_pending_food_nutrients_pending_food_id_fkey
    TO user_pending_edible_nutrients_pending_edible_id_fkey;

