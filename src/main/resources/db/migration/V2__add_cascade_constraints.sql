ALTER TABLE refresh_tokens
DROP CONSTRAINT refresh_tokens_user_id_fkey,
    ADD CONSTRAINT refresh_tokens_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE password_reset_tokens
DROP CONSTRAINT password_reset_tokens_user_id_fkey,
    ADD CONSTRAINT password_reset_tokens_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE user_wallets
DROP CONSTRAINT user_wallets_user_id_fkey,
    ADD CONSTRAINT user_wallets_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE user_pending_foods
DROP CONSTRAINT user_pending_foods_created_by_fkey,
    ADD CONSTRAINT user_pending_foods_created_by_fkey
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE user_pending_foods
    ALTER COLUMN created_by DROP NOT NULL;