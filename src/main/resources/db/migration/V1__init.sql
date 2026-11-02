CREATE TABLE IF NOT EXISTS subscription_plans
(
    id        BIGINT PRIMARY KEY,
    type      VARCHAR(20)    NOT NULL,
    free_call_minutes INTEGER        NOT NULL DEFAULT 0,
    call_rate NUMERIC(10, 2) NOT NULL,
    free_sms          INTEGER        NOT NULL DEFAULT 0,
    sms_rate  NUMERIC(10, 2) NOT NULL,
    data_rate NUMERIC(10, 2) NOT NULL,
    CONSTRAINT uq_subscription_plans_type UNIQUE (type)
);

INSERT INTO subscription_plans (id, type, free_call_minutes, call_rate, free_sms, sms_rate, data_rate)
VALUES (1, 'STANDARD', 0, 0.05, 0, 0.01, 2.00),
       (2, 'PREMIUM', 100, 0.03, 20, 0.01, 2.00)
ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS customers
(
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email                VARCHAR(255)             NOT NULL,
    name                 VARCHAR(255)             NOT NULL,
    subscription_plan_id BIGINT                   NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_customers_email UNIQUE (email),
    CONSTRAINT fk_customers_subscription_plan
        FOREIGN KEY (subscription_plan_id) REFERENCES subscription_plans (id)
);

CREATE TABLE IF NOT EXISTS cdr_events
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id     UUID                     NOT NULL,
    customer_id  BIGINT                   NOT NULL,
    type         VARCHAR(20)              NOT NULL,
    quantity     INTEGER                  NOT NULL,
    event_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_cdr_events_event_id UNIQUE (event_id),
    CONSTRAINT fk_cdr_events_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE TABLE IF NOT EXISTS customer_billing_cycles
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    cycle             VARCHAR(7)     NOT NULL,
    total_cost        NUMERIC(19, 8) NOT NULL DEFAULT 0,
    used_free_seconds INTEGER        NOT NULL DEFAULT 0,
    used_free_sms     INTEGER        NOT NULL DEFAULT 0,
    CONSTRAINT uq_customer_billing_cycles_customer_cycle UNIQUE (customer_id, cycle),
    CONSTRAINT fk_customer_billing_cycles_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id)
);
