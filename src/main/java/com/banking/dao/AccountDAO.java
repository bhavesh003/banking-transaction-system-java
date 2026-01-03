package com.banking.dao;

import com.banking.model.Account;
import com.banking.exception.BankingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Account operations.
 * 
 * This interface defines all database operations related to accounts
 * following the DAO pattern for clean separation of data access logic.
 * 
 * @author Banking System Team
 * @version 1.0
 */
public interface AccountDAO {
    
    /**
     * Create a new account in the database.
     * 
     * @param account Account object to create
     * @return Created account with generated ID
     * @throws BankingException if account creation fails
     */
    Account create(Account account) throws BankingException;
    
    /**
     * Find an account by its unique ID.
     * 
     * @param accountId Account ID to search for
     * @return Optional containing account if found, empty otherwise
     * @throws BankingException if database operation fails
     */
    Optional<Account> findById(Long accountId) throws BankingException;
    
    /**
     * Find an account by its account number.
     * 
     * @param accountNumber Account number to search for
     * @return Optional containing account if found, empty otherwise
     * @throws BankingException if database operation fails
     */
    Optional<Account> findByAccountNumber(String accountNumber) throws BankingException;
    
    /**
     * Find all accounts belonging to a specific customer.
     * 
     * @param customerId Customer ID to search for
     * @return List of accounts belonging to the customer
     * @throws BankingException if database operation fails
     */
    List<Account> findByCustomerId(Long customerId) throws BankingException;
    
    /**
     * Update an existing account's information.
     * 
     * @param account Account object with updated information
     * @return Updated account object
     * @throws BankingException if account update fails
     */
    Account update(Account account) throws BankingException;
    
    /**
     * Update account balance atomically.
     * This method is used for transaction processing.
     * 
     * @param accountId Account ID to update
     * @param newBalance New balance amount
     * @return true if update was successful, false otherwise
     * @throws BankingException if balance update fails
     */
    boolean updateBalance(Long accountId, BigDecimal newBalance) throws BankingException;
    
    /**
     * Delete an account by its ID.
     * Note: This is a soft delete that changes status to CLOSED.
     * 
     * @param accountId Account ID to delete
     * @return true if deletion was successful, false otherwise
     * @throws BankingException if deletion fails
     */
    boolean delete(Long accountId) throws BankingException;
    
    /**
     * Find all accounts with pagination support.
     * 
     * @param offset Starting position for pagination
     * @param limit Maximum number of accounts to return
     * @return List of accounts
     * @throws BankingException if database operation fails
     */
    List<Account> findAll(int offset, int limit) throws BankingException;
    
    /**
     * Find accounts by type.
     * 
     * @param accountType Account type to filter by
     * @param offset Starting position for pagination
     * @param limit Maximum number of accounts to return
     * @return List of accounts with specified type
     * @throws BankingException if database operation fails
     */
    List<Account> findByType(String accountType, int offset, int limit) throws BankingException;
    
    /**
     * Find accounts by status.
     * 
     * @param status Account status to filter by
     * @param offset Starting position for pagination
     * @param limit Maximum number of accounts to return
     * @return List of accounts with specified status
     * @throws BankingException if database operation fails
     */
    List<Account> findByStatus(String status, int offset, int limit) throws BankingException;
    
    /**
     * Find accounts with balance greater than specified amount.
     * 
     * @param minBalance Minimum balance threshold
     * @param offset Starting position for pagination
     * @param limit Maximum number of accounts to return
     * @return List of accounts with balance >= minBalance
     * @throws BankingException if database operation fails
     */
    List<Account> findByMinBalance(BigDecimal minBalance, int offset, int limit) throws BankingException;
    
    /**
     * Check if an account number already exists.
     * 
     * @param accountNumber Account number to check
     * @return true if account number exists, false otherwise
     * @throws BankingException if database operation fails
     */
    boolean existsByAccountNumber(String accountNumber) throws BankingException;
    
    /**
     * Get total count of accounts.
     * 
     * @return Total number of accounts in the database
     * @throws BankingException if database operation fails
     */
    long getTotalCount() throws BankingException;
    
    /**
     * Get count of accounts by customer.
     * 
     * @param customerId Customer ID to count accounts for
     * @return Number of accounts belonging to the customer
     * @throws BankingException if database operation fails
     */
    long getCountByCustomer(Long customerId) throws BankingException;
    
    /**
     * Get count of accounts by type.
     * 
     * @param accountType Account type to count
     * @return Number of accounts with specified type
     * @throws BankingException if database operation fails
     */
    long getCountByType(String accountType) throws BankingException;
    
    /**
     * Get total balance across all accounts for a customer.
     * 
     * @param customerId Customer ID to calculate total balance for
     * @return Total balance across all customer's accounts
     * @throws BankingException if database operation fails
     */
    BigDecimal getTotalBalanceByCustomer(Long customerId) throws BankingException;
    
    /**
     * Generate next available account number.
     * This method ensures unique account numbers.
     * 
     * @param accountType Account type for number generation
     * @return Generated unique account number
     * @throws BankingException if number generation fails
     */
    String generateAccountNumber(String accountType) throws BankingException;
}