package com.banking.dao.impl;

import com.banking.dao.AccountDAO;
import com.banking.model.*;
import com.banking.exception.BankingException;
import com.banking.util.DBConnection;
import com.banking.util.DBConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of AccountDAO interface using JDBC.
 * 
 * This class handles all database operations for accounts using
 * PreparedStatements for security and performance.
 * 
 * @author Banking System Team
 * @version 1.0
 */
public class AccountDAOImpl implements AccountDAO {
    
    // SQL Queries
    private static final String INSERT_ACCOUNT = 
        "INSERT INTO " + DBConstants.TABLE_ACCOUNTS + 
        " (account_number, customer_id, account_type, balance, status, " +
        "interest_rate, overdraft_limit, business_license) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM " + DBConstants.TABLE_ACCOUNTS + 
        " WHERE account_id = ?";
    
    private static final String SELECT_BY_ACCOUNT_NUMBER = 
        "SELECT * FROM " + DBConstants.TABLE_ACCOUNTS + 
        " WHERE account_number = ?";
    
    private static final String SELECT_BY_CUSTOMER_ID = 
        "SELECT * FROM " + DBConstants.TABLE_ACCOUNTS + 
        " WHERE customer_id = ? ORDER BY created_at DESC";
    
    private static final String UPDATE_ACCOUNT = 
        "UPDATE " + DBConstants.TABLE_ACCOUNTS + 
        " SET account_number = ?, customer_id = ?, account_type = ?, " +
        "balance = ?, status = ?, interest_rate = ?, overdraft_limit = ?, " +
        "business_license = ?, updated_at = CURRENT_TIMESTAMP " +
        "WHERE account_id = ?";
    
    private static final String UPDATE_BALANCE = 
        "UPDATE " + DBConstants.TABLE_ACCOUNTS + 
        " SET balance = ?, updated_at = CURRENT_TIMESTAMP " +
        "WHERE account_id = ?";
    
    private static final String SOFT_DELETE_ACCOUNT = 
        "UPDATE " + DBConstants.TABLE_ACCOUNTS + 
        " SET status = 'CLOSED', updated_at = CURRENT_TIMESTAMP " +
        "WHERE account_id = ?";
    
    private static final String SELECT_ALL_PAGINATED = 
        "SELECT * FROM " + DBConstants.TABLE_ACCOUNTS + 
        " ORDER BY created_at DESC LIMIT ?, ?";
    
    private static final String SELECT_BY_TYPE = 
        "SELECT * FROM " + DBConstants.TABLE_ACCOUNTS + 
        " WHERE account_type = ? ORDER BY created_at DESC LIMIT ?, ?";
    
    private static final String SELECT_BY_STATUS = 
        "SELECT * FROM " + DBConstants.TABLE_ACCOUNTS + 
        " WHERE status = ? ORDER BY created_at DESC LIMIT ?, ?";
    
    private static final String SELECT_BY_MIN_BALANCE = 
        "SELECT * FROM " + DBConstants.TABLE_ACCOUNTS + 
        " WHERE balance >= ? ORDER BY balance DESC LIMIT ?, ?";
    
    private static final String EXISTS_BY_ACCOUNT_NUMBER = 
        "SELECT COUNT(*) FROM " + DBConstants.TABLE_ACCOUNTS + 
        " WHERE account_number = ?";
    
    private static final String COUNT_ALL = 
        "SELECT COUNT(*) FROM " + DBConstants.TABLE_ACCOUNTS;
    
    private static final String COUNT_BY_CUSTOMER = 
        "SELECT COUNT(*) FROM " + DBConstants.TABLE_ACCOUNTS + 
        " WHERE customer_id = ?";
    
    private static final String COUNT_BY_TYPE = 
        "SELECT COUNT(*) FROM " + DBConstants.TABLE_ACCOUNTS + 
        " WHERE account_type = ?";
    
    private static final String TOTAL_BALANCE_BY_CUSTOMER = 
        "SELECT COALESCE(SUM(balance), 0) FROM " + DBConstants.TABLE_ACCOUNTS + 
        " WHERE customer_id = ? AND status = 'ACTIVE'";
    
    private static final String GENERATE_ACCOUNT_NUMBER = 
        "SELECT MAX(CAST(SUBSTRING(account_number, 4) AS UNSIGNED)) " +
        "FROM " + DBConstants.TABLE_ACCOUNTS + 
        " WHERE account_number LIKE ?";
    
    @Override
    public Account create(Account account) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(INSERT_ACCOUNT, Statement.RETURN_GENERATED_KEYS);
            
            // Set parameters
            stmt.setString(1, account.getAccountNumber());
            stmt.setLong(2, Long.parseLong(account.getCustomerId()));
            stmt.setString(3, account.getAccountType().name());
            stmt.setBigDecimal(4, account.getBalance());
            stmt.setString(5, account.getStatus().name());
            
