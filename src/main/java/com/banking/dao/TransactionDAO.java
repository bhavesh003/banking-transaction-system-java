package com.banking.dao;

import com.banking.model.Transaction;
import com.banking.exception.BankingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Transaction operations.
 * 
 * This interface defines all database operations related to transactions
 * following the DAO pattern for clean separation of data access logic.
 * 
 * @author Banking System Team
 * @version 1.0
 */
public interface TransactionDAO {
    
    /**
     * Create a new transaction in the database.
     * 
     * @param transaction Transaction object to create
     * @return Created transaction with generated ID
     * @throws BankingException if transaction creation fails
     */
    Transaction create(Transaction transaction) throws BankingException;
    
    /**
     * Find a transaction by its unique ID.
     * 
     * @param transactionId Transaction ID to search for
     * @return Optional containing transaction if found, empty otherwise
     * @throws BankingException if database operation fails
     */
    Optional<Transaction> findById(Long transactionId) throws BankingException;
    
    /**
     * Find a transaction by its reference number.
     * 
     * @param transactionReference Transaction reference to search for
     * @return Optional containing transaction if found, empty otherwise
     * @throws BankingException if database operation fails
     */
    Optional<Transaction> findByReference(String transactionReference) throws BankingException;
    
    /**
     * Find all transactions for a specific account.
     * 
     * @param accountId Account ID to search for
     * @param offset Starting position for pagination
     * @param limit Maximum number of transactions to return
     * @return List of transactions for the account
     * @throws BankingException if database operation fails
     */
    List<Transaction> findByAccountId(Long accountId, int offset, int limit) throws BankingException;
    
    /**
     * Find transactions where account is the source (from_account_id).
     * 
     * @param fromAccountId Source account ID
     * @param offset Starting position for pagination
     * @param limit Maximum number of transactions to return
     * @return List of outgoing transactions
     * @throws BankingException if database operation fails
     */
    List<Transaction> findByFromAccountId(Long fromAccountId, int offset, int limit) throws BankingException;
    
    /**
     * Find transactions where account is the destination (to_account_id).
     * 
     * @param toAccountId Destination account ID
     * @param offset Starting position for pagination
     * @param limit Maximum number of transactions to return
     * @return List of incoming transactions
     * @throws BankingException if database operation fails
     */
    List<Transaction> findByToAccountId(Long toAccountId, int offset, int limit) throws BankingException;
    
    /**
     * Update an existing transaction's information.
     * 
     * @param transaction Transaction object with updated information
     * @return Updated transaction object
     * @throws BankingException if transaction update fails
     */
    Transaction update(Transaction transaction) throws BankingException;
    
    /**
     * Update transaction status.
     * 
     * @param transactionId Transaction ID to update
     * @param status New transaction status
     * @return true if update was successful, false otherwise
     * @throws BankingException if status update fails
     */
    boolean updateStatus(Long transactionId, String status) throws BankingException;
    
    /**
     * Complete a transaction by updating status and completion time.
     * 
     * @param transactionId Transaction ID to complete
     * @return true if completion was successful, false otherwise
     * @throws BankingException if completion fails
     */
    boolean completeTransaction(Long transactionId) throws BankingException;
    
    /**
     * Cancel a transaction by updating status.
     * 
     * @param transactionId Transaction ID to cancel
     * @return true if cancellation was successful, false otherwise
     * @throws BankingException if cancellation fails
     */
    boolean cancelTransaction(Long transactionId) throws BankingException;
    
    /**
     * Find all transactions with pagination support.
     * 
     * @param offset Starting position for pagination
     * @param limit Maximum number of transactions to return
     * @return List of transactions
     * @throws BankingException if database operation fails
     */
    List<Transaction> findAll(int offset, int limit) throws BankingException;
    
    /**
     * Find transactions by type.
     * 
     * @param transactionType Transaction type to filter by
     * @param offset Starting position for pagination
     * @param limit Maximum number of transactions to return
     * @return List of transactions with specified type
     * @throws BankingException if database operation fails
     */
    List<Transaction> findByType(String transactionType, int offset, int limit) throws BankingException;
    
    /**
     * Find transactions by status.
     * 
     * @param status Transaction status to filter by
     * @param offset Starting position for pagination
     * @param limit Maximum number of transactions to return
     * @return List of transactions with specified status
     * @throws BankingException if database operation fails
     */
    List<Transaction> findByStatus(String status, int offset, int limit) throws BankingException;
    
    /**
     * Find transactions within a date range.
     * 
     * @param startDate Start date for the range
     * @param endDate End date for the range
     * @param offset Starting position for pagination
     * @param limit Maximum number of transactions to return
     * @return List of transactions within the date range
     * @throws BankingException if database operation fails
     */
    List<Transaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, 
                                     int offset, int limit) throws BankingException;
    
    /**
     * Find transactions by amount range.
     * 
     * @param minAmount Minimum transaction amount
     * @param maxAmount Maximum transaction amount
     * @param offset Starting position for pagination
     * @param limit Maximum number of transactions to return
     * @return List of transactions within the amount range
     * @throws BankingException if database operation fails
     */
    List<Transaction> findByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, 
                                       int offset, int limit) throws BankingException;
    
    /**
     * Get account statement (all transactions for an account within date range).
     * 
     * @param accountId Account ID to get statement for
     * @param startDate Start date for the statement
     * @param endDate End date for the statement
     * @return List of transactions for the account statement
     * @throws BankingException if database operation fails
     */
    List<Transaction> getAccountStatement(Long accountId, LocalDateTime startDate, 
                                         LocalDateTime endDate) throws BankingException;
    
    /**
     * Check if a transaction reference already exists.
     * 
     * @param transactionReference Transaction reference to check
     * @return true if reference exists, false otherwise
     * @throws BankingException if database operation fails
     */
    boolean existsByReference(String transactionReference) throws BankingException;
    
    /**
     * Get total count of transactions.
     * 
     * @return Total number of transactions in the database
     * @throws BankingException if database operation fails
     */
    long getTotalCount() throws BankingException;
    
    /**
     * Get count of transactions by account.
     * 
     * @param accountId Account ID to count transactions for
     * @return Number of transactions for the account
     * @throws BankingException if database operation fails
     */
    long getCountByAccount(Long accountId) throws BankingException;
    
    /**
     * Get count of transactions by type.
     * 
     * @param transactionType Transaction type to count
     * @return Number of transactions with specified type
     * @throws BankingException if database operation fails
     */
    long getCountByType(String transactionType) throws BankingException;
    
    /**
     * Get count of transactions by status.
     * 
     * @param status Transaction status to count
     * @return Number of transactions with specified status
     * @throws BankingException if database operation fails
     */
    long getCountByStatus(String status) throws BankingException;
    
    /**
     * Get total transaction amount by account within date range.
     * 
     * @param accountId Account ID to calculate total for
     * @param startDate Start date for calculation
     * @param endDate End date for calculation
     * @return Total transaction amount for the account
     * @throws BankingException if database operation fails
     */
    BigDecimal getTotalAmountByAccount(Long accountId, LocalDateTime startDate, 
                                      LocalDateTime endDate) throws BankingException;
    
    /**
     * Generate next available transaction reference.
     * This method ensures unique transaction references.
     * 
     * @return Generated unique transaction reference
     * @throws BankingException if reference generation fails
     */
    String generateTransactionReference() throws BankingException;
}