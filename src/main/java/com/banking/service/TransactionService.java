package com.banking.service;

import com.banking.dao.AccountDAO;
import com.banking.dao.TransactionDAO;
import com.banking.dao.impl.AccountDAOImpl;
import com.banking.dao.impl.TransactionDAOImpl;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.BankingException;
import com.banking.exception.InsufficientFundsException;
import com.banking.model.*;
import com.banking.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Transaction operations.
 * 
 * This class contains business logic for transaction processing including
 * deposits, withdrawals, transfers, and transaction history management.
 * 
 * @author Banking System Team
 * @version 1.0
 */
public class TransactionService {
    private final TransactionDAO transactionDAO;
    private final AccountDAO accountDAO;
    private final AccountService accountService;
    private final TransactionManager transactionManager;

    public TransactionService() {
        this.transactionDAO = new TransactionDAOImpl();
        this.accountDAO = new AccountDAOImpl();
        this.accountService = new AccountService();
        this.transactionManager = new TransactionManager(accountDAO, transactionDAO);
    }
    
    // Constructor for dependency injection (testing)
    public TransactionService(TransactionDAO transactionDAO, AccountDAO accountDAO, 
                            AccountService accountService, TransactionManager transactionManager) {
        this.transactionDAO = transactionDAO;
        this.accountDAO = accountDAO;
        this.accountService = accountService;
        this.transactionManager = transactionManager;
    }

    /**
     * Process a deposit transaction using safe transaction management.
     * 
     * @param accountNumber Account to deposit to
     * @param amount Amount to deposit
     * @param description Transaction description
     * @return Completed transaction
     * @throws BankingException if deposit fails
     */
    public Transaction deposit(String accountNumber, BigDecimal amount, String description) 
            throws BankingException {
        // Use TransactionManager for safe execution
        return transactionManager.executeDeposit(accountNumber, amount, description);
    }

    /**
     * Process a withdrawal transaction using safe transaction management.
     * 
     * @param accountNumber Account to withdraw from
     * @param amount Amount to withdraw
     * @param description Transaction description
     * @return Completed transaction
     * @throws BankingException if withdrawal fails
     */
    public Transaction withdraw(String accountNumber, BigDecimal amount, String description) 
            throws BankingException {
        // Use TransactionManager for safe execution
        return transactionManager.executeWithdrawal(accountNumber, amount, description);
    }

    /**
     * Process a transfer transaction using safe transaction management.
     * 
     * @param fromAccountNumber Source account
     * @param toAccountNumber Destination account
     * @param amount Amount to transfer
     * @param description Transaction description
     * @return Completed transaction
     * @throws BankingException if transfer fails
     */
    public Transaction transfer(String fromAccountNumber, String toAccountNumber, 
                              BigDecimal amount, String description) throws BankingException {
        // Use TransactionManager for safe execution
        return transactionManager.executeTransfer(fromAccountNumber, toAccountNumber, amount, description);
    }

    /**
     * Get transaction by reference number.
     * 
     * @param transactionReference Transaction reference to search for
     * @return Transaction if found
     * @throws BankingException if transaction not found
     */
    public Transaction getTransaction(String transactionReference) throws BankingException {
        Optional<Transaction> transaction = transactionDAO.findByReference(transactionReference);
        return transaction.orElseThrow(() -> 
            new BankingException("Transaction not found: " + transactionReference));
    }

    /**
     * Get transaction history for an account.
     * 
     * @param accountNumber Account number
     * @param offset Starting position for pagination
     * @param limit Maximum number of transactions to return
     * @return List of transactions
     * @throws BankingException if retrieval fails
     */
    public List<Transaction> getTransactionHistory(String accountNumber, int offset, int limit) 
            throws BankingException {
        // Validate account exists
        accountService.getAccount(accountNumber);
        
        Long accountId = getAccountIdByNumber(accountNumber);
        return transactionDAO.findByAccountId(accountId, offset, limit);
    }

    /**
     * Get account statement for a date range.
     * 
     * @param accountNumber Account number
     * @param startDate Start date for statement
     * @param endDate End date for statement
     * @return List of transactions in the date range
     * @throws BankingException if retrieval fails
     */
    public List<Transaction> getAccountStatement(String accountNumber, 
                                               LocalDateTime startDate, LocalDateTime endDate) 
            throws BankingException {
        // Validate account exists
        accountService.getAccount(accountNumber);
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new BankingException("Start date cannot be after end date");
        }
        
