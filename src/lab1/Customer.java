
package lab1;

public class Customer {
    private int id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String pin;

    // Constructor for customers without a PIN
    public Customer(int id, String fullName, String email,
                    String phoneNumber) {
        this.id = id;
        setFullName(fullName);
        setEmail(email);
        setPhoneNumber(phoneNumber);
    }

    // Constructor for registering a customer with a PIN
    public Customer(int id, String fullName, String email,
                    String phoneNumber, String pin) {
        this(id, fullName, email, phoneNumber);
        setPin(pin);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        this.fullName = fullName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email.");
        }
        this.email = email.trim();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone is required.");
        }
        this.phoneNumber = phoneNumber.trim();
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        if (pin == null || !pin.matches("\\d{4,6}")) {
            throw new IllegalArgumentException(
                    "PIN must contain 4 to 6 digits.");
        }
        this.pin = pin;
    }

    @Override
    public String toString() {
        return "Customer ID: " + id
                + ", Name: " + fullName
                + ", Email: " + email
                + ", Phone: " + phoneNumber;
    }
}
