package com.banking.model;

public enum TransactionType {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    TRANSFER("Transfer"),
    INTEREST_CREDIT("Interest Credit"),
    FEE_DEBIT("Fee Debit"),
    OVERDRAFT_FEE("Overdraft Fee");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}