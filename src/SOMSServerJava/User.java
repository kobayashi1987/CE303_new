package SOMSServerJava;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a user in the SOMS system.
 */
public class User {
    @SerializedName("userID")
    private String userID;

    @SerializedName("password")
    private String password;

    @SerializedName("role")
    private String role;

    @SerializedName("name")
    private String name; // Added field for user's name

    @SerializedName("accountNumber")
    private int accountNumber;

    /**
     * Default constructor required for JSON deserialization.
     */
    public User() {
    }

    /**
     * Parameterized constructor for creating a new User.
     *
     * @param userID        The unique identifier for the user.
     * @param password      The user's password.
     * @param role          The role of the user (e.g., customer, seller).
     * @param name          The name of the user.
     * @param accountNumber The associated account number.
     */
    public User(String userID, String password, String role, String name, int accountNumber) {
        this.userID = userID;
        this.password = password;
        this.role = role;
        this.name = name;
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

    /**
     * Sets the user's password.
     *
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     *
     * @param role The role to set (e.g., customer, seller).
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Retrieves the user's name.
     *
     * @return The user's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's name.
     *
     * @param name The name to set for the user.
     */
    public void setName(String name) {
        this.name = name;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    /**
     * Sets the user's account number.
     *
     * @param accountNumber The account number to associate with the user.
     */
    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }
}