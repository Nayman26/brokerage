-- Admin kullanıcı (şifresi: admin123)
INSERT INTO customers (id, username, password)
VALUES (1, 'admin', '$2a$10$7ZBnY8G7S4LbX0PSwAF28.azCjZl2E4/Spj1/8Fh2LDDqmdJlmnBi');

-- Normal kullanıcı (şifresi: user123)
INSERT INTO customers (id, username, password)
VALUES (2, 'enes', '$2a$10$qYZhUto.DS13ffBaLuI1YOYsSMyAOZd3J2qM6EXMvjWEpBt8b20DK');

-- Test kullanıcı (şifresi: test123)
INSERT INTO customers (id, username, password)
VALUES (3, 'test', '$2a$10$E3nK2yvQayzirOB4ePTZIOupwa141Ku4Ch27zMfoiKmYkm.7wLLa2');

INSERT INTO customer_roles (customer_id, roles) VALUES (1, 'ADMIN');

INSERT INTO customer_roles (customer_id, roles) VALUES (2, 'USER');

INSERT INTO customer_roles (customer_id, roles) VALUES (3, 'USER');

INSERT INTO assets (customer_id, asset_name, size, usable_size)
VALUES (2, 'TRY', 1000, 1000);

INSERT INTO assets (customer_id, asset_name, size, usable_size)
VALUES (3, 'NYMN', 100, 100);

INSERT INTO orders (id, customer_id, asset_name, order_side, size, price, status, create_date)
VALUES (10, 1, 'NYMN', 'BUY', 50, 100, 'PENDING', CURRENT_TIMESTAMP);

INSERT INTO orders (id, customer_id, asset_name, order_side, size, price, status, create_date)
VALUES (11, 2, 'NYMN', 'SELL', 50, 90, 'PENDING', CURRENT_TIMESTAMP);

INSERT INTO orders (id, customer_id, asset_name, order_side, size, price, status, create_date)
VALUES (12, 1, 'NYMN', 'BUY', 30, 80, 'PENDING', CURRENT_TIMESTAMP);

INSERT INTO orders (id, customer_id, asset_name, order_side, size, price, status, create_date)
VALUES (13, 2, 'THYAO', 'SELL', 50, 90, 'PENDING', CURRENT_TIMESTAMP);
