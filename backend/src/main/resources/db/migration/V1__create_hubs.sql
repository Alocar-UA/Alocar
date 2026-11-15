CREATE TABLE hubs (
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL UNIQUE,
    block    VARCHAR(50)  NOT NULL,
    capacity INTEGER      NOT NULL CHECK (capacity >= 0)
);
