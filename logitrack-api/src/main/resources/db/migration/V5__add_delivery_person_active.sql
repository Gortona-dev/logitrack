ALTER TABLE delivery_persons
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT true;

CREATE INDEX idx_delivery_persons_active ON delivery_persons (active);
