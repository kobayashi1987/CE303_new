package SOMSServerJava;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SOMS {
    private final Map<Integer, Account> accounts = new TreeMap<>();
    private static final String DATABASE_FILE = "soms_database.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Load existing accounts from a JSON file
    public SOMS() {
        loadDatabase();
    }

    public void createAccount(int clientId, int accountNumber, int initialBalance) {
        Account account = new Account(clientId, accountNumber, initialBalance); // Pass initial balance directly
        accounts.put(accountNumber, account);
        saveDatabase();
    }

    public List<Integer> getListOfAccounts(int clientId) {
        List<Integer> accountNumbers = new ArrayList<>();
        for (Account account : accounts.values()) {
            if (account.getClientId() == clientId) {
                accountNumbers.add(account.getAccountNumber());
            }
        }
        return accountNumbers;
    }

    public int getAccountBalance(int clientId, int accountNumber) throws Exception {
        Account account = accounts.get(accountNumber);
        if (account == null) throw new Exception("Account not found.");
        if (account.getClientId() != clientId) {
            throw new Exception("Unauthorized access to account balance.");
        }
        return account.getBalance();
    }

    public void transfer(int clientId, int fromAccount, int toAccount, int amount) throws Exception {
        synchronized (accounts) {
            Account sourceAccount = accounts.get(fromAccount);
            Account targetAccount = accounts.get(toAccount);

            if (sourceAccount == null || targetAccount == null) {
                throw new Exception("One or both accounts not found.");
            }

            if (sourceAccount.getClientId() != clientId) {
                throw new Exception("Unauthorized transfer request.");
            }

            if (sourceAccount.getBalance() < amount) {
                throw new Exception("Insufficient funds.");
            }

            if (amount <= 0) {
                throw new Exception("Transfer amount must be positive.");
            }

            // Perform the transfer
            sourceAccount.setBalance(sourceAccount.getBalance() - amount);
            targetAccount.setBalance(targetAccount.getBalance() + amount);
            saveDatabase();  // Save updated balances
        }
    }

    // Save accounts to JSON file
    private void saveDatabase() {
        try (FileWriter writer = new FileWriter(DATABASE_FILE)) {
            gson.toJson(accounts, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load accounts from JSON file
    private void loadDatabase() {
        try (FileReader reader = new FileReader(DATABASE_FILE)) {
            Type type = new TypeToken<TreeMap<Integer, Account>>() {}.getType();
            Map<Integer, Account> loadedAccounts = gson.fromJson(reader, type);
            if (loadedAccounts != null) accounts.putAll(loadedAccounts);
        } catch (IOException e) {
            System.out.println("No existing database found, starting fresh.");
        }
    }
}