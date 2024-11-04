package SOMSServerJava;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerProgram {
    private static final int PORT = 8888;
    private static final SOMS soms = new SOMS();
    private static final int THREAD_POOL_SIZE = 10; // Configurable thread pool size for handling multiple clients
    private static final ExecutorService clientThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) {
        initializeAccounts(); // Initialize accounts with sample data
        runServer(); // Start the server
    }

    /**
     * Initializes the server with sample accounts.
     */
    private static void initializeAccounts() {
        soms.createAccount(1, 1001, 500);
        soms.createAccount(1, 1002, 1000);
        soms.createAccount(2, 2001, 750);
        soms.createAccount(3, 3001, 1200);
        System.out.println("Sample accounts initialized.");
    }

    /**
     * Runs the server, accepting and handling client connections.
     */
    private static void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT + ". Waiting for incoming connections...");

            // Continuously accept client connections
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());

                    // Handle each client connection in a separate thread
                    ClientHandler clientHandler = new ClientHandler(clientSocket, soms);
                    clientThreadPool.execute(clientHandler);

                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: Could not start the server on port " + PORT);
            e.printStackTrace();
        } finally {
            shutdownServer();
        }
    }

    /**
     * Shuts down the server and releases all resources.
     */
    private static void shutdownServer() {
        System.out.println("Shutting down server...");
        clientThreadPool.shutdown(); // Gracefully shut down the client thread pool
        System.out.println("Server shut down successfully.");
    }
}