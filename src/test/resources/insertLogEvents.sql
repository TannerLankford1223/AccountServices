TRUNCATE TABLE event_log;
INSERT INTO event_log(id, action, date, object, path, subject)
VALUES  (1,'CREATE_USER', '2022-6-19', 'john@acme.com',
         '/api/auth/signup', 'Anonymous'),
        (2, 'GRANT_ROLE', '2022-7-19', 'Grant role ACCOUNTANT to jane@acme.com',
         '/api/admin/user/role', 'john@acme.com'),
        (3, 'CHANGE_PASSWORD', '2022-5-10', 'paul@acme.com',
         '/api/auth/changepass', 'paul@acme.com'),
        (4, 'BRUTE_FORCE', '2022-6-21', 'api/admin/user/role',
         'api/admin/user/role', 'jane@acme.com'),
        (5, 'LOCK_USER', '2022-6-21', 'Lock user jane@acme.com',
         'api/admin/user/role', 'jane@acme.com'),
        (6, 'DELETE_USER', '2022-6-22', 'jane@acme.com',
         'api/admin/user/', 'john@acme.com')
ON CONFLICT (id) DO NOTHING
;
