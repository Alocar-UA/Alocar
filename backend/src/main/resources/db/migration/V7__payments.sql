-- US06: payments of a resident, one per charge (duration of a use, BR4, or late fine, BR8).
CREATE TABLE payments (
    id           BIGSERIAL PRIMARY KEY,
    resident_id  BIGINT      NOT NULL REFERENCES residents (id),
    use_id       BIGINT      NOT NULL REFERENCES uses (id),
    kind         VARCHAR(10) NOT NULL CHECK (kind IN ('USE', 'FINE')),
    amount_cents BIGINT      NOT NULL CHECK (amount_cents > 0),
    status       VARCHAR(10) NOT NULL CHECK (status IN ('PAID', 'DECLINED')),
    created_at   TIMESTAMPTZ NOT NULL
);

CREATE INDEX payments_resident_created_at ON payments (resident_id, created_at DESC);
