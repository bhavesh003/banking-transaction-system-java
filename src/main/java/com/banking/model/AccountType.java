package com.banking.model;

public enum AccountType {
    SAVINGS("Savings Account"),
    CHECKING("Checking Account"),
    BUSINESS("Business Account");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}