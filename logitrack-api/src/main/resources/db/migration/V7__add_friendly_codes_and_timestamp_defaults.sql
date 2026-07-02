ALTER TABLE clients
    ADD COLUMN IF NOT EXISTS code VARCHAR(20);

ALTER TABLE delivery_persons
    ADD COLUMN IF NOT EXISTS code VARCHAR(20);

ALTER TABLE vehicles
    ADD COLUMN IF NOT EXISTS code VARCHAR(20),
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS tracking_code VARCHAR(30);

UPDATE clients
SET code = 'CLI-' || lpad(row_number::text, 4, '0')
FROM (
    SELECT id, row_number() OVER (ORDER BY created_at, id) AS row_number
    FROM clients
    WHERE code IS NULL
) numbered
WHERE clients.id = numbered.id;

UPDATE delivery_persons
SET code = 'ENT-' || lpad(row_number::text, 4, '0')
FROM (
    SELECT id, row_number() OVER (ORDER BY created_at, id) AS row_number
    FROM delivery_persons
    WHERE code IS NULL
) numbered
WHERE delivery_persons.id = numbered.id;

UPDATE vehicles
SET code = 'VEI-' || lpad(row_number::text, 4, '0')
FROM (
    SELECT id, row_number() OVER (ORDER BY created_at, id) AS row_number
    FROM vehicles
    WHERE code IS NULL
) numbered
WHERE vehicles.id = numbered.id;

UPDATE orders
SET tracking_code = 'PED-' || extract(year from created_at)::int || '-' || lpad(row_number::text, 4, '0')
FROM (
    SELECT id, row_number() OVER (PARTITION BY extract(year from created_at)::int ORDER BY created_at, id) AS row_number
    FROM orders
    WHERE tracking_code IS NULL
) numbered
WHERE orders.id = numbered.id;

CREATE UNIQUE INDEX IF NOT EXISTS uk_clients_code ON clients (code);
CREATE UNIQUE INDEX IF NOT EXISTS uk_delivery_persons_code ON delivery_persons (code);
CREATE UNIQUE INDEX IF NOT EXISTS uk_vehicles_code ON vehicles (code);
CREATE UNIQUE INDEX IF NOT EXISTS uk_orders_tracking_code ON orders (tracking_code);

ALTER TABLE clients
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE delivery_persons
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE vehicles
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE orders
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE deliveries
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE delivery_status_history
    ALTER COLUMN changed_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE app_users
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;
