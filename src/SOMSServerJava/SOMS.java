package SOMSServerJava;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * SOMS is the main server class that listens for client connections
 * and delegates each connection to a ClientHandler.
 */
public class SOMS {
    private static final int PORT = 12345;
    private static final Logger logger = Logger.getLogger(SOMS.class.getName());
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<Integer, Account> accounts = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        SOMS server = new SOMS();
        server.startServer();
    }

    /**
     * Starts the server, loads user and account data, validates consistency,
     * and listens for client connections.
     */
    public void startServer() {
        setupLogger();

        // Load users and accounts using SOMSUtils
        Map<String, User> loadedUsers = SOMSUtils.loadUsers("users.json");
        Map<Integer, Account> loadedAccounts = SOMSUtils.loadAccounts("accounts.json");

        if (loadedUsers == null || loadedAccounts == null) {
            logger.severe("Failed to load users or accounts. Server is shutting down.");
            return;
        }

        users.putAll(loadedUsers);
        accounts.putAll(loadedAccounts);

        // Validate consistency between users and accounts
        boolean isConsistent = SOMSUtils.validateUserAccountConsistency(users, accounts);
        if (!isConsistent) {
            logger.severe("User and account data are inconsistent. Server is shutting down.");
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Server starting on port " + PORT);
            logger.info("Server started. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, users, accounts);
                Thread clientThread = new Thread(handler);
                clientThread.start();
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server error: ", e);
        }
    }

    /**
     * Sets up the logger to log messages to both the console and a file.
     */
    private void setupLogger() {
        try {
            LogManager.getLogManager().reset();
            Logger rootLogger = Logger.getLogger("");

            // Console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            rootLogger.addHandler(consoleHandler);

            // File handler
            FileHandler fileHandler = new FileHandler("soms.log", true);
            fileHandler.setLevel(Level.INFO);
            fileHandler.setFormatter(new SimpleFormatter());
            rootLogger.addHandler(fileHandler);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to setup logger: ", e);
        }
    }
}