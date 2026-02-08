-- Items table
CREATE TABLE IF NOT EXISTS items (
    item_code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    price DECIMAL(10, 2) NOT NULL,
    category_code VARCHAR(20),
    current_stock INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 10
);

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    customer_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(255)
);

-- Bills table
CREATE TABLE IF NOT EXISTS bills (
    bill_id VARCHAR(30) PRIMARY KEY,
    customer_id VARCHAR(20) NOT NULL,
    cashier_id VARCHAR(20) NOT NULL,
    payment_method VARCHAR(10) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    tax DECIMAL(10, 2) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- Bill items table
CREATE TABLE IF NOT EXISTS bill_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id VARCHAR(30) NOT NULL,
    item_code VARCHAR(20) NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES bills(bill_id),
    FOREIGN KEY (item_code) REFERENCES items(item_code)
);

-- Sample data
INSERT INTO items VALUES 
('ITEM-001', 'Laptop', 'HP Laptop 15-inch', 750.00, 'ELECTRONICS', 25, 5),
('ITEM-002', 'Mouse', 'Wireless Mouse', 15.50, 'ACCESSORIES', 100, 20),
('ITEM-003', 'Keyboard', 'Mechanical Keyboard', 89.99, 'ACCESSORIES', 50, 10),
('ITEM-004', 'Monitor', '24-inch LED Monitor', 199.99, 'ELECTRONICS', 15, 5),
('ITEM-005', 'USB Cable', 'Type-C USB Cable', 9.99, 'ACCESSORIES', 200, 50);

INSERT INTO customers VALUES
('CUST-001', 'John Doe', 'john@email.com', '555-0001', '123 Main St'),
('CUST-002', 'Jane Smith', 'jane@email.com', '555-0002', '456 Oak Ave'),
('CUST-003', 'Bob Johnson', 'bob@email.com', '555-0003', '789 Pine Rd');