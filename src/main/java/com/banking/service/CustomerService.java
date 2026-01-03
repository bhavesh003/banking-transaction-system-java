package com.banking.service;

import com.banking.dao.CustomerDAO;
import com.banking.dao.impl.CustomerDAOImpl;
import com.banking.exception.BankingException;
import com.banking.model.Customer;
import com.banking.model.CustomerStatus;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service class for Customer operations.
 * 
 * This class contains business logic for customer management including
 * customer creation, validation, and profile management.
 * 
 * @author Banking System Team
 * @version 1.0
 */
public class CustomerService {
    private final CustomerDAO customerDAO;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );
    
    // Phone validation pattern (basic US format)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?1?[-.\\s]?\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$"
    );

    public CustomerService() {
        this.customerDAO = new CustomerDAOImpl();
    }
    
    // Constructor for dependency injection (testing)
    public CustomerService(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    /**
     * Create a new customer.
     * 
     * @param firstName Customer's first name
     * @param lastName Customer's last name
     * @param email Customer's email address
     * @param phone Customer's phone number
     * @param address Customer's address
     * @param dateOfBirth Customer's date of birth
     * @return Created customer
     * @throws BankingException if customer creation fails
     */
    public Customer createCustomer(String firstName, String lastName, String email, 
                                 String phone, String address, LocalDate dateOfBirth) 
            throws BankingException {
        // Validate input data
        validateCustomerData(firstName, lastName, email, phone, dateOfBirth);
        
        // Check if email already exists
        if (customerDAO.existsByEmail(email)) {
            throw new BankingException("Customer with email already exists: " + email);
        }
        
        // Create customer object
        Customer customer = new Customer();
        customer.setFirstName(firstName.trim());
        customer.setLastName(lastName.trim());
        customer.setEmail(email.toLowerCase().trim());
        customer.setPhone(phone != null ? phone.trim() : null);
        customer.setAddress(address != null ? address.trim() : null);
        customer.setDateOfBirth(dateOfBirth);
        customer.setStatus(CustomerStatus.ACTIVE);
        
        return customerDAO.create(customer);
    }

    /**
     * Get customer by ID.
     * 
     * @param customerId Customer ID to search for
     * @return Customer if found
     * @throws BankingException if customer not found
     */
    public Customer getCustomer(Long customerId) throws BankingException {
        Optional<Customer> customer = customerDAO.findById(customerId);
        return customer.orElseThrow(() -> 
            new BankingException("Customer not found with ID: " + customerId));
    }

    /**
     * Get customer by email.
     * 
     * @param email Email address to search for
     * @return Customer if found
     * @throws BankingException if customer not found
     */
    public Customer getCustomerByEmail(String email) throws BankingException {
        if (email == null || email.trim().isEmpty()) {
            throw new BankingException("Email is required");
        }
        
        Optional<Customer> customer = customerDAO.findByEmail(email.toLowerCase().trim());
        return customer.orElseThrow(() -> 
            new BankingException("Customer not found with email: " + email));
    }

    /**
     * Update customer information.
     * 
     * @param customerId Customer ID to update
     * @param firstName Updated first name
     * @param lastName Updated last name
     * @param email Updated email address
     * @param phone Updated phone number
     * @param address Updated address
     * @param dateOfBirth Updated date of birth
     * @return Updated customer
     * @throws BankingException if update fails
     */
    public Customer updateCustomer(Long customerId, String firstName, String lastName, 
                                 String email, String phone, String address, LocalDate dateOfBirth) 
            throws BankingException {
        // Get existing customer
        Customer customer = getCustomer(customerId);
        
        // Validate new data
        validateCustomerData(firstName, lastName, email, phone, dateOfBirth);
        
        // Check if email is being changed and if new email already exists
        if (!customer.getEmail().equalsIgnoreCase(email.trim())) {
            if (customerDAO.existsByEmail(email)) {
                throw new BankingException("Customer with email already exists: " + email);
            }
        }
        
        // Update customer data
        customer.setFirstName(firstName.trim());
        customer.setLastName(lastName.trim());
        customer.setEmail(email.toLowerCase().trim());
        customer.setPhone(phone != null ? phone.trim() : null);
        customer.setAddress(address != null ? address.trim() : null);
        customer.setDateOfBirth(dateOfBirth);
        
        return customerDAO.update(customer);
    }

    /**
     * Update customer status.
     * 
     * @param customerId Customer ID to update
     * @param status New customer status
     * @return Updated customer
     * @throws BankingException if update fails
     */
    public Customer updateCustomerStatus(Long customerId, CustomerStatus status) 
            throws BankingException {
        Customer customer = getCustomer(customerId);
        
        // Validate status transition
        validateStatusTransition(customer.getStatus(), status);
        
        customer.setStatus(status);
        return customerDAO.update(customer);
    }

    /**
     * Deactivate a customer (soft delete).
     * 
     * @param customerId Customer ID to deactivate
     * @return true if deactivation was successful
     * @throws BankingException if deactivation fails
     */
    public boolean deactivateCustomer(Long customerId) throws BankingException {
        Customer customer = getCustomer(customerId);
        
        if (customer.getStatus() == CustomerStatus.CLOSED) {
            throw new BankingException("Customer is already closed");
        }
        
        return customerDAO.delete(customerId);
    }

    /**
     * Search customers by name.
     * 
     * @param searchTerm Search term to match against names
     * @param offset Starting position for pagination
     * @param limit Maximum number of customers to return
     * @return List of matching customers
     * @throws BankingException if search fails
     */
    public List<Customer> searchCustomers(String searchTerm, int offset, int limit) 
            throws BankingException {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BankingException("Search term is required");
        }
        
        return customerDAO.searchByName(searchTerm.trim(), offset, limit);
    }

    /**
     * Get all customers with pagination.
     * 
     * @param offset Starting position
     * @param limit Maximum number of customers
     * @return List of customers
     * @throws BankingException if retrieval fails
     */
    public List<Customer> getAllCustomers(int offset, int limit) throws BankingException {
        return customerDAO.findAll(offset, limit);
    }

    /**
     * Get customers by status.
     * 
     * @param status Customer status to filter by
     * @param offset Starting position
     * @param limit Maximum number of customers
     * @return List of customers with specified status
     * @throws BankingException if retrieval fails
     */
    public List<Customer> getCustomersByStatus(CustomerStatus status, int offset, int limit) 
            throws BankingException {
        return customerDAO.findByStatus(status.name(), offset, limit);
    }

    /**
     * Get total customer count.
     * 
     * @return Total number of customers
     * @throws BankingException if count fails
     */
    public long getTotalCustomerCount() throws BankingException {
        return customerDAO.getTotalCount();
    }

    /**
     * Get customer count by status.
     * 
     * @param status Customer status to count
     * @return Number of customers with specified status
     * @throws BankingException if count fails
     */
    public long getCustomerCountByStatus(CustomerStatus status) throws BankingException {
        return customerDAO.getCountByStatus(status.name());
    }

    /**
     * Check if customer exists by email.
     * 
     * @param email Email to check
     * @return true if customer exists, false otherwise
     * @throws BankingException if check fails
     */
    public boolean customerExists(String email) throws BankingException {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        return customerDAO.existsByEmail(email.toLowerCase().trim());
    }

    // Private validation methods

    /**
     * Validate customer data.
     */
    private void validateCustomerData(String firstName, String lastName, String email, 
                                    String phone, LocalDate dateOfBirth) throws BankingException {
        // Validate required fields
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new BankingException("First name is required");
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new BankingException("Last name is required");
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw new BankingException("Email is required");
        }
        
        // Validate field lengths
        if (firstName.trim().length() < 2 || firstName.trim().length() > 50) {
            throw new BankingException("First name must be between 2 and 50 characters");
        }
        
        if (lastName.trim().length() < 2 || lastName.trim().length() > 50) {
            throw new BankingException("Last name must be between 2 and 50 characters");
        }
        
        // Validate email format
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new BankingException("Invalid email format");
        }
        
        // Validate phone format (if provided)
        if (phone != null && !phone.trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
                throw new BankingException("Invalid phone number format");
            }
        }
        
        // Validate date of birth
        if (dateOfBirth != null) {
            LocalDate now = LocalDate.now();
            
            if (dateOfBirth.isAfter(now)) {
                throw new BankingException("Date of birth cannot be in the future");
            }
            
            int age = Period.between(dateOfBirth, now).getYears();
            if (age < 18) {
                throw new BankingException("Customer must be at least 18 years old");
            }
            
            if (age > 120) {
                throw new BankingException("Invalid date of birth");
            }
        }
    }

    /**
     * Validate customer status transition.
     */
    private void validateStatusTransition(CustomerStatus currentStatus, CustomerStatus newStatus) 
            throws BankingException {
        // Define allowed status transitions
        switch (currentStatus) {
            case ACTIVE:
                if (newStatus != CustomerStatus.INACTIVE && 
                    newStatus != CustomerStatus.SUSPENDED && 
                    newStatus != CustomerStatus.CLOSED) {
                    throw new BankingException("Invalid status transition from ACTIVE to " + newStatus);
                }
                break;
            case INACTIVE:
                if (newStatus != CustomerStatus.ACTIVE && newStatus != CustomerStatus.CLOSED) {
                    throw new BankingException("Invalid status transition from INACTIVE to " + newStatus);
                }
                break;
            case SUSPENDED:
                if (newStatus != CustomerStatus.ACTIVE && newStatus != CustomerStatus.CLOSED) {
                    throw new BankingException("Invalid status transition from SUSPENDED to " + newStatus);
                }
                break;
            case CLOSED:
                throw new BankingException("Cannot change status of a closed customer");
            default:
                throw new BankingException("Unknown customer status: " + currentStatus);
        }
    }
}