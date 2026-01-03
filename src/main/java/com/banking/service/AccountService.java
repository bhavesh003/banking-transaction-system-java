package com.banking.service;

import com.banking.dao.AccountDAO;
import com.banking.dao.CustomerDAO;
import com.banking.dao.impl.AccountDAOImpl;
import com.banking.dao.impl.CustomerDAOImpl;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.BankingException;
import com.banking.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Account operations.
 * 
 * This class contains business logic for account management including
 * account creation, validation, and status management.
 * 
 * @author Banking System Team
 * @version 1.0
 */
public class AccountService {
    private final AccountDAO accountDAO;
    private final CustomerDAO customerDAO;

    public AccountService() {
        this.accountDAO = new AccountDAOImpl();
        this.customerDAO = new CustomerDAOImpl();
    }
    
    // Constructor for dependency injection (testing)
    public AccountService(AccountDAO accountDAO, CustomerDAO customerDAO) {
        this.accountDAO = accountDAO;
        this.customerDAO = customerDAO;
    }

    /**
     * Create a new savings account.
     * 
     * @param customerId Customer ID who owns the account
     * @param initialDeposit Initial deposit amount
     * @return Created savings account
     * @throws BankingException if account creation fails
     */
    public Account createSavingsAccount(Long customerId, BigDecimal initialDeposit) throws BankingException {
        // Validate customer exists
        validateCustomerExists(customerId);
        
        // Validate initial deposit
        validateInitialDeposit(initialDeposit, new BigDecimal("100.00"));
        
        // Generate unique account number
        String accountNumber = accountDAO.generateAccountNumber("SAVINGS");
        
        // Create savings account
        SavingsAccount account = new SavingsAccount(accountNumber, String.valueOf(customerId), initialDeposit);
        
        return accountDAO.create(account);
    }

    /**
     * Create a new checking account.
     * 
     * @param customerId Customer ID who owns the account
     * @param initialDeposit Initial deposit amount
     * @return Created checking account
     * @throws BankingException if account creation fails
     */
    public Account createCheckingAccount(Long customerId, BigDecimal initialDeposit) throws BankingException {
        // Validate customer exists
        validateCustomerExists(customerId);
        
        // Validate initial deposit
        validateInitialDeposit(initialDeposit, new BigDecimal("50.00"));
        
        // Generate unique account number
        String accountNumber = accountDAO.generateAccountNumber("CHECKING");
        
        // Create checking account
        CheckingAccount account = new CheckingAccount(accountNumber, String.valueOf(customerId), initialDeposit);
        
        return accountDAO.create(account);
    }

    /**
     * Create a new business account.
     * 
     * @param customerId Customer ID who owns the account
     * @param initialDeposit Initial deposit amount
     * @param businessName Name of the business
     * @param taxId Business tax ID
     * @return Created business account
     * @throws BankingException if account creation fails
     */
    public Account createBusinessAccount(Long customerId, BigDecimal initialDeposit, 
                                       String businessName, String taxId) throws BankingException {
        // Validate customer exists
        validateCustomerExists(customerId);
        
        // Validate initial deposit
        validateInitialDeposit(initialDeposit, new BigDecimal("1000.00"));
        
        // Validate business information
        validateBusinessInfo(businessName, taxId);
        
        // Generate unique account number
        String accountNumber = accountDAO.generateAccountNumber("BUSINESS");
        
        // Create business account
        BusinessAccount account = new BusinessAccount(accountNumber, String.valueOf(customerId), 
                                                    initialDeposit, businessName, taxId);
        
        return accountDAO.create(account);
    }

    /**
     * Get account by account number.
     * 
     * @param accountNumber Account number to search for
     * @return Account if found
     * @throws AccountNotFoundException if account not found
     */
    public Account getAccount(String accountNumber) throws AccountNotFoundException {
        try {
            Optional<Account> account = accountDAO.findByAccountNumber(accountNumber);
            return account.orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        } catch (BankingException e) {
            throw new AccountNotFoundException("Error retrieving account: " + e.getMessage(), e);
        }
    }

    /**
     * Get all accounts for a customer.
     * 
     * @param customerId Customer ID to search for
     * @return List of customer's accounts
     * @throws BankingException if retrieval fails
     */
    public List<Account> getAccountsByCustomerId(Long customerId) throws BankingException {
        return accountDAO.findByCustomerId(customerId);
    }

    /**
     * Update account status.
     * 
     * @param accountNumber Account number to update
     * @param status New account status
     * @return Updated account
     * @throws AccountNotFoundException if account not found
     * @throws BankingException if update fails
     */
    public Account updateAccountStatus(String accountNumber, AccountStatus status) 
            throws AccountNotFoundException, BankingException {
        Account account = getAccount(accountNumber);
        
        // Validate status transition
        validateStatusTransition(account.getStatus(), status);
        
        account.setStatus(status);
        return accountDAO.update(account);
    }

