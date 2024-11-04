package SOMSServerJava;

public class User {
    private String userID;
    private String password;
    private String role; // e.g., customer, admin
    private int accountNumber;

    public User(String userID, String password, String role, int accountNumber) {
        this.userID = userID;
        this.password = password;
        this.role = role;
        this.accountNumber = accountNumber;
    }

    // Getters and setters

    public String getUserID() {
        return userID;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    // Provides a string representation of the user
    @Override
    public String toString() {
        return "User{" +
                "userID='" + userID + '\'' +
                ", role='" + role + '\'' +
                ", accountNumber=" + accountNumber +
                '}';
    }
}