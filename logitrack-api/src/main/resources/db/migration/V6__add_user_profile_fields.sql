ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS document VARCHAR(20),
    ADD COLUMN IF NOT EXISTS phone VARCHAR(20);

CREATE INDEX IF NOT EXISTS idx_app_users_role ON app_users (role);
