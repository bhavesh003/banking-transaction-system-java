package com.banking;

import com.banking.service.CustomerService;
import com.banking.service.AccountService;
import com.banking.service.TransactionService;
import com.banking.model.*;
import com.banking.exception.BankingException;
import com.banking.util.DBConnection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Simple CLI Application for the Banking Transaction System (Windows Compatible).
 * 
 * This is a simplified version without Unicode characters for Windows compatibility.
 */
public class MainSimple {
    
    private static final Scanner scanner = new Scanner(System.in);
    private static final CustomerService customerService = new CustomerService();
    private static final AccountService accountService = new AccountService();
    private static final TransactionService transactionService = new TransactionService();
    
    public static void main(String[] args) {
        System.out.println("Banking Transaction System");
        System.out.println("==========================");
        
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
    
    private static void runMainMenu() {
        while (true) {
            displayMainMenu();
            
            try {
                int choice = getIntInput("Enter your choice: ");
                
                switch (choice) {
                    case 1:
                        customerMenu();
                        break;
                    case 2:
                        accountMenu();
                        break;
                    case 3:
                        transactionMenu();
                        break;
                    case 4:
                        System.out.println("System Information:");
                        System.out.println("Database Pool Size: " + DBConnection.getPoolSize());
                        pressEnterToContinue();
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
    
    private static void displayMainMenu() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("BANKING SYSTEM - MAIN MENU");
        System.out.println("=".repeat(40));
        System.out.println("1. Customer Management");
        System.out.println("2. Account Management");
        System.out.println("3. Transactions");
        System.out.println("4. System Information");
        System.out.println("0. Exit");
        System.out.println("=".repeat(40));
    }
    
    private static void customerMenu() {
        while (true) {
            System.out.println("\n--- Customer Management ---");
            System.out.println("1. Create New Customer");
            System.out.println("2. View Customer Details");
            System.out.println("3. List All Customers");
            System.out.println("0. Back to Main Menu");
            
            int choice = getIntInput("Enter your choice: ");
            
            try {
                switch (choice) {
                    case 1:
                        createCustomer();
                        break;
                    case 2:
                        viewCustomer();
                        break;
                    case 3:
                        listCustomers();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                pressEnterToContinue();
            }
        }
    }
    
    private static void accountMenu() {
        while (true) {
            System.out.println("\n--- Account Management ---");
            System.out.println("1. Open New Account");
            System.out.println("2. View Account Details");
            System.out.println("3. Check Account Balance");
            System.out.println("0. Back to Main Menu");
            
            int choice = getIntInput("Enter your choice: ");
            
            try {
                switch (choice) {
                    case 1:
                        openAccount();
                        break;
                    case 2:
                        viewAccount();
                        break;
                    case 3:
                        checkBalance();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                pressEnterToContinue();
            }
        }
    }
    
    private static void transactionMenu() {
        while (true) {
            System.out.println("\n--- Transactions ---");
            System.out.println("1. Deposit Money");
            System.out.println("2. Withdraw Money");
            System.out.println("3. Transfer Money");
            System.out.println("4. View Transaction History");
            System.out.println("0. Back to Main Menu");
            
            int choice = getIntInput("Enter your choice: ");
            
            try {
                switch (choice) {
                    case 1:
                        deposit();
                        break;
                    case 2:
                        withdraw();
                        break;
                    case 3:
                        transfer();
                        break;
                    case 4:
                        viewTransactionHistory();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                pressEnterToContinue();
            }
        }
    }
    
    // Customer operations
    private static void createCustomer() throws BankingException {
        System.out.println("\n--- Create New Customer ---");
        
        String firstName = getStringInput("First Name: ");
        String lastName = getStringInput("Last Name: ");
        String email = getStringInput("Email: ");
        String phone = getStringInput("Phone (optional): ");
        String address = getStringInput("Address (optional): ");
        
        System.out.print("Date of Birth (yyyy-mm-dd): ");
        LocalDate dateOfBirth = LocalDate.parse(scanner.nextLine().trim());
        
        if (phone.trim().isEmpty()) phone = null;
        if (address.trim().isEmpty()) address = null;
        
        Customer customer = customerService.createCustomer(firstName, lastName, email, phone, address, dateOfBirth);
        
        System.out.println("Customer created successfully!");
        System.out.println("Customer ID: " + customer.getCustomerId());
        System.out.println("Name: " + customer.getFirstName() + " " + customer.getLastName());
        System.out.println("Email: " + customer.getEmail());
        
        pressEnterToContinue();
    }
    
    private static void viewCustomer() throws BankingException {
        System.out.println("\n--- View Customer Details ---");
        
        Long customerId = getLongInput("Customer ID: ");
        Customer customer = customerService.getCustomer(customerId);
        
        System.out.println("Customer ID: " + customer.getCustomerId());
        System.out.println("Name: " + customer.getFirstName() + " " + customer.getLastName());
        System.out.println("Email: " + customer.getEmail());
        System.out.println("Phone: " + (customer.getPhone() != null ? customer.getPhone() : "N/A"));
        System.out.println("Status: " + customer.getStatus());
        
        pressEnterToContinue();
    }
    
    private static void listCustomers() throws BankingException {
        System.out.println("\n--- All Customers ---");
        
        List<Customer> customers = customerService.getAllCustomers(0, 10);
        
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
        } else {
            System.out.println("First 10 customers:");
            for (Customer customer : customers) {
                System.out.printf("ID: %d | %s %s | %s%n", 
                    customer.getCustomerId(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getEmail());
            }
        }
        
        pressEnterToContinue();
    }
    
    // Account operations
    private static void openAccount() throws BankingException {
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
        
        System.out.println("Account created successfully!");
        System.out.println("Account Number: " + account.getAccountNumber());
        System.out.println("Account Type: " + account.getAccountType());
        System.out.println("Balance: $" + account.getBalance());
        
        pressEnterToContinue();
    }
    
    private static void viewAccount() throws BankingException {
        System.out.println("\n--- View Account Details ---");
        
        String accountNumber = getStringInput("Account Number: ");
        Account account = accountService.getAccount(accountNumber);
        
        System.out.println("Account Number: " + account.getAccountNumber());
        System.out.println("Customer ID: " + account.getCustomerId());
        System.out.println("Account Type: " + account.getAccountType());
        System.out.println("Balance: $" + account.getBalance());
        System.out.println("Status: " + account.getStatus());
        
        pressEnterToContinue();
    }
    
    private static void checkBalance() throws BankingException {
        System.out.println("\n--- Check Account Balance ---");
        
        String accountNumber = getStringInput("Account Number: ");
        BigDecimal balance = accountService.getAccountBalance(accountNumber);
        
        System.out.println("Account: " + accountNumber);
        System.out.println("Current Balance: $" + balance);
        
        pressEnterToContinue();
    }
    
    // Transaction operations
    private static void deposit() throws BankingException {
        System.out.println("\n--- Deposit Money ---");
        
        String accountNumber = getStringInput("Account Number: ");
        BigDecimal amount = getBigDecimalInput("Deposit Amount: $");
        String description = getStringInput("Description (optional): ");
        
        if (description.trim().isEmpty()) {
            description = "Cash deposit";
        }
        
        Transaction transaction = transactionService.deposit(accountNumber, amount, description);
        
        System.out.println("Deposit successful!");
        System.out.println("Transaction ID: " + transaction.getTransactionId());
        System.out.println("Amount: $" + transaction.getAmount());
        System.out.println("Status: " + transaction.getStatus());
        
        pressEnterToContinue();
    }
    
    private static void withdraw() throws BankingException {
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
        
        System.out.println("Withdrawal successful!");
        System.out.println("Transaction ID: " + transaction.getTransactionId());
        System.out.println("Amount: $" + transaction.getAmount());
        System.out.println("Status: " + transaction.getStatus());
        
        pressEnterToContinue();
    }
    
    private static void transfer() throws BankingException {
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
        
        System.out.println("Transfer successful!");
        System.out.println("Transaction ID: " + transaction.getTransactionId());
        System.out.println("Amount: $" + transaction.getAmount());
        System.out.println("Status: " + transaction.getStatus());
        
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
                System.out.printf("%s | %s | $%s | %s%n",
                    transaction.getTransactionId(),
                    transaction.getType(),
                    transaction.getAmount(),
                    transaction.getStatus());
            }
        }
        
        pressEnterToContinue();
    }
    
    // Input helper methods
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
    
    private static Long getLongInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
    
    private static BigDecimal getBigDecimalInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return new BigDecimal(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }
    
    private static void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}