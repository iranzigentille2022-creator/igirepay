
package lab2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionDAO {

    public void viewTransactionHistory(int customerId) {

        String sql = "SELECT t.* FROM transactions t " +
                "JOIN accounts a ON t.account_id = a.account_id " +
                "WHERE a.customer_id = ? " +
                "ORDER BY t.transaction_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {

                boolean found = false;

                while (rs.next()) {
                    found = true;

                    System.out.println("\n===== TRANSACTION =====");

                    System.out.println(
                            "Transaction ID: "
                                    + rs.getInt("transaction_id")
                    );

                    System.out.println(
                            "Account ID: "
                                    + rs.getInt("account_id")
                    );

                    System.out.println(
                            "Transaction Type: "
                                    + rs.getString("transaction_type")
                    );

                    System.out.println(
                            "Amount: "
                                    + rs.getDouble("amount")
                                    + " RWF"
                    );

                    System.out.println(
                            "Reference ID: "
                                    + rs.getString("reference_id")
                    );
                }

                if (!found) {
                    System.out.println("No transactions found.");
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error viewing transaction history: "
                            + e.getMessage()
            );
        }
    }
}
