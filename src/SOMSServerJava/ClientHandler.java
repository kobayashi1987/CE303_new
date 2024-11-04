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

    public ClientHandler(Socket socket, Map<String, User> users, Map<Integer, Account> accounts) {
        this.clientSocket = socket;
        this.users = users;
        this.accounts = accounts;
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

    private boolean authenticate(String userID, String password) {
        if (users.containsKey(userID)) {
            User user = users.get(userID);
            return user.getPassword().equals(password);
        }
        return false;
    }

    private void sendTopSellers(PrintWriter out) {
        // Example top sellers list
        String topSellers = "Top Sellers: [Item1, Item2, Item3]";
        out.println(topSellers);
        logger.info("Top sellers sent to client.");
    }

    private void processCommand(String command, PrintWriter out, String userID) {
        // Example command processing
        switch (command.toLowerCase()) {
            case "view credits":
                int accountNumber = users.get(userID).getAccountNumber();
                Account account = accounts.get(accountNumber);
                out.println("Your current balance: $" + account.getBalance());
                logger.info("User " + userID + " viewed credits.");
                break;
            case "buy item1":
                // Example buy operation
                out.println("You have bought: Item1");
                logger.info("User " + userID + " bought item: Item1");
                break;
            case "top up 500":
                // Example top up operation
                account = accounts.get(users.get(userID).getAccountNumber());
                account.setBalance(account.getBalance() + 500);
                out.println("Top up successful. New balance: $" + account.getBalance());
                logger.info("User " + userID + " topped up $500.");
                break;
            case "exit":
                // Exit command handled in the run loop
                break;
            default:
                out.println("Unknown command.");
                logger.info("User " + userID + " entered unknown command: " + command);
                break;
        }
    }
}