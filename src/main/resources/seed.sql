-- Demo data loaded once, on first run, when the database is empty.
-- Login rows are NOT here: passwords are salted + PBKDF2-hashed in Java (see SchemaInitializer).

INSERT INTO departments (name) VALUES ('Store Operations');
INSERT INTO departments (name) VALUES ('Warehouse');
INSERT INTO departments (name) VALUES ('Finance');
INSERT INTO departments (name) VALUES ('IT');

INSERT INTO employees (full_name, email, department_id, salary, joining_date) VALUES
 ('Anita Rao',        'anita.rao@supermart.example',      1, 42000.00, '2021-03-15'),
 ('Bharat Menon',     'bharat.menon@supermart.example',   1, 38500.00, '2022-07-01'),
 ('Chitra Iyer',      'chitra.iyer@supermart.example',    2, 35000.00, '2020-11-20'),
 ('Devendra Singh',   'devendra.singh@supermart.example', 2, 33750.00, '2023-01-09'),
 ('Esha Kulkarni',    'esha.kulkarni@supermart.example',  3, 61000.00, '2019-06-25'),
 ('Farhan Qureshi',   'farhan.qureshi@supermart.example', 4, 72000.00, '2022-02-14'),
 ('Gitanjali Bose',   'gitanjali.bose@supermart.example', 4, 68500.00, '2023-08-30');

INSERT INTO categories (name) VALUES ('Beverages');
INSERT INTO categories (name) VALUES ('Dairy');
INSERT INTO categories (name) VALUES ('Household');
INSERT INTO categories (name) VALUES ('Snacks');

INSERT INTO suppliers (name, contact_email) VALUES
 ('Kaveri Distributors', 'orders@kaveri.example'),
 ('Nandini Farms',       'supply@nandini.example'),
 ('Ganga Home Goods',    'sales@gangahome.example');

INSERT INTO products (name, sku, category_id, supplier_id, unit_price, stock_quantity, reorder_level) VALUES
 ('Filter Coffee 500g',    'BEV-001', 1, 1, 320.00, 140,  40),
 ('Masala Chai 250g',      'BEV-002', 1, 1, 180.00,  22,  30),
 ('Bottled Water 1L x12',  'BEV-003', 1, 1, 240.00, 310,  60),
 ('Toned Milk 1L',         'DRY-001', 2, 2,  56.00,  18,  50),
 ('Paneer 200g',           'DRY-002', 2, 2, 110.00,  75,  25),
 ('Curd 400g',             'DRY-003', 2, 2,  45.00, 120,  40),
 ('Dish Wash Gel 750ml',   'HHD-001', 3, 3, 199.00,  64,  20),
 ('Floor Cleaner 1L',      'HHD-002', 3, 3, 175.00,   9,  25),
 ('Banana Chips 200g',     'SNK-001', 4, 1,  95.00, 210,  50),
 ('Roasted Peanuts 500g',  'SNK-002', 4, 1, 130.00,  47,  50);
