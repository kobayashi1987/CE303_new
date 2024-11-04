package SOMSServerJava;

import java.io.FileReader;
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

    public void startServer() {
        setupLogger();

        // Load users and accounts from JSON files
        loadUsers("users.json");
        loadAccounts("accounts.json");

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

    private void loadUsers(String filename) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filename)) {
            Map<String, User> loadedUsers = gson.fromJson(reader, new TypeToken<Map<String, User>>() {}.getType());
            if (loadedUsers != null) {
                users.putAll(loadedUsers);
                logger.info("Loaded " + loadedUsers.size() + " users.");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading users: ", e);
        }
    }

    private void loadAccounts(String filename) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filename)) {
            Map<String, Account> loadedAccounts = gson.fromJson(reader, new TypeToken<Map<String, Account>>() {}.getType());
            if (loadedAccounts != null) {
                for (Map.Entry<String, Account> entry : loadedAccounts.entrySet()) {
                    accounts.put(Integer.parseInt(entry.getKey()), entry.getValue());
                }
                logger.info("Loaded " + loadedAccounts.size() + " accounts.");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading accounts: ", e);
        }
    }

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