CREATE INDEX IF NOT EXISTS idx_deliveries_updated_at ON deliveries (updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_deliveries_status_updated_at ON deliveries (status, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_deliveries_delivery_person_updated_at ON deliveries (delivery_person_id, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_orders_client_updated_at ON orders (client_id, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_orders_tracking_code ON orders (tracking_code);
