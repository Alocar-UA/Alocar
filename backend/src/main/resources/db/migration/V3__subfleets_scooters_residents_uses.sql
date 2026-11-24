-- Domain from the Product Specs (section 3): subfleet (one per residence/condominium),
-- hub with opening hours, scooter, resident and use (utilização).

CREATE TABLE subfleets (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Existing hubs belong to the first client residence.
INSERT INTO subfleets (name) VALUES ('Residencial Quinta das Acácias');

ALTER TABLE hubs ADD COLUMN subfleet_id BIGINT REFERENCES subfleets (id);
UPDATE hubs SET subfleet_id = (SELECT id FROM subfleets WHERE name = 'Residencial Quinta das Acácias');
ALTER TABLE hubs ALTER COLUMN subfleet_id SET NOT NULL;

-- Opening hours (BR3, BR5). NULL on both means the hub is always open.
ALTER TABLE hubs ADD COLUMN opening_time TIME;
ALTER TABLE hubs ADD COLUMN closing_time TIME;
ALTER TABLE hubs ADD CONSTRAINT hubs_opening_hours_check CHECK (
    (opening_time IS NULL AND closing_time IS NULL)
    OR (opening_time IS NOT NULL AND closing_time IS NOT NULL AND opening_time < closing_time)
);

CREATE TABLE scooters (
    id      BIGSERIAL PRIMARY KEY,
    code    VARCHAR(20) NOT NULL UNIQUE,
    hub_id  BIGINT      NOT NULL REFERENCES hubs (id),
    status  VARCHAR(20) NOT NULL,
    version BIGINT      NOT NULL DEFAULT 0
);

CREATE TABLE residents (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    subfleet_id BIGINT       NOT NULL REFERENCES subfleets (id)
);

CREATE TABLE uses (
    id                  BIGSERIAL PRIMARY KEY,
    resident_id         BIGINT      NOT NULL REFERENCES residents (id),
    scooter_id          BIGINT      NOT NULL REFERENCES scooters (id),
    hub_id              BIGINT      NOT NULL REFERENCES hubs (id),
    started_at          TIMESTAMPTZ NOT NULL,
    duration_minutes    INTEGER     NOT NULL CHECK (duration_minutes > 0),
    expected_end_at     TIMESTAMPTZ NOT NULL,
    amount_paid_cents   BIGINT      NOT NULL CHECK (amount_paid_cents >= 0),
    ended_at            TIMESTAMPTZ,
    fine_cents          BIGINT      CHECK (fine_cents >= 0)
);

-- BR6: at most one active use per resident; a scooter is in at most one active use.
CREATE UNIQUE INDEX uses_one_active_per_resident ON uses (resident_id) WHERE ended_at IS NULL;
CREATE UNIQUE INDEX uses_one_active_per_scooter ON uses (scooter_id) WHERE ended_at IS NULL;
CREATE INDEX uses_resident_started_at ON uses (resident_id, started_at DESC);
