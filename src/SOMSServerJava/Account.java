package SOMSServerJava;

/**
 * Account represents a user's account with balance and reserved funds.
 */
public class Account {
    private int customerId;
    private int accountNumber;
    private double balance;
    private double reservedFunds;

    // Constructors
    public Account() {}

    public Account(int customerId, int accountNumber, double balance) {
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.reservedFunds = 0.0;
    }

    // Getters and Setters
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    /**
     * Sets the balance of the account.
     *
     * @param balance The new balance.
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getReservedFunds() {
        return reservedFunds;
    }

    /**
     * Sets the reserved funds for pending purchases.
     *
     * @param reservedFunds The amount to reserve.
     */
    public void setReservedFunds(double reservedFunds) {
        this.reservedFunds = reservedFunds;
    }

    /**
     * Adds funds to the account balance.
     *
     * @param amount The amount to add.
     */
    public void addFunds(double amount) {
        this.balance += amount;
    }

    /**
     * Reserves funds for a purchase.
     *
     * @param amount The amount to reserve.
     * @return True if reservation is successful, else false.
     */
    public boolean reserveFunds(double amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            this.reservedFunds += amount;
            return true;
        }
        return false;
    }

    /**
     * Releases reserved funds when a purchase is canceled.
     *
     * @param amount The amount to release.
     */
    public void releaseFunds(double amount) {
        this.reservedFunds -= amount;
        this.balance += amount;
    }

    /**
     * Completes a purchase by deducting reserved funds.
     *
     * @param amount The amount to deduct.
     */
    public void completePurchase(double amount) {
        this.reservedFunds -= amount;
        // Funds have already been reserved, so no action on balance.
    }
}