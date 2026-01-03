package com.banking.dao.impl;

import com.banking.dao.CustomerDAO;
import com.banking.model.Customer;
import com.banking.model.CustomerStatus;
import com.banking.exception.BankingException;
import com.banking.util.DBConnection;
import com.banking.util.DBConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of CustomerDAO interface using JDBC.
 * 
 * This class handles all database operations for customers using
 * PreparedStatements for security and performance.
 * 
 * @author Banking System Team
 * @version 1.0
 */
public class CustomerDAOImpl implements CustomerDAO {
    
    // SQL Queries
    private static final String INSERT_CUSTOMER = 
        "INSERT INTO " + DBConstants.TABLE_CUSTOMERS + 
        " (first_name, last_name, email, phone, address, date_of_birth, status) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM " + DBConstants.TABLE_CUSTOMERS + 
        " WHERE customer_id = ?";
    
    private static final String SELECT_BY_EMAIL = 
        "SELECT * FROM " + DBConstants.TABLE_CUSTOMERS + 
        " WHERE email = ?";
    
    private static final String UPDATE_CUSTOMER = 
        "UPDATE " + DBConstants.TABLE_CUSTOMERS + 
        " SET first_name = ?, last_name = ?, email = ?, phone = ?, " +
        "address = ?, date_of_birth = ?, status = ?, updated_at = CURRENT_TIMESTAMP " +
        "WHERE customer_id = ?";
    
    private static final String SOFT_DELETE_CUSTOMER = 
        "UPDATE " + DBConstants.TABLE_CUSTOMERS + 
        " SET status = 'CLOSED', updated_at = CURRENT_TIMESTAMP " +
        "WHERE customer_id = ?";
    
    private static final String SELECT_ALL_PAGINATED = 
        "SELECT * FROM " + DBConstants.TABLE_CUSTOMERS + 
        " ORDER BY created_at DESC LIMIT ?, ?";
    
    private static final String SELECT_BY_STATUS = 
        "SELECT * FROM " + DBConstants.TABLE_CUSTOMERS + 
        " WHERE status = ? ORDER BY created_at DESC LIMIT ?, ?";
    
    private static final String SEARCH_BY_NAME = 
        "SELECT * FROM " + DBConstants.TABLE_CUSTOMERS + 
        " WHERE (first_name LIKE ? OR last_name LIKE ?) " +
        "ORDER BY last_name, first_name LIMIT ?, ?";
    
    private static final String EXISTS_BY_EMAIL = 
        "SELECT COUNT(*) FROM " + DBConstants.TABLE_CUSTOMERS + 
        " WHERE email = ?";
    
    private static final String COUNT_ALL = 
        "SELECT COUNT(*) FROM " + DBConstants.TABLE_CUSTOMERS;
    
    private static final String COUNT_BY_STATUS = 
        "SELECT COUNT(*) FROM " + DBConstants.TABLE_CUSTOMERS + 
        " WHERE status = ?";
    
    @Override
    public Customer create(Customer customer) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(INSERT_CUSTOMER, Statement.RETURN_GENERATED_KEYS);
            
