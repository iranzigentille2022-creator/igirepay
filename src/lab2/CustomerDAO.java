
package lab2;

import lab1.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CustomerDAO {

    // Register a new customer
    public void addCustomer(Customer customer) {
        String sql = "INSERT INTO customers "
                + "(full_name, email, phone_number, pin) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getFullName());
            statement.setString(2, customer.getEmail());
            statement.setString(3, customer.getPhoneNumber());
            statement.setString(4, customer.getPin());

            statement.executeUpdate();
            System.out.println("Registration successful!");

        } catch (SQLException e) {
            System.out.println(
                    "Registration failed. The email may already be registered.");
            System.out.println("Reason: " + e.getMessage());
        }
    }

    // Login using email and PIN
    public Customer authenticateCustomer(String email, String pin) {
        String sql = "SELECT customer_id, full_name, email, phone_number "
                + "FROM customers WHERE email = ? AND pin = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setString(2, pin);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new Customer(
                            result.getInt("customer_id"),
                            result.getString("full_name"),
                            result.getString("email"),
                            result.getString("phone_number")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Login failed because of a database error.");
            System.out.println("Reason: " + e.getMessage());
        }

        return null;
    }

    // Retrieve all customers for administrative use
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();

        String sql = "SELECT customer_id, full_name, email, phone_number "
                + "FROM customers ORDER BY customer_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                customers.add(new Customer(
                        result.getInt("customer_id"),
                        result.getString("full_name"),
                        result.getString("email"),
                        result.getString("phone_number")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Could not retrieve customers.");
            System.out.println("Reason: " + e.getMessage());
        }

        return customers;
    }

    // Display all customers
    public void viewCustomers() {
        List<Customer> customers = getAllCustomers();

        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }

        for (Customer customer : customers) {
            System.out.println(customer);
        }
    }

    // Update customer details
    public boolean updateCustomer(int customerId, String fullName,
                                  String email, String phoneNumber) {

        if (customerId <= 0 || fullName == null || fullName.isBlank()
                || email == null || email.isBlank()
                || phoneNumber == null || phoneNumber.isBlank()) {
            System.out.println("Please enter valid customer details.");
            return false;
        }

        String sql = "UPDATE customers "
                + "SET full_name = ?, email = ?, phone_number = ? "
                + "WHERE customer_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, fullName);
            statement.setString(2, email);
            statement.setString(3, phoneNumber);
            statement.setInt(4, customerId);

            if (statement.executeUpdate() > 0) {
                System.out.println("Customer details updated.");
                return true;
            }

            System.out.println("Customer not found.");
            return false;

        } catch (SQLException e) {
            System.out.println(
                    "Could not update details. Check whether the email is already used.");
            return false;
        }
    }

    // Change PIN after verifying the current PIN
    public boolean changePin(int customerId, String currentPin,
                             String newPin) {

        if (customerId <= 0 || currentPin == null
                || newPin == null || !newPin.matches("[0-9]{4,6}")) {
            System.out.println("New PIN must contain 4 to 6 digits.");
            return false;
        }

        String sql = "UPDATE customers SET pin = ? "
                + "WHERE customer_id = ? AND pin = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, newPin);
            statement.setInt(2, customerId);
            statement.setString(3, currentPin);

            if (statement.executeUpdate() > 0) {
                System.out.println("PIN changed successfully.");
                return true;
            }

            System.out.println("Current PIN is incorrect.");
            return false;

        } catch (SQLException e) {
            System.out.println("Could not change PIN.");
            return false;
        }
    }

    // Verify a customer's PIN
    public boolean verifyPin(int customerId, String pin) {
        String sql = "SELECT customer_id FROM customers "
                + "WHERE customer_id = ? AND pin = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);
            statement.setString(2, pin);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException e) {
            System.out.println("Could not verify PIN.");
            return false;
        }
    }

    // Ask for a PIN before using an account
    public boolean requestPinVerification(Scanner scanner, int customerId) {
        System.out.print("Enter your PIN: ");
        String pin = scanner.nextLine().trim();

        if (pin.isEmpty()) {
            System.out.println("PIN cannot be empty.");
            return false;
        }

        if (verifyPin(customerId, pin)) {
            return true;
        }

        System.out.println("Incorrect PIN.");
        return false;
    }
}
