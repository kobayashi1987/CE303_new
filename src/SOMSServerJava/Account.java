package SOMSServerJava;

/**
 * Represents a user's account.
 */
public class Account {
    private int accountNumber;
    private double balance;

    /**
     * Default constructor required for JSON deserialization.
     */
    public Account() {}

    /**
     * Constructs a new Account.
     *
     * @param accountNumber The unique account number.
     * @param balance       The initial balance of the account.
     */
    public Account(int accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    // Getters and Setters

    public int getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * Reserves funds from the account for a purchase.
     *
     * @param amount The amount to reserve.
     * @return True if reservation is successful; false otherwise.
     */
    public synchronized boolean reserveFunds(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to reserve must be positive.");
        }
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }

    /**
     * Releases previously reserved funds back to the account.
     *
     * @param amount The amount to release.
     */
    public synchronized void releaseFunds(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to release must be positive.");
        }
        balance += amount;
    }

    /**
     * Adds funds to the account.
     *
     * @param amount The amount to add.
     */
    public synchronized void addFunds(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to add must be positive.");
        }
        balance += amount;
    }

    /**
     * Withdraws funds from the account.
     *
     * @param amount The amount to withdraw.
     * @return True if withdrawal is successful; false otherwise.
     */
    public synchronized boolean withdrawFunds(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to withdraw must be positive.");
        }
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }
}