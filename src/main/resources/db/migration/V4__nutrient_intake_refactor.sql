ALTER TABLE user_nutrient_intake
DROP COLUMN id;

ALTER TABLE user_nutrient_intake
RENAME COLUMN intake_amount TO amount;