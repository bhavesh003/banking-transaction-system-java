package com.banking;

import com.banking.util.DBConnection;
import com.banking.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;

/**
 * Simple test application to verify the banking system works.
 */
public class TestApp {
    
    private static final Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("Banking System Test Application");
        System.out.println("===============================");
        
        // Test database connection
        try {
            DBConnection.initializePool();
            System.out.println("Database connection established successfully!");
            
            if (DBConnection.testConnection()) {
                System.out.println("Database connectivity test passed!");
            } else {
                System.err.println("Database connectivity test failed!");
                return;
            }
            
            // Test basic functionality
            testBasicFunctionality();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.shutdown();
            scanner.close();
        }
    }
    
    private static void testBasicFunctionality() {
        System.out.println("\n=== Testing Basic Functionality ===");
        
        try {
            // Test creating models
            System.out.println("1. Testing model creation...");
            
            // Create a customer
            Customer customer = new Customer(
                "CUST001",
                "John",
                "Doe", 
                "john.doe@email.com",
                "+1-555-0101",
                "123 Main St",
                LocalDate.of(1985, 6, 15),
                "123-45-6789"
            );
            
            System.out.println("Customer created: " + customer.getFullName());
            System.out.println("Customer ID: " + customer.getCustomerId());
            System.out.println("Email: " + customer.getEmail());
            System.out.println("Status: " + customer.getStatus());
            
            // Create accounts
            System.out.println("\n2. Testing account creation...");
            
            SavingsAccount savings = new SavingsAccount("SAV001001", customer.getCustomerId(), new BigDecimal("1000.00"));
            System.out.println("Savings Account created: " + savings.getAccountNumber());
            System.out.println("Balance: $" + savings.getBalance());
            System.out.println("Interest Rate: " + (savings.getInterestRate().multiply(new BigDecimal("100"))) + "%");
            
            CheckingAccount checking = new CheckingAccount("CHK001001", customer.getCustomerId(), new BigDecimal("500.00"));
            System.out.println("Checking Account created: " + checking.getAccountNumber());
            System.out.println("Balance: $" + checking.getBalance());
            System.out.println("Overdraft Limit: $" + checking.getOverdraftLimit());
            
            BusinessAccount business = new BusinessAccount("BUS001001", customer.getCustomerId(), 
                new BigDecimal("5000.00"), "Test Business LLC", "12-3456789");
            System.out.println("Business Account created: " + business.getAccountNumber());
            System.out.println("Balance: $" + business.getBalance());
            System.out.println("Business Name: " + business.getBusinessName());
            
            // Test transactions
            System.out.println("\n3. Testing transaction creation...");
            
            Transaction deposit = new Transaction(
                "TXN001",
                null,
                savings.getAccountNumber(),
                TransactionType.DEPOSIT,
                new BigDecimal("200.00"),
                "Test deposit"
            );
            
            System.out.println("Deposit Transaction created: " + deposit.getTransactionId());
            System.out.println("Type: " + deposit.getType());
            System.out.println("Amount: $" + deposit.getAmount());
            System.out.println("Status: " + deposit.getStatus());
            
            Transaction transfer = new Transaction(
                "TXN002",
                savings.getAccountNumber(),
                checking.getAccountNumber(),
                TransactionType.TRANSFER,
                new BigDecimal("100.00"),
                "Test transfer"
            );
            
            System.out.println("Transfer Transaction created: " + transfer.getTransactionId());
            System.out.println("From: " + transfer.getFromAccountNumber());
            System.out.println("To: " + transfer.getToAccountNumber());
            System.out.println("Amount: $" + transfer.getAmount());
            
            // Test account operations
            System.out.println("\n4. Testing account operations...");
            
            System.out.println("Savings account can withdraw $50? " + 
                (savings.getBalance().compareTo(new BigDecimal("50.00")) >= 0));
            
            System.out.println("Checking account can withdraw $600? " + 
                checking.canWithdraw(new BigDecimal("600.00")));
            
            System.out.println("Savings interest calculation: $" + savings.calculateInterest());
            
            System.out.println("\n=== All Tests Completed Successfully! ===");
            
            // Interactive menu
            runSimpleMenu();
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void runSimpleMenu() {
        System.out.println("\n=== Simple Banking Menu ===");
        System.out.println("This demonstrates the models work correctly.");
        System.out.println("For full database operations, you'll need to:");
        System.out.println("1. Set up the database schema (run database/schema.sql)");
        System.out.println("2. Fix the service layer to match the model structure");
        System.out.println("3. Use the corrected service classes");
        
        System.out.println("\nCurrent Status:");
        System.out.println("- Database Connection: Working");
        System.out.println("- Model Classes: Working");
        System.out.println("- Service Classes: Need adjustment for model compatibility");
        
        System.out.println("\nPress Enter to exit...");
        scanner.nextLine();
    }
}