            // Handle account type specific fields
            if (account instanceof SavingsAccount) {
                SavingsAccount savings = (SavingsAccount) account;
                stmt.setBigDecimal(6, savings.getInterestRate());
                stmt.setNull(7, java.sql.Types.DECIMAL);
                stmt.setNull(8, java.sql.Types.VARCHAR);
            } else if (account instanceof CheckingAccount) {
                CheckingAccount checking = (CheckingAccount) account;
                stmt.setNull(6, java.sql.Types.DECIMAL);
                stmt.setBigDecimal(7, checking.getOverdraftLimit());
                stmt.setNull(8, java.sql.Types.VARCHAR);
            } else if (account instanceof BusinessAccount) {
                BusinessAccount business = (BusinessAccount) account;
                stmt.setNull(6, java.sql.Types.DECIMAL);
                stmt.setNull(7, java.sql.Types.DECIMAL);
                stmt.setString(8, business.getTaxId()); // Using taxId as business license
            } else {
                stmt.setNull(6, java.sql.Types.DECIMAL);
                stmt.setNull(7, java.sql.Types.DECIMAL);
                stmt.setNull(8, java.sql.Types.VARCHAR);
            }
            
            // Execute insert
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new BankingException("Creating account failed, no rows affected");
            }
            
            // Get generated ID and set it (we'll need to add accountId field to Account model)
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                // Note: Account model doesn't have accountId field, but database does
                // This is a design consideration for future enhancement
            }
            
            return account;
            
        } catch (SQLException e) {
            throw new BankingException("Error creating account: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new BankingException("Invalid customer ID format: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public Optional<Account> findById(Long accountId) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SELECT_BY_ID);
            stmt.setLong(1, accountId);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToAccount(rs));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new BankingException("Error finding account by ID: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SELECT_BY_ACCOUNT_NUMBER);
            stmt.setString(1, accountNumber);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToAccount(rs));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new BankingException("Error finding account by number: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public List<Account> findByCustomerId(Long customerId) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SELECT_BY_CUSTOMER_ID);
            stmt.setLong(1, customerId);
            
            rs = stmt.executeQuery();
            
            List<Account> accounts = new ArrayList<>();
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
            
            return accounts;
            
        } catch (SQLException e) {
            throw new BankingException("Error finding accounts by customer ID: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public Account update(Account account) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(UPDATE_ACCOUNT);
            
            // Set parameters
            stmt.setString(1, account.getAccountNumber());
            stmt.setLong(2, Long.parseLong(account.getCustomerId()));
            stmt.setString(3, account.getAccountType().name());
            stmt.setBigDecimal(4, account.getBalance());
            stmt.setString(5, account.getStatus().name());
            
            // Handle account type specific fields
            if (account instanceof SavingsAccount) {
                SavingsAccount savings = (SavingsAccount) account;
                stmt.setBigDecimal(6, savings.getInterestRate());
                stmt.setNull(7, java.sql.Types.DECIMAL);
                stmt.setNull(8, java.sql.Types.VARCHAR);
            } else if (account instanceof CheckingAccount) {
                CheckingAccount checking = (CheckingAccount) account;
                stmt.setNull(6, java.sql.Types.DECIMAL);
                stmt.setBigDecimal(7, checking.getOverdraftLimit());
                stmt.setNull(8, java.sql.Types.VARCHAR);
            } else if (account instanceof BusinessAccount) {
                BusinessAccount business = (BusinessAccount) account;
                stmt.setNull(6, java.sql.Types.DECIMAL);
                stmt.setNull(7, java.sql.Types.DECIMAL);
                stmt.setString(8, business.getTaxId());
            } else {
                stmt.setNull(6, java.sql.Types.DECIMAL);
                stmt.setNull(7, java.sql.Types.DECIMAL);
                stmt.setNull(8, java.sql.Types.VARCHAR);
            }
            
            // Note: We need accountId to update, but Account model doesn't have it
            // This is a limitation that would need to be addressed in a real implementation
            // For now, we'll throw an exception
            throw new BankingException("Account update not supported - missing account ID in model");
            
        } catch (SQLException e) {
            throw new BankingException("Error updating account: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new BankingException("Invalid customer ID format: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, null);
        }
    }
    
    @Override
    public boolean updateBalance(Long accountId, BigDecimal newBalance) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(UPDATE_BALANCE);
            stmt.setBigDecimal(1, newBalance);
            stmt.setLong(2, accountId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new BankingException("Error updating account balance: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, null);
        }
    }
    
    @Override
    public boolean delete(Long accountId) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SOFT_DELETE_ACCOUNT);
            stmt.setLong(1, accountId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new BankingException("Error deleting account: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, null);
        }
    }
    
    @Override
    public List<Account> findAll(int offset, int limit) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SELECT_ALL_PAGINATED);
            stmt.setInt(1, offset);
            stmt.setInt(2, limit);
            
            rs = stmt.executeQuery();
            
            List<Account> accounts = new ArrayList<>();
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
            
            return accounts;
            
        } catch (SQLException e) {
            throw new BankingException("Error finding all accounts: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public List<Account> findByType(String accountType, int offset, int limit) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SELECT_BY_TYPE);
            stmt.setString(1, accountType);
            stmt.setInt(2, offset);
            stmt.setInt(3, limit);
            
            rs = stmt.executeQuery();
            
            List<Account> accounts = new ArrayList<>();
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
            
            return accounts;
            
        } catch (SQLException e) {
            throw new BankingException("Error finding accounts by type: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public List<Account> findByStatus(String status, int offset, int limit) throws BankingException {
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
            
            List<Account> accounts = new ArrayList<>();
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
            
            return accounts;
            
        } catch (SQLException e) {
            throw new BankingException("Error finding accounts by status: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public List<Account> findByMinBalance(BigDecimal minBalance, int offset, int limit) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(SELECT_BY_MIN_BALANCE);
            stmt.setBigDecimal(1, minBalance);
            stmt.setInt(2, offset);
            stmt.setInt(3, limit);
            
            rs = stmt.executeQuery();
            
            List<Account> accounts = new ArrayList<>();
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
            
            return accounts;
            
        } catch (SQLException e) {
            throw new BankingException("Error finding accounts by minimum balance: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public boolean existsByAccountNumber(String accountNumber) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(EXISTS_BY_ACCOUNT_NUMBER);
            stmt.setString(1, accountNumber);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
            return false;
            
        } catch (SQLException e) {
            throw new BankingException("Error checking if account exists: " + e.getMessage(), e);
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
            throw new BankingException("Error getting total account count: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public long getCountByCustomer(Long customerId) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(COUNT_BY_CUSTOMER);
            stmt.setLong(1, customerId);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            throw new BankingException("Error getting account count by customer: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public long getCountByType(String accountType) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(COUNT_BY_TYPE);
            stmt.setString(1, accountType);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            throw new BankingException("Error getting account count by type: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public BigDecimal getTotalBalanceByCustomer(Long customerId) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(TOTAL_BALANCE_BY_CUSTOMER);
            stmt.setLong(1, customerId);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            
            return BigDecimal.ZERO;
            
        } catch (SQLException e) {
            throw new BankingException("Error getting total balance by customer: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    @Override
    public String generateAccountNumber(String accountType) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Generate prefix based on account type
            String prefix;
            switch (accountType.toUpperCase()) {
                case "SAVINGS":
                    prefix = "SAV";
                    break;
                case "CHECKING":
                    prefix = "CHK";
                    break;
                case "BUSINESS":
                    prefix = "BUS";
                    break;
                default:
                    prefix = "ACC";
            }
            
            stmt = conn.prepareStatement(GENERATE_ACCOUNT_NUMBER);
            stmt.setString(1, prefix + "%");
            
            rs = stmt.executeQuery();
            
            long nextNumber = 1;
            if (rs.next()) {
                Long maxNumber = rs.getLong(1);
                if (maxNumber != null && maxNumber > 0) {
                    nextNumber = maxNumber + 1;
                }
            }
            
            return String.format("%s%06d", prefix, nextNumber);
            
        } catch (SQLException e) {
            throw new BankingException("Error generating account number: " + e.getMessage(), e);
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
    }
    
    /**
     * Map ResultSet to Account object.
     * Creates the appropriate Account subclass based on account type.
     * 
     * @param rs ResultSet containing account data
     * @return Account object (SavingsAccount, CheckingAccount, or BusinessAccount)
     * @throws SQLException if mapping fails
     */
    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        String accountNumber = rs.getString(DBConstants.ACCOUNT_NUMBER);
        String customerId = String.valueOf(rs.getLong(DBConstants.ACCOUNT_CUSTOMER_ID));
        BigDecimal balance = rs.getBigDecimal(DBConstants.ACCOUNT_BALANCE);
        String accountType = rs.getString(DBConstants.ACCOUNT_TYPE);
        String status = rs.getString(DBConstants.ACCOUNT_STATUS);
        
        Account account;
        
        // Create appropriate account type
        switch (AccountType.valueOf(accountType)) {
            case SAVINGS:
                account = new SavingsAccount(accountNumber, customerId, balance);
                BigDecimal interestRate = rs.getBigDecimal(DBConstants.ACCOUNT_INTEREST_RATE);
                if (interestRate != null) {
                    ((SavingsAccount) account).setInterestRate(interestRate);
                }
                break;
                
            case CHECKING:
                account = new CheckingAccount(accountNumber, customerId, balance);
                BigDecimal overdraftLimit = rs.getBigDecimal(DBConstants.ACCOUNT_OVERDRAFT_LIMIT);
                if (overdraftLimit != null) {
                    ((CheckingAccount) account).setOverdraftLimit(overdraftLimit);
                }
                break;
                
            case BUSINESS:
                String businessLicense = rs.getString(DBConstants.ACCOUNT_BUSINESS_LICENSE);
                account = new BusinessAccount(accountNumber, customerId, balance, 
                    "Business Account", businessLicense); // Using license as taxId
                break;
                
            default:
                throw new SQLException("Unknown account type: " + accountType);
        }
        
        // Set common fields
        account.setStatus(AccountStatus.valueOf(status));
        
        return account;
    }
}