
package lab1;

import java.time.LocalDateTime;

public class Transaction {

    private int id;
    private String referenceId;
    private double amount;
    private String transactionType;
    private LocalDateTime timestamp;

    public Transaction(int id, String referenceId, double amount,
                       String transactionType, LocalDateTime timestamp) {

        this.id = id;
        setReferenceId(referenceId);
        setAmount(amount);
        setTransactionType(transactionType);
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id >= 0) {
            this.id = id;
        }
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        if (referenceId != null
                && referenceId.matches("[A-Za-z0-9_-]{3,100}")) {
            this.referenceId = referenceId;
        } else {
            throw new IllegalArgumentException(
                    "Reference must contain 3-100 letters, numbers, hyphens, or underscores.");
        }
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (Double.isFinite(amount) && amount > 0) {
            this.amount = amount;
        } else {
            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero.");
        }
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        if (transactionType != null
                && (transactionType.equals("Deposit")
                || transactionType.equals("Withdrawal")
                || transactionType.equals("Transfer"))) {
            this.transactionType = transactionType;
        } else {
            throw new IllegalArgumentException(
                    "Transaction type must be Deposit, Withdrawal, or Transfer.");
        }
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Transaction ID: " + id
                + ", Reference ID: " + referenceId
                + ", Amount: " + amount
                + ", Type: " + transactionType
                + ", Timestamp: " + timestamp;
    }
}
