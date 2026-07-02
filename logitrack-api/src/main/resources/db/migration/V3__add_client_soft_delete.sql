ALTER TABLE clients
    ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_clients_deleted_at ON clients (deleted_at);
