-- Supermart ERP schema. Written in portable SQL; H2 runs in MySQL compatibility mode.

CREATE TABLE IF NOT EXISTS departments (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS employees (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(120)   NOT NULL,
    email         VARCHAR(160)   NOT NULL UNIQUE,
    department_id INT            NOT NULL,
    salary        DECIMAL(12, 2) NOT NULL,
    joining_date  DATE           NOT NULL,
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES departments (id)
);

CREATE TABLE IF NOT EXISTS categories (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS suppliers (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(120) NOT NULL UNIQUE,
    contact_email VARCHAR(160) NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(140)   NOT NULL,
    sku            VARCHAR(40)    NOT NULL UNIQUE,
    category_id    INT            NOT NULL,
    supplier_id    INT            NOT NULL,
    unit_price     DECIMAL(12, 2) NOT NULL,
    stock_quantity INT            NOT NULL,
    reorder_level  INT            NOT NULL,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id)
);

CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(60)  NOT NULL UNIQUE,
    full_name     VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL
);
