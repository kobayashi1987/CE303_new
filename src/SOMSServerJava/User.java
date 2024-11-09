package SOMSServerJava;

/**
 * User represents a user in the system with credentials, role, and account information.
 */
public class User {
    private String userID;
    private String password;
    private String role; // "customer" or "seller"
    private int accountNumber;

    // Constructors
    public User() {}

    public User(String userID, String password, String role, int accountNumber) {
        this.userID = userID;
        this.password = password;
        this.role = role;
        this.accountNumber = accountNumber;
    }

    // Getters and Setters
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    // Note: In a production environment, passwords should be hashed.
    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    /**
     * Sets the role of the user. Must be either "customer" or "seller".
     *
     * @param role The role to set.
     */
    public void setRole(String role) {
        if (role.equalsIgnoreCase("customer") || role.equalsIgnoreCase("seller")) {
            this.role = role.toLowerCase();
        } else {
            throw new IllegalArgumentException("Invalid role. Must be 'customer' or 'seller'.");
        }
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }
}