package SOMSServerJava;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ClientHandler handles communication with a connected client.
 */
public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private final Socket clientSocket;
    private final Map<String, User> users;
    private final Map<Integer, Account> accounts;
    private final Map<String, Double> availableItems;

    public ClientHandler(Socket socket, Map<String, User> users, Map<Integer, Account> accounts) {
        this.clientSocket = socket;
        this.users = users;
        this.accounts = accounts;
        this.availableItems = SOMSUtils.getAvailableItems();
    }

    @Override
    public void run() {
        try (
                // Initialize PrintWriter with autoFlush set to true
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            logger.info("Client connected: " + clientSocket.getRemoteSocketAddress());

            // Send welcome message
            out.println("Welcome to SOMS Server!");

            // Handle authentication
            String userID = in.readLine();
            String password = in.readLine();

            if (authenticate(userID, password)) {
                out.println("Authentication successful.");
                sendTopSellers(out);

                // Handle client commands
                String command;
                while ((command = in.readLine()) != null) {
                    processCommand(command, out, userID);
                    if (command.equalsIgnoreCase("exit")) {
                        out.println("Goodbye!");
                        break;
                    }
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

    /**
     * Authenticates the user based on userID and password.
     *
     * @param userID   The userID entered by the client.
     * @param password The password entered by the client.
     * @return True if authentication is successful, else false.
     */
    private boolean authenticate(String userID, String password) {
        if (users.containsKey(userID)) {
            User user = users.get(userID);
            return user.getPassword().equals(password);
        }
        return false;
    }

    /**
     * Sends the list of top sellers to the client.
     *
     * @param out The PrintWriter to send messages to the client.
     */
    private void sendTopSellers(PrintWriter out) {
        // Example top sellers list
        String topSellers = "Top Sellers: " + String.join(", ", availableItems.keySet());
        out.println(topSellers);
        logger.info("Top sellers sent to client.");
    }

    /**
     * Processes commands sent by the client.
     *
     * @param command The command received from the client.
     * @param out     The PrintWriter to send responses to the client.
     * @param userID  The userID of the authenticated client.
     */
    private void processCommand(String command, PrintWriter out, String userID) {
        if (command == null || command.trim().isEmpty()) {
            out.println("Invalid command.");
            logger.warning("Received empty command from user: " + userID);
            return;
        }

        String[] tokens = command.trim().split("\\s+", 2);
        String action = tokens[0].toLowerCase();

        switch (action) {
            case "view":
                if (tokens.length < 2) {
                    out.println("Usage: view [credits]");
                } else if (tokens[1].equalsIgnoreCase("credits")) {
                    viewCredits(out, userID);
                } else {
                    out.println("Unknown view command. Usage: view [credits]");
                }
                break;

            case "buy":
                if (tokens.length < 2) {
                    out.println("Usage: buy [item]");
                } else {
                    buyItem(out, userID, tokens[1]);
                }
                break;

            case "top":
                if (tokens.length < 2) {
                    out.println("Usage: top up [amount]");
                } else {
                    String[] subTokens = tokens[1].split("\\s+", 2);
                    if (subTokens.length < 2 || !subTokens[0].equalsIgnoreCase("up")) {
                        out.println("Usage: top up [amount]");
                    } else {
                        try {
                            double amount = Double.parseDouble(subTokens[1]);
                            topUpAmount(out, userID, amount);
                        } catch (NumberFormatException e) {
                            out.println("Invalid amount. Please enter a numeric value.");
                            logger.warning("Invalid top up amount from user: " + userID + " - " + subTokens[1]);
                        }
                    }
                }
                break;

            case "exit":
                // Handled in the run loop
                break;

            default:
                out.println("Unknown command.");
                logger.warning("Received unknown command from user: " + userID + " - " + command);
                break;
        }
    }

    /**
     * Displays the current balance of the user's account.
     *
     * @param out    The PrintWriter to send messages to the client.
     * @param userID The userID of the authenticated client.
     */
    private void viewCredits(PrintWriter out, String userID) {
        int accountNumber = users.get(userID).getAccountNumber();
        Account account = accounts.get(accountNumber);
        if (account != null) {
            out.println("Your current balance: $" + String.format("%.2f", account.getBalance()));
            logger.info("User " + userID + " viewed credits. Balance: $" + account.getBalance());
        } else {
            out.println("Account not found.");
            logger.severe("Account not found for userID: " + userID);
        }
    }

    /**
     * Processes the purchase of an item by the user.
     *
     * @param out    The PrintWriter to send messages to the client.
     * @param userID The userID of the authenticated client.
     * @param item   The item to be purchased.
     */
    private void buyItem(PrintWriter out, String userID, String item) {
        String formattedItem = capitalizeFirstLetter(item.trim());
        if (!availableItems.containsKey(formattedItem)) {
            out.println("Item \"" + formattedItem + "\" is not available for purchase.");
            logger.warning("User " + userID + " attempted to buy unavailable item: " + formattedItem);
            return;
        }

        double itemPrice = availableItems.get(formattedItem);
        int accountNumber = users.get(userID).getAccountNumber();
        Account account = accounts.get(accountNumber);

        if (account == null) {
            out.println("Account not found.");
            logger.severe("Account not found for userID: " + userID);
            return;
        }

        synchronized (account) {
            if (account.getBalance() >= itemPrice) {
                account.setBalance(account.getBalance() - itemPrice);
                out.println("You have successfully purchased: " + formattedItem + " for $" + String.format("%.2f", itemPrice));
                logger.info("User " + userID + " purchased item: " + formattedItem + " for $" + String.format("%.2f", itemPrice));
            } else {
                out.println("Insufficient balance to purchase: " + formattedItem);
                logger.warning("User " + userID + " has insufficient balance for item: " + formattedItem);
            }
        }
    }

    /**
     * Processes the top-up of the user's account by a specified amount.
     *
     * @param out     The PrintWriter to send messages to the client.
     * @param userID  The userID of the authenticated client.
     * @param amount  The amount to top up.
     */
    private void topUpAmount(PrintWriter out, String userID, double amount) {
        if (amount <= 0) {
            out.println("Top-up amount must be positive.");
            logger.warning("User " + userID + " attempted to top up a non-positive amount: $" + amount);
            return;
        }

        int accountNumber = users.get(userID).getAccountNumber();
        Account account = accounts.get(accountNumber);

        if (account == null) {
            out.println("Account not found.");
            logger.severe("Account not found for userID: " + userID);
            return;
        }

        synchronized (account) {
            account.setBalance(account.getBalance() + amount);
            out.println("Top-up successful. New balance: $" + String.format("%.2f", account.getBalance()));
            logger.info("User " + userID + " topped up $" + String.format("%.2f", amount) + ". New balance: $" + String.format("%.2f", account.getBalance()));
        }
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str The input string.
     * @return The string with the first letter capitalized.
     */
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }
}