package com.banking.util;

/**
 * Database configuration constants for the Banking Transaction System.
 * 
 * This class contains all database-related configuration parameters including
 * connection details, SQL queries, and database settings.
 * 
 * @author Bhavesh
 * @version 1.0
 */
public final class DBConstants {
    
    // Prevent instantiation
    private DBConstants() {
        throw new AssertionError("DBConstants class cannot be instantiated");
    }
    
    // ================================================
    // DATABASE CONNECTION CONFIGURATION
    // ================================================
    
    /** MySQL database URL */
    public static final String DB_URL = "jdbc:mysql://localhost:3306/banking_system";
    
    /** Database username - UPDATE THIS WITH YOUR MYSQL USERNAME */
    public static final String DB_USERNAME = "root";
    
    /** Database password - UPDATE THIS WITH YOUR MYSQL PASSWORD */
    public static final String DB_PASSWORD = "your_password_here";
    
    /** MySQL JDBC driver class name */
    public static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // ================================================
    // CONNECTION POOL SETTINGS
    // ================================================
    
    /** Maximum number of connections in the pool */
    public static final int MAX_POOL_SIZE = 20;
    
    /** Minimum number of connections in the pool */
    public static final int MIN_POOL_SIZE = 5;
    
    /** Connection timeout in milliseconds */
    public static final int CONNECTION_TIMEOUT = 30000; // 30 seconds
    
    /** Maximum idle time for connections in milliseconds */
    public static final int MAX_IDLE_TIME = 600000; // 10 minutes
    
    // ================================================
    // SQL QUERY TIMEOUTS
    // ================================================
    
    /** Default query timeout in seconds */
    public static final int QUERY_TIMEOUT = 30;
    
    /** Transaction timeout in seconds */
    public static final int TRANSACTION_TIMEOUT = 60;
    
    // ================================================
    // TABLE NAMES
    // ================================================
    
    public static final String TABLE_CUSTOMERS = "customers";
    public static final String TABLE_ACCOUNTS = "accounts";
    public static final String TABLE_TRANSACTIONS = "transactions";
    
    // ================================================
    // CUSTOMER TABLE COLUMNS
    // ================================================
    
    public static final String CUSTOMER_ID = "customer_id";
    public static final String CUSTOMER_FIRST_NAME = "first_name";
    public static final String CUSTOMER_LAST_NAME = "last_name";
    public static final String CUSTOMER_EMAIL = "email";
    public static final String CUSTOMER_PHONE = "phone";
    public static final String CUSTOMER_ADDRESS = "address";
    public static final String CUSTOMER_DATE_OF_BIRTH = "date_of_birth";
    public static final String CUSTOMER_STATUS = "status";
    public static final String CUSTOMER_CREATED_AT = "created_at";
    public static final String CUSTOMER_UPDATED_AT = "updated_at";
    
    // ================================================
    // ACCOUNT TABLE COLUMNS
    // ================================================
    
    public static final String ACCOUNT_ID = "account_id";
    public static final String ACCOUNT_NUMBER = "account_number";
    public static final String ACCOUNT_CUSTOMER_ID = "customer_id";
    public static final String ACCOUNT_TYPE = "account_type";
    public static final String ACCOUNT_BALANCE = "balance";
    public static final String ACCOUNT_STATUS = "status";
    public static final String ACCOUNT_INTEREST_RATE = "interest_rate";
    public static final String ACCOUNT_OVERDRAFT_LIMIT = "overdraft_limit";
    public static final String ACCOUNT_BUSINESS_LICENSE = "business_license";
    public static final String ACCOUNT_CREATED_AT = "created_at";
    public static final String ACCOUNT_UPDATED_AT = "updated_at";
    
    // ================================================
    // TRANSACTION TABLE COLUMNS
    // ================================================
    
    public static final String TRANSACTION_ID = "transaction_id";
    public static final String TRANSACTION_REFERENCE = "transaction_reference";
    public static final String TRANSACTION_FROM_ACCOUNT_ID = "from_account_id";
    public static final String TRANSACTION_TO_ACCOUNT_ID = "to_account_id";
    public static final String TRANSACTION_TYPE = "transaction_type";
    public static final String TRANSACTION_AMOUNT = "amount";
    public static final String TRANSACTION_DESCRIPTION = "description";
    public static final String TRANSACTION_STATUS = "status";
    public static final String TRANSACTION_CREATED_AT = "created_at";
    public static final String TRANSACTION_COMPLETED_AT = "completed_at";
    
    // ================================================
    // COMMON SQL PATTERNS
    // ================================================
    
    /** Standard ORDER BY created_at DESC clause */
    public static final String ORDER_BY_CREATED_DESC = " ORDER BY created_at DESC";
    
    /** Standard LIMIT clause for pagination */
    public static final String LIMIT_CLAUSE = " LIMIT ?, ?";
    
    /** Standard WHERE active status clause */
    public static final String WHERE_ACTIVE_STATUS = " WHERE status = 'ACTIVE'";
    
    // ================================================
    // ERROR MESSAGES
    // ================================================
    
    public static final String ERROR_CONNECTION_FAILED = "Failed to establish database connection";
    public static final String ERROR_QUERY_EXECUTION = "Error executing database query";
    public static final String ERROR_TRANSACTION_FAILED = "Database transaction failed";
    public static final String ERROR_RESOURCE_CLEANUP = "Error during resource cleanup";
    
    // ================================================
    // SUCCESS MESSAGES
    // ================================================
    
    public static final String SUCCESS_CONNECTION_ESTABLISHED = "Database connection established successfully";
    public static final String SUCCESS_TRANSACTION_COMMITTED = "Transaction committed successfully";
    public static final String SUCCESS_TRANSACTION_ROLLED_BACK = "Transaction rolled back successfully";
}