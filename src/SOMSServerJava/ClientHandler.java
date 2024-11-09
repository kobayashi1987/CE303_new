package SOMSServerJava;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private final Socket clientSocket;
    private final Map<String, User> users;
    private final Map<Integer, Account> accounts;
    private final Map<String, Item> items;
    private final Map<String, Map<Integer, Purchase>> purchases;

    public ClientHandler(Socket socket, Map<String, User> users, Map<Integer, Account> accounts,
                         Map<String, Item> items, Map<String, Map<Integer, Purchase>> purchases) {
        this.clientSocket = socket;
        this.users = users;
        this.accounts = accounts;
        this.items = items;
        this.purchases = purchases;
    }

    @Override
    public void run() {
        try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            logger.info("Client connected: " + clientSocket.getRemoteSocketAddress());

            out.println("Welcome to SOMS Server!");

            // Authentication
            String userID = in.readLine();
            String password = in.readLine();

            if (authenticate(userID, password)) {
                out.println("Authentication successful.");
                User user = users.get(userID);
                if (user.getRole().equals("customer")) {
                    out.println("You are logged in as a Customer.");
                    handleCustomer(out, in, user);
                } else if (user.getRole().equals("seller")) {
                    out.println("You are logged in as a Seller.");
                    handleSeller(out, in, user);
                } else {
                    out.println("Unknown role. Connection terminated.");
                    logger.warning("User " + userID + " has an unknown role: " + user.getRole());
                }
            } else {
                out.println("Authentication failed. Invalid userID or password.");
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

    private boolean authenticate(String userID, String password) {
        if (users.containsKey(userID)) {
            User user = users.get(userID);
            return user.getPassword().equals(password);
        }
        return false;
    }

    private void handleCustomer(PrintWriter out, BufferedReader in, User user) throws IOException {
        displayAvailableItems(out);

        String command;
        while ((command = in.readLine()) != null) {
            processCustomerCommand(command, out, user);
            if (command.equalsIgnoreCase("exit")) {
                out.println("Goodbye!");
                break;
            }
        }
    }

    private void processCustomerCommand(String command, PrintWriter out, User user) {
        if (command == null || command.trim().isEmpty()) {
            out.println("Invalid command.");
            out.println("---END---");
            logger.warning("Received empty command from user: " + user.getUserID());
            return;
        }

        String[] tokens = command.trim().split("\\s+", 2);
        String action = tokens[0].toLowerCase();

        switch (action) {
            case "view":
                if (tokens.length < 2) {
                    out.println("Usage: view [credits|items|history]");
                    out.println("---END---");
                } else {
                    String subAction = tokens[1].toLowerCase();
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
                            break;
                    }
                }
                break;

            case "buy":
                if (tokens.length < 2) {
                    out.println("Usage: buy [itemName] [quantity]");
                    out.println("---END---");
                } else {
                    String[] buyParams = tokens[1].split("\\s+");
                    if (buyParams.length == 1) {
                        // Default quantity to 1
                        String itemName = buyParams[0];
                        makePurchase(out, user, itemName, 1);
                    } else if (buyParams.length >=2 ) {
                        String itemName = buyParams[0];
                        try {
                            int quantity = Integer.parseInt(buyParams[1]);
                            makePurchase(out, user, itemName, quantity);
                        } catch (NumberFormatException e) {
                            out.println("Invalid quantity. Please enter a numeric value.");
                            out.println("---END---");
                            logger.warning("Invalid quantity from user: " + user.getUserID() + " - " + buyParams[1]);
                        }
                    } else {
                        out.println("Usage: buy [itemName] [quantity]");
                        out.println("---END---");
                    }
                }
                break;

            case "top":
                if (tokens.length < 2) {
                    out.println("Usage: top up [amount]");
                    out.println("---END---");
                } else {
                    String[] topParams = tokens[1].split("\\s+");
                    if (topParams.length < 2 || !topParams[0].equalsIgnoreCase("up")) {
                        out.println("Usage: top up [amount]");
                        out.println("---END---");
                    } else {
                        try {
                            double amount = Double.parseDouble(topParams[1]);
                            topUpAmount(out, user, amount);
                        } catch (NumberFormatException e) {
                            out.println("Invalid amount. Please enter a numeric value.");
                            out.println("---END---");
                            logger.warning("Invalid top up amount from user: " + user.getUserID() + " - " + topParams[1]);
                        }
                    }
                }
                break;

            case "exit":
                // Handled in the run loop
                break;

            default:
                out.println("Unknown command.");
                out.println("---END---");
                logger.warning("Received unknown command from user: " + user.getUserID() + " - " + command);
                break;
        }
    }

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

    private void makePurchase(PrintWriter out, User user, String itemName, int quantity) {
        if (quantity <= 0) {
            out.println("Quantity must be greater than zero.");
            out.println("---END---");
            logger.warning("User " + user.getUserID() + " attempted to purchase non-positive quantity: " + quantity);
            return;
        }

        Item item = items.get(itemName);
        if (item == null) {
            out.println("Item \"" + itemName + "\" does not exist.");
            out.println("---END---");
            logger.warning("User " + user.getUserID() + " attempted to purchase non-existent item: " + itemName);
            return;
        }

        synchronized (item) {
            if (item.getQuantityAvailable() < quantity) {
                out.println("Insufficient quantity available for \"" + itemName + "\". Available: " + item.getQuantityAvailable());
                out.println("---END---");
                logger.warning("User " + user.getUserID() + " attempted to purchase more than available for item: " + itemName);
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
        purchase.setSellerID("admin_user"); // Placeholder
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

    private void handleSeller(PrintWriter out, BufferedReader in, User user) throws IOException {
        out.println("Seller functionalities are not yet implemented.");
        out.println("---END---");
        logger.info("Seller functionalities not implemented for user: " + user.getUserID());
    }
}