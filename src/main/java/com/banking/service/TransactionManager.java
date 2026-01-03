package com.banking.service;

import com.banking.dao.AccountDAO;
import com.banking.dao.TransactionDAO;
import com.banking.dao.impl.AccountDAOImpl;
import com.banking.dao.impl.TransactionDAOImpl;
import com.banking.exception.BankingException;
import com.banking.exception.InsufficientFundsException;
import com.banking.model.*;
import com.banking.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Transaction Manager for handling complex banking transactions with ACID guarantees.
 * 
 * This class provides thread-safe transaction processing with proper locking,
 * rollback mechanisms, and concurrent operation handling.
 * 
 * Features:
 * - ACID transaction compliance
 * - Deadlock prevention through ordered locking
 * - Concurrent transaction handling
 * - Automatic rollback on failures
 * - Transaction status tracking
 * 
 * @author Banking System Team
 * @version 1.0
 */
public class TransactionManager {
    
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    
    // Account-level locks to prevent concurrent modifications
    private final ConcurrentHashMap<String, ReentrantLock> accountLocks = new ConcurrentHashMap<>();
    
    // SQL queries for atomic operations
    private static final String LOCK_ACCOUNT_FOR_UPDATE = 
        "SELECT account_id, balance, status FROM accounts WHERE account_number = ? FOR UPDATE";
    
    private static final String UPDATE_ACCOUNT_BALANCE_ATOMIC = 
        "UPDATE accounts SET balance = ?, updated_at = CURRENT_TIMESTAMP WHERE account_number = ? AND balance = ?";
    
    private static final String INSERT_TRANSACTION_WITH_STATUS = 
        "INSERT INTO transactions (transaction_reference, from_account_id, to_account_id, " +
        "transaction_type, amount, description, status, created_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
    
    private static final String UPDATE_TRANSACTION_STATUS = 
        "UPDATE transactions SET status = ?, completed_at = ? WHERE transaction_reference = ?";

    public TransactionManager() {
        this.accountDAO = new AccountDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
    }
    
    public TransactionManager(AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
    }

    /**
     * Execute a safe money transfer with full ACID compliance.
     * 
     * This method handles:
     * - Account locking to prevent concurrent modifications
     * - Balance validation with real-time checks
     * - Atomic balance updates
     * - Transaction logging with status tracking
     * - Automatic rollback on any failure
     * 
     * @param fromAccountNumber Source account
     * @param toAccountNumber Destination account
     * @param amount Transfer amount
     * @param description Transaction description
     * @return Completed transaction
     * @throws BankingException if transfer fails
     */
    public Transaction executeTransfer(String fromAccountNumber, String toAccountNumber, 
                                     BigDecimal amount, String description) throws BankingException {
        
        // Validate inputs
        validateTransferInputs(fromAccountNumber, toAccountNumber, amount);
        
        // Order account numbers to prevent deadlocks (always lock in same order)
        String firstAccount = fromAccountNumber.compareTo(toAccountNumber) < 0 ? fromAccountNumber : toAccountNumber;
        String secondAccount = fromAccountNumber.compareTo(toAccountNumber) < 0 ? toAccountNumber : fromAccountNumber;
        
        // Acquire locks in ordered fashion to prevent deadlocks
        ReentrantLock firstLock = getAccountLock(firstAccount);
        ReentrantLock secondLock = getAccountLock(secondAccount);
        
        firstLock.lock();
        try {
            secondLock.lock();
            try {
                return executeTransferWithLocks(fromAccountNumber, toAccountNumber, amount, description);
            } finally {
                secondLock.unlock();
            }
        } finally {
            firstLock.unlock();
        }
    }

    /**
     * Execute a safe deposit with ACID compliance.
     * 
     * @param accountNumber Account to deposit to
     * @param amount Deposit amount
     * @param description Transaction description
     * @return Completed transaction
     * @throws BankingException if deposit fails
     */
    public Transaction executeDeposit(String accountNumber, BigDecimal amount, String description) 
            throws BankingException {
        
        validateDepositInputs(accountNumber, amount);
        
        ReentrantLock accountLock = getAccountLock(accountNumber);
        accountLock.lock();
        
        try {
            return executeDepositWithLock(accountNumber, amount, description);
        } finally {
            accountLock.unlock();
        }
    }

