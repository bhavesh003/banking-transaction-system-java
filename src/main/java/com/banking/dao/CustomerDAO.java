package com.banking.dao;

import com.banking.model.Customer;
import com.banking.exception.BankingException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Customer operations.
 * 
 * This interface defines all database operations related to customers
 * following the DAO pattern for clean separation of data access logic.
 * 
 * @author Banking System Team
 * @version 1.0
 */
public interface CustomerDAO {
    
    /**
     * Create a new customer in the database.
     * 
     * @param customer Customer object to create
     * @return Created customer with generated ID
     * @throws BankingException if customer creation fails
     */
    Customer create(Customer customer) throws BankingException;
    
    /**
     * Find a customer by their unique ID.
     * 
     * @param customerId Customer ID to search for
     * @return Optional containing customer if found, empty otherwise
     * @throws BankingException if database operation fails
     */
    Optional<Customer> findById(Long customerId) throws BankingException;
    
    /**
     * Find a customer by their email address.
     * 
     * @param email Email address to search for
     * @return Optional containing customer if found, empty otherwise
     * @throws BankingException if database operation fails
     */
    Optional<Customer> findByEmail(String email) throws BankingException;
    
    /**
     * Update an existing customer's information.
     * 
     * @param customer Customer object with updated information
     * @return Updated customer object
     * @throws BankingException if customer update fails
     */
    Customer update(Customer customer) throws BankingException;
    
    /**
     * Delete a customer by their ID.
     * Note: This is a soft delete that changes status to CLOSED.
     * 
     * @param customerId Customer ID to delete
     * @return true if deletion was successful, false otherwise
     * @throws BankingException if deletion fails
     */
    boolean delete(Long customerId) throws BankingException;
    
    /**
     * Find all customers with pagination support.
     * 
     * @param offset Starting position for pagination
     * @param limit Maximum number of customers to return
     * @return List of customers
     * @throws BankingException if database operation fails
     */
    List<Customer> findAll(int offset, int limit) throws BankingException;
    
    /**
     * Find customers by status.
     * 
     * @param status Customer status to filter by
     * @param offset Starting position for pagination
     * @param limit Maximum number of customers to return
     * @return List of customers with specified status
     * @throws BankingException if database operation fails
     */
    List<Customer> findByStatus(String status, int offset, int limit) throws BankingException;
    
    /**
     * Search customers by name (first name or last name).
     * 
     * @param searchTerm Search term to match against names
     * @param offset Starting position for pagination
     * @param limit Maximum number of customers to return
     * @return List of customers matching the search term
     * @throws BankingException if database operation fails
     */
    List<Customer> searchByName(String searchTerm, int offset, int limit) throws BankingException;
    
    /**
     * Check if a customer exists by email.
     * 
     * @param email Email address to check
     * @return true if customer exists, false otherwise
     * @throws BankingException if database operation fails
     */
    boolean existsByEmail(String email) throws BankingException;
    
    /**
     * Get total count of customers.
     * 
     * @return Total number of customers in the database
     * @throws BankingException if database operation fails
     */
    long getTotalCount() throws BankingException;
    
    /**
     * Get count of customers by status.
     * 
     * @param status Customer status to count
     * @return Number of customers with specified status
     * @throws BankingException if database operation fails
     */
    long getCountByStatus(String status) throws BankingException;
}