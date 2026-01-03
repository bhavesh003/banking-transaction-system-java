-- ================================================
-- Banking Transaction System - MySQL Database Schema
-- ================================================

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS customers;

-- ================================================
-- CUSTOMERS TABLE
-- ================================================
CREATE TABLE customers (
    customer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address TEXT,
    date_of_birth DATE,
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes for performance
    INDEX idx_customer_email (email),
    INDEX idx_customer_status (status),
    INDEX idx_customer_name (last_name, first_name)
);

-- ================================================
-- ACCOUNTS TABLE
-- ================================================
CREATE TABLE accounts (
    account_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    account_type ENUM('SAVINGS', 'CHECKING', 'BUSINESS') NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    
    -- Account type specific fields
    interest_rate DECIMAL(5, 4) DEFAULT NULL, -- For savings accounts
    overdraft_limit DECIMAL(15, 2) DEFAULT NULL, -- For checking accounts
    business_license VARCHAR(50) DEFAULT NULL, -- For business accounts
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT,
    
    -- Indexes for performance
    INDEX idx_account_number (account_number),
    INDEX idx_customer_id (customer_id),
    INDEX idx_account_type (account_type),
    INDEX idx_account_status (status),
    
    -- Constraints
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT chk_interest_rate_valid CHECK (interest_rate IS NULL OR (interest_rate >= 0 AND interest_rate <= 1)),
    CONSTRAINT chk_overdraft_limit_valid CHECK (overdraft_limit IS NULL OR overdraft_limit >= 0)
);

-- ================================================
-- TRANSACTIONS TABLE
-- ================================================
CREATE TABLE transactions (
    transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_reference VARCHAR(50) NOT NULL UNIQUE,
    from_account_id BIGINT,
    to_account_id BIGINT,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER') NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    description TEXT,
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    
    -- Foreign key constraints
    FOREIGN KEY (from_account_id) REFERENCES accounts(account_id) ON DELETE RESTRICT,
    FOREIGN KEY (to_account_id) REFERENCES accounts(account_id) ON DELETE RESTRICT,
    
    -- Indexes for performance
    INDEX idx_transaction_reference (transaction_reference),
    INDEX idx_from_account (from_account_id),
    INDEX idx_to_account (to_account_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_transaction_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_account_transactions (from_account_id, created_at),
    
    -- Constraints
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_valid_accounts CHECK (
        (transaction_type = 'DEPOSIT' AND from_account_id IS NULL AND to_account_id IS NOT NULL) OR
        (transaction_type = 'WITHDRAWAL' AND from_account_id IS NOT NULL AND to_account_id IS NULL) OR
        (transaction_type = 'TRANSFER' AND from_account_id IS NOT NULL AND to_account_id IS NOT NULL AND from_account_id != to_account_id)
    )
);

-- ================================================
-- SAMPLE DATA FOR TESTING
-- ================================================

-- Insert sample customers
INSERT INTO customers (first_name, last_name, email, phone, address, date_of_birth, status) VALUES
('John', 'Doe', 'john.doe@email.com', '+1-555-0101', '123 Main St, City, State 12345', '1985-06-15', 'ACTIVE'),
('Jane', 'Smith', 'jane.smith@email.com', '+1-555-0102', '456 Oak Ave, City, State 12345', '1990-03-22', 'ACTIVE'),
('Bob', 'Johnson', 'bob.johnson@email.com', '+1-555-0103', '789 Pine Rd, City, State 12345', '1978-11-08', 'ACTIVE');

-- Insert sample accounts
INSERT INTO accounts (account_number, customer_id, account_type, balance, interest_rate, overdraft_limit, business_license, status) VALUES
('ACC001001', 1, 'SAVINGS', 5000.00, 0.0250, NULL, NULL, 'ACTIVE'),
('ACC001002', 1, 'CHECKING', 2500.00, NULL, 1000.00, NULL, 'ACTIVE'),
('ACC002001', 2, 'SAVINGS', 10000.00, 0.0300, NULL, NULL, 'ACTIVE'),
('ACC003001', 3, 'BUSINESS', 25000.00, NULL, 5000.00, 'BL123456', 'ACTIVE');

-- Insert sample transactions
INSERT INTO transactions (transaction_reference, from_account_id, to_account_id, transaction_type, amount, description, status, completed_at) VALUES
('TXN001', NULL, 1, 'DEPOSIT', 1000.00, 'Initial deposit', 'COMPLETED', NOW()),
('TXN002', NULL, 2, 'DEPOSIT', 500.00, 'Salary deposit', 'COMPLETED', NOW()),
('TXN003', 1, NULL, 'WITHDRAWAL', 200.00, 'ATM withdrawal', 'COMPLETED', NOW()),
('TXN004', 1, 3, 'TRANSFER', 300.00, 'Transfer to savings', 'COMPLETED', NOW());

-- ================================================
-- USEFUL QUERIES FOR VERIFICATION
-- ================================================

-- View all customers with their account counts
-- SELECT c.*, COUNT(a.account_id) as account_count 
-- FROM customers c 
-- LEFT JOIN accounts a ON c.customer_id = a.customer_id 
-- GROUP BY c.customer_id;

-- View account balances
-- SELECT a.account_number, a.account_type, a.balance, a.status, 
--        CONCAT(c.first_name, ' ', c.last_name) as customer_name
-- FROM accounts a 
-- JOIN customers c ON a.customer_id = c.customer_id;

-- View transaction history
-- SELECT t.transaction_reference, t.transaction_type, t.amount, t.status,
--        fa.account_number as from_account, ta.account_number as to_account,
--        t.created_at
-- FROM transactions t
-- LEFT JOIN accounts fa ON t.from_account_id = fa.account_id
-- LEFT JOIN accounts ta ON t.to_account_id = ta.account_id
-- ORDER BY t.created_at DESC;