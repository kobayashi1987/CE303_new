package SOMSServerJava;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * SOMSServer is the main server class that listens for client connections.
 */
public class SOMSServer {
    private static final int PORT = 12345; // Define your desired port number
    private static final Logger logger = Logger.getLogger(SOMSServer.class.getName());

    // Data maps
    private Map<String, User> users;
    private Map<Integer, Account> accounts;
    private Map<String, Item> items;
    private Map<String, Map<Integer, Purchase>> purchases;

    /**
     * Constructor initializes data maps by loading from JSON files.
     */
    public SOMSServer() {
        users = SOMSUtils.loadUsers("users.json");
        accounts = SOMSUtils.loadAccounts("accounts.json");
        items = SOMSUtils.loadItems("items.json");
        purchases = SOMSUtils.loadPurchases("purchases.json");
    }

    /**
     * Starts the server and listens for client connections.
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("SOMSServer started. Listening on port " + PORT + ".");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("New client connected from " + clientSocket.getInetAddress());

                // Create a new ClientHandler thread for each client
                ClientHandler clientHandler = new ClientHandler(clientSocket, users, accounts, items, purchases);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }

        } catch (IOException e) {
            logger.severe("Error starting SOMSServer: " + e.getMessage());
        }
    }

    /**
     * The main method to run the SOMSServer.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        SOMSServer server = new SOMSServer();
        server.start();
    }
}