        Long accountId = getAccountIdByNumber(accountNumber);
        return transactionDAO.getAccountStatement(accountId, startDate, endDate);
    }

    /**
     * Get all transactions with pagination.
     * 
     * @param offset Starting position
     * @param limit Maximum number of transactions
     * @return List of transactions
     * @throws BankingException if retrieval fails
     */
    public List<Transaction> getAllTransactions(int offset, int limit) throws BankingException {
        return transactionDAO.findAll(offset, limit);
    }

    /**
     * Get transactions by type.
     * 
     * @param transactionType Transaction type to filter by
     * @param offset Starting position
     * @param limit Maximum number of transactions
     * @return List of transactions
     * @throws BankingException if retrieval fails
     */
    public List<Transaction> getTransactionsByType(TransactionType transactionType, 
                                                 int offset, int limit) throws BankingException {
        return transactionDAO.findByType(transactionType.name(), offset, limit);
    }

    /**
     * Get transactions by status.
     * 
     * @param status Transaction status to filter by
     * @param offset Starting position
     * @param limit Maximum number of transactions
     * @return List of transactions
     * @throws BankingException if retrieval fails
     */
    public List<Transaction> getTransactionsByStatus(TransactionStatus status, 
                                                   int offset, int limit) throws BankingException {
        return transactionDAO.findByStatus(status.name(), offset, limit);
    }

    /**
     * Get transaction count for an account.
     * 
     * @param accountNumber Account number
     * @return Number of transactions for the account
     * @throws BankingException if count fails
     */
    public long getTransactionCount(String accountNumber) throws BankingException {
        // Validate account exists
        accountService.getAccount(accountNumber);
        
        Long accountId = getAccountIdByNumber(accountNumber);
        return transactionDAO.getCountByAccount(accountId);
    }

    /**
     * Cancel a pending transaction.
     * 
     * @param transactionReference Transaction reference to cancel
     * @return true if cancellation was successful
     * @throws BankingException if cancellation fails
     */
    public boolean cancelTransaction(String transactionReference) throws BankingException {
        Transaction transaction = getTransaction(transactionReference);
        
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new BankingException("Only pending transactions can be cancelled");
        }
        
        // Note: This would require the transaction database ID
        // For now, we'll throw an exception indicating the limitation
        throw new BankingException("Transaction cancellation not supported - missing transaction database ID");
    }

    // Private helper methods

    /**
     * Validate transaction amount.
     */
    private void validateAmount(BigDecimal amount) throws BankingException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Transaction amount must be greater than zero");
        }
        
        // Set reasonable maximum transaction limit
        BigDecimal maxTransactionAmount = new BigDecimal("100000.00");
        if (amount.compareTo(maxTransactionAmount) > 0) {
            throw new BankingException("Transaction amount exceeds maximum limit of " + maxTransactionAmount);
        }
    }

    /**
     * Validate account number format.
     */
    private void validateAccountNumber(String accountNumber) throws BankingException {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new BankingException("Account number is required");
        }
        
        if (accountNumber.length() < 6) {
            throw new BankingException("Invalid account number format");
        }
    }

    /**
     * Validate account is active for transactions.
     */
    private void validateAccountActive(Account account) throws BankingException {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BankingException("Account is not active: " + account.getStatus());
        }
    }

    /**
     * Helper method to get account ID by account number.
     * Note: This is a workaround for the model limitation.
     */
    private Long getAccountIdByNumber(String accountNumber) throws BankingException {
        try {
            Optional<Account> account = accountDAO.findByAccountNumber(accountNumber);
            if (!account.isPresent()) {
                throw new BankingException("Account not found: " + accountNumber);
            }
            
            // This is a limitation - we need to query the database directly
            // In a real implementation, the Account model should have an ID field
            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                var stmt = conn.prepareStatement("SELECT account_id FROM accounts WHERE account_number = ?");
                stmt.setString(1, accountNumber);
                var rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getLong(1);
                }
                
                throw new BankingException("Account ID not found for: " + accountNumber);
                
            } finally {
                DBConnection.releaseConnection(conn);
            }
            
        } catch (SQLException e) {
            throw new BankingException("Error getting account ID: " + e.getMessage(), e);
        }
    }
}