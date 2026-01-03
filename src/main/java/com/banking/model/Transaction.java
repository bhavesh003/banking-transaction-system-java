package com.banking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {
    private String transactionId;
    private String fromAccountNumber;
    private String toAccountNumber;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private TransactionStatus status;
    private LocalDateTime timestamp;
    private BigDecimal balanceAfter;

    public Transaction(String transactionId, String fromAccountNumber, String toAccountNumber,
                      TransactionType type, BigDecimal amount, String description) {
        this.transactionId = transactionId;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.status = TransactionStatus.PENDING;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public String getFromAccountNumber() { return fromAccountNumber; }
    public String getToAccountNumber() { return toAccountNumber; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public TransactionStatus getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return String.format("Transaction{id='%s', type=%s, amount=%s, status=%s, timestamp=%s}",
                transactionId, type, amount, status, timestamp);
    }
}