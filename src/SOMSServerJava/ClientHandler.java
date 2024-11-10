package SOMSServerJava;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Handles client connections and processes commands based on user roles.
 */
public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private final Socket clientSocket;
    private final Map<String, User> users;
    private final Map<Integer, Account> accounts;
    private final Map<String, Item> items;
    private final Map<String, Map<Integer, Purchase>> purchases;

    /**
     * Constructs a new ClientHandler.
     *
     * @param socket     The client socket.
     * @param users      Map of userID to User objects.
     * @param accounts   Map of accountNumber to Account objects.
     * @param items      Map of itemName to Item objects.
     * @param purchases  Map of userID to their purchases.
     */
    public ClientHandler(Socket socket, Map<String, User> users, Map<Integer, Account> accounts,
                         Map<String, Item> items, Map<String, Map<Integer, Purchase>> purchases) {
        this.clientSocket = socket;
        this.users = users;
        this.accounts = accounts;
        this.items = items;
        this.purchases = purchases;
    }

    /**
     * The main run method for handling client interactions.
     */
    @Override
    public void run() {
        try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            logger.info("Client connected: " + clientSocket.getRemoteSocketAddress());

            // Send Welcome Message
            out.println("Welcome to SOMS Server!");
            out.println("---END---");

            // Authentication
            String userID = in.readLine();
            String password = in.readLine();

            if (authenticate(userID, password)) {
                out.println("Authentication successful.");
                out.println("---END---");

                User user = users.get(userID);
                if (user.getRole().equalsIgnoreCase("customer")) {
                    out.println("You are logged in as a Customer.");
                    out.println("---END---");
                    handleCustomer(out, in, user);
                } else if (user.getRole().equalsIgnoreCase("seller")) {
                    out.println("You are logged in as a Seller.");
                    out.println("---END---");
                    handleSeller(out, in, user);
                } else {
                    out.println("Unknown role. Connection terminated.");
                    out.println("---END---");
                    logger.warning("User " + userID + " has an unknown role: " + user.getRole());
                }
            } else {
                out.println("Authentication failed. Invalid userID or password.");
                out.println("---END---");
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Client handler error: ", e);
        } finally {
            try {
                clientSocket.close();
                logger.info("Client disconnected: " + clientSocket.getRemoteSocketAddress());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing client socket: ", e);
            }
        }
    }

    /**
     * Authenticates a user based on userID and password.
     *
     * @param userID   The userID entered by the client.
     * @param password The password entered by the client.
     * @return True if authentication is successful; otherwise, false.
     */
    private boolean authenticate(String userID, String password) {
        if (users.containsKey(userID)) {
            User user = users.get(userID);
            return user.getPassword().equals(password);
        }
        return false;
    }

    /**
     * Handles customer-specific commands.
     *
     * @param out  The PrintWriter to send responses to the client.
     * @param in   The BufferedReader to read client commands.
     * @param user The authenticated User object.
     * @throws IOException If an I/O error occurs.
     */
    private void handleCustomer(PrintWriter out, BufferedReader in, User user) throws IOException {
        // Send Top 5 Sellers
        List<TopSeller> topSellers = getTopSellers();
        if (!topSellers.isEmpty()) {
            out.println("Top 5 Sellers by Number of Transactions:");
            int rank = 1;
            for (TopSeller seller : topSellers) {
                out.println(rank + ". " + seller.getSellerName() + " (ID: " + seller.getSellerID() + ") - " + seller.getTransactionCount() + " transactions");
                rank++;
            }
        } else {
            out.println("No seller transactions available to display.");
        }
        // out.println("---END---");

        displayAvailableItems(out);

        // Send Customer Command Panel
        out.println("Available Commands for Customers:");
        out.println("1. view credits - View your account balance.");
        out.println("2. view items - Display available items for purchase.");
        out.println("3. buy [itemName] [quantity] - Purchase a specified quantity of an item.");
        out.println("4. top up [amount] - Add funds to your account.");
        out.println("5. view history - View your purchase history.");
        out.println("6. exit - Exit the application.");
        out.println("---END---"); // End of command panel

        String command;
        while ((command = in.readLine()) != null) {
            // Ignore empty commands
            if (command.trim().isEmpty()) {
                out.println("Invalid command.");
                out.println("---END---");
                continue;
            }

            processCustomerCommand(command, out, user);

            if (command.equalsIgnoreCase("exit")) {
                out.println("Goodbye!");
                out.println("---END---");
                break;
            }
        }
    }

    /**
     * Processes customer commands and executes appropriate actions.
     *
     * @param command The command string received from the client.
     * @param out     The PrintWriter to send responses to the client.
     * @param user    The authenticated User object.
     */
    private void processCustomerCommand(String command, PrintWriter out, User user) {
        logger.info("Processing command from user " + user.getUserID() + ": " + command);

        String trimmedCommand = command.trim();
        if (trimmedCommand.isEmpty()) {
            out.println("Invalid command.");
            out.println("---END---");
            return;
        }

        String[] parts = trimmedCommand.split("\\s+", 2);
        String action = parts[0].toLowerCase();

        switch (action) {
            case "view":
                if (parts.length < 2) {
                    out.println("Usage: view [credits|items|history]");
                    out.println("---END---");
                    break;
                }
                String subAction = parts[1].toLowerCase();
                switch (subAction) {
                    case "credits":
                        viewCredits(out, user);
                        break;
                    case "items":
                        displayAvailableItems(out);
                        break;
                    case "history":
                        viewPurchaseHistory(out, user);
                        break;
                    default:
                        out.println("Unknown view command. Usage: view [credits|items|history]");
                        out.println("---END---");
                        logger.warning("Unknown sub-action for view command from user: " + user.getUserID() + " - " + subAction);
                        break;
                }
                break;

            case "buy":
                if (parts.length < 2) {
                    out.println("Usage: buy [itemName] [quantity]");
                    out.println("---END---");
                    break;
                }
                String[] buyParams = parts[1].split("\\s+");
                if (buyParams.length < 2) {
                    out.println("Usage: buy [itemName] [quantity]");
                    out.println("---END---");
                    break;
                }
                String itemName = buyParams[0];
                String quantityStr = buyParams[1];
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    makePurchase(out, user, itemName, quantity);
                } catch (NumberFormatException e) {
                    out.println("Invalid quantity. Please enter a numeric value.");
                    out.println("---END---");
                }
                break;

            case "top":
                if (parts.length < 2) {
                    out.println("Usage: top up [amount]");
                    out.println("---END---");
                    break;
                }
                String[] topParams = parts[1].split("\\s+", 2);
                if (topParams.length < 2 || !topParams[0].equalsIgnoreCase("up")) {
                    out.println("Usage: top up [amount]");
                    out.println("---END---");
                    break;
                }
                String amountStr = topParams[1];
                try {
                    double amount = Double.parseDouble(amountStr);
                    topUpAmount(out, user, amount);
                } catch (NumberFormatException e) {
                    out.println("Invalid amount. Please enter a numeric value.");
                    out.println("---END---");
                }
                break;

            case "exit":
                // Handled in the loop
                break;

            default:
                out.println("Unknown command.");
                out.println("---END---");
                logger.warning("Unknown command from user: " + user.getUserID() + " - " + command);
                break;
        }
    }

    /**
     * Displays the current balance of the user's account.
     *
     * @param out  The PrintWriter to send responses to the client.
     * @param user The authenticated User object.
     */
    private void viewCredits(PrintWriter out, User user) {
        int accountNumber = user.getAccountNumber();
        Account account = accounts.get(accountNumber);
        if (account != null) {
            out.println("Your current balance: $" + String.format("%.2f", account.getBalance()));
            out.println("---END---");
            logger.info("User " + user.getUserID() + " viewed credits. Balance: $" + account.getBalance());
        } else {
            out.println("Account not found.");
            out.println("---END---");
            logger.severe("Account not found for userID: " + user.getUserID());
        }
    }

    /**
     * Displays all available items for purchase.
     *
     * @param out The PrintWriter to send responses to the client.
     */
    private void displayAvailableItems(PrintWriter out) {
        if (items.isEmpty()) {
            out.println("No items are currently available.");
            out.println("---END---");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Available Items:\n");
        sb.append(String.format("%-20s %-10s %-10s\n", "Item Name", "Price($)", "Quantity"));
        sb.append("-------------------------------------------------\n");
        for (Item item : items.values()) {
            sb.append(String.format("%-20s %-10.2f %-10d\n",
                    item.getName(), item.getPrice(), item.getQuantityAvailable()));
        }
        out.println(sb.toString());
        out.println("---END---");
    }

    /**
     * Processes a purchase request from a customer.
     *
     * @param out       The PrintWriter to send responses to the client.
     * @param user      The authenticated User object.
     * @param itemName  The name of the item to purchase.
     * @param quantity  The quantity of the item to purchase.
     */
    private void makePurchase(PrintWriter out, User user, String itemName, int quantity) {
        if (quantity <= 0) {
            out.println("Quantity must be greater than zero.");
            out.println("---END---");
            logger.warning("User " + user.getUserID() + " attempted to purchase non-positive quantity: " + quantity);
            return;
        }

        Item item = getItemByName(itemName);
        if (item == null) {
            out.println("Item \"" + itemName + "\" does not exist.");
            out.println("---END---");
            logger.warning("User " + user.getUserID() + " attempted to purchase non-existent item: " + itemName);
            return;
        }

        synchronized (item) {
            if (item.getQuantityAvailable() < quantity) {
                out.println("Insufficient quantity available for \"" + item.getName() + "\". Available: " + item.getQuantityAvailable());
                out.println("---END---");
                logger.warning("User " + user.getUserID() + " attempted to purchase more than available for item: " + item.getName());
                return;
            }
            item.reduceQuantity(quantity);
        }

        double totalCost = item.getPrice() * quantity;
        int accountNumber = user.getAccountNumber();
        Account account = accounts.get(accountNumber);

        if (account == null) {
            out.println("Account not found.");
            out.println("---END---");
            logger.severe("Account not found for userID: " + user.getUserID());
            synchronized (item) {
                item.increaseQuantity(quantity);
            }
            return;
        }

        boolean reserved = account.reserveFunds(totalCost);
        if (!reserved) {
            out.println("Insufficient balance to reserve $" + String.format("%.2f", totalCost) + ".");
            out.println("---END---");
            logger.warning("User " + user.getUserID() + " has insufficient balance to reserve funds for purchase.");
            synchronized (item) {
                item.increaseQuantity(quantity);
            }
            return;
        }

        Purchase purchase = new Purchase();
        purchase.setUserID(user.getUserID());
        purchase.setItemName(item.getName());
        purchase.setQuantity(quantity);
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setSellerID("pending"); // Updated to "pending"
        purchase.setTotalCost(totalCost);
        purchase.setStatus("pending");

        Map<Integer, Purchase> userPurchases = purchases.computeIfAbsent(user.getUserID(), k -> new ConcurrentHashMap<>());
        int purchaseId = userPurchases.size() + 1;
        userPurchases.put(purchaseId, purchase);

        SOMSUtils.saveAllData(users, accounts, items, purchases);

        out.println("Purchase of \"" + item.getName() + "\" x" + quantity + " reserved for $" + String.format("%.2f", totalCost) + ". Awaiting seller confirmation.");
        out.println("---END---");
        logger.info("User " + user.getUserID() + " reserved purchase: " + item.getName() + " x" + quantity + " for $" + String.format("%.2f", totalCost));
    }

    /**
     * Adds funds to the user's account.
     *
     * @param out    The PrintWriter to send responses to the client.
     * @param user   The authenticated User object.
     * @param amount The amount to add to the account.
     */
    private void topUpAmount(PrintWriter out, User user, double amount) {
        if (amount <= 0) {
            out.println("Top-up amount must be positive.");
            out.println("---END---");
            logger.warning("User " + user.getUserID() + " attempted to top up a non-positive amount: $" + amount);
            return;
        }

        int accountNumber = user.getAccountNumber();
        Account account = accounts.get(accountNumber);

        if (account == null) {
            out.println("Account not found.");
            out.println("---END---");
            logger.severe("Account not found for userID: " + user.getUserID());
            return;
        }

        synchronized (account) {
            account.addFunds(amount);
        }

        SOMSUtils.saveAllData(users, accounts, items, purchases);

        out.println("Top-up successful. New balance: $" + String.format("%.2f", account.getBalance()));
        out.println("---END---");
        logger.info("User " + user.getUserID() + " topped up $" + String.format("%.2f", amount) + ". New balance: $" + String.format("%.2f", account.getBalance()));
    }

    /**
     * Displays the purchase history of the user.
     *
     * @param out  The PrintWriter to send responses to the client.
     * @param user The authenticated User object.
     */
    private void viewPurchaseHistory(PrintWriter out, User user) {
        Map<Integer, Purchase> userPurchases = purchases.get(user.getUserID());
        if (userPurchases == null || userPurchases.isEmpty()) {
            out.println("No purchase history found.");
            out.println("---END---");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Purchase History:\n");
        sb.append(String.format("%-5s %-20s %-10s %-20s %-15s %-10s %-10s\n",
                "ID", "Item Name", "Quantity", "Date", "Seller", "Cost($)", "Status"));
        sb.append("------------------------------------------------------------------------------------------\n");

        for (Map.Entry<Integer, Purchase> entry : userPurchases.entrySet()) {
            Purchase purchase = entry.getValue();
            sb.append(String.format("%-5d %-20s %-10d %-20s %-15s %-10.2f %-10s\n",
                    entry.getKey(),
                    purchase.getItemName(),
                    purchase.getQuantity(),
                    purchase.getPurchaseDate().toString(),
                    purchase.getSellerID(),
                    purchase.getTotalCost(),
                    purchase.getStatus()));
        }

        out.println(sb.toString());
        out.println("---END---");
        logger.info("User " + user.getUserID() + " viewed purchase history.");
    }

    /**
     * Handles seller-specific commands.
     *
     * @param out  The PrintWriter to send responses to the client.
     * @param in   The BufferedReader to read client commands.
     * @param user The authenticated User object.
     * @throws IOException If an I/O error occurs.
     */
    private void handleSeller(PrintWriter out, BufferedReader in, User user) throws IOException {
        // Send Seller Command Panel
        out.println("Available Commands for Sellers:");
        out.println("1. add [itemName] [price] [quantity] - Add or update an item in inventory.");
        out.println("2. complete [purchaseId] [delivered|unfulfilled] - Mark a purchase as delivered or unfulfilled.");
        out.println("3. view transactions - View all your transaction history.");
        out.println("4. exit - Exit the application.");
        out.println("---END---"); // End of command panel

        String command;
        while ((command = in.readLine()) != null) {
            // Ignore empty commands
            if (command.trim().isEmpty()) {
                out.println("Invalid command.");
                out.println("---END---");
                continue;
            }

            processSellerCommand(command, out, user);

            if (command.equalsIgnoreCase("exit")) {
                out.println("Goodbye!");
                out.println("---END---");
                break;
            }
        }
    }

    /**
     * Processes seller commands and executes appropriate actions.
     *
     * @param command The command string received from the client.
     * @param out     The PrintWriter to send responses to the client.
     * @param user    The authenticated User object.
     */
    private void processSellerCommand(String command, PrintWriter out, User user) {
        logger.info("Processing command from seller " + user.getUserID() + ": " + command);

        String trimmedCommand = command.trim();
        if (trimmedCommand.isEmpty()) {
            out.println("Invalid command.");
            out.println("---END---");
            return;
        }

        String[] parts = trimmedCommand.split("\\s+", 2);
        String action = parts[0].toLowerCase();

        switch (action) {
            case "add":
                if (parts.length < 2) {
                    out.println("Usage: add [itemName] [price] [quantity]");
                    out.println("---END---");
                    break;
                }
                String[] addParams = parts[1].split("\\s+");
                if (addParams.length < 3) {
                    out.println("Usage: add [itemName] [price] [quantity]");
                    out.println("---END---");
                    break;
                }
                String itemName = addParams[0];
                String priceStr = addParams[1];
                String quantityStr = addParams[2];
                try {
                    double price = Double.parseDouble(priceStr);
                    int quantity = Integer.parseInt(quantityStr);
                    addItem(out, itemName, price, quantity, user);
                } catch (NumberFormatException e) {
                    out.println("Invalid price or quantity. Please enter numeric values.");
                    out.println("---END---");
                }
                break;

            case "complete":
                if (parts.length < 2) {
                    out.println("Usage: complete [purchaseId] [delivered|unfulfilled]");
                    out.println("---END---");
                    break;
                }
                String[] completeParams = parts[1].split("\\s+");
                if (completeParams.length < 2) {
                    out.println("Usage: complete [purchaseId] [delivered|unfulfilled]");
                    out.println("---END---");
                    break;
                }
                String purchaseIdStr = completeParams[0];
                String status = completeParams[1].toLowerCase();
                try {
                    int purchaseId = Integer.parseInt(purchaseIdStr);
                    if (!status.equals("delivered") && !status.equals("unfulfilled")) {
                        out.println("Invalid status. Use 'delivered' or 'unfulfilled'.");
                        out.println("---END---");
                        break;
                    }
                    completeTransaction(out, purchaseId, status, user);
                } catch (NumberFormatException e) {
                    out.println("Invalid purchase ID. Please enter a numeric value.");
                    out.println("---END---");
                }
                break;

            case "view":
                if (parts.length < 2) {
                    out.println("Usage: view transactions");
                    out.println("---END---");
                    break;
                }
                String subAction = parts[1].toLowerCase();
                if (subAction.equals("transactions")) {
                    viewTransactionHistory(out, user);
                } else {
                    out.println("Unknown view command. Usage: view transactions");
                    out.println("---END---");
                    logger.warning("Unknown sub-action for view command from seller: " + user.getUserID() + " - " + subAction);
                }
                break;

            case "exit":
                // Handled in the loop
                break;

            default:
                out.println("Unknown command.");
                out.println("---END---");
                logger.warning("Unknown command from seller: " + user.getUserID() + " - " + command);
                break;
        }
    }

    /**
     * Retrieves an item by name, case-insensitively.
     *
     * @param itemName The name of the item to retrieve.
     * @return The Item object if found; otherwise, null.
     */
    private Item getItemByName(String itemName) {
        for (Map.Entry<String, Item> entry : items.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(itemName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Adds or updates an item in the inventory.
     *
     * @param out       The PrintWriter to send responses to the client.
     * @param itemName  The name of the item.
     * @param price     The price of the item.
     * @param quantity  The quantity of the item.
     * @param user      The seller performing the action.
     */
    private void addItem(PrintWriter out, String itemName, double price, int quantity, User user) {
        if (price <= 0 || quantity <= 0) {
            out.println("Price and quantity must be positive.");
            out.println("---END---");
            logger.warning("Seller " + user.getUserID() + " attempted to add item with non-positive price or quantity: " + itemName);
            return;
        }

        synchronized (items) {
            if (items.containsKey(itemName)) {
                Item existingItem = items.get(itemName);
                existingItem.setPrice(price);
                existingItem.increaseQuantity(quantity);
                logger.info("Seller " + user.getUserID() + " updated item: " + itemName + " with price: $" + price + " and additional quantity: " + quantity);
            } else {
                Item newItem = new Item(itemName, price, quantity);
                items.put(itemName, newItem);
                logger.info("Seller " + user.getUserID() + " added new item: " + itemName + " with price: $" + price + " and quantity: " + quantity);
            }
        }

        SOMSUtils.saveAllData(users, accounts, items, purchases);

        out.println("Item \"" + itemName + "\" added/updated successfully.");
        out.println("---END---");
    }

    /**
     * Completes a transaction by marking it as delivered or unfulfilled.
     *
     * @param out         The PrintWriter to send responses to the client.
     * @param purchaseId  The ID of the purchase to complete.
     * @param status      The status to mark the purchase as ("delivered" or "unfulfilled").
     * @param user        The seller performing the action.
     */
    private void completeTransaction(PrintWriter out, int purchaseId, String status, User user) {
        boolean found = false;
        String buyerID = null;
        Purchase purchase = null;

        // Find the purchase across all users
        for (Map.Entry<String, Map<Integer, Purchase>> entry : purchases.entrySet()) {
            if (entry.getValue().containsKey(purchaseId)) {
                purchase = entry.getValue().get(purchaseId);
                buyerID = entry.getKey();
                found = true;
                break;
            }
        }

        if (!found || purchase == null) {
            out.println("Purchase ID not found.");
            out.println("---END---");
            logger.warning("Seller " + user.getUserID() + " attempted to complete non-existent purchase ID: " + purchaseId);
            return;
        }

        synchronized (purchase) {
            if (!purchase.getStatus().equals("pending")) {
                out.println("Purchase already processed.");
                out.println("---END---");
                logger.warning("Seller " + user.getUserID() + " attempted to reprocess purchase ID: " + purchaseId);
                return;
            }

            if (status.equals("delivered")) {
                // Transfer funds to seller
                double amount = purchase.getTotalCost();
                int sellerAccountNumber = user.getAccountNumber();
                Account sellerAccount = accounts.get(sellerAccountNumber);
                if (sellerAccount == null) {
                    out.println("Seller account not found.");
                    out.println("---END---");
                    logger.severe("Seller " + user.getUserID() + " account not found.");
                    return;
                }

                synchronized (sellerAccount) {
                    sellerAccount.addFunds(amount);
                }

                // Update purchase status
                purchase.setStatus("fulfilled");
                purchase.setSellerID(user.getUserID());

                // Persist data
                SOMSUtils.saveAllData(users, accounts, items, purchases);

                out.println("Purchase ID " + purchaseId + " marked as delivered. $" + String.format("%.2f", amount) + " transferred to your account.");
                out.println("---END---");
                logger.info("Seller " + user.getUserID() + " fulfilled purchase ID: " + purchaseId + " and transferred $" + amount + " to their account.");
            } else if (status.equals("unfulfilled")) {
                // Return funds to customer
                double amount = purchase.getTotalCost();
                int customerAccountNumber = users.get(buyerID).getAccountNumber();
                Account customerAccount = accounts.get(customerAccountNumber);
                if (customerAccount == null) {
                    out.println("Customer account not found.");
                    out.println("---END---");
                    logger.severe("Customer " + buyerID + " account not found.");
                    return;
                }

                synchronized (customerAccount) {
                    customerAccount.releaseFunds(amount);
                }

                // Update purchase status
                purchase.setStatus("unfulfilled");
                purchase.setSellerID("unfulfilled");

                // Restore item quantity
                synchronized (items) {
                    Item item = getItemByName(purchase.getItemName());
                    if (item != null) {
                        item.increaseQuantity(purchase.getQuantity());
                    }
                }

                // Persist data
                SOMSUtils.saveAllData(users, accounts, items, purchases);

                out.println("Purchase ID " + purchaseId + " marked as unfulfilled. $" + String.format("%.2f", amount) + " returned to the customer.");
                out.println("---END---");
                logger.info("Seller " + user.getUserID() + " marked purchase ID: " + purchaseId + " as unfulfilled and returned $" + amount + " to customer " + buyerID + ".");
            }
        }
    }

    /**
     * Retrieves the top 5 sellers based on the number of transactions.
     *
     * @return A list of TopSeller objects representing the top 5 sellers.
     */
    private List<TopSeller> getTopSellers() {
        Map<String, Integer> sellerTransactionCount = new HashMap<>();

        // Iterate through all purchases to count transactions per seller
        for (Map<Integer, Purchase> userPurchases : purchases.values()) {
            for (Purchase purchase : userPurchases.values()) {
                String sellerID = purchase.getSellerID();
                if (sellerID == null || sellerID.equalsIgnoreCase("pending") || sellerID.equalsIgnoreCase("unfulfilled")) {
                    continue; // Ignore pending or unfulfilled transactions
                }
                sellerTransactionCount.put(sellerID, sellerTransactionCount.getOrDefault(sellerID, 0) + 1);
            }
        }

        // Create a list of TopSeller objects
        List<TopSeller> topSellersList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sellerTransactionCount.entrySet()) {
            String sellerID = entry.getKey();
            int transactionCount = entry.getValue();
            User seller = users.get(sellerID);
            if (seller != null) {
                topSellersList.add(new TopSeller(sellerID, seller.getName(), transactionCount));
            }
        }

        // Sort the list in descending order of transaction count
        topSellersList.sort((s1, s2) -> Integer.compare(s2.getTransactionCount(), s1.getTransactionCount()));

        // Return the top 5 sellers or fewer if not enough sellers exist
        return topSellersList.stream().limit(5).collect(Collectors.toList());
    }

    /**
     * Represents a top seller with their ID, name, and transaction count.
     */
    private static class TopSeller {
        private final String sellerID;
        private final String sellerName;
        private final int transactionCount;

        /**
         * Constructs a new TopSeller.
         *
         * @param sellerID         The ID of the seller.
         * @param sellerName       The name of the seller.
         * @param transactionCount The number of transactions completed by the seller.
         */
        public TopSeller(String sellerID, String sellerName, int transactionCount) {
            this.sellerID = sellerID;
            this.sellerName = sellerName;
            this.transactionCount = transactionCount;
        }

        public String getSellerID() {
            return sellerID;
        }

        public String getSellerName() {
            return sellerName;
        }

        public int getTransactionCount() {
            return transactionCount;
        }
    }

    /**
     * Displays the transaction history of the seller.
     *
     * @param out    The PrintWriter to send responses to the client.
     * @param seller The authenticated Seller User object.
     */
    private void viewTransactionHistory(PrintWriter out, User seller) {
        String sellerID = seller.getUserID();
        StringBuilder sb = new StringBuilder();
        sb.append("Transaction History:\n");
        sb.append(String.format("%-5s %-20s %-10s %-20s %-15s %-10s %-15s\n",
                "ID", "Item Name", "Quantity", "Date", "Buyer", "Cost($)", "Status"));
        sb.append("----------------------------------------------------------------------------------------------\n");

        boolean hasTransactions = false;

        for (Map.Entry<String, Map<Integer, Purchase>> entry : purchases.entrySet()) {
            String buyerID = entry.getKey();
            for (Map.Entry<Integer, Purchase> purchaseEntry : entry.getValue().entrySet()) {
                Purchase purchase = purchaseEntry.getValue();
                if (purchase.getSellerID().equalsIgnoreCase(sellerID) || purchase.getSellerID().equalsIgnoreCase("unfulfilled")) {
                    sb.append(String.format("%-5d %-20s %-10d %-20s %-15s %-10.2f %-15s\n",
                            purchaseEntry.getKey(),
                            purchase.getItemName(),
                            purchase.getQuantity(),
                            purchase.getPurchaseDate().toString(),
                            buyerID,
                            purchase.getTotalCost(),
                            purchase.getStatus()));
                    hasTransactions = true;
                }
            }
        }

        if (!hasTransactions) {
            sb.append("No transactions found.");
        }

        out.println(sb.toString());
        out.println("---END---");
        logger.info("Seller " + seller.getUserID() + " viewed transaction history.");
    }
}