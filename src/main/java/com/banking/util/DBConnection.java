package com.banking.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Database connection utility class for the Banking Transaction System.
 * 
 * This class provides:
 * - Connection pool management
 * - Safe connection acquisition and release
 * - Proper resource cleanup
 * - Transaction management utilities
 * 
 * Thread-safe implementation with connection pooling for better performance.
 * 
 * @author Banking System Team
 * @version 1.0
 */
public final class DBConnection {
    
    // Connection pool to manage database connections
    private static BlockingQueue<Connection> connectionPool;
    private static volatile boolean isInitialized = false;
    private static final Object lock = new Object();
    
    // Prevent instantiation
    private DBConnection() {
        throw new AssertionError("DBConnection class cannot be instantiated");
    }
    
    /**
     * Initialize the connection pool.
     * This method is thread-safe and will only initialize once.
     */
    public static void initializePool() {
        if (!isInitialized) {
            synchronized (lock) {
                if (!isInitialized) {
                    try {
                        // Load MySQL JDBC driver
                        Class.forName(DBConstants.DB_DRIVER);
                        
                        // Initialize connection pool
                        connectionPool = new ArrayBlockingQueue<>(DBConstants.MAX_POOL_SIZE);
                        
                        // Create initial connections
                        for (int i = 0; i < DBConstants.MIN_POOL_SIZE; i++) {
                            Connection conn = createNewConnection();
                            if (conn != null) {
                                connectionPool.offer(conn);
                            }
                        }
                        
                        isInitialized = true;
                        System.out.println(DBConstants.SUCCESS_CONNECTION_ESTABLISHED);
                        
                    } catch (ClassNotFoundException e) {
                        System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
                        throw new RuntimeException("Failed to load database driver", e);
                    } catch (SQLException e) {
                        System.err.println("Failed to initialize connection pool: " + e.getMessage());
                        throw new RuntimeException("Failed to initialize database connection pool", e);
                    }
                }
            }
        }
    }
    
    /**
     * Get a connection from the pool.
     * If no connection is available, creates a new one (up to max pool size).
     * 
     * @return Database connection
     * @throws SQLException if unable to get connection
     */
    public static Connection getConnection() throws SQLException {
        if (!isInitialized) {
            initializePool();
        }
        
        try {
            // Try to get connection from pool with timeout
            Connection conn = connectionPool.poll(DBConstants.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
            
            if (conn == null || conn.isClosed()) {
                // Create new connection if pool is empty or connection is closed
                conn = createNewConnection();
            }
            
            if (conn == null) {
                throw new SQLException(DBConstants.ERROR_CONNECTION_FAILED);
            }
            
            // Ensure connection is valid and auto-commit is enabled by default
            conn.setAutoCommit(true);
            return conn;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for database connection", e);
        }
    }
    
    /**
     * Return a connection to the pool.
     * 
     * @param connection Connection to return to pool
     */
    public static void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                // Reset connection state
                if (!connection.isClosed()) {
                    connection.setAutoCommit(true);
                    
                    // Return to pool if there's space, otherwise close
                    if (!connectionPool.offer(connection)) {
                        connection.close();
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error releasing connection: " + e.getMessage());
                // Force close the connection
                closeQuietly(connection);
            }
        }
    }
    
    /**
     * Create a new database connection.
     * 
     * @return New database connection
     * @throws SQLException if connection cannot be created
     */
    private static Connection createNewConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(
                DBConstants.DB_URL,
                DBConstants.DB_USERNAME,
                DBConstants.DB_PASSWORD
            );
            
            // Set connection properties
            conn.setAutoCommit(true);
            
            return conn;
            
        } catch (SQLException e) {
            System.err.println("Failed to create database connection: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Begin a database transaction.
     * Disables auto-commit for the connection.
     * 
     * @param connection Database connection
     * @throws SQLException if transaction cannot be started
     */
    public static void beginTransaction(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.setAutoCommit(false);
        } else {
            throw new SQLException("Cannot begin transaction: connection is null or closed");
        }
    }
    
    /**
     * Commit a database transaction.
     * Re-enables auto-commit for the connection.
     * 
     * @param connection Database connection
     * @throws SQLException if transaction cannot be committed
     */
    public static void commitTransaction(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.commit();
            connection.setAutoCommit(true);
            System.out.println(DBConstants.SUCCESS_TRANSACTION_COMMITTED);
        } else {
            throw new SQLException("Cannot commit transaction: connection is null or closed");
        }
    }
    
    /**
     * Rollback a database transaction.
     * Re-enables auto-commit for the connection.
     * 
     * @param connection Database connection
     */
    public static void rollbackTransaction(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    System.out.println(DBConstants.SUCCESS_TRANSACTION_ROLLED_BACK);
                }
            } catch (SQLException e) {
                System.err.println("Error rolling back transaction: " + e.getMessage());
            }
        }
    }
    
    /**
     * Close database resources safely without throwing exceptions.
     * 
     * @param connection Database connection to close
     */
    public static void closeQuietly(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(DBConstants.ERROR_RESOURCE_CLEANUP + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Close PreparedStatement safely without throwing exceptions.
     * 
     * @param statement PreparedStatement to close
     */
    public static void closeQuietly(PreparedStatement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println(DBConstants.ERROR_RESOURCE_CLEANUP + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Close Statement safely without throwing exceptions.
     * 
     * @param statement Statement to close
     */
    public static void closeQuietly(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println(DBConstants.ERROR_RESOURCE_CLEANUP + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Close ResultSet safely without throwing exceptions.
     * 
     * @param resultSet ResultSet to close
     */
    public static void closeQuietly(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                System.err.println(DBConstants.ERROR_RESOURCE_CLEANUP + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Close all database resources safely.
     * 
     * @param connection Database connection
     * @param statement PreparedStatement or Statement
     * @param resultSet ResultSet
     */
    public static void closeAll(Connection connection, Statement statement, ResultSet resultSet) {
        closeQuietly(resultSet);
        closeQuietly(statement);
        releaseConnection(connection);
    }
    
    /**
     * Test database connectivity.
     * 
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        } finally {
            releaseConnection(conn);
        }
    }
    
    /**
     * Shutdown the connection pool and close all connections.
     * Should be called when application is shutting down.
     */
    public static void shutdown() {
        if (isInitialized && connectionPool != null) {
            synchronized (lock) {
                // Close all connections in the pool
                Connection conn;
                while ((conn = connectionPool.poll()) != null) {
                    closeQuietly(conn);
                }
                
                connectionPool = null;
                isInitialized = false;
                System.out.println("Database connection pool shutdown completed");
            }
        }
    }
    
    /**
     * Get current pool size for monitoring purposes.
     * 
     * @return Current number of connections in pool
     */
    public static int getPoolSize() {
        return isInitialized && connectionPool != null ? connectionPool.size() : 0;
    }
}