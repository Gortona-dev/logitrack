CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE clients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL,
    document VARCHAR(20) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_clients_email UNIQUE (email),
    CONSTRAINT uk_clients_document UNIQUE (document)
);

CREATE TABLE delivery_persons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL,
    document VARCHAR(20) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_delivery_persons_email UNIQUE (email),
    CONSTRAINT uk_delivery_persons_document UNIQUE (document),
    CONSTRAINT ck_delivery_persons_status CHECK (status IN ('AVAILABLE', 'UNAVAILABLE', 'ON_DELIVERY'))
);

CREATE TABLE vehicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    license_plate VARCHAR(12) NOT NULL,
    brand VARCHAR(80) NOT NULL,
    model VARCHAR(80) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_vehicles_license_plate UNIQUE (license_plate)
);

CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL,
    pickup_address VARCHAR(255) NOT NULL,
    delivery_address VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_orders_client FOREIGN KEY (client_id) REFERENCES clients (id)
);

CREATE TABLE deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    delivery_person_id UUID,
    vehicle_id UUID,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_deliveries_order UNIQUE (order_id),
    CONSTRAINT fk_deliveries_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_deliveries_delivery_person FOREIGN KEY (delivery_person_id) REFERENCES delivery_persons (id),
    CONSTRAINT fk_deliveries_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id),
    CONSTRAINT ck_deliveries_status CHECK (status IN ('PENDING', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED'))
);

CREATE TABLE delivery_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    notes VARCHAR(500),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_delivery_status_history_delivery FOREIGN KEY (delivery_id) REFERENCES deliveries (id),
    CONSTRAINT ck_delivery_status_history_previous_status CHECK (
        previous_status IS NULL OR previous_status IN ('PENDING', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED')
    ),
    CONSTRAINT ck_delivery_status_history_new_status CHECK (
        new_status IN ('PENDING', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED')
    )
);

CREATE INDEX idx_orders_client_id ON orders (client_id);
CREATE INDEX idx_deliveries_status ON deliveries (status);
CREATE INDEX idx_deliveries_delivery_person_id ON deliveries (delivery_person_id);
CREATE INDEX idx_delivery_status_history_delivery_id ON delivery_status_history (delivery_id);
CREATE INDEX idx_delivery_status_history_changed_at ON delivery_status_history (changed_at);