    /**
     * Execute a safe withdrawal with ACID compliance.
     * 
     * @param accountNumber Account to withdraw from
     * @param amount Withdrawal amount
     * @param description Transaction description
     * @return Completed transaction
     * @throws BankingException if withdrawal fails
     */
    public Transaction executeWithdrawal(String accountNumber, BigDecimal amount, String description) 
            throws BankingException {
        
        validateWithdrawalInputs(accountNumber, amount);
        
        ReentrantLock accountLock = getAccountLock(accountNumber);
        accountLock.lock();
        
        try {
            return executeWithdrawalWithLock(accountNumber, amount, description);
        } finally {
            accountLock.unlock();
        }
    }

    /**
     * Internal transfer execution with database locks.
     */
    private Transaction executeTransferWithLocks(String fromAccountNumber, String toAccountNumber, 
                                               BigDecimal amount, String description) throws BankingException {
        
        Connection conn = null;
        String transactionRef = null;
        
        try {
            // Get database connection and begin transaction
            conn = DBConnection.getConnection();
            DBConnection.beginTransaction(conn);
            
            // Generate unique transaction reference
            transactionRef = transactionDAO.generateTransactionReference();
            
            // Lock and validate both accounts
            AccountInfo fromAccount = lockAndValidateAccount(conn, fromAccountNumber, true);
            AccountInfo toAccount = lockAndValidateAccount(conn, toAccountNumber, false);
            
            // Validate transfer is possible
            validateTransferPossible(fromAccount, amount);
            
            // Create transaction record as PENDING
            Long transactionId = createPendingTransaction(conn, transactionRef, fromAccount.accountId, 
                                                        toAccount.accountId, TransactionType.TRANSFER, 
                                                        amount, description);
            
            // Calculate new balances
            BigDecimal fromNewBalance = fromAccount.balance.subtract(amount);
            BigDecimal toNewBalance = toAccount.balance.add(amount);
            
            // Update balances atomically
            updateAccountBalanceAtomic(conn, fromAccountNumber, fromAccount.balance, fromNewBalance);
            updateAccountBalanceAtomic(conn, toAccountNumber, toAccount.balance, toNewBalance);
            
            // Mark transaction as completed
            completeTransaction(conn, transactionRef);
            
            // Commit the entire transaction
            DBConnection.commitTransaction(conn);
            
            // Create and return transaction object
            Transaction transaction = new Transaction(transactionRef, fromAccountNumber, toAccountNumber,
                                                    TransactionType.TRANSFER, amount, description);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setBalanceAfter(fromNewBalance);
            
            return transaction;
            
        } catch (Exception e) {
            // Rollback on any error
            DBConnection.rollbackTransaction(conn);
            
            // Mark transaction as failed if it was created
            if (transactionRef != null) {
                try {
                    markTransactionFailed(transactionRef, e.getMessage());
                } catch (Exception rollbackError) {
                    // Log rollback error but don't mask original exception
                    System.err.println("Error marking transaction as failed: " + rollbackError.getMessage());
                }
            }
            
            throw new BankingException("Transfer failed: " + e.getMessage(), e);
            
        } finally {
            DBConnection.releaseConnection(conn);
        }
    }

    /**
     * Internal deposit execution with database locks.
     */
    private Transaction executeDepositWithLock(String accountNumber, BigDecimal amount, String description) 
            throws BankingException {
        
        Connection conn = null;
        String transactionRef = null;
        
        try {
            conn = DBConnection.getConnection();
            DBConnection.beginTransaction(conn);
            
            transactionRef = transactionDAO.generateTransactionReference();
            
            // Lock and validate account
            AccountInfo account = lockAndValidateAccount(conn, accountNumber, false);
            
            // Create transaction record as PENDING
            Long transactionId = createPendingTransaction(conn, transactionRef, null, account.accountId, 
                                                        TransactionType.DEPOSIT, amount, description);
            
            // Calculate new balance
            BigDecimal newBalance = account.balance.add(amount);
            
            // Update balance atomically
            updateAccountBalanceAtomic(conn, accountNumber, account.balance, newBalance);
            
            // Mark transaction as completed
            completeTransaction(conn, transactionRef);
            
            // Commit transaction
            DBConnection.commitTransaction(conn);
            
            // Create and return transaction object
            Transaction transaction = new Transaction(transactionRef, null, accountNumber,
                                                    TransactionType.DEPOSIT, amount, description);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setBalanceAfter(newBalance);
            
            return transaction;
            
        } catch (Exception e) {
            DBConnection.rollbackTransaction(conn);
            
            if (transactionRef != null) {
                try {
                    markTransactionFailed(transactionRef, e.getMessage());
                } catch (Exception rollbackError) {
                    System.err.println("Error marking transaction as failed: " + rollbackError.getMessage());
                }
            }
            
            throw new BankingException("Deposit failed: " + e.getMessage(), e);
            
        } finally {
            DBConnection.releaseConnection(conn);
        }
    }

