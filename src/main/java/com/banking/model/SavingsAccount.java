package com.banking.model;

import java.math.BigDecimal;

public class SavingsAccount extends Account {
    private static final BigDecimal MINIMUM_BALANCE = new BigDecimal("100.00");
    private static final BigDecimal WITHDRAWAL_LIMIT = new BigDecimal("5000.00");
    private BigDecimal interestRate;

    public SavingsAccount(String accountNumber, String customerId, BigDecimal initialBalance) {
        super(accountNumber, customerId, initialBalance);
        this.interestRate = new BigDecimal("0.025"); // 2.5% annual interest
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.SAVINGS;
    }

    @Override
    public BigDecimal getMinimumBalance() {
        return MINIMUM_BALANCE;
    }

    @Override
    public BigDecimal getWithdrawalLimit() {
        return WITHDRAWAL_LIMIT;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal calculateInterest() {
        return getBalance().multiply(interestRate).divide(new BigDecimal("12"), 2, java.math.RoundingMode.HALF_UP); // Monthly interest
    }
}