package SOMSClientJava;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.*;

/**
 * ClientProgram handles user interactions, sends commands to the server,
 * and displays server responses.
 */
public class ClientProgram {
    private static final Logger logger = Logger.getLogger(ClientProgram.class.getName());

    public static void main(String[] args) {
        // Setup logger
        setupLogger();

        try (Client client = new Client();
             Scanner scanner = new Scanner(System.in)) {

            logger.info("Connected to the server.");

            // Initial server welcome message
            String welcome = client.readResponse();
            if (welcome != null) {
                System.out.println(welcome);
            }

            // Authentication
            System.out.print("Enter your userID: ");
            String userID = scanner.nextLine();
            client.sendCommand(userID);

            System.out.print("Enter your password: ");
            String password = scanner.nextLine();
            client.sendCommand(password);

            String authResponse = client.readResponse();
            if (authResponse != null) {
                System.out.println(authResponse);
            }

            if (authResponse == null || !authResponse.equalsIgnoreCase("Authentication successful.")) {
                System.out.println("Exiting application.");
                return;
            }

            // If authenticated, receive top sellers
            String topSellers = client.readResponse();
            if (topSellers != null && !topSellers.isEmpty()) {
                System.out.println(topSellers);
            }

            // Command loop
            String response;
            while (true) {
                System.out.print("Enter a command: ");
                String command = scanner.nextLine();
                client.sendCommand(command);
                response = client.readResponse();
                if (response != null) {
                    System.out.println(response);
                }

                if (response != null && response.equalsIgnoreCase("Goodbye!")) {
                    break;
                }
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Client error: ", e);
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Configures the logger to log severe messages to the console.
     */
    private static void setupLogger() {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.SEVERE); // Set the root logger level

        // Remove default handlers to prevent duplicate logging
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }

        // Add console handler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.SEVERE); // Set handler level
        consoleHandler.setFormatter(new SimpleFormatter()); // Optional: set a formatter
        rootLogger.addHandler(consoleHandler);
    }
}