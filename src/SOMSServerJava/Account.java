package SOMSServerJava;

public class Account {
    private final int clientId;            // Immutable client ID associated with the account
    private final int accountNumber;       // Immutable account number
    private int balance;                   // Mutable balance field, access controlled for thread safety

    /**
     * Creates an Account instance with a specific client ID, account number, and initial balance.
     *
     * @param clientId the ID of the client who owns this account
     * @param accountNumber the unique number assigned to this account
     * @param initialBalance the initial balance for the account
     * @throws IllegalArgumentException if the initial balance is negative
     */
    public Account(int clientId, int accountNumber, int initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative.");
        }
        this.clientId = clientId;
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
    }

    /**
     * Gets the client ID associated with this account.
     *
     * @return the client ID
     */
    public int getClientId() {
        return clientId;
    }

    /**
     * Gets the account number.
     *
     * @return the account number
     */
    public int getAccountNumber() {
        return accountNumber;
    }

    /**
     * Gets the current balance of the account.
     *
     * @return the current balance
     */
    public synchronized int getBalance() {
        return balance;
    }

    /**
     * Sets the balance to a new amount.
     *
     * @param newBalance the new balance amount
     * @throws IllegalArgumentException if the new balance is negative
     */
    public synchronized void setBalance(int newBalance) {
        if (newBalance < 0) {
            throw new IllegalArgumentException("Balance cannot be set to a negative value.");
        }
        this.balance = newBalance;
    }

    /**
     * Increases the account balance by a specified amount.
     *
     * @param amount the amount to add to the balance
     * @throws IllegalArgumentException if the amount is negative
     */
    public synchronized void deposit(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Deposit amount cannot be negative.");
        }
        this.balance += amount;
    }

    /**
     * Decreases the account balance by a specified amount.
     *
     * @param amount the amount to subtract from the balance
     * @throws IllegalArgumentException if the amount is negative or if it exceeds the current balance
     */
    public synchronized void withdraw(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Withdrawal amount cannot be negative.");
        }
        if (amount > this.balance) {
            throw new IllegalArgumentException("Insufficient balance for the withdrawal.");
        }
        this.balance -= amount;
    }

    @Override
    public String toString() {
        return String.format("Account[ClientID=%d, AccountNumber=%d, Balance=%d]", clientId, accountNumber, balance);
    }
}