INSERT INTO roles (name) VALUES ('ROLE_VIEWER'), ('ROLE_ANALYST'), ('ROLE_ADMIN');
INSERT INTO users (email, password_hash, full_name, status)
VALUES ('admin@finance.com',
        '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
        'System Admin', 'ACTIVE');
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@finance.com' AND r.name = 'ROLE_ADMIN';

