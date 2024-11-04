package SOMSServerJava;

public class Account {
    private final int customerId;
    private final int accountNumber;
    private int balance;

    public Account(int customerId, int accountNumber, int initialBalance) {
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
    }

    // Getter for account number
    public int getAccountNumber() {
        return accountNumber;
    }

    // Getter for customer ID
    public int getCustomerId() {
        return customerId;
    }

    // Getter for balance
    public synchronized int getBalance() {
        return balance;
    }

    // Setter for balance with basic validation
    public synchronized void setBalance(int newBalance) {
        if (newBalance >= 0) {
            this.balance = newBalance;
        } else {
            throw new IllegalArgumentException("Balance cannot be negative.");
        }
    }

    // Method to deposit an amount to the account balance
    public synchronized void deposit(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        this.balance += amount;
    }

    // Method to withdraw an amount from the account balance
    public synchronized void withdraw(int amount) throws IllegalArgumentException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (amount > balance) {
            throw new IllegalArgumentException("Insufficient balance for withdrawal.");
        }
        this.balance -= amount;
    }

    // Provides a string representation of the account
    @Override
    public String toString() {
        return "Account{" +
                "customerId=" + customerId +
                ", accountNumber=" + accountNumber +
                ", balance=" + balance +
                '}';
    }
}