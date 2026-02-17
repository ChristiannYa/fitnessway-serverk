CREATE TABLE IF NOT EXISTS user_wallets (
    user_id UUID NOT NULL PRIMARY KEY REFERENCES users(id),
    amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00
);

CREATE TYPE user_currency_transaction_type AS ENUM ('food approval', 'redeem', 'food_logged');

CREATE TABLE IF NOT EXISTS user_currency_transactions (
    id SERIAL NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    amount DECIMAL(12, 2) NOT NULL,
    transaction_type user_currency_transaction_type NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE user_type AS ENUM ('user', 'contributor', 'admin');

-- Add type column to users with default 'user'
ALTER TABLE users
ADD COLUMN type user_type NOT NULL DEFAULT 'user';

CREATE TABLE IF NOT EXISTS app_foods (
    id SERIAL NOT NULL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    brand VARCHAR(50) NOT NULL,
    amount_per_serving NUMERIC(12, 4) NOT NULL CHECK (amount_per_serving > 0),
    serving_unit nutrient_unit NOT NULL,
    created_by UUID REFERENCES users (id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (name, brand, amount_per_serving, serving_unit)
);

-- Fast case-insensitive search by food name
-- Without these indexes, Postgres scans the entire table and computes LOWER(name) and
-- LOWER(brand) on-the-fly which slows down as the table grows
CREATE INDEX idx_app_foods_name_lower ON app_foods (LOWER(name));

-- Fast case-insensitive search by brand
CREATE INDEX idx_app_foods_brand_lower ON app_foods (LOWER(brand));

-- Fast lookup of all foods created by a user (for contribution stats)
CREATE INDEX idx_app_foods_created_by ON app_foods (created_by)
WHERE created_by IS NOT NULL;

CREATE TABLE IF NOT EXISTS app_food_nutrients (
    app_food_id INTEGER NOT NULL REFERENCES app_foods (id) ON DELETE CASCADE,
    nutrient_id INTEGER NOT NULL REFERENCES nutrients (id) ON DELETE CASCADE,
    amount NUMERIC(12, 4) NOT NULL CHECK (amount >= 0),
    PRIMARY KEY (app_food_id, nutrient_id)
);

-- Fast lookup of all nutrients for a specific food
-- Without this, a full table scan of app_food_nutrients is required
CREATE INDEX idx_app_food_nutrients_food_id ON app_food_nutrients (app_food_id);

CREATE TYPE user_pending_food_status AS ENUM ('pending', 'approved', 'rejected');

CREATE TABLE IF NOT EXISTS user_pending_foods (
    id SERIAL NOT NULL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    brand VARCHAR(50) NOT NULL,
    amount_per_serving DECIMAL(12, 4) NOT NULL CHECK (amount_per_serving > 0),
    serving_unit nutrient_unit NOT NULL,
    submitted_by UUID REFERENCES users (id) NOT NULL,
    status user_pending_food_status NOT NULL DEFAULT 'pending',
    reviewed_by UUID REFERENCES users (id),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    rejection_reason TEXT CHECK (
        rejection_reason IS NULL
            OR
        (LENGTH (rejection_reason) <= 250)
    ),
    UNIQUE (submitted_by, name, brand, amount_per_serving, serving_unit)
);

-- Fast lookup for checking daily submission limits
CREATE INDEX idx_user_pending_foods_user_date ON user_pending_foods (submitted_by, created_at);

CREATE TABLE IF NOT EXISTS user_pending_food_nutrients (
    pending_food_id INT NOT NULL REFERENCES user_pending_foods (id) ON DELETE CASCADE,
    nutrient_id INT REFERENCES nutrients (id) NOT NULL,
    amount DECIMAL(12, 4) NOT NULL CHECK (amount > 0),
    UNIQUE (pending_food_id, nutrient_id)
);

CREATE TABLE IF NOT EXISTS user_favorite_app_foods (
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    app_food_id INTEGER NOT NULL REFERENCES app_foods (id) ON DELETE CASCADE,
    favorited_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, app_food_id)
);

-- Fast lookup of all favorited foods for a user
CREATE INDEX idx_user_favorite_app_foods_user_id ON user_favorite_app_foods (user_id);

CREATE TYPE food_source AS ENUM ('user', 'app');

-- Add new columns to user_food_logs
ALTER TABLE user_food_logs
ADD COLUMN logged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN source food_source;

-- Backfill source column for existing records (all are 'user' foods)
UPDATE user_food_logs
SET source = 'user'
WHERE source IS NULL;

-- Make source NOT NULL after backfilling
ALTER TABLE user_food_logs
ALTER COLUMN source
SET NOT NULL;

-- Rename log_time to time in user_food_logs
ALTER TABLE user_food_logs
RENAME COLUMN log_time TO time;

-- Fast lookup of recently logged user-created foods
-- Optimizes queries that fetch user's custom foods sorted by when they logged them
CREATE INDEX idx_user_food_logs_user_source_logged ON user_food_logs (user_id, source, food_id, logged_at DESC)
WHERE source = 'user';

-- Fast lookup of recently logged app foods
-- Optimizes queries that fetch database foods sorted by when user logged them
CREATE INDEX idx_user_food_logs_app_source_logged ON user_food_logs (user_id, source, food_id, logged_at DESC)
WHERE source = 'app';

-- Add updated_at to user_foods
ALTER TABLE user_foods
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE user_food_nutrients
DROP COLUMN id;