package SOMSServerJava;

/**
 * User represents a user in the system with credentials and account information.
 */
public class User {
    private String userID;
    private String password;
    private String role;
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

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }
}