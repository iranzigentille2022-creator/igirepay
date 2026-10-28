
package lab3;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import lab2.AccountDAO;
import lab2.CustomerDAO;
import lab2.DatabaseConnection;
import lab2.TransactionDAO;
import lab1.Customer;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final CustomerDAO customerDAO = new CustomerDAO();
    private static final AccountDAO accountDAO = new AccountDAO();
    private static final TransactionDAO transactionDAO =
            new TransactionDAO();

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            System.out.println("\n========== IGIREPAY ==========");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("0. Exit");

            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1:
                    register();
                    break;
                case 2:
                    login();
                    break;
                case 0:
                    running = false;
                    System.out.println("Thank you for using IGIREPAY!");
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }

        scanner.close();
    }

    // ================= REGISTER =================

    private static void register() {
        System.out.println("\n===== REGISTER =====");

        System.out.print("Enter your full name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter your email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter your phone number: ");
        String phone = scanner.nextLine().trim();

        System.out.print("Enter your PIN: ");
        String pin = scanner.nextLine().trim();

        if (name.isEmpty() || email.isEmpty()
                || phone.isEmpty() || pin.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }

        if (!pin.matches("\\d{4,6}")) {
            System.out.println("PIN must contain 4 to 6 digits.");
            return;
        }

        System.out.print("Confirm your PIN: ");
        String confirmPin = scanner.nextLine().trim();

        if (!pin.equals(confirmPin)) {
            System.out.println("PINs do not match.");
            return;
        }

        Customer customer =
                new Customer(0, name, email, phone, pin);

        customerDAO.addCustomer(customer);
    }

    // ================= LOGIN =================

    private static void login() {
        System.out.println("\n===== LOGIN =====");

        System.out.print("Enter your email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter your PIN: ");
        String pin = scanner.nextLine().trim();

        Customer customer =
                customerDAO.authenticateCustomer(email, pin);

        if (customer != null) {
            System.out.println("Login successful!");
            customerMenu(customer);
        } else {
            System.out.println(
                    "Login failed. Check your email and PIN.");
        }
    }

    // ================= CUSTOMER MENU =================

    private static void customerMenu(Customer customer) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n========== CUSTOMER MENU ==========");
            System.out.println("Welcome, " + customer.getFullName());
            System.out.println("1. View My Accounts");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Transfer Money");
            System.out.println("5. View Transaction History");
            System.out.println("6. Reports");
            System.out.println("7. Change PIN");
            System.out.println("0. Logout");

            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1:
                    accountDAO.viewAccountsForCustomer(
                            customer.getId());
                    break;
                case 2:
                    depositMoney(customer);
                    break;
                case 3:
                    withdrawMoney(customer);
                    break;
                case 4:
                    transferMoney(customer);
                    break;
                case 5:
                    transactionDAO.viewTransactionHistory(
                            customer.getId());
                    break;
                case 6:
                    reportsMenu(customer);
                    break;
                case 7:
                    changePin(customer);
                    break;
                case 0:
                    loggedIn = false;
                    System.out.println("You have logged out.");
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    // ================= DEPOSIT =================

    private static void depositMoney(Customer customer) {
        System.out.println("\n===== DEPOSIT MONEY =====");

        int accountId = readInt("Enter your account ID: ");
        double amount = readDouble("Enter amount to deposit: ");

        accountDAO.depositToAccount(
                customer.getId(), accountId, amount);
    }

    // ================= WITHDRAW =================

    private static void withdrawMoney(Customer customer) {
        System.out.println("\n===== WITHDRAW MONEY =====");

        int accountId = readInt("Enter your account ID: ");
        double amount = readDouble("Enter amount to withdraw: ");

        accountDAO.withdrawFromAccount(
                customer.getId(), accountId, amount);
    }

    // ================= TRANSFER =================

    private static void transferMoney(Customer customer) {
        System.out.println("\n===== TRANSFER MONEY =====");

        int sourceAccountId =
                readInt("Enter your source account ID: ");

        int destinationAccountId =
                readInt("Enter destination account ID: ");

        double amount =
                readDouble("Enter amount to transfer: ");

        if (sourceAccountId == destinationAccountId) {
            System.out.println("Choose two different accounts.");
            return;
        }

        accountDAO.transferMoney(
                customer.getId(),
                sourceAccountId,
                destinationAccountId,
                amount);
    }

    // ================= CHANGE PIN =================

    private static void changePin(Customer customer) {
        System.out.println("\n===== CHANGE PIN =====");

        System.out.print("Enter your current PIN: ");
        String currentPin = scanner.nextLine().trim();

        System.out.print("Enter your new PIN: ");
        String newPin = scanner.nextLine().trim();

        System.out.print("Confirm your new PIN: ");
        String confirmPin = scanner.nextLine().trim();

        if (!newPin.matches("\\d{4,6}")) {
            System.out.println(
                    "PIN must contain 4 to 6 digits.");
            return;
        }

        if (!newPin.equals(confirmPin)) {
            System.out.println("The new PINs do not match.");
            return;
        }

        if (currentPin.equals(newPin)) {
            System.out.println("Choose a different new PIN.");
            return;
        }

        customerDAO.changePin(
                customer.getId(), currentPin, newPin);
    }

    // ================= REPORTS MENU =================

    private static void reportsMenu(Customer customer) {
        boolean inReportsMenu = true;

        while (inReportsMenu) {
            System.out.println("\n========== REPORTS ==========");
            System.out.println("1. Export All Transactions to CSV");
            System.out.println("2. Daily Transaction Summary");
            System.out.println("3. Customer Transaction Statement");
            System.out.println("4. Export Customer Statement to CSV");
            System.out.println("0. Back to Customer Menu");

            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1:
                    exportTransactions(customer.getId());
                    break;
                case 2:
                    dailySummary(customer.getId());
                    break;
                case 3:
                    customerStatement(customer);
                    break;
                case 4:
                    exportCustomerStatement(customer);
                    break;
                case 0:
                    inReportsMenu = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    // ================= EXPORT ALL TRANSACTIONS =================

    private static void exportTransactions(int customerId) {
        String sql =
                "SELECT t.transaction_id, t.account_id, " +
                        "t.transaction_time, t.transaction_type, " +
                        "t.amount, t.reference_id " +
                        "FROM transactions t " +
                        "JOIN accounts a ON t.account_id = a.account_id " +
                        "WHERE a.customer_id = ? " +
                        "ORDER BY t.transaction_time, t.transaction_id";

        String fileName = "transaction_report_" +
                customerId + ".csv";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery();
                 FileWriter writer = new FileWriter(fileName)) {

                writer.write(
                        "Transaction ID,Account ID,Date and Time," +
                                "Type,Amount,Reference ID\n");

                int count = 0;

                while (rs.next()) {
                    writer.write(
                            rs.getInt("transaction_id") + "," +
                                    rs.getInt("account_id") + "," +
                                    csvValue(String.valueOf(
                                            rs.getTimestamp("transaction_time"))) + "," +
                                    csvValue(rs.getString("transaction_type")) + "," +
                                    rs.getBigDecimal("amount") + "," +
                                    csvValue(rs.getString("reference_id")) +
                                    "\n");

                    count++;
                }

                System.out.println(
                        count == 0
                                ? "No transactions found to export."
                                : "CSV report created: " + fileName);
                System.out.println("Transactions exported: " + count);
            }

        } catch (SQLException | IOException e) {
            System.out.println(
                    "Could not export report: " + e.getMessage());
        }
    }

    // ================= CUSTOMER STATEMENT =================

    private static void customerStatement(Customer customer) {
        System.out.println(
                "\n===== CUSTOMER TRANSACTION STATEMENT =====");
        System.out.println("Customer Name: " + customer.getFullName());
        System.out.println("Customer ID: " + customer.getId());

        LocalDate fromDate =
                readDate("Enter start date (YYYY-MM-DD): ");
        if (fromDate == null) return;

        LocalDate toDate =
                readDate("Enter end date (YYYY-MM-DD): ");
        if (toDate == null) return;

        if (fromDate.isAfter(toDate)) {
            System.out.println(
                    "Start date cannot be after end date.");
            return;
        }

        String sql =
                "SELECT t.transaction_id, t.account_id, " +
                        "t.transaction_time, t.transaction_type, " +
                        "t.amount, t.reference_id " +
                        "FROM transactions t " +
                        "JOIN accounts a ON t.account_id = a.account_id " +
                        "WHERE a.customer_id = ? " +
                        "AND t.transaction_time >= ? " +
                        "AND t.transaction_time < ? " +
                        "ORDER BY t.transaction_time, t.transaction_id";

        int count = 0;
        BigDecimal total = BigDecimal.ZERO;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customer.getId());
            stmt.setTimestamp(
                    2, java.sql.Timestamp.valueOf(fromDate.atStartOfDay()));
            stmt.setTimestamp(
                    3, java.sql.Timestamp.valueOf(
                            toDate.plusDays(1).atStartOfDay()));

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println(
                        "Period: " + fromDate + " to " + toDate);
                System.out.println("----------------------------------------");

                while (rs.next()) {
                    count++;

                    BigDecimal amount = rs.getBigDecimal("amount");
                    if (amount != null) {
                        total = total.add(amount);
                    }

                    System.out.println(
                            "Transaction ID: " +
                                    rs.getInt("transaction_id"));
                    System.out.println(
                            "Account ID: " + rs.getInt("account_id"));
                    System.out.println(
                            "Date and Time: " +
                                    rs.getTimestamp("transaction_time"));
                    System.out.println(
                            "Type: " + rs.getString("transaction_type"));
                    System.out.println("Amount: " + amount + " RWF");
                    System.out.println(
                            "Reference ID: " +
                                    rs.getString("reference_id"));
                    System.out.println("----------------------------------------");
                }
            }

            System.out.println("Total transactions: " + count);
            System.out.println("Total recorded amount: " +
                    total + " RWF");

            if (count == 0) {
                System.out.println(
                        "No transactions found in this date range.");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not generate statement: " + e.getMessage());
        }
    }

    // ================= EXPORT CUSTOMER STATEMENT =================

    private static void exportCustomerStatement(Customer customer) {
        System.out.println("\n===== EXPORT CUSTOMER STATEMENT =====");

        LocalDate fromDate =
                readDate("Enter start date (YYYY-MM-DD): ");
        if (fromDate == null) return;

        LocalDate toDate =
                readDate("Enter end date (YYYY-MM-DD): ");
        if (toDate == null) return;

        if (fromDate.isAfter(toDate)) {
            System.out.println(
                    "Start date cannot be after end date.");
            return;
        }

        String sql =
                "SELECT t.transaction_id, t.account_id, " +
                        "t.transaction_time, t.transaction_type, " +
                        "t.amount, t.reference_id " +
                        "FROM transactions t " +
                        "JOIN accounts a ON t.account_id = a.account_id " +
                        "WHERE a.customer_id = ? " +
                        "AND t.transaction_time >= ? " +
                        "AND t.transaction_time < ? " +
                        "ORDER BY t.transaction_time, t.transaction_id";

        String fileName = "customer_statement_" +
                customer.getId() + "_" + fromDate + "_" +
                toDate + ".csv";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customer.getId());
            stmt.setTimestamp(
                    2, java.sql.Timestamp.valueOf(fromDate.atStartOfDay()));
            stmt.setTimestamp(
                    3, java.sql.Timestamp.valueOf(
                            toDate.plusDays(1).atStartOfDay()));

            try (ResultSet rs = stmt.executeQuery();
                 FileWriter writer = new FileWriter(fileName)) {

                writer.write(
                        "Customer Name,Customer ID,Transaction ID," +
                                "Account ID,Date and Time,Type,Amount,Reference ID\n");

                int count = 0;
                BigDecimal total = BigDecimal.ZERO;

                while (rs.next()) {
                    BigDecimal amount = rs.getBigDecimal("amount");
                    if (amount != null) {
                        total = total.add(amount);
                    }

                    writer.write(
                            csvValue(customer.getFullName()) + "," +
                                    customer.getId() + "," +
                                    rs.getInt("transaction_id") + "," +
                                    rs.getInt("account_id") + "," +
                                    csvValue(String.valueOf(
                                            rs.getTimestamp("transaction_time"))) + "," +
                                    csvValue(rs.getString("transaction_type")) + "," +
                                    amount + "," +
                                    csvValue(rs.getString("reference_id")) +
                                    "\n");

                    count++;
                }

                writer.write("\n");
                writer.write("Start Date," + fromDate + "\n");
                writer.write("End Date," + toDate + "\n");
                writer.write("Total Transactions," + count + "\n");
                writer.write("Total Recorded Amount," + total + "\n");

                System.out.println("Statement export completed.");
                System.out.println("Transactions exported: " + count);
                System.out.println("Total recorded amount: " +
                        total + " RWF");
                System.out.println("File created: " + fileName);
            }

        } catch (SQLException | IOException e) {
            System.out.println(
                    "Could not export statement: " + e.getMessage());
        }
    }

    // ================= DAILY SUMMARY =================

    private static void dailySummary(int customerId) {
        LocalDate date = readDate("Enter date (YYYY-MM-DD): ");
        if (date == null) return;

        String sql =
                "SELECT t.transaction_type, " +
                        "COUNT(*) AS total_transactions, " +
                        "SUM(t.amount) AS total_amount " +
                        "FROM transactions t " +
                        "JOIN accounts a ON t.account_id = a.account_id " +
                        "WHERE a.customer_id = ? " +
                        "AND t.transaction_time >= ? " +
                        "AND t.transaction_time < ? " +
                        "GROUP BY t.transaction_type " +
                        "ORDER BY t.transaction_type";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            stmt.setTimestamp(
                    2, java.sql.Timestamp.valueOf(date.atStartOfDay()));
            stmt.setTimestamp(
                    3, java.sql.Timestamp.valueOf(
                            date.plusDays(1).atStartOfDay()));

            try (ResultSet rs = stmt.executeQuery()) {
                boolean found = false;

                System.out.println(
                        "\n===== DAILY TRANSACTION SUMMARY =====");
                System.out.println("Date: " + date);

                while (rs.next()) {
                    found = true;

                    System.out.println(
                            "Transaction type: " +
                                    rs.getString("transaction_type"));
                    System.out.println(
                            "Number of transactions: " +
                                    rs.getInt("total_transactions"));
                    System.out.println(
                            "Total amount: " +
                                    rs.getBigDecimal("total_amount") + " RWF");
                    System.out.println("------------------------------");
                }

                if (!found) {
                    System.out.println(
                            "No transactions found for this date.");
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not generate summary: " + e.getMessage());
        }
    }

    // ================= CSV HELPER =================

    private static String csvValue(String value) {
        if (value == null) {
            return "";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // ================= DATE INPUT =================

    private static LocalDate readDate(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date. Use YYYY-MM-DD.");
            return null;
        }
    }

    // ================= NUMBER INPUT =================

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(
                        "Please enter a valid whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                double value = Double.parseDouble(input);

                if (Double.isFinite(value) && value > 0) {
                    return value;
                }

                System.out.println(
                        "Enter an amount greater than zero.");

            } catch (NumberFormatException e) {
                System.out.println(
                        "Please enter a valid amount.");
            }
        }
    }
}
