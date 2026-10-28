
package lab2;

import lab1.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountDAO {

    // Create only one wallet and one savings account per customer
    public void addAccount(int customerId, Account account,
                           String accountType) {

        if (customerId <= 0 || account == null || accountType == null
                || !(accountType.equalsIgnoreCase("Wallet")
                || accountType.equalsIgnoreCase("Savings"))) {
            System.out.println("Invalid account details.");
            return;
        }

        String checkSql = "SELECT account_id FROM accounts "
                + "WHERE customer_id = ? "
                + "AND LOWER(account_type) = LOWER(?)";

        String insertSql = "INSERT INTO accounts "
                + "(customer_id, account_type, balance) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {

            try (PreparedStatement statement =
                         connection.prepareStatement(checkSql)) {

                statement.setInt(1, customerId);
                statement.setString(2, accountType);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        System.out.println(
                                "You already have a " + accountType
                                        + " account. You can only have one.");
                        return;
                    }
                }
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(insertSql)) {

                statement.setInt(1, customerId);
                statement.setString(2, accountType);
                statement.setBigDecimal(
                        3, BigDecimal.valueOf(account.getBalance()));

                statement.executeUpdate();
                System.out.println(accountType + " account created successfully!");
            }

        } catch (SQLException e) {
            System.out.println("Could not create account.");
            System.out.println(e.getMessage());
        }
    }

    // Retrieve active accounts belonging to a customer
    public List<Account> getAccountsForCustomer(int customerId) {
        List<Account> accounts = new ArrayList<>();

        String sql = "SELECT account_id, balance FROM accounts "
                + "WHERE customer_id = ? AND status = 'active' "
                + "ORDER BY account_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    accounts.add(new Account(
                            result.getInt("account_id"),
                            result.getDouble("balance")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not retrieve your accounts.");
            System.out.println(e.getMessage());
        }

        return accounts;
    }

    // Display active accounts belonging to this customer
    public void viewAccountsForCustomer(int customerId) {
        String sql = "SELECT account_id, account_type, balance "
                + "FROM accounts WHERE customer_id = ? "
                + "AND status = 'active' ORDER BY account_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet result = statement.executeQuery()) {
                boolean found = false;

                while (result.next()) {
                    found = true;
                    System.out.println("Account ID: "
                            + result.getInt("account_id"));
                    System.out.println("Account Type: "
                            + result.getString("account_type"));
                    System.out.println("Balance: "
                            + result.getBigDecimal("balance") + " RWF");
                    System.out.println("----------------------");
                }

                if (!found) {
                    System.out.println("You have no active accounts.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not retrieve your accounts.");
            System.out.println(e.getMessage());
        }
    }

    // View the balance of one active account
    public void viewAccountBalance(int customerId, int accountId) {
        String sql = "SELECT account_type, balance FROM accounts "
                + "WHERE customer_id = ? AND account_id = ? "
                + "AND status = 'active'";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);
            statement.setInt(2, accountId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    System.out.println("\n===== ACCOUNT BALANCE =====");
                    System.out.println("Account ID: " + accountId);
                    System.out.println("Account Type: "
                            + result.getString("account_type"));
                    System.out.println("Balance: "
                            + result.getBigDecimal("balance") + " RWF");
                } else {
                    System.out.println(
                            "Active account not found or does not belong to you.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not retrieve account balance.");
            System.out.println(e.getMessage());
        }
    }

    // Deactivate an account with a zero balance
    public void deactivateInactiveAccount(int customerId, int accountId) {
        if (customerId <= 0 || accountId <= 0) {
            System.out.println("Invalid account details.");
            return;
        }

        String sql = "UPDATE accounts SET status = 'inactive' "
                + "WHERE account_id = ? AND customer_id = ? "
                + "AND status = 'active' AND balance = 0";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, accountId);
            statement.setInt(2, customerId);

            int updated = statement.executeUpdate();

            if (updated == 1) {
                System.out.println("Account deactivated successfully.");
                System.out.println("Transaction history has been preserved.");
            } else {
                System.out.println(
                        "Account not found, already inactive, or balance is not zero.");
            }

        } catch (SQLException e) {
            System.out.println("Could not deactivate the account.");
            System.out.println(e.getMessage());
        }
    }

    // Deposit with an automatically generated reference
    public void depositToAccount(int customerId, int accountId,
                                 double amount) {
        depositToAccount(customerId, accountId, amount,
                "DEP-" + UUID.randomUUID());
    }

    // Deposit using a supplied reference
    public void depositToAccount(int customerId, int accountId,
                                 double amount, String referenceId) {
        processMoney(customerId, accountId, amount, referenceId, true);
    }

    // Withdraw with an automatically generated reference
    public void withdrawFromAccount(int customerId, int accountId,
                                    double amount) {
        withdrawFromAccount(customerId, accountId, amount,
                "WDR-" + UUID.randomUUID());
    }

    // Withdraw using a supplied reference
    public void withdrawFromAccount(int customerId, int accountId,
                                    double amount, String referenceId) {
        processMoney(customerId, accountId, amount, referenceId, false);
    }

    // Deposit or withdrawal using one database transaction
    public void processMoney(int customerId, int accountId, double amount,
                             String referenceId, boolean deposit) {

        if (customerId <= 0 || accountId <= 0
                || !Double.isFinite(amount) || amount <= 0
                || !isValidReference(referenceId)) {
            System.out.println("Enter valid transaction details.");
            return;
        }

        String accountSql = "SELECT account_type FROM accounts "
                + "WHERE account_id = ? AND customer_id = ? "
                + "AND status = 'active' FOR UPDATE";

        String updateSql = deposit
                ? "UPDATE accounts SET balance = balance + ? "
                + "WHERE account_id = ? AND customer_id = ? "
                + "AND status = 'active'"
                : "UPDATE accounts SET balance = balance - ? "
                + "WHERE account_id = ? AND customer_id = ? "
                + "AND status = 'active' AND balance >= ?";

        String transactionSql = "INSERT INTO transactions "
                + "(reference_id, account_id, amount, transaction_type) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String accountType;

                try (PreparedStatement statement =
                             connection.prepareStatement(accountSql)) {
                    statement.setInt(1, accountId);
                    statement.setInt(2, customerId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            System.out.println(
                                    "Active account not found or does not belong to you.");
                            return;
                        }
                        accountType = result.getString("account_type");
                    }
                }

                if (!registerRequest(connection, referenceId)) {
                    connection.rollback();
                    System.out.println(
                            "Duplicate request. Transaction rejected.");
                    return;
                }

                BigDecimal transactionAmount = BigDecimal.valueOf(amount);
                BigDecimal balanceChange = transactionAmount;

                // Savings withdrawals have a 100 RWF fee
                if (!deposit && accountType.equalsIgnoreCase("Savings")) {
                    balanceChange = transactionAmount.add(
                            BigDecimal.valueOf(100));
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(updateSql)) {
                    statement.setBigDecimal(1, balanceChange);
                    statement.setInt(2, accountId);
                    statement.setInt(3, customerId);

                    if (!deposit) {
                        statement.setBigDecimal(4, balanceChange);
                    }

                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        System.out.println(
                                "Transaction failed. Check your balance.");
                        return;
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(transactionSql)) {
                    statement.setString(1, referenceId);
                    statement.setInt(2, accountId);
                    statement.setBigDecimal(3, transactionAmount);
                    statement.setString(4,
                            deposit ? "Deposit" : "Withdrawal");
                    statement.executeUpdate();
                }

                connection.commit();

                System.out.println(deposit
                        ? "Deposit successful!" : "Withdrawal successful!");
                if (!deposit && accountType.equalsIgnoreCase("Savings")) {
                    System.out.println("Withdrawal fee: 100 RWF");
                }
                System.out.println("Reference ID: " + referenceId);

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Transaction failed.");
            System.out.println(e.getMessage());
        }
    }

    // Transfer with an automatically generated reference
    public void transferMoney(int customerId, int senderAccountId,
                              int receiverAccountId, double amount) {
        transferMoney(customerId, senderAccountId, receiverAccountId,
                amount, "TRF-" + UUID.randomUUID());
    }

    // Transfer using a supplied reference so a retry can reuse it
    public void transferMoney(int customerId, int senderAccountId,
                              int receiverAccountId, double amount,
                              String referenceId) {

        if (customerId <= 0 || senderAccountId <= 0
                || receiverAccountId <= 0
                || senderAccountId == receiverAccountId
                || !Double.isFinite(amount) || amount <= 0
                || !isValidReference(referenceId)) {
            System.out.println("Invalid transfer details.");
            return;
        }

        BigDecimal transferAmount = BigDecimal.valueOf(amount);

        String checkSql = "SELECT account_id FROM accounts "
                + "WHERE customer_id = ? AND account_id IN (?, ?) "
                + "AND status = 'active' ORDER BY account_id FOR UPDATE";

        String withdrawSql = "UPDATE accounts SET balance = balance - ? "
                + "WHERE account_id = ? AND customer_id = ? "
                + "AND status = 'active' AND balance >= ?";

        String depositSql = "UPDATE accounts SET balance = balance + ? "
                + "WHERE account_id = ? AND customer_id = ? "
                + "AND status = 'active'";

        String transactionSql = "INSERT INTO transactions "
                + "(reference_id, account_id, amount, transaction_type) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                boolean senderFound = false;
                boolean receiverFound = false;

                try (PreparedStatement statement =
                             connection.prepareStatement(checkSql)) {
                    statement.setInt(1, customerId);
                    statement.setInt(2, senderAccountId);
                    statement.setInt(3, receiverAccountId);

                    try (ResultSet result = statement.executeQuery()) {
                        while (result.next()) {
                            int id = result.getInt("account_id");
                            if (id == senderAccountId) senderFound = true;
                            if (id == receiverAccountId) receiverFound = true;
                        }
                    }
                }

                if (!senderFound || !receiverFound) {
                    connection.rollback();
                    System.out.println(
                            "One or both active accounts were not found "
                                    + "or do not belong to you.");
                    return;
                }

                if (!registerRequest(connection, referenceId)) {
                    connection.rollback();
                    System.out.println("Duplicate transfer rejected.");
                    return;
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(withdrawSql)) {
                    statement.setBigDecimal(1, transferAmount);
                    statement.setInt(2, senderAccountId);
                    statement.setInt(3, customerId);
                    statement.setBigDecimal(4, transferAmount);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Insufficient balance or sender account unavailable.");
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(depositSql)) {
                    statement.setBigDecimal(1, transferAmount);
                    statement.setInt(2, receiverAccountId);
                    statement.setInt(3, customerId);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Could not add money to receiver.");
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(transactionSql)) {
                    statement.setString(1, referenceId + "-OUT");
                    statement.setInt(2, senderAccountId);
                    statement.setBigDecimal(3, transferAmount);
                    statement.setString(4, "Transfer");
                    statement.executeUpdate();

                    statement.setString(1, referenceId + "-IN");
                    statement.setInt(2, receiverAccountId);
                    statement.setBigDecimal(3, transferAmount);
                    statement.setString(4, "Transfer");
                    statement.executeUpdate();
                }

                connection.commit();
                System.out.println("Transfer successful!");
                System.out.println("Amount: " + transferAmount + " RWF");
                System.out.println("Reference ID: " + referenceId);

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Transfer failed. Transaction rolled back.");
            System.out.println(e.getMessage());
        }
    }

    // Save a reference only if it has not already been processed
    private boolean registerRequest(Connection connection, String referenceId)
            throws SQLException {

        String sql = "INSERT INTO processed_requests (reference_id) "
                + "VALUES (?) ON CONFLICT (reference_id) DO NOTHING";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, referenceId);
            return statement.executeUpdate() == 1;
        }
    }

    // Validate transaction references
    private boolean isValidReference(String referenceId) {
        return referenceId != null
                && referenceId.matches("[A-Za-z0-9_-]{3,100}");
    }
}
