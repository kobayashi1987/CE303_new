package SOMSServerJava;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * SOMSUtils provides utility methods for the SOMS server application,
 * such as loading JSON data and validating inputs.
 */
public class SOMSUtils {
    private static final Logger logger = Logger.getLogger(SOMSUtils.class.getName());
    private static final Gson gson = new Gson();

    /**
     * Loads users from a JSON file into a Map.
     *
     * @param filename The name of the JSON file containing user data.
     * @return A Map with userID as the key and User objects as values.
     */
    public static Map<String, User> loadUsers(String filename) {
        try (FileReader reader = new FileReader(filename)) {
            Type userMapType = new TypeToken<Map<String, User>>() {}.getType();
            Map<String, User> users = gson.fromJson(reader, userMapType);
            logger.info("Successfully loaded users from " + filename);
            return users;
        } catch (JsonSyntaxException e) {
            logger.log(Level.SEVERE, "JSON syntax error while loading users: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error while loading users: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Loads accounts from a JSON file into a Map.
     *
     * @param filename The name of the JSON file containing account data.
     * @return A Map with accountNumber as the key and Account objects as values.
     */
    public static Map<Integer, Account> loadAccounts(String filename) {
        try (FileReader reader = new FileReader(filename)) {
            Type accountMapType = new TypeToken<Map<String, Account>>() {}.getType();
            Map<String, Account> tempAccounts = gson.fromJson(reader, accountMapType);
            Map<Integer, Account> accounts = new ConcurrentHashMap<>();
            if (tempAccounts != null) {
                for (Map.Entry<String, Account> entry : tempAccounts.entrySet()) {
                    try {
                        int accountNumber = Integer.parseInt(entry.getKey());
                        accounts.put(accountNumber, entry.getValue());
                    } catch (NumberFormatException e) {
                        logger.log(Level.WARNING, "Invalid account number format: " + entry.getKey(), e);
                    }
                }
                logger.info("Successfully loaded accounts from " + filename);
            }
            return accounts;
        } catch (JsonSyntaxException e) {
            logger.log(Level.SEVERE, "JSON syntax error while loading accounts: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error while loading accounts: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Validates the consistency between users and accounts data.
     *
     * @param users    The Map of users loaded from users.json.
     * @param accounts The Map of accounts loaded from accounts.json.
     * @return True if all users have corresponding accounts, else false.
     */
    public static boolean validateUserAccountConsistency(Map<String, User> users, Map<Integer, Account> accounts) {
        boolean isConsistent = true;
        for (Map.Entry<String, User> entry : users.entrySet()) {
            String userID = entry.getKey();
            int accountNumber = entry.getValue().getAccountNumber();
            if (!accounts.containsKey(accountNumber)) {
                logger.severe("No account found for userID: " + userID + " with accountNumber: " + accountNumber);
                isConsistent = false;
            }
        }
        if (isConsistent) {
            logger.info("All users have corresponding accounts.");
        }
        return isConsistent;
    }

    /**
     * Retrieves a map of available items for purchase.
     *
     * @return A Map with item names as keys and their prices as values.
     */
    public static Map<String, Double> getAvailableItems() {
        // In a real application, this could be loaded from a database or a configuration file.
        return Map.of(
                "Item1", 100.0,
                "Item2", 150.0,
                "Item3", 200.0,
                "Item4", 250.0,
                "Item5", 300.0
        );
    }

    /**
     * Additional utility methods can be added here as needed.
     */
}