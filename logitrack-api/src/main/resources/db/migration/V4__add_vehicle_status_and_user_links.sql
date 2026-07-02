ALTER TABLE vehicles
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE';

ALTER TABLE vehicles
    ADD CONSTRAINT ck_vehicles_status CHECK (status IN ('AVAILABLE', 'IN_USE', 'MAINTENANCE'));

ALTER TABLE app_users
    ADD COLUMN client_id UUID,
    ADD COLUMN delivery_person_id UUID;

ALTER TABLE app_users
    ADD CONSTRAINT fk_app_users_client FOREIGN KEY (client_id) REFERENCES clients (id),
    ADD CONSTRAINT fk_app_users_delivery_person FOREIGN KEY (delivery_person_id) REFERENCES delivery_persons (id);

CREATE INDEX idx_app_users_client_id ON app_users (client_id);
CREATE INDEX idx_app_users_delivery_person_id ON app_users (delivery_person_id);
