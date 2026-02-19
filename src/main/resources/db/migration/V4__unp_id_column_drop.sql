-- Drop the existing primary key
ALTER TABLE user_nutrient_preferences DROP CONSTRAINT user_nutrient_preferences_pkey;

-- Drop the unique constraint (replaced with the primary key)
ALTER TABLE user_nutrient_preferences DROP CONSTRAINT user_nutrient_preferences_user_id_nutrient_id_key;

-- Make (user_id, nutrient_id) the new primary key
ALTER TABLE user_nutrient_preferences ADD PRIMARY KEY (user_id, nutrient_id);

-- Drop the id column (and its sequence)
ALTER TABLE user_nutrient_preferences DROP COLUMN id;
DROP SEQUENCE IF EXISTS user_nutrient_preferences_id_seq;