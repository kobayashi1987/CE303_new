package SOMSServerJava;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonParseException;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

/**
 * SOMSUtils provides utility methods for the SOMS server application,
 * such as loading JSON data, saving data, and validating inputs.
 */
public class SOMSUtils {
    private static final Logger logger = Logger.getLogger(SOMSUtils.class.getName());

    // Configure GSON with custom serializers and deserializers for LocalDateTime
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, context) -> {
                try {
                    return LocalDateTime.parse(json.getAsString());
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to deserialize LocalDateTime: " + json.getAsString(), e);
                    return null;
                }
            })
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, type, context) -> {
                return new JsonPrimitive(src.toString());
            })
            .setPrettyPrinting()
            .create();

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
        } catch (JsonParseException e) {
            logger.log(Level.SEVERE, "JSON syntax error while loading users: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error while loading users: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Saves users to a JSON file from a Map.
     *
     * @param filename The name of the JSON file to save user data.
     * @param users    The Map containing user data.
     */
    public static void saveUsers(String filename, Map<String, User> users) {
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(users, writer);
            logger.info("Successfully saved users to " + filename);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error while saving users: " + e.getMessage(), e);
        }
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
        } catch (JsonParseException e) {
            logger.log(Level.SEVERE, "JSON syntax error while loading accounts: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error while loading accounts: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Saves accounts to a JSON file from a Map.
     *
     * @param filename The name of the JSON file to save account data.
     * @param accounts The Map containing account data.
     */
    public static void saveAccounts(String filename, Map<Integer, Account> accounts) {
        try (FileWriter writer = new FileWriter(filename)) {
            // Convert key to String for JSON compatibility
            Map<String, Account> tempAccounts = new ConcurrentHashMap<>();
            for (Map.Entry<Integer, Account> entry : accounts.entrySet()) {
                tempAccounts.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            gson.toJson(tempAccounts, writer);
            logger.info("Successfully saved accounts to " + filename);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error while saving accounts: " + e.getMessage(), e);
        }
    }

    /**
     * Loads items from a JSON file into a Map.
     *
     * @param filename The name of the JSON file containing item data.
     * @return A Map with itemName as the key and Item objects as values.
     */
    public static Map<String, Item> loadItems(String filename) {
        try (FileReader reader = new FileReader(filename)) {
            Type itemMapType = new TypeToken<Map<String, Item>>() {}.getType();
            Map<String, Item> items = gson.fromJson(reader, itemMapType);
            logger.info("Successfully loaded items from " + filename);
            return items;
        } catch (JsonParseException e) {
            logger.log(Level.SEVERE, "JSON syntax error while loading items: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error while loading items: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Saves items to a JSON file from a Map.
     *
     * @param filename The name of the JSON file to save item data.
     * @param items    The Map containing item data.
     */
    public static void saveItems(String filename, Map<String, Item> items) {
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(items, writer);
            logger.info("Successfully saved items to " + filename);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error while saving items: " + e.getMessage(), e);
        }
    }

    /**
     * Loads purchases from a JSON file into a Map.
     *
     * @param filename The name of the JSON file containing purchase data.
     * @return A Map with userID as the key and a map of purchaseID to Purchase objects as values.
     */
    public static Map<String, Map<Integer, Purchase>> loadPurchases(String filename) {
        try (FileReader reader = new FileReader(filename)) {
            Type purchaseMapType = new TypeToken<Map<String, Map<Integer, Purchase>>>() {}.getType();
            Map<String, Map<Integer, Purchase>> purchases = gson.fromJson(reader, purchaseMapType);
            logger.info("Successfully loaded purchases from " + filename);
            return purchases;
        } catch (JsonParseException e) {
            logger.log(Level.SEVERE, "JSON syntax error while loading purchases: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error while loading purchases: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Saves purchases to a JSON file from a Map.
     *
     * @param filename  The name of the JSON file to save purchase data.
     * @param purchases The Map containing purchase data.
     */
    public static void savePurchases(String filename, Map<String, Map<Integer, Purchase>> purchases) {
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(purchases, writer);
            logger.info("Successfully saved purchases to " + filename);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error while saving purchases: " + e.getMessage(), e);
        }
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
     * @return A Map with item names as keys and their details as values.
     */
    public static Map<String, Item> getAvailableItems() {
        // Load from items.json using SOMSUtils
        return loadItems("items.json");
    }

    /**
     * Saves all data back to their respective JSON files.
     *
     * @param users     The Map of users.
     * @param accounts  The Map of accounts.
     * @param items     The Map of items.
     * @param purchases The Map of purchases.
     */
    public static void saveAllData(Map<String, User> users, Map<Integer, Account> accounts,
                                   Map<String, Item> items, Map<String, Map<Integer, Purchase>> purchases) {
        saveUsers("users.json", users);
        saveAccounts("accounts.json", accounts);
        saveItems("items.json", items);
        savePurchases("purchases.json", purchases);

    }
}