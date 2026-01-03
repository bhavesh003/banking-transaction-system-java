package com.banking;

import com.banking.service.CustomerService;
import com.banking.service.AccountService;
import com.banking.service.TransactionService;
import com.banking.model.*;
import com.banking.exception.BankingException;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.InsufficientFundsException;
import com.banking.util.DBConnection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Main CLI Application for the Banking Transaction System.
 * 
 * This application provides a comprehensive menu-driven interface for:
 * - Customer management
 * - Account operations
 * - Transaction processing
 * - Reports and statements
 * 
 * Features:
 * - User-friendly menu navigation
 * - Input validation and error handling
 * - Formatted output for better readability
 * - Safe transaction processing
 * 
 * @author Banking System Team
 * @version 1.0
 */
public class Main {
    
    private static final Scanner scanner = new Scanner(System.in);
    private static final CustomerService customerService = new CustomerService();
    private static final AccountService accountService = new AccountService();
    private static final TransactionService transactionService = new TransactionService();
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static void main(String[] args) {
        System.out.println("Welcome to the Banking Transaction System");
        System.out.println("==========================================");
        
        // Initialize database connection
        try {
            DBConnection.initializePool();
            System.out.println("Database connection established successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize database connection: " + e.getMessage());
            System.err.println("Please check your database configuration in DBConstants.java");
            return;
        }
        
        try {
            // Test database connectivity
            if (!DBConnection.testConnection()) {
                System.err.println("Database connectivity test failed");
                return;
            }
            
            // Start main application loop
            runMainMenu();
            
        } catch (Exception e) {
            System.err.println("Application error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup resources
            DBConnection.shutdown();
            scanner.close();
            System.out.println("\nThank you for using the Banking Transaction System!");
        }
    }
    
    /**
     * Main menu loop.
     */
    private static void runMainMenu() {
        while (true) {
            displayMainMenu();
            
            try {
                int choice = getIntInput("Enter your choice: ");
                
                switch (choice) {
                    case 1:
                        customerManagementMenu();
                        break;
                    case 2:
                        accountManagementMenu();
                        break;
                    case 3:
                        transactionMenu();
                        break;
                    case 4:
                        reportsMenu();
                        break;
                    case 5:
                        systemInfoMenu();
                        break;
                    case 0:
                        System.out.println("Exiting application...");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
                
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                pressEnterToContinue();
            }
        }
    }
    
    /**
     * Display main menu options.
     */
    private static void displayMainMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("BANKING TRANSACTION SYSTEM - MAIN MENU");
        System.out.println("=".repeat(50));
        System.out.println("1. Customer Management");
        System.out.println("2. Account Management");
        System.out.println("3. Transactions");
        System.out.println("4. Reports & Statements");
        System.out.println("5. System Information");
        System.out.println("0. Exit");
        System.out.println("=".repeat(50));
    }
    
    /**
     * Customer management menu.
     */
    private static void customerManagementMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(40));
            System.out.println("👤 CUSTOMER MANAGEMENT");
            System.out.println("=".repeat(40));
            System.out.println("1. Create New Customer");
            System.out.println("2. View Customer Details");
            System.out.println("3. Update Customer Information");
            System.out.println("4. Search Customers");
            System.out.println("5. List All Customers");
            System.out.println("6. Customer Statistics");
            System.out.println("0. Back to Main Menu");
            System.out.println("=".repeat(40));
            
            int choice = getIntInput("Enter your choice: ");
            
            try {
                switch (choice) {
                    case 1:
                        createCustomer();
                        break;
                    case 2:
                        viewCustomerDetails();
                        break;
                    case 3:
                        updateCustomer();
                        break;
                    case 4:
                        searchCustomers();
                        break;
                    case 5:
                        listAllCustomers();
                        break;
                    case 6:
                        customerStatistics();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("❌ Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                pressEnterToContinue();
            }
        }
    }
    
