INSERT INTO users(user_id, account_non_blocked, failed_attempt,
                  last_name, lock_time, name, password, username)
VALUES  (2, true, 0, 'Doe', null, 'John', 'testpassword', 'john@acme.com'),
        (3, true, 0, 'Doe', null, 'Jane', 'thisIsATestPassword1234', 'jane@acme.com'),
        (4, true, 0, 'McCartney', null, 'Paul', 'BeatlesPassword', 'paul@acme.com')
ON CONFLICT (user_id) DO UPDATE
    SET password = EXCLUDED.password
;