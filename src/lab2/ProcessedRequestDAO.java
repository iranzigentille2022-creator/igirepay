package lab2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProcessedRequestDAO {

    public boolean isProcessed(String referenceId) {
        if (!isValidReference(referenceId)) {
            return false;
        }

        String sql = "SELECT 1 FROM processed_requests WHERE reference_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, referenceId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException e) {
            System.out.println("Could not check request.");
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean markAsProcessed(String referenceId) {
        if (!isValidReference(referenceId)) {
            System.out.println("Invalid transaction reference.");
            return false;
        }

        String sql = "INSERT INTO processed_requests (reference_id) "
                + "VALUES (?) ON CONFLICT (reference_id) DO NOTHING";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, referenceId);
            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Could not save processed request.");
            System.out.println(e.getMessage());
            return false;
        }
    }

    private boolean isValidReference(String referenceId) {
        return referenceId != null
                && referenceId.matches("[A-Za-z0-9_-]{3,100}");
    }
}
