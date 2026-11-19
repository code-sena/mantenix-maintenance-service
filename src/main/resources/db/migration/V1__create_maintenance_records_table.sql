-- V1: maintenance_records table
-- Owner of this schema: maintenance-service (ADR-004). Never edit this file once applied;
-- create a new migration (V2__...) instead.

CREATE TABLE maintenance_records (
    id                    UUID          PRIMARY KEY,
    vehicle_id            UUID          NOT NULL,      -- vehicle id from vehicle-service (no FK: another database)
    type                  VARCHAR(30)   NOT NULL,
    description           VARCHAR(500),
    service_date          DATE          NOT NULL,
    mileage               INTEGER       NOT NULL,      -- vehicle mileage when the service was done
    cost                  NUMERIC(12,2),
    workshop              VARCHAR(100),
    next_service_date     DATE,
    next_service_mileage  INTEGER,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_maintenance_type CHECK (type IN
        ('OIL_CHANGE', 'TIRES', 'BRAKES', 'BATTERY', 'GENERAL_INSPECTION', 'OTHER')),
    CONSTRAINT ck_maintenance_mileage CHECK (mileage >= 0),
    CONSTRAINT ck_maintenance_cost CHECK (cost IS NULL OR cost >= 0),
    CONSTRAINT ck_maintenance_next_date CHECK (next_service_date IS NULL OR next_service_date > service_date),
    CONSTRAINT ck_maintenance_next_mileage CHECK (next_service_mileage IS NULL OR next_service_mileage > mileage)
);

CREATE INDEX idx_maintenance_vehicle_id ON maintenance_records (vehicle_id);
CREATE INDEX idx_maintenance_next_service_date ON maintenance_records (next_service_date);
