CREATE TABLE customer
(
    id UUID PRIMARY KEY
);

CREATE TABLE account
(
    id          UUID PRIMARY KEY,
    currency    VARCHAR(255) NOT NULL,
    balance     NUMERIC(24, 2) DEFAULT 0,
    customer_id UUID         NOT NULL,
    CONSTRAINT fk_account_customer FOREIGN KEY (customer_id) REFERENCES customer (id)
);

CREATE INDEX idx_account_customer_id ON account (customer_id);