    /**
     * Internal withdrawal execution with database locks.
     */
    private Transaction executeWithdrawalWithLock(String accountNumber, BigDecimal amount, String description) 
            throws BankingException {
        
        Connection conn = null;
        String transactionRef = null;
        
        try {
            conn = DBConnection.getConnection();
            DBConnection.beginTransaction(conn);
            
            transactionRef = transactionDAO.generateTransactionReference();
            
            // Lock and validate account
            AccountInfo account = lockAndValidateAccount(conn, accountNumber, true);
            
            // Validate withdrawal is possible
            validateWithdrawalPossible(account, amount);
            
            // Create transaction record as PENDING
            Long transactionId = createPendingTransaction(conn, transactionRef, account.accountId, null, 
                                                        TransactionType.WITHDRAWAL, amount, description);
            
            // Calculate new balance
            BigDecimal newBalance = account.balance.subtract(amount);
            
            // Update balance atomically
            updateAccountBalanceAtomic(conn, accountNumber, account.balance, newBalance);
            
            // Mark transaction as completed
            completeTransaction(conn, transactionRef);
            
            // Commit transaction
            DBConnection.commitTransaction(conn);
            
            // Create and return transaction object
            Transaction transaction = new Transaction(transactionRef, accountNumber, null,
                                                    TransactionType.WITHDRAWAL, amount, description);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setBalanceAfter(newBalance);
            
            return transaction;
            
        } catch (Exception e) {
            DBConnection.rollbackTransaction(conn);
            
            if (transactionRef != null) {
                try {
                    markTransactionFailed(transactionRef, e.getMessage());
                } catch (Exception rollbackError) {
                    System.err.println("Error marking transaction as failed: " + rollbackError.getMessage());
                }
            }
            
            throw new BankingException("Withdrawal failed: " + e.getMessage(), e);
            
        } finally {
            DBConnection.releaseConnection(conn);
        }
    }

    // Helper methods

    /**
     * Get or create account lock for thread safety.
     */
    private ReentrantLock getAccountLock(String accountNumber) {
        return accountLocks.computeIfAbsent(accountNumber, k -> new ReentrantLock());
    }

