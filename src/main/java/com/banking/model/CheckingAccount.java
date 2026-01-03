package com.banking.model;

import java.math.BigDecimal;

public class CheckingAccount extends Account {
    private static final BigDecimal MINIMUM_BALANCE = new BigDecimal("50.00");
    private static final BigDecimal WITHDRAWAL_LIMIT = new BigDecimal("10000.00");
    private BigDecimal overdraftLimit;

    public CheckingAccount(String accountNumber, String customerId, BigDecimal initialBalance) {
        super(accountNumber, customerId, initialBalance);
        this.overdraftLimit = new BigDecimal("500.00"); // Default overdraft limit
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.CHECKING;
    }

    @Override
    public BigDecimal getMinimumBalance() {
        return MINIMUM_BALANCE;
    }

    @Override
    public BigDecimal getWithdrawalLimit() {
        return WITHDRAWAL_LIMIT;
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(BigDecimal overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public boolean canWithdraw(BigDecimal amount) {
        BigDecimal availableBalance = getBalance().add(overdraftLimit);
        return amount.compareTo(availableBalance) <= 0;
    }
}