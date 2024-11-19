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
     * @param socket    The client socket.
     * @param users     Map of userID to User objects.
     * @param accounts  Map of accountNumber to Account objects.
     * @param items     Map of itemName to Item objects.
     * @param purchases Map of userID to their purchases.
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

            // Prompt for UserID
            out.println("Enter your userID:");
            out.println("---END---");

            // Read UserID
            String userID = in.readLine();
            logger.info("Received userID: " + userID + " from " + clientSocket.getRemoteSocketAddress());

            if (userID == null || userID.trim().isEmpty()) {
                out.println("UserID cannot be empty.");
                out.println("---END---");
                logger.warning("Received empty UserID from client: " + clientSocket.getRemoteSocketAddress());
                return;
            }

            // Prompt for Password
            out.println("Enter your password:");
            out.println("---END---");

            // Read Password
            String password = in.readLine();
            logger.info("Received password from userID: " + userID + " from " + clientSocket.getRemoteSocketAddress());

            if (password == null || password.trim().isEmpty()) {
                out.println("Password cannot be empty.");
                out.println("---END---");
                logger.warning("Received empty password from userID: " + userID);
                return;
            }

            // Authenticate
            if (authenticate(userID, password)) {
                User user = users.get(userID);
                String welcomeMessage = (user.getName() != null) ? user.getName() : user.getUserID();
                out.println("Authentication successful. Welcome, " + welcomeMessage + "!");
                out.println("---END---");

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
                logger.warning("Authentication failed for userID: " + userID);
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
            boolean isAuthenticated = user.getPassword().equals(password);
            logger.info("Authentication attempt for userID: " + userID + " - " + (isAuthenticated ? "SUCCESS" : "FAILURE"));
            return isAuthenticated;
        }
        logger.warning("UserID: " + userID + " not found.");
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
        out.println("---END---"); // Ensure delimiter is sent

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
     * @param out      The PrintWriter to send responses to the client.
     * @param user     The authenticated User object.
     * @param itemName The name of the item to purchase.
     * @param quantity The quantity of the item to purchase.
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
        purchase.setSellerID(item.getSellerID()); // Set to item's sellerID
        purchase.setTotalCost(totalCost);
        purchase.setStatus("pending");

        Map<Integer, Purchase> userPurchases = purchases.computeIfAbsent(user.getUserID(), k -> new ConcurrentHashMap<>());
        int purchaseId = generatePurchaseId(userPurchases);
        purchase.setPurchaseId(purchaseId);
        userPurchases.put(purchaseId, purchase);

        SOMSUtils.saveAllData(users, accounts, items, purchases);

        out.println("Purchase of \"" + item.getName() + "\" x" + quantity + " reserved for $" + String.format("%.2f", totalCost) + ". Awaiting seller confirmation.");
        out.println("---END---");
        logger.info("User " + user.getUserID() + " reserved purchase: " + item.getName() + " x" + quantity + " for $" + String.format("%.2f", totalCost));
    }



    /**
     * Generates a unique purchase ID based on existing purchases.
     *
     * @param userPurchases The map of existing purchases for the user.
     * @return A unique purchase ID.
     */
    private int generatePurchaseId(Map<Integer, Purchase> userPurchases) {
        if (userPurchases.isEmpty()) {
            return 1;
        }
        return Collections.max(userPurchases.keySet()) + 1;
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
                    purchase.getPurchaseId(),
                    purchase.getItemName(),
                    purchase.getQuantity(),
                    purchase.getPurchaseDate().toString(),
                    (purchase.getSellerID() != null) ? purchase.getSellerID() : "N/A",
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
        out.println("2. complete [buyerID] [purchaseId] [delivered|unfulfilled] - Mark a purchase as delivered or unfulfilled.");
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
     * @param user    The authenticated User object representing the seller.
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
                // Handle the 'add' command
                handleAddCommand(parts, out, user);
                break;

            case "complete":
                // Handle the 'complete' command with enhanced parameters
                handleCompleteCommand(parts, out, user);
                break;

            case "view":
                // Handle the 'view' command
                handleViewCommand(parts, out, user);
                break;

            case "exit":
                // Handle the 'exit' command
                handleExitCommand(out, user);
                break;

            default:
                // Handle unknown commands
                out.println("Unknown command.");
                out.println("---END---");
                logger.warning("Unknown command from seller: " + user.getUserID() + " - " + command);
                break;
        }
    }

    /**
     * Handles the 'complete' command issued by the seller.
     *
     * @param parts  The split command parts.
     * @param out    The PrintWriter to send responses to the client.
     * @param user   The authenticated User object representing the seller.
     */
    private void handleCompleteCommand(String[] parts, PrintWriter out, User user) {
        if (parts.length < 2) {
            out.println("Usage: complete [buyerID] [purchaseId] [delivered|unfulfilled]");
            out.println("---END---");
            return;
        }

        // Split the parameters into buyerID, purchaseId, and status
        String[] completeParams = parts[1].split("\\s+");
        if (completeParams.length < 3) {
            out.println("Usage: complete [buyerID] [purchaseId] [delivered|unfulfilled]");
            out.println("---END---");
            return;
        }

        String buyerID = completeParams[0];
        String purchaseIdStr = completeParams[1];
        String status = completeParams[2].toLowerCase();

        // Validate status input
        if (!status.equals("delivered") && !status.equals("unfulfilled")) {
            out.println("Invalid status. Use 'delivered' or 'unfulfilled'.");
            out.println("---END---");
            return;
        }

        // Validate and parse purchaseId
        int purchaseId;
        try {
            purchaseId = Integer.parseInt(purchaseIdStr);
            if (purchaseId <= 0) {
                throw new NumberFormatException("Purchase ID must be positive.");
            }
        } catch (NumberFormatException e) {
            out.println("Invalid purchase ID. Please enter a positive numeric value.");
            out.println("---END---");
            return;
        }

        // Proceed to complete the transaction
        completeTransaction(out, buyerID, purchaseId, status, user);
    }

    /**
     * Completes a transaction by marking it as delivered or unfulfilled.
     *
     * @param out        The PrintWriter to send responses to the client.
     * @param buyerID    The ID of the buyer associated with the purchase.
     * @param purchaseId The ID of the purchase to complete.
     * @param status     The status to mark the purchase as ("delivered" or "unfulfilled").
     * @param user       The seller performing the action.
     */
    private void completeTransaction(PrintWriter out, String buyerID, int purchaseId, String status, User user) {
        logger.info("Attempting to complete transaction. Buyer ID: " + buyerID + ", Purchase ID: " + purchaseId + ", Status: " + status);

        Purchase purchase = null;

        // Validate buyerID exists
        if (!purchases.containsKey(buyerID)) {
            out.println("Buyer ID not found.");
            out.println("---END---");
            logger.warning("Seller " + user.getUserID() + " attempted to complete a purchase for non-existent buyer ID: " + buyerID);
            return;
        }

        Map<Integer, Purchase> buyerPurchases = purchases.get(buyerID);
        if (buyerPurchases == null || !buyerPurchases.containsKey(purchaseId)) {
            out.println("Purchase ID not found for the specified buyer.");
            out.println("---END---");
            logger.warning("Seller " + user.getUserID() + " attempted to complete non-existent purchase ID: " + purchaseId + " for buyer ID: " + buyerID);
            return;
        }

        purchase = buyerPurchases.get(purchaseId);

        // Ensure purchase is not null
        if (purchase == null) {
            out.println("Purchase details are unavailable.");
            out.println("---END---");
            logger.warning("Purchase details are null for Purchase ID: " + purchaseId + " and Buyer ID: " + buyerID);
            return;
        }

        synchronized (purchase) {
            if (!purchase.getStatus().equalsIgnoreCase("pending")) {
                out.println("Purchase already processed.");
                out.println("---END---");
                logger.warning("Purchase ID " + purchaseId + " for Buyer ID " + buyerID + " is already " + purchase.getStatus() + ".");
                return;
            }

            if (status.equals("delivered")) {
                // Handle 'delivered' status
                handleDeliveredStatus(out, purchase, user, buyerID);
            } else if (status.equals("unfulfilled")) {
                // Handle 'unfulfilled' status
                handleUnfulfilledStatus(out, purchase, user, buyerID);
            }
        }
    }

    /**
     * Handles the 'delivered' status for a completed transaction.
     *
     * @param out      The PrintWriter to send responses to the client.
     * @param purchase The Purchase object being processed.
     * @param user     The seller performing the action.
     * @param buyerID  The ID of the buyer associated with the purchase.
     */
    private void handleDeliveredStatus(PrintWriter out, Purchase purchase, User user, String buyerID) {
        double amount = purchase.getTotalCost();
        int sellerAccountNumber = user.getAccountNumber();
        Account sellerAccount = accounts.get(sellerAccountNumber);

        if (sellerAccount == null) {
            out.println("Seller account not found.");
            out.println("---END---");
            logger.severe("Seller " + user.getUserID() + " account not found.");
            return;
        }

        // Transfer funds to seller's account
        synchronized (sellerAccount) {
            sellerAccount.addFunds(amount);
        }

        // Update purchase status and sellerID
        purchase.setStatus("fulfilled");
        purchase.setSellerID(user.getUserID());

        // Persist data
        SOMSUtils.saveAllData(users, accounts, items, purchases);

        // Notify seller
        out.println("Purchase ID " + purchase.getPurchaseId() + " for buyer ID " + buyerID + " marked as delivered. $" + String.format("%.2f", amount) + " transferred to your account.");
        out.println("---END---");
        logger.info("Seller " + user.getUserID() + " fulfilled purchase ID: " + purchase.getPurchaseId() + " for buyer ID: " + buyerID + " and transferred $" + String.format("%.2f", amount) + " to their account.");
    }

    /**
     * Handles the 'unfulfilled' status for a completed transaction.
     *
     * @param out      The PrintWriter to send responses to the client.
     * @param purchase The Purchase object being processed.
     * @param user     The seller performing the action.
     * @param buyerID  The ID of the buyer associated with the purchase.
     */
    private void handleUnfulfilledStatus(PrintWriter out, Purchase purchase, User user, String buyerID) {
        double amount = purchase.getTotalCost();

        // Log the attempt to mark as unfulfilled
        logger.info("Seller " + user.getUserID() + " is attempting to mark Purchase ID " + purchase.getPurchaseId() +
                " for Buyer ID " + buyerID + " as unfulfilled.");

        // Get customer information
        User customer = users.get(buyerID);
        if (customer == null) {
            out.println("Buyer ID not found.");
            out.println("---END---");
            logger.severe("Buyer ID " + buyerID + " not found.");
            return;
        }

        // Get customer's account number and validate account exists
        int customerAccountNumber = customer.getAccountNumber();
        Account customerAccount = accounts.get(customerAccountNumber);
        if (customerAccount == null) {
            out.println("Customer account not found.");
            out.println("---END---");
            logger.severe("Customer account number " + customerAccountNumber + " for Buyer ID " + buyerID + " not found.");
            return;
        }

        // Return funds to customer's account
        synchronized (customerAccount) {
            customerAccount.releaseFunds(amount);
        }
        logger.info("Released $" + String.format("%.2f", amount) + " to Customer account number " + customerAccountNumber + ".");

        // Update purchase status
        purchase.setStatus("unfulfilled");
        purchase.setSellerID("unfulfilled"); // Indicates unfulfilled status
        logger.info("Updated Purchase ID " + purchase.getPurchaseId() + " status to 'unfulfilled'.");

        // Restore item quantity in inventory
        synchronized (items) {
            Item item = getItemByName(purchase.getItemName());
            if (item != null) {
                item.increaseQuantity(purchase.getQuantity());
                logger.info("Restored quantity for item: " + purchase.getItemName() + ", Quantity: " + purchase.getQuantity());
            } else {
                logger.warning("Item \"" + purchase.getItemName() + "\" not found during quantity restoration.");
            }
        }

        // Persist data
        SOMSUtils.saveAllData(users, accounts, items, purchases);
        logger.info("Persisted data after marking Purchase ID " + purchase.getPurchaseId() + " as unfulfilled.");

        // Notify seller
        out.println("Purchase ID " + purchase.getPurchaseId() + " for buyer ID " + buyerID + " marked as unfulfilled. $" +
                String.format("%.2f", amount) + " returned to the customer.");
        out.println("---END---");
        logger.info("Seller " + user.getUserID() + " successfully marked Purchase ID " + purchase.getPurchaseId() +
                " as unfulfilled and returned $" + String.format("%.2f", amount) + " to the customer.");
    }

    /**
     * Handles the 'add' command issued by the seller.
     *
     * @param parts  The split command parts.
     * @param out    The PrintWriter to send responses to the client.
     * @param user   The authenticated User object representing the seller.
     */
    private void handleAddCommand(String[] parts, PrintWriter out, User user) {
        if (parts.length < 2) {
            out.println("Usage: add [itemName] [price] [quantity]");
            out.println("---END---");
            return;
        }

        String[] addParams = parts[1].split("\\s+");
        if (addParams.length < 3) {
            out.println("Usage: add [itemName] [price] [quantity]");
            out.println("---END---");
            return;
        }

        String itemName = addParams[0];
        String priceStr = addParams[1];
        String quantityStr = addParams[2];

        double price;
        int quantity;

        // Validate and parse price
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                throw new NumberFormatException("Price must be positive.");
            }
        } catch (NumberFormatException e) {
            out.println("Invalid price. Please enter a positive numeric value.");
            out.println("---END---");
            return;
        }

        // Validate and parse quantity
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                throw new NumberFormatException("Quantity must be positive.");
            }
        } catch (NumberFormatException e) {
            out.println("Invalid quantity. Please enter a positive integer value.");
            out.println("---END---");
            return;
        }

        // Proceed to add or update the item
        addItem(out, itemName, price, quantity, user);
    }

    /**
     * Adds or updates an item in the inventory.
     *
     * @param out      The PrintWriter to send responses to the client.
     * @param itemName The name of the item.
     * @param price    The price of the item.
     * @param quantity The quantity of the item.
     * @param user     The seller performing the action.
     */
    private void addItem(PrintWriter out, String itemName, double price, int quantity, User user) {
        // Validate price and quantity
        if (price <= 0 || quantity <= 0) {
            out.println("Price and quantity must be positive.");
            out.println("---END---");
            logger.warning("Seller " + user.getUserID() + " attempted to add item with non-positive price or quantity: " + itemName);
            return;
        }

        // Normalize item name to maintain consistency (e.g., all lowercase)
        String normalizedItemName = itemName.toLowerCase();

        synchronized (items) {
            if (items.containsKey(normalizedItemName)) {
                Item existingItem = items.get(normalizedItemName);

                // Check if the current seller is the owner of the item
                if (!existingItem.getSellerID().equalsIgnoreCase(user.getUserID())) {
                    out.println("You do not have permission to modify this item.");
                    out.println("---END---");
                    logger.warning("Seller " + user.getUserID() + " attempted to modify item not owned by them: " + itemName);
                    return;
                }

                // Update price and quantity
                existingItem.setPrice(price);
                existingItem.increaseQuantity(quantity);
                logger.info("Seller " + user.getUserID() + " updated item: " + itemName + " with price: $" + price + " and additional quantity: " + quantity);
            } else {
                // Create a new item with sellerID assigned to the current seller
                Item newItem = new Item(itemName, price, quantity, user.getUserID());
                items.put(normalizedItemName, newItem);
                logger.info("Seller " + user.getUserID() + " added new item: " + itemName + " with price: $" + price + " and quantity: " + quantity);
            }
        }

        // Persist data after modification
        SOMSUtils.saveAllData(users, accounts, items, purchases);

        // Send confirmation to the seller
        out.println("Item \"" + itemName + "\" added/updated successfully.");
        out.println("---END---");
    }




    /**
     * Handles the 'view' command issued by the seller.
     *
     * @param parts  The split command parts.
     * @param out    The PrintWriter to send responses to the client.
     * @param user   The authenticated User object representing the seller.
     */
    private void handleViewCommand(String[] parts, PrintWriter out, User user) {
        if (parts.length < 2) {
            out.println("Usage: view [transactions]");
            out.println("---END---");
            return;
        }

        String subAction = parts[1].toLowerCase();
        if (subAction.equals("transactions")) {
            viewTransactionHistory(out, user);
        } else {
            out.println("Unknown view command. Usage: view transactions");
            out.println("---END---");
            logger.warning("Unknown sub-action for view command from seller: " + user.getUserID() + " - " + subAction);
        }
    }

    /**
     * Handles the 'exit' command issued by the seller.
     *
     * @param out    The PrintWriter to send responses to the client.
     * @param user   The authenticated User object representing the seller.
     */
    private void handleExitCommand(PrintWriter out, User user) {
        out.println("Goodbye!");
        out.println("---END---");
        logger.info("Seller " + user.getUserID() + " has exited the application.");
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
     * Retrieves the top 5 sellers based on the number of fulfilled transactions.
     *
     * @return A list of TopSeller objects.
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

        logger.info("Seller Transaction Counts: " + sellerTransactionCount);

        // Create a list of TopSeller objects
        List<TopSeller> topSellersList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sellerTransactionCount.entrySet()) {
            String sellerID = entry.getKey();
            int transactionCount = entry.getValue();
            User seller = users.get(sellerID);
            if (seller != null && seller.getName() != null) {
                topSellersList.add(new TopSeller(sellerID, seller.getName(), transactionCount));
            } else {
                logger.warning("Seller with ID " + sellerID + " not found in users map or has no name.");
            }
        }

        // Sort the list in descending order of transaction count
        topSellersList.sort((s1, s2) -> Integer.compare(s2.getTransactionCount(), s1.getTransactionCount()));

        logger.info("Sorted Top Sellers: " + topSellersList.stream()
                .map(s -> s.getSellerName() + ": " + s.getTransactionCount())
                .collect(Collectors.joining(", ")));

        // Return the top 5 sellers or fewer if not enough sellers exist
        List<TopSeller> top5Sellers = topSellersList.stream().limit(5).collect(Collectors.toList());
        logger.info("Final Top 5 Sellers: " + top5Sellers.stream()
                .map(s -> s.getSellerName() + ": " + s.getTransactionCount())
                .collect(Collectors.joining(", ")));
        return top5Sellers;
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
                if (purchase.getSellerID().equalsIgnoreCase(sellerID)) { // Only include fulfilled transactions
                    sb.append(String.format("%-5d %-20s %-10d %-20s %-15s %-10.2f %-15s\n",
                            purchase.getPurchaseId(),
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