    /**
     * Account management menu.
     */
    private static void accountManagementMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(40));
            System.out.println("💳 ACCOUNT MANAGEMENT");
            System.out.println("=".repeat(40));
            System.out.println("1. Open New Account");
            System.out.println("2. View Account Details");
            System.out.println("3. View Customer Accounts");
            System.out.println("4. Check Account Balance");
            System.out.println("5. Update Account Status");
            System.out.println("6. Close Account");
            System.out.println("0. Back to Main Menu");
            System.out.println("=".repeat(40));
            
            int choice = getIntInput("Enter your choice: ");
            
            try {
                switch (choice) {
                    case 1:
                        openNewAccount();
                        break;
                    case 2:
                        viewAccountDetails();
                        break;
                    case 3:
                        viewCustomerAccounts();
                        break;
                    case 4:
                        checkAccountBalance();
                        break;
                    case 5:
                        updateAccountStatus();
                        break;
                    case 6:
                        closeAccount();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("❌ Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                pressEnterToContinue();
            }
        }
    }
    
    /**
     * Transaction menu.
     */
    private static void transactionMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(40));
            System.out.println("💰 TRANSACTIONS");
            System.out.println("=".repeat(40));
            System.out.println("1. Deposit Money");
            System.out.println("2. Withdraw Money");
            System.out.println("3. Transfer Money");
            System.out.println("4. View Transaction History");
            System.out.println("5. View Transaction Details");
            System.out.println("0. Back to Main Menu");
            System.out.println("=".repeat(40));
            
            int choice = getIntInput("Enter your choice: ");
            
            try {
                switch (choice) {
                    case 1:
                        depositMoney();
                        break;
                    case 2:
                        withdrawMoney();
                        break;
                    case 3:
                        transferMoney();
                        break;
                    case 4:
                        viewTransactionHistory();
                        break;
                    case 5:
                        viewTransactionDetails();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("❌ Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                pressEnterToContinue();
            }
        }
    }
    
    /**
     * Reports menu.
     */
    private static void reportsMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(40));
            System.out.println("📊 REPORTS & STATEMENTS");
            System.out.println("=".repeat(40));
            System.out.println("1. Account Statement");
            System.out.println("2. Customer Summary");
            System.out.println("3. Transaction Summary");
            System.out.println("4. System Statistics");
            System.out.println("0. Back to Main Menu");
            System.out.println("=".repeat(40));
            
            int choice = getIntInput("Enter your choice: ");
            
            try {
                switch (choice) {
                    case 1:
                        generateAccountStatement();
                        break;
                    case 2:
                        generateCustomerSummary();
                        break;
                    case 3:
                        generateTransactionSummary();
                        break;
                    case 4:
                        systemStatistics();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("❌ Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                pressEnterToContinue();
            }
        }
    }
    
    /**
     * System information menu.
     */
    private static void systemInfoMenu() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("⚙️  SYSTEM INFORMATION");
        System.out.println("=".repeat(40));
        System.out.println("Database Connection Pool Size: " + DBConnection.getPoolSize());
        System.out.println("Application Version: 1.0");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Operating System: " + System.getProperty("os.name"));
        System.out.println("=".repeat(40));
        pressEnterToContinue();
    }
    
    // Customer Management Methods
    
    private static void createCustomer() throws BankingException {
        System.out.println("\n--- Create New Customer ---");
        
        String firstName = getStringInput("First Name: ");
        String lastName = getStringInput("Last Name: ");
        String email = getStringInput("Email: ");
        String phone = getStringInput("Phone (optional): ");
        String address = getStringInput("Address (optional): ");
        LocalDate dateOfBirth = getDateInput("Date of Birth (yyyy-mm-dd): ");
        
        if (phone.trim().isEmpty()) phone = null;
        if (address.trim().isEmpty()) address = null;
        
        Customer customer = customerService.createCustomer(firstName, lastName, email, phone, address, dateOfBirth);
        
        System.out.println("✅ Customer created successfully!");
        displayCustomer(customer);
        pressEnterToContinue();
    }
    
    private static void viewCustomerDetails() throws BankingException {
        System.out.println("\n--- View Customer Details ---");
        
        Long customerId = getLongInput("Customer ID: ");
        Customer customer = customerService.getCustomer(customerId);
        
        displayCustomer(customer);
        pressEnterToContinue();
    }
    
    private static void updateCustomer() throws BankingException {
        System.out.println("\n--- Update Customer Information ---");
        
        Long customerId = getLongInput("Customer ID: ");
        Customer customer = customerService.getCustomer(customerId);
        
        System.out.println("Current Information:");
        displayCustomer(customer);
        
        System.out.println("\nEnter new information (press Enter to keep current value):");
        
        String firstName = getStringInputWithDefault("First Name", customer.getFirstName());
        String lastName = getStringInputWithDefault("Last Name", customer.getLastName());
        String email = getStringInputWithDefault("Email", customer.getEmail());
        String phone = getStringInputWithDefault("Phone", customer.getPhone());
        String address = getStringInputWithDefault("Address", customer.getAddress());
        
        Customer updatedCustomer = customerService.updateCustomer(customerId, firstName, lastName, 
                                                                email, phone, address, customer.getDateOfBirth());
        
        System.out.println("✅ Customer updated successfully!");
        displayCustomer(updatedCustomer);
        pressEnterToContinue();
    }
    
    private static void searchCustomers() throws BankingException {
        System.out.println("\n--- Search Customers ---");
        
        String searchTerm = getStringInput("Enter search term (name): ");
        List<Customer> customers = customerService.searchCustomers(searchTerm, 0, 10);
        
        if (customers.isEmpty()) {
            System.out.println("No customers found matching: " + searchTerm);
        } else {
            System.out.println("Found " + customers.size() + " customer(s):");
            for (Customer customer : customers) {
                displayCustomerSummary(customer);
            }
        }
        
        pressEnterToContinue();
    }
    
    private static void listAllCustomers() throws BankingException {
        System.out.println("\n--- All Customers ---");
        
        List<Customer> customers = customerService.getAllCustomers(0, 20);
        
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
        } else {
            System.out.println("Showing first 20 customers:");
            for (Customer customer : customers) {
                displayCustomerSummary(customer);
            }
        }
        
        pressEnterToContinue();
    }
    
    private static void customerStatistics() throws BankingException {
        System.out.println("\n--- Customer Statistics ---");
        
        long totalCustomers = customerService.getTotalCustomerCount();
        long activeCustomers = customerService.getCustomerCountByStatus(CustomerStatus.ACTIVE);
        long inactiveCustomers = customerService.getCustomerCountByStatus(CustomerStatus.INACTIVE);
        long suspendedCustomers = customerService.getCustomerCountByStatus(CustomerStatus.SUSPENDED);
        long closedCustomers = customerService.getCustomerCountByStatus(CustomerStatus.CLOSED);
        
        System.out.println("Total Customers: " + totalCustomers);
        System.out.println("Active: " + activeCustomers);
        System.out.println("Inactive: " + inactiveCustomers);
        System.out.println("Suspended: " + suspendedCustomers);
        System.out.println("Closed: " + closedCustomers);
        
        pressEnterToContinue();
    }
    
    // Account Management Methods
    
    private static void openNewAccount() throws BankingException {
        System.out.println("\n--- Open New Account ---");
        
        Long customerId = getLongInput("Customer ID: ");
        
        // Verify customer exists
        Customer customer = customerService.getCustomer(customerId);
        System.out.println("Customer: " + customer.getFirstName() + " " + customer.getLastName());
        
        System.out.println("\nAccount Types:");
        System.out.println("1. Savings Account (Min: $100)");
        System.out.println("2. Checking Account (Min: $50)");
        System.out.println("3. Business Account (Min: $1000)");
        
        int accountType = getIntInput("Select account type: ");
        BigDecimal initialDeposit = getBigDecimalInput("Initial deposit amount: $");
        
        Account account;
        switch (accountType) {
            case 1:
                account = accountService.createSavingsAccount(customerId, initialDeposit);
                break;
            case 2:
                account = accountService.createCheckingAccount(customerId, initialDeposit);
                break;
            case 3:
                String businessName = getStringInput("Business Name: ");
                String taxId = getStringInput("Tax ID: ");
                account = accountService.createBusinessAccount(customerId, initialDeposit, businessName, taxId);
                break;
            default:
                throw new BankingException("Invalid account type selected");
        }
        
        System.out.println("✅ Account created successfully!");
        displayAccount(account);
        pressEnterToContinue();
    }
    
    private static void viewAccountDetails() throws BankingException {
        System.out.println("\n--- View Account Details ---");
        
        String accountNumber = getStringInput("Account Number: ");
        Account account = accountService.getAccount(accountNumber);
        
        displayAccount(account);
        pressEnterToContinue();
    }
    
    private static void viewCustomerAccounts() throws BankingException {
        System.out.println("\n--- Customer Accounts ---");
        
        Long customerId = getLongInput("Customer ID: ");
        List<Account> accounts = accountService.getAccountsByCustomerId(customerId);
        
        if (accounts.isEmpty()) {
            System.out.println("No accounts found for customer ID: " + customerId);
        } else {
            System.out.println("Found " + accounts.size() + " account(s):");
            for (Account account : accounts) {
                displayAccountSummary(account);
            }
        }
        
        pressEnterToContinue();
    }
    
    private static void checkAccountBalance() throws BankingException {
        System.out.println("\n--- Check Account Balance ---");
        
        String accountNumber = getStringInput("Account Number: ");
        BigDecimal balance = accountService.getAccountBalance(accountNumber);
        
        System.out.println("Account: " + accountNumber);
        System.out.println("Current Balance: $" + balance);
        
        pressEnterToContinue();
    }
    
    private static void updateAccountStatus() throws BankingException {
        System.out.println("\n--- Update Account Status ---");
        
        String accountNumber = getStringInput("Account Number: ");
        Account account = accountService.getAccount(accountNumber);
        
        System.out.println("Current Status: " + account.getStatus());
        System.out.println("\nAvailable Statuses:");
        System.out.println("1. ACTIVE");
        System.out.println("2. INACTIVE");
        System.out.println("3. SUSPENDED");
        System.out.println("4. CLOSED");
        
        int statusChoice = getIntInput("Select new status: ");
        AccountStatus newStatus;
        
        switch (statusChoice) {
            case 1: newStatus = AccountStatus.ACTIVE; break;
            case 2: newStatus = AccountStatus.INACTIVE; break;
            case 3: newStatus = AccountStatus.SUSPENDED; break;
            case 4: newStatus = AccountStatus.CLOSED; break;
            default: throw new BankingException("Invalid status selected");
        }
        
        Account updatedAccount = accountService.updateAccountStatus(accountNumber, newStatus);
        
        System.out.println("✅ Account status updated successfully!");
        System.out.println("New Status: " + updatedAccount.getStatus());
        
        pressEnterToContinue();
    }
    
    private static void closeAccount() throws BankingException {
        System.out.println("\n--- Close Account ---");
        
        String accountNumber = getStringInput("Account Number: ");
        Account account = accountService.getAccount(accountNumber);
        
        System.out.println("Account: " + accountNumber);
        System.out.println("Current Balance: $" + account.getBalance());
        
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            System.out.println("❌ Cannot close account with non-zero balance.");
            System.out.println("Please withdraw all funds before closing the account.");
            pressEnterToContinue();
            return;
        }
        
        String confirmation = getStringInput("Type 'CONFIRM' to close this account: ");
        if (!"CONFIRM".equals(confirmation)) {
            System.out.println("Account closure cancelled.");
            pressEnterToContinue();
            return;
        }
        
        accountService.closeAccount(accountNumber);
        
        System.out.println("✅ Account closed successfully!");
        pressEnterToContinue();
    }
    
    // Transaction Methods
    
    private static void depositMoney() throws BankingException {
        System.out.println("\n--- Deposit Money ---");
        
        String accountNumber = getStringInput("Account Number: ");
        BigDecimal amount = getBigDecimalInput("Deposit Amount: $");
        String description = getStringInput("Description (optional): ");
        
        if (description.trim().isEmpty()) {
            description = "Cash deposit";
        }
        
        Transaction transaction = transactionService.deposit(accountNumber, amount, description);
        
        System.out.println("✅ Deposit successful!");
        displayTransaction(transaction);
        pressEnterToContinue();
    }
    
    private static void withdrawMoney() throws BankingException {
        System.out.println("\n--- Withdraw Money ---");
        
        String accountNumber = getStringInput("Account Number: ");
        
        // Show current balance
        BigDecimal currentBalance = accountService.getAccountBalance(accountNumber);
        System.out.println("Current Balance: $" + currentBalance);
        
        BigDecimal amount = getBigDecimalInput("Withdrawal Amount: $");
        String description = getStringInput("Description (optional): ");
        
        if (description.trim().isEmpty()) {
            description = "Cash withdrawal";
        }
        
        Transaction transaction = transactionService.withdraw(accountNumber, amount, description);
        
        System.out.println("✅ Withdrawal successful!");
        displayTransaction(transaction);
        pressEnterToContinue();
    }
    
    private static void transferMoney() throws BankingException {
        System.out.println("\n--- Transfer Money ---");
        
        String fromAccount = getStringInput("From Account Number: ");
        String toAccount = getStringInput("To Account Number: ");
        
        // Show current balance of source account
        BigDecimal currentBalance = accountService.getAccountBalance(fromAccount);
        System.out.println("Available Balance: $" + currentBalance);
        
        BigDecimal amount = getBigDecimalInput("Transfer Amount: $");
        String description = getStringInput("Description (optional): ");
        
        if (description.trim().isEmpty()) {
            description = "Account transfer";
        }
        
        Transaction transaction = transactionService.transfer(fromAccount, toAccount, amount, description);
        
        System.out.println("✅ Transfer successful!");
        displayTransaction(transaction);
        pressEnterToContinue();
    }
    
    private static void viewTransactionHistory() throws BankingException {
        System.out.println("\n--- Transaction History ---");
        
        String accountNumber = getStringInput("Account Number: ");
        List<Transaction> transactions = transactionService.getTransactionHistory(accountNumber, 0, 10);
        
        if (transactions.isEmpty()) {
            System.out.println("No transactions found for account: " + accountNumber);
        } else {
            System.out.println("Recent transactions (last 10):");
            for (Transaction transaction : transactions) {
                displayTransactionSummary(transaction);
            }
        }
        
        pressEnterToContinue();
    }
    
    private static void viewTransactionDetails() throws BankingException {
        System.out.println("\n--- Transaction Details ---");
        
        String transactionRef = getStringInput("Transaction Reference: ");
        Transaction transaction = transactionService.getTransaction(transactionRef);
        
        displayTransaction(transaction);
        pressEnterToContinue();
    }
    
    // Report Methods
    
    private static void generateAccountStatement() throws BankingException {
        System.out.println("\n--- Account Statement ---");
        
        String accountNumber = getStringInput("Account Number: ");
        LocalDateTime startDate = getDateTimeInput("Start Date (yyyy-mm-dd): ");
        LocalDateTime endDate = getDateTimeInput("End Date (yyyy-mm-dd): ");
        
        List<Transaction> transactions = transactionService.getAccountStatement(accountNumber, startDate, endDate);
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ACCOUNT STATEMENT");
        System.out.println("Account: " + accountNumber);
        System.out.println("Period: " + startDate.format(DATE_FORMATTER) + " to " + endDate.format(DATE_FORMATTER));
        System.out.println("=".repeat(60));
        
        if (transactions.isEmpty()) {
            System.out.println("No transactions found for the specified period.");
        } else {
            for (Transaction transaction : transactions) {
                displayTransactionSummary(transaction);
            }
            System.out.println("=".repeat(60));
            System.out.println("Total Transactions: " + transactions.size());
        }
        
        pressEnterToContinue();
    }
    
    private static void generateCustomerSummary() throws BankingException {
        System.out.println("\n--- Customer Summary ---");
        
        Long customerId = getLongInput("Customer ID: ");
        Customer customer = customerService.getCustomer(customerId);
        List<Account> accounts = accountService.getAccountsByCustomerId(customerId);
        BigDecimal totalBalance = accountService.getTotalCustomerBalance(customerId);
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("CUSTOMER SUMMARY");
        System.out.println("=".repeat(50));
        displayCustomer(customer);
        
        System.out.println("\nACCOUNTS:");
        for (Account account : accounts) {
            displayAccountSummary(account);
        }
        
        System.out.println("\nTOTAL BALANCE: $" + totalBalance);
        System.out.println("=".repeat(50));
        
        pressEnterToContinue();
    }
    
    private static void generateTransactionSummary() throws BankingException {
        System.out.println("\n--- Transaction Summary ---");
        
        List<Transaction> recentTransactions = transactionService.getAllTransactions(0, 20);
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("RECENT TRANSACTIONS (Last 20)");
        System.out.println("=".repeat(60));
        
        if (recentTransactions.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            for (Transaction transaction : recentTransactions) {
                displayTransactionSummary(transaction);
            }
        }
        
        System.out.println("=".repeat(60));
        pressEnterToContinue();
    }
    
    private static void systemStatistics() throws BankingException {
        System.out.println("\n--- System Statistics ---");
        
        long totalCustomers = customerService.getTotalCustomerCount();
        long activeCustomers = customerService.getCustomerCountByStatus(CustomerStatus.ACTIVE);
        
        System.out.println("Total Customers: " + totalCustomers);
        System.out.println("Active Customers: " + activeCustomers);
        System.out.println("Database Pool Size: " + DBConnection.getPoolSize());
        
        pressEnterToContinue();
    }
    
    // Display Methods
    
    private static void displayCustomer(Customer customer) {
        System.out.println("Customer ID: " + customer.getCustomerId());
        System.out.println("Name: " + customer.getFirstName() + " " + customer.getLastName());
        System.out.println("Email: " + customer.getEmail());
        System.out.println("Phone: " + (customer.getPhone() != null ? customer.getPhone() : "N/A"));
        System.out.println("Address: " + (customer.getAddress() != null ? customer.getAddress() : "N/A"));
        System.out.println("Date of Birth: " + (customer.getDateOfBirth() != null ? customer.getDateOfBirth() : "N/A"));
        System.out.println("Status: " + customer.getStatus());
        System.out.println("Created: " + customer.getCreatedAt().format(DATETIME_FORMATTER));
    }
    
    private static void displayCustomerSummary(Customer customer) {
        System.out.printf("ID: %d | %s %s | %s | %s%n", 
            customer.getCustomerId(),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getEmail(),
            customer.getStatus());
    }
    
    private static void displayAccount(Account account) {
        System.out.println("Account Number: " + account.getAccountNumber());
        System.out.println("Customer ID: " + account.getCustomerId());
        System.out.println("Account Type: " + account.getAccountType());
        System.out.println("Balance: $" + account.getBalance());
        System.out.println("Status: " + account.getStatus());
        System.out.println("Minimum Balance: $" + account.getMinimumBalance());
        System.out.println("Withdrawal Limit: $" + account.getWithdrawalLimit());
        
        if (account instanceof SavingsAccount) {
            SavingsAccount savings = (SavingsAccount) account;
            System.out.println("Interest Rate: " + (savings.getInterestRate().multiply(new BigDecimal("100"))) + "%");
        } else if (account instanceof CheckingAccount) {
            CheckingAccount checking = (CheckingAccount) account;
            System.out.println("Overdraft Limit: $" + checking.getOverdraftLimit());
        } else if (account instanceof BusinessAccount) {
            BusinessAccount business = (BusinessAccount) account;
            System.out.println("Business Name: " + business.getBusinessName());
            System.out.println("Tax ID: " + business.getTaxId());
        }
        
        System.out.println("Created: " + account.getCreatedAt().format(DATETIME_FORMATTER));
    }
    
    private static void displayAccountSummary(Account account) {
        System.out.printf("%s | %s | $%s | %s%n",
            account.getAccountNumber(),
            account.getAccountType(),
            account.getBalance(),
            account.getStatus());
    }
    
    private static void displayTransaction(Transaction transaction) {
        System.out.println("Transaction ID: " + transaction.getTransactionId());
        System.out.println("Type: " + transaction.getType());
        System.out.println("Amount: $" + transaction.getAmount());
        System.out.println("From Account: " + (transaction.getFromAccountNumber() != null ? transaction.getFromAccountNumber() : "N/A"));
        System.out.println("To Account: " + (transaction.getToAccountNumber() != null ? transaction.getToAccountNumber() : "N/A"));
        System.out.println("Description: " + transaction.getDescription());
        System.out.println("Status: " + transaction.getStatus());
        System.out.println("Timestamp: " + transaction.getTimestamp().format(DATETIME_FORMATTER));
        if (transaction.getBalanceAfter() != null) {
            System.out.println("Balance After: $" + transaction.getBalanceAfter());
        }
    }
    
    private static void displayTransactionSummary(Transaction transaction) {
        System.out.printf("%s | %s | $%s | %s | %s%n",
            transaction.getTransactionId(),
            transaction.getType(),
            transaction.getAmount(),
            transaction.getStatus(),
            transaction.getTimestamp().format(DATETIME_FORMATTER));
    }
    
    // Input Helper Methods
    
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    private static String getStringInputWithDefault(String prompt, String defaultValue) {
        System.out.print(prompt + " [" + (defaultValue != null ? defaultValue : "") + "]: ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }
    
    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid number.");
            }
        }
    }
    
    private static Long getLongInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid number.");
            }
        }
    }
    
    private static BigDecimal getBigDecimalInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return new BigDecimal(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid amount.");
            }
        }
    }
    
    private static LocalDate getDateInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return LocalDate.parse(scanner.nextLine().trim(), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("❌ Please enter date in format yyyy-mm-dd.");
            }
        }
    }
    
    private static LocalDateTime getDateTimeInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                LocalDate date = LocalDate.parse(input, DATE_FORMATTER);
                return date.atStartOfDay();
            } catch (DateTimeParseException e) {
                System.out.println("❌ Please enter date in format yyyy-mm-dd.");
            }
        }
    }
    
    private static void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}