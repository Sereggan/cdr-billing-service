-- Demo dataset, applied after the Flyway migrations when the "demo" profile is active.
INSERT INTO customers (email, name, subscription_plan_id, created_at)
VALUES ('alice@example.com', 'Alice Fischer', 2, now()),
       ('bob@example.com', 'Bob Neumann', 1, now()),
       ('carol@example.com', 'Carol Weber', 2, now()),
       ('dave@example.com', 'Dave Krüger', 1, now()),
       ('eve@example.com', 'Eve Lehmann', 1, now()),
       ('frank@example.com', 'Frank Bauer', 2, now()),
       ('grace@example.com', 'Grace Hoffmann', 1, now()),
       ('heidi@example.com', 'Heidi Schulz', 1, now()),
       ('ivan@example.com', 'Ivan Petrov', 2, now()),
       ('judy@example.com', 'Judy Wagner', 1, now()),
       ('karl@example.com', 'Karl Schmidt', 1, now()),
       ('laura@example.com', 'Laura Becker', 2, now())
ON CONFLICT DO NOTHING;

-- The customer ids are assigned by the identity column, so the rows below join on the email instead.
INSERT INTO cdr_events (event_id, customer_id, type, quantity, event_timestamp, processed_at)
SELECT event.event_id::uuid, customer.id, event.type, event.quantity, now(), now()
FROM (VALUES ('00000000-0000-4000-8000-000000000001', 'alice@example.com', 'CALL', 12000),
             ('00000000-0000-4000-8000-000000000002', 'alice@example.com', 'SMS', 50),
             ('00000000-0000-4000-8000-000000000003', 'bob@example.com', 'CALL', 3600),
             ('00000000-0000-4000-8000-000000000004', 'bob@example.com', 'SMS', 100),
             ('00000000-0000-4000-8000-000000000005', 'carol@example.com', 'CALL', 3000),
             ('00000000-0000-4000-8000-000000000006', 'carol@example.com', 'SMS', 10),
             ('00000000-0000-4000-8000-000000000007', 'carol@example.com', 'DATA', 1024),
             ('00000000-0000-4000-8000-000000000008', 'dave@example.com', 'DATA', 5120),
             ('00000000-0000-4000-8000-000000000009', 'eve@example.com', 'CALL', 7200),
             ('00000000-0000-4000-8000-000000000010', 'frank@example.com', 'CALL', 18000),
             ('00000000-0000-4000-8000-000000000011', 'grace@example.com', 'SMS', 250),
             ('00000000-0000-4000-8000-000000000012', 'heidi@example.com', 'CALL', 1800),
             ('00000000-0000-4000-8000-000000000013', 'heidi@example.com', 'SMS', 20),
             ('00000000-0000-4000-8000-000000000014', 'ivan@example.com', 'DATA', 2048),
             ('00000000-0000-4000-8000-000000000015', 'judy@example.com', 'CALL', 600),
             ('00000000-0000-4000-8000-000000000016', 'karl@example.com', 'SMS', 30),
             ('00000000-0000-4000-8000-000000000017', 'laura@example.com', 'CALL', 1200))
         AS event (event_id, email, type, quantity)
         JOIN customers customer ON customer.email = event.email
ON CONFLICT DO NOTHING;

INSERT INTO customer_billing_cycles (customer_id, cycle, total_cost, used_free_seconds, used_free_sms)
SELECT customer.id, to_char(now(), 'YYYY-MM'), cycle.total_cost, cycle.used_free_seconds, cycle.used_free_sms
FROM (VALUES ('alice@example.com', 3.30, 6000, 20),
             ('bob@example.com', 4.00, 0, 0),
             ('carol@example.com', 2.00, 3000, 10),
             ('dave@example.com', 10.00, 0, 0),
             ('eve@example.com', 6.00, 0, 0),
             ('frank@example.com', 6.00, 6000, 0),
             ('grace@example.com', 2.50, 0, 0),
             ('heidi@example.com', 1.70, 0, 0),
             ('ivan@example.com', 4.00, 0, 0),
             ('judy@example.com', 0.50, 0, 0),
             ('karl@example.com', 0.30, 0, 0),
             ('laura@example.com', 0.00, 1200, 0))
         AS cycle (email, total_cost, used_free_seconds, used_free_sms)
         JOIN customers customer ON customer.email = cycle.email
ON CONFLICT DO NOTHING;