            // Set parameters
            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getLastName());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getPhone());
            stmt.setString(5, customer.getAddress());
            
            // Handle date of birth
            if (customer.getDateOfBirth() != null) {
                stmt.setDate(6, Date.valueOf(customer.getDateOfBirth()));
            } else {
                stmt.setNull(6, java.sql.Types.DATE);
            }
            
            stmt.setString(7, customer.getStatus().name());
            
            // Execute insert
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new BankingException("Creating customer failed, no rows affected");
            }
            
            // Get generated ID
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                customer.setCustomerId(rs.getLong(1));
                customer.setCreatedAt(LocalDateTime.now());
                customer.setUpdatedAt(LocalDateTime.now());
            } else {
                throw new BankingException("Creating customer failed, no ID obtained");
            }
            
            return customer;
            
        } catch (SQLException e) {
            throw new BankingException("Error creating customer: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public Optional<Customer> findById(Long customerId) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SELECT_BY_ID);
            stmt.setLong(1, customerId);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToCustomer(rs));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new BankingException("Error finding customer by ID: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public Optional<Customer> findByEmail(String email) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SELECT_BY_EMAIL);
            stmt.setString(1, email);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToCustomer(rs));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new BankingException("Error finding customer by email: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public Customer update(Customer customer) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(UPDATE_CUSTOMER);
            
            // Set parameters
            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getLastName());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getPhone());
            stmt.setString(5, customer.getAddress());
            
            // Handle date of birth
            if (customer.getDateOfBirth() != null) {
                stmt.setDate(6, Date.valueOf(customer.getDateOfBirth()));
            } else {
                stmt.setNull(6, java.sql.Types.DATE);
            }
            
            stmt.setString(7, customer.getStatus().name());
            stmt.setLong(8, customer.getCustomerId());
            
            // Execute update
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new BankingException("Updating customer failed, customer not found");
            }
            
            customer.setUpdatedAt(LocalDateTime.now());
            return customer;
            
        } catch (SQLException e) {
            throw new BankingException("Error updating customer: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, null);
        }
    }
    
    @Override
    public boolean delete(Long customerId) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SOFT_DELETE_CUSTOMER);
            stmt.setLong(1, customerId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new BankingException("Error deleting customer: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, null);
        }
    }
    
    @Override
    public List<Customer> findAll(int offset, int limit) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SELECT_ALL_PAGINATED);
            stmt.setInt(1, offset);
            stmt.setInt(2, limit);
            
            rs = stmt.executeQuery();
            
            List<Customer> customers = new ArrayList<>();
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
            
            return customers;
            
        } catch (SQLException e) {
            throw new BankingException("Error finding all customers: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public List<Customer> findByStatus(String status, int offset, int limit) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SELECT_BY_STATUS);
            stmt.setString(1, status);
            stmt.setInt(2, offset);
            stmt.setInt(3, limit);
            
            rs = stmt.executeQuery();
            
            List<Customer> customers = new ArrayList<>();
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
            
            return customers;
            
        } catch (SQLException e) {
            throw new BankingException("Error finding customers by status: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public List<Customer> searchByName(String searchTerm, int offset, int limit) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SEARCH_BY_NAME);
            
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setInt(3, offset);
            stmt.setInt(4, limit);
            
            rs = stmt.executeQuery();
            
            List<Customer> customers = new ArrayList<>();
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
            
            return customers;
            
        } catch (SQLException e) {
            throw new BankingException("Error searching customers by name: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public boolean existsByEmail(String email) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(EXISTS_BY_EMAIL);
            stmt.setString(1, email);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
            return false;
            
        } catch (SQLException e) {
            throw new BankingException("Error checking if customer exists by email: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public long getTotalCount() throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(COUNT_ALL);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            throw new BankingException("Error getting total customer count: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public long getCountByStatus(String status) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(COUNT_BY_STATUS);
            stmt.setString(1, status);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            throw new BankingException("Error getting customer count by status: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    /**
     * Map ResultSet to Customer object.
     * 
     * @param rs ResultSet containing customer data
     * @return Customer object
     * @throws SQLException if mapping fails
     */
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        
        customer.setCustomerId(rs.getLong(DBConstants.CUSTOMER_ID));
        customer.setFirstName(rs.getString(DBConstants.CUSTOMER_FIRST_NAME));
        customer.setLastName(rs.getString(DBConstants.CUSTOMER_LAST_NAME));
        customer.setEmail(rs.getString(DBConstants.CUSTOMER_EMAIL));
        customer.setPhone(rs.getString(DBConstants.CUSTOMER_PHONE));
        customer.setAddress(rs.getString(DBConstants.CUSTOMER_ADDRESS));
        
        // Handle date of birth
        Date dateOfBirth = rs.getDate(DBConstants.CUSTOMER_DATE_OF_BIRTH);
        if (dateOfBirth != null) {
            customer.setDateOfBirth(dateOfBirth.toLocalDate());
        }
        
        // Handle status
        String statusStr = rs.getString(DBConstants.CUSTOMER_STATUS);
        customer.setStatus(CustomerStatus.valueOf(statusStr));
        
        // Handle timestamps
        customer.setCreatedAt(rs.getTimestamp(DBConstants.CUSTOMER_CREATED_AT).toLocalDateTime());
        customer.setUpdatedAt(rs.getTimestamp(DBConstants.CUSTOMER_UPDATED_AT).toLocalDateTime());
        
        return customer;
    }
}