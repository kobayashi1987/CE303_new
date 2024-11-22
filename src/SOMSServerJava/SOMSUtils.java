
package SOMSServerJava;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * SOMSUtils handles data persistence and utility functions for the SOMS application.
 */
public class SOMSUtils {
    private static final Logger logger = Logger.getLogger(SOMSUtils.class.getName());
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    /**
     * Loads users from the specified JSON file.
     *
     * @param filename The name of the JSON file containing users.
     * @return A map of userID to User objects.
     */
    public static Map<String, User> loadUsers(String filename) {
        try (Reader reader = new FileReader(filename)) {
            Type type = new TypeToken<ConcurrentHashMap<String, User>>() {}.getType();
            Map<String, User> users = gson.fromJson(reader, type);
            if (users == null) {
                users = new ConcurrentHashMap<>();
            }
            logger.info("Loaded " + users.size() + " users from " + filename + ".");
            return users;
        } catch (FileNotFoundException e) {
            logger.warning("Users file not found. Creating a new users map.");
            return new ConcurrentHashMap<>();
        } catch (IOException e) {
            logger.severe("Error reading users file: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Loads accounts from the specified JSON file.
     *
     * @param filename The name of the JSON file containing accounts.
     * @return A map of accountNumber to Account objects.
     */
    public static Map<Integer, Account> loadAccounts(String filename) {
        try (Reader reader = new FileReader(filename)) {
            Type type = new TypeToken<ConcurrentHashMap<Integer, Account>>() {}.getType();
            Map<Integer, Account> accounts = gson.fromJson(reader, type);
            if (accounts == null) {
                accounts = new ConcurrentHashMap<>();
            }
            logger.info("Loaded " + accounts.size() + " accounts from " + filename + ".");
            return accounts;
        } catch (FileNotFoundException e) {
            logger.warning("Accounts file not found. Creating a new accounts map.");
            return new ConcurrentHashMap<>();
        } catch (IOException e) {
            logger.severe("Error reading accounts file: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Loads items from the specified JSON file.
     *
     * @param filename The name of the JSON file containing items.
     * @return A map of itemName to Item objects.
     */
    public static Map<String, Item> loadItems(String filename) {
        try (Reader reader = new FileReader(filename)) {
            Type type = new TypeToken<ConcurrentHashMap<String, Item>>() {}.getType();
            Map<String, Item> items = gson.fromJson(reader, type);
            if (items == null) {
                items = new ConcurrentHashMap<>();
            }
            logger.info("Loaded " + items.size() + " items from " + filename + ".");
            return items;
        } catch (FileNotFoundException e) {
            logger.warning("Items file not found. Creating a new items map.");
            return new ConcurrentHashMap<>();
        } catch (IOException e) {
            logger.severe("Error reading items file: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Loads purchases from the specified JSON file.
     *
     * @param filename The name of the JSON file containing purchases.
     * @return A map of buyerID to a map of purchaseId to Purchase objects.
     */
    public static Map<String, Map<Integer, Purchase>> loadPurchases(String filename) {
        try (Reader reader = new FileReader(filename)) {
            Type type = new TypeToken<ConcurrentHashMap<String, ConcurrentHashMap<Integer, Purchase>>>() {}.getType();
            Map<String, Map<Integer, Purchase>> purchases = gson.fromJson(reader, type);
            if (purchases == null) {
                purchases = new ConcurrentHashMap<>();
            }
            logger.info("Loaded purchases for " + purchases.size() + " buyers from " + filename + ".");
            return purchases;
        } catch (FileNotFoundException e) {
            logger.warning("Purchases file not found. Creating a new purchases map.");
            return new ConcurrentHashMap<>();
        } catch (IOException e) {
            logger.severe("Error reading purchases file: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Saves all data (users, accounts, items, purchases) to their respective JSON files.
     *
     * @param users     The map of users.
     * @param accounts  The map of accounts.
     * @param items     The map of items.
     * @param purchases The map of purchases.
     */
    public static void saveAllData(Map<String, User> users,
                                   Map<Integer, Account> accounts,
                                   Map<String, Item> items,
                                   Map<String, Map<Integer, Purchase>> purchases) {
        saveData(users, "users.json");
        saveData(accounts, "accounts.json");
        saveData(items, "items.json");
        saveData(purchases, "purchases.json");
    }

    /**
     * Generic method to save a map to a JSON file.
     *
     * @param data     The data map to save.
     * @param filename The name of the JSON file.
     * @param <T>      The type of the keys in the map.
     * @param <V>      The type of the values in the map.
     */
    private static <T, V> void saveData(Map<T, V> data, String filename) {
        try (Writer writer = new FileWriter(filename)) {
            gson.toJson(data, writer);
            logger.info("Data saved to " + filename + " successfully.");
        } catch (IOException e) {
            logger.severe("Error saving data to " + filename + ": " + e.getMessage());
        }
    }

    /**
     * Validates consistency between users and accounts.
     *
     * @param users    The map of users.
     * @param accounts The map of accounts.
     * @return True if consistent, false otherwise.
     */
    public static boolean validateUserAccountConsistency(Map<String, User> users, Map<Integer, Account> accounts) {
        boolean isConsistent = true;

        // Check that every user has a corresponding account
        for (User user : users.values()) {
            if (!accounts.containsKey(user.getAccountNumber())) {
                logger.severe("User '" + user.getUserID() + "' has an account number " + user.getAccountNumber() + " that does not exist.");
                isConsistent = false;
            }
        }

        // Optionally, check that every account is associated with a user
        for (Account account : accounts.values()) {
            boolean found = false;
            for (User user : users.values()) {
                if (user.getAccountNumber() == account.getAccountNumber()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                logger.warning("Account number " + account.getAccountNumber() + " is not associated with any user.");
                // Depending on your application logic, decide whether this is critical
            }
        }

        return isConsistent;
    }
}