    /**
     * Close an account.
     * 
     * @param accountNumber Account number to close
     * @throws BankingException if account cannot be closed
     */
    public void closeAccount(String accountNumber) throws BankingException {
        Account account = getAccount(accountNumber);
        
        // Validate account can be closed
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BankingException("Cannot close account with non-zero balance. Current balance: " + account.getBalance());
        }
        
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BankingException("Account is already closed");
        }
        
        account.setStatus(AccountStatus.CLOSED);
        accountDAO.update(account);
    }

    /**
     * Get account balance.
     * 
     * @param accountNumber Account number to check
     * @return Current account balance
     * @throws AccountNotFoundException if account not found
     */
    public BigDecimal getAccountBalance(String accountNumber) throws AccountNotFoundException {
        Account account = getAccount(accountNumber);
        return account.getBalance();
    }

    /**
     * Check if account can perform withdrawal.
     * 
     * @param accountNumber Account number to check
     * @param amount Amount to withdraw
     * @return true if withdrawal is allowed
     * @throws AccountNotFoundException if account not found
     * @throws BankingException if validation fails
     */
    public boolean canWithdraw(String accountNumber, BigDecimal amount) 
            throws AccountNotFoundException, BankingException {
        Account account = getAccount(accountNumber);
        
        // Check account status
        if (account.getStatus() != AccountStatus.ACTIVE) {
            return false;
        }
        
        // Check withdrawal limit
        if (amount.compareTo(account.getWithdrawalLimit()) > 0) {
            return false;
        }
        
        // Check sufficient funds based on account type
        if (account instanceof CheckingAccount) {
            CheckingAccount checking = (CheckingAccount) account;
            return checking.canWithdraw(amount);
        } else {
            // For savings and business accounts, check minimum balance
            BigDecimal balanceAfterWithdrawal = account.getBalance().subtract(amount);
            return balanceAfterWithdrawal.compareTo(account.getMinimumBalance()) >= 0;
        }
    }

    /**
     * Get total balance for all customer accounts.
     * 
     * @param customerId Customer ID
     * @return Total balance across all accounts
     * @throws BankingException if calculation fails
     */
    public BigDecimal getTotalCustomerBalance(Long customerId) throws BankingException {
        return accountDAO.getTotalBalanceByCustomer(customerId);
    }

    /**
     * Get account count by customer.
     * 
     * @param customerId Customer ID
     * @return Number of accounts for the customer
     * @throws BankingException if count fails
     */
    public long getAccountCountByCustomer(Long customerId) throws BankingException {
        return accountDAO.getCountByCustomer(customerId);
    }

    /**
     * Validate initial deposit amount.
     * 
     * @param amount Deposit amount to validate
     * @param minimumRequired Minimum required amount
     * @throws BankingException if validation fails
     */
    private void validateInitialDeposit(BigDecimal amount, BigDecimal minimumRequired) throws BankingException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Initial deposit must be greater than zero");
        }
        
        if (amount.compareTo(minimumRequired) < 0) {
            throw new BankingException("Initial deposit must be at least " + minimumRequired);
        }
    }

    /**
     * Validate that customer exists.
     * 
     * @param customerId Customer ID to validate
     * @throws BankingException if customer doesn't exist
     */
    private void validateCustomerExists(Long customerId) throws BankingException {
        try {
            Optional<Customer> customer = customerDAO.findById(customerId);
            if (!customer.isPresent()) {
                throw new BankingException("Customer not found with ID: " + customerId);
            }
            
            if (customer.get().getStatus() != CustomerStatus.ACTIVE) {
                throw new BankingException("Customer account is not active: " + customer.get().getStatus());
            }
        } catch (BankingException e) {
            throw new BankingException("Error validating customer: " + e.getMessage(), e);
        }
    }

    /**
     * Validate business information for business accounts.
     * 
     * @param businessName Business name
     * @param taxId Business tax ID
     * @throws BankingException if validation fails
     */
    private void validateBusinessInfo(String businessName, String taxId) throws BankingException {
        if (businessName == null || businessName.trim().isEmpty()) {
            throw new BankingException("Business name is required");
        }
        
        if (taxId == null || taxId.trim().isEmpty()) {
            throw new BankingException("Business tax ID is required");
        }
        
        // Additional business validation logic can be added here
        if (businessName.length() < 2) {
            throw new BankingException("Business name must be at least 2 characters long");
        }
        
        if (taxId.length() < 9) {
            throw new BankingException("Tax ID must be at least 9 characters long");
        }
    }

    /**
     * Validate account status transition.
     * 
     * @param currentStatus Current account status
     * @param newStatus New account status
     * @throws BankingException if transition is not allowed
     */
    private void validateStatusTransition(AccountStatus currentStatus, AccountStatus newStatus) 
            throws BankingException {
        // Define allowed status transitions
        switch (currentStatus) {
            case ACTIVE:
                if (newStatus != AccountStatus.SUSPENDED && newStatus != AccountStatus.CLOSED) {
                    throw new BankingException("Invalid status transition from ACTIVE to " + newStatus);
                }
                break;
            case INACTIVE:
                if (newStatus != AccountStatus.ACTIVE && newStatus != AccountStatus.CLOSED) {
                    throw new BankingException("Invalid status transition from INACTIVE to " + newStatus);
                }
                break;
            case SUSPENDED:
                if (newStatus != AccountStatus.ACTIVE && newStatus != AccountStatus.CLOSED) {
                    throw new BankingException("Invalid status transition from SUSPENDED to " + newStatus);
                }
                break;
            case CLOSED:
                throw new BankingException("Cannot change status of a closed account");
            default:
                throw new BankingException("Unknown account status: " + currentStatus);
        }
    }
}