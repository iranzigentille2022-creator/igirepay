
package lab1;

public class Account {

    private int id;
    private double balance;

    // Constructor
    public Account(int id, double balance) {
        if (id < 0) {
            throw new IllegalArgumentException(
                    "Account ID cannot be negative.");
        }

        if (!Double.isFinite(balance) || balance < 0) {
            throw new IllegalArgumentException(
                    "Balance must be zero or greater.");
        }

        this.id = id;
        this.balance = balance;
    }

    // Getters
    public int getId() {
        return id;
    }

    public double getBalance() {
        return balance;
    }

    // Setters
    public void setId(int id) {
        if (id > 0) {
            this.id = id;
        } else {
            System.out.println("Invalid account ID.");
        }
    }

    public void setBalance(double balance) {
        if (Double.isFinite(balance) && balance >= 0) {
            this.balance = balance;
        } else {
            System.out.println("Invalid balance.");
        }
    }

    // Deposit money
    public void deposit(double amount) {
        if (Double.isFinite(amount) && amount > 0) {
            balance += amount;
            System.out.println("Deposit successful.");
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    // Withdraw money
    public void withdraw(double amount) {
        if (Double.isFinite(amount)
                && amount > 0
                && amount <= balance) {

            balance -= amount;
            System.out.println("Withdrawal successful.");

        } else {
            System.out.println(
                    "Withdrawal failed. Check the amount and balance.");
        }
    }

    // Display account information
    @Override
    public String toString() {
        return "Account ID: " + id
                + ", Balance: " + balance + " RWF";
    }
}
