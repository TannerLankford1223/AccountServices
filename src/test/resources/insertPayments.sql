ALTER TABLE payment DROP CONSTRAINT IF EXISTS unique_payment;
ALTER TABLE payment ADD CONSTRAINT unique_payment UNIQUE (period, user_id);

INSERT INTO payment(payment_id, period, salary, username, user_id)
VALUES  (1, '05-2021', 5000000, 'john@acme.com', 2),
        (2, '05-2022', 10000000, 'jane@acme.com', 3),
        (3, '06-2022', 12500000, 'jane@acme.com', 3),
        (4, '07-2022', 13500000, 'jane@acme.com', 3),
        (5, '05-2021', 7500000, 'paul@acme.com', 4),
        (6, '06-2021', 7000000, 'paul@acme.com', 4)
ON CONFLICT (period, user_id)
    DO NOTHING;