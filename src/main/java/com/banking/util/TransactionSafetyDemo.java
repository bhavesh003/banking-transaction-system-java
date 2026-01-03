package com.banking.util;

import com.banking.service.TransactionManager;
import com.banking.service.AccountService;
import com.banking.service.CustomerService;
import com.banking.model.*;
import com.banking.exception.BankingException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

/**
 * Demonstration class for transaction safety and concurrent operations.
 * 
 * This class shows how the TransactionManager handles:
 * - Concurrent transactions safely
 * - Deadlock prevention
 * - ACID compliance
 * - Rollback on failures
 * 
 * @author Banking System Team
 * @version 1.0
 */
public class TransactionSafetyDemo {
    
    private final TransactionManager transactionManager;
    private final AccountService accountService;
    private final CustomerService customerService;
    
    public TransactionSafetyDemo() {
        this.transactionManager = new TransactionManager();
        this.accountService = new AccountService();
        this.customerService = new CustomerService();
    }
    
    /**
     * Demonstrate concurrent transfer safety.
     * 
     * This test creates multiple threads that perform transfers between
     * the same accounts simultaneously to test for race conditions.
     */
    public void demonstrateConcurrentTransferSafety() {
        System.out.println("=== Concurrent Transfer Safety Demo ===");
        
        try {
            // Setup test accounts
            String[] accountNumbers = setupTestAccounts();
            String account1 = accountNumbers[0];
            String account2 = accountNumbers[1];
            
            // Initial balances
            BigDecimal initialBalance1 = new BigDecimal("1000.00");
            BigDecimal initialBalance2 = new BigDecimal("1000.00");
            
            System.out.println("Initial Balances:");
            System.out.println("Account 1: " + initialBalance1);
            System.out.println("Account 2: " + initialBalance2);
            
            // Create multiple threads for concurrent transfers
            int numberOfThreads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch latch = new CountDownLatch(numberOfThreads);
            List<Exception> exceptions = new ArrayList<>();
            
            // Submit concurrent transfer tasks
            for (int i = 0; i < numberOfThreads; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        if (threadId % 2 == 0) {
                            // Even threads: transfer from account1 to account2
                            transactionManager.executeTransfer(account1, account2, 
                                new BigDecimal("50.00"), "Concurrent transfer " + threadId);
                        } else {
                            // Odd threads: transfer from account2 to account1
                            transactionManager.executeTransfer(account2, account1, 
                                new BigDecimal("30.00"), "Concurrent transfer " + threadId);
                        }
                        System.out.println("Thread " + threadId + " completed successfully");
                    } catch (Exception e) {
                        synchronized (exceptions) {
                            exceptions.add(e);
                        }
                        System.err.println("Thread " + threadId + " failed: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            // Wait for all threads to complete
            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();
            
            // Check final balances
            System.out.println("\nFinal Results:");
            System.out.println("Successful transfers: " + (numberOfThreads - exceptions.size()));
            System.out.println("Failed transfers: " + exceptions.size());
            
            // The total money in the system should remain the same
            BigDecimal totalInitial = initialBalance1.add(initialBalance2);
            System.out.println("Total money should remain: " + totalInitial);
            
        } catch (Exception e) {
            System.err.println("Demo failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Demonstrate deadlock prevention.
     * 
     * This test creates scenarios that would typically cause deadlocks
     * but should be handled safely by the ordered locking mechanism.
     */
    public void demonstrateDeadlockPrevention() {
        System.out.println("\n=== Deadlock Prevention Demo ===");
        
        try {
            String[] accountNumbers = setupTestAccounts();
            String account1 = accountNumbers[0];
            String account2 = accountNumbers[1];
            
            ExecutorService executor = Executors.newFixedThreadPool(2);
            CountDownLatch latch = new CountDownLatch(2);
            
            // Thread 1: Transfer A -> B
            executor.submit(() -> {
                try {
                    System.out.println("Thread 1: Starting transfer A -> B");
                    transactionManager.executeTransfer(account1, account2, 
                        new BigDecimal("100.00"), "Deadlock test A->B");
                    System.out.println("Thread 1: Completed transfer A -> B");
                } catch (Exception e) {
                    System.err.println("Thread 1 failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
            
            // Thread 2: Transfer B -> A (potential deadlock scenario)
            executor.submit(() -> {
                try {
                    System.out.println("Thread 2: Starting transfer B -> A");
                    transactionManager.executeTransfer(account2, account1, 
                        new BigDecimal("50.00"), "Deadlock test B->A");
                    System.out.println("Thread 2: Completed transfer B -> A");
                } catch (Exception e) {
                    System.err.println("Thread 2 failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
            
            // Wait for completion
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();
            
            if (completed) {
                System.out.println("Deadlock prevention successful - both transfers completed");
            } else {
                System.err.println("Potential deadlock detected - transfers timed out");
            }
            
        } catch (Exception e) {
            System.err.println("Deadlock prevention demo failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Demonstrate rollback on failure.
     * 
     * This test shows how failed transactions are properly rolled back
     * without affecting account balances.
     */
    public void demonstrateRollbackOnFailure() {
        System.out.println("\n=== Rollback on Failure Demo ===");
        
        try {
            String[] accountNumbers = setupTestAccounts();
            String validAccount = accountNumbers[0];
            String invalidAccount = "INVALID123";
            
            System.out.println("Attempting transfer to invalid account...");
            
            try {
                transactionManager.executeTransfer(validAccount, invalidAccount, 
                    new BigDecimal("100.00"), "Transfer to invalid account");
                System.err.println("ERROR: Transfer should have failed!");
            } catch (BankingException e) {
                System.out.println("Expected failure occurred: " + e.getMessage());
                System.out.println("Account balance should remain unchanged");
            }
            
            System.out.println("Attempting withdrawal with insufficient funds...");
            
            try {
                transactionManager.executeWithdrawal(validAccount, 
                    new BigDecimal("999999.00"), "Excessive withdrawal");
                System.err.println("ERROR: Withdrawal should have failed!");
            } catch (BankingException e) {
                System.out.println("Expected failure occurred: " + e.getMessage());
                System.out.println("Account balance should remain unchanged");
            }
            
        } catch (Exception e) {
            System.err.println("Rollback demo failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Setup test accounts for demonstrations.
     */
    private String[] setupTestAccounts() throws BankingException {
        // Create test customer
        Customer customer = customerService.createCustomer(
            "Test", "User", "test@example.com", 
            "+1-555-0123", "123 Test St", 
            LocalDate.of(1990, 1, 1)
        );
        
        // Create test accounts
        Account account1 = accountService.createCheckingAccount(
            customer.getCustomerId(), new BigDecimal("1000.00"));
        Account account2 = accountService.createSavingsAccount(
            customer.getCustomerId(), new BigDecimal("1000.00"));
        
        return new String[]{account1.getAccountNumber(), account2.getAccountNumber()};
    }
    
    /**
     * Run all safety demonstrations.
     */
    public void runAllDemos() {
        System.out.println("Starting Transaction Safety Demonstrations...\n");
        
        // Initialize database connection
        DBConnection.initializePool();
        
        try {
            demonstrateConcurrentTransferSafety();
            demonstrateDeadlockPrevention();
            demonstrateRollbackOnFailure();
            
            System.out.println("\n=== All Demonstrations Completed ===");
            
        } finally {
            // Cleanup
            DBConnection.shutdown();
        }
    }
    
    /**
     * Main method to run the demonstrations.
     */
    public static void main(String[] args) {
        TransactionSafetyDemo demo = new TransactionSafetyDemo();
        demo.runAllDemos();
    }
}