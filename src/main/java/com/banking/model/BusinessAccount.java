package com.banking.model;

import java.math.BigDecimal;

public class BusinessAccount extends Account {
    private static final BigDecimal MINIMUM_BALANCE = new BigDecimal("1000.00");
    private static final BigDecimal WITHDRAWAL_LIMIT = new BigDecimal("50000.00");
    private String businessName;
    private String taxId;

    public BusinessAccount(String accountNumber, String customerId, BigDecimal initialBalance, 
                          String businessName, String taxId) {
        super(accountNumber, customerId, initialBalance);
        this.businessName = businessName;
        this.taxId = taxId;
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.BUSINESS;
    }

    @Override
    public BigDecimal getMinimumBalance() {
        return MINIMUM_BALANCE;
    }

    @Override
    public BigDecimal getWithdrawalLimit() {
        return WITHDRAWAL_LIMIT;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }
}