    /**
     * Lock account and get current information.
     */
    private AccountInfo lockAndValidateAccount(Connection conn, String accountNumber, boolean isSource) 
            throws SQLException, BankingException {
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.prepareStatement(LOCK_ACCOUNT_FOR_UPDATE);
            stmt.setString(1, accountNumber);
            rs = stmt.executeQuery();
            
            if (!rs.next()) {
                throw new BankingException("Account not found: " + accountNumber);
            }
            
            Long accountId = rs.getLong("account_id");
            BigDecimal balance = rs.getBigDecimal("balance");
            String status = rs.getString("status");
            
            // Validate account status
            if (!"ACTIVE".equals(status)) {
                throw new BankingException("Account is not active: " + accountNumber + " (Status: " + status + ")");
            }
            
            return new AccountInfo(accountId, balance, status);
            
        } finally {
            DBConnection.closeQuietly(rs);
            DBConnection.closeQuietly(stmt);
        }
    }

    /**
     * Update account balance atomically with optimistic locking.
     */
    private void updateAccountBalanceAtomic(Connection conn, String accountNumber, 
                                          BigDecimal expectedBalance, BigDecimal newBalance) 
            throws SQLException, BankingException {
        
        PreparedStatement stmt = null;
        
        try {
            stmt = conn.prepareStatement(UPDATE_ACCOUNT_BALANCE_ATOMIC);
            stmt.setBigDecimal(1, newBalance);
            stmt.setString(2, accountNumber);
            stmt.setBigDecimal(3, expectedBalance);
            
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new BankingException("Account balance was modified by another transaction");
            }
            
        } finally {
            DBConnection.closeQuietly(stmt);
        }
    }

    /**
     * Create pending transaction record.
     */
    private Long createPendingTransaction(Connection conn, String transactionRef, Long fromAccountId, 
                                        Long toAccountId, TransactionType type, BigDecimal amount, 
                                        String description) throws SQLException {
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.prepareStatement(INSERT_TRANSACTION_WITH_STATUS, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, transactionRef);
            
            if (fromAccountId != null) {
                stmt.setLong(2, fromAccountId);
            } else {
                stmt.setNull(2, java.sql.Types.BIGINT);
            }
            
            if (toAccountId != null) {
                stmt.setLong(3, toAccountId);
            } else {
                stmt.setNull(3, java.sql.Types.BIGINT);
            }
            
            stmt.setString(4, type.name());
            stmt.setBigDecimal(5, amount);
            stmt.setString(6, description);
            stmt.setString(7, TransactionStatus.PENDING.name());
            
            stmt.executeUpdate();
            
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            
            throw new SQLException("Failed to get generated transaction ID");
            
        } finally {
            DBConnection.closeQuietly(rs);
            DBConnection.closeQuietly(stmt);
        }
    }

    /**
     * Mark transaction as completed.
     */
    private void completeTransaction(Connection conn, String transactionRef) throws SQLException {
        PreparedStatement stmt = null;
        
        try {
            stmt = conn.prepareStatement(UPDATE_TRANSACTION_STATUS);
            stmt.setString(1, TransactionStatus.COMPLETED.name());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3, transactionRef);
            
            stmt.executeUpdate();
            
        } finally {
            DBConnection.closeQuietly(stmt);
        }
    }

    /**
     * Mark transaction as failed.
     */
    private void markTransactionFailed(String transactionRef, String errorMessage) throws BankingException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(UPDATE_TRANSACTION_STATUS);
            stmt.setString(1, TransactionStatus.FAILED.name());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3, transactionRef);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new BankingException("Error marking transaction as failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(stmt);
            DBConnection.releaseConnection(conn);
        }
    }

    // Validation methods

    private void validateTransferInputs(String fromAccount, String toAccount, BigDecimal amount) 
            throws BankingException {
        if (fromAccount == null || fromAccount.trim().isEmpty()) {
            throw new BankingException("Source account number is required");
        }
        if (toAccount == null || toAccount.trim().isEmpty()) {
            throw new BankingException("Destination account number is required");
        }
        if (fromAccount.equals(toAccount)) {
            throw new BankingException("Cannot transfer to the same account");
        }
        validateAmount(amount);
    }

    private void validateDepositInputs(String accountNumber, BigDecimal amount) throws BankingException {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new BankingException("Account number is required");
        }
        validateAmount(amount);
    }

    private void validateWithdrawalInputs(String accountNumber, BigDecimal amount) throws BankingException {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new BankingException("Account number is required");
        }
        validateAmount(amount);
    }

    private void validateAmount(BigDecimal amount) throws BankingException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Amount must be greater than zero");
        }
        
        BigDecimal maxAmount = new BigDecimal("100000.00");
        if (amount.compareTo(maxAmount) > 0) {
            throw new BankingException("Amount exceeds maximum transaction limit");
        }
    }

    private void validateTransferPossible(AccountInfo fromAccount, BigDecimal amount) throws BankingException {
        if (fromAccount.balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Available: " + fromAccount.balance + 
                                               ", Required: " + amount);
        }
    }

    private void validateWithdrawalPossible(AccountInfo account, BigDecimal amount) throws BankingException {
        if (account.balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Available: " + account.balance + 
                                               ", Required: " + amount);
        }
    }

    /**
     * Helper class to hold account information.
     */
    private static class AccountInfo {
        final Long accountId;
        final BigDecimal balance;
        final String status;
        
        AccountInfo(Long accountId, BigDecimal balance, String status) {
            this.accountId = accountId;
            this.balance = balance;
            this.status = status;
        }
    }
}