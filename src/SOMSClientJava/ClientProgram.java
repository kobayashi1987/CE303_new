//package SOMSClientJava;
//
//import java.io.IOException;
//import java.util.Scanner;
//import java.util.logging.*;
//import java.util.logging.ConsoleHandler;
//import java.util.logging.SimpleFormatter;
//
//public class ClientProgram {
//    private static final Logger logger = Logger.getLogger(ClientProgram.class.getName());
//
//    public static void main(String[] args) {
//        setupLogger();
//
//        try (Client client = new Client();
//             Scanner scanner = new Scanner(System.in)) {
//
//            logger.info("Connected to the server.");
//
//            // Welcome message
//            String welcome = readMultiLineResponse(client);
//            if (welcome != null) {
//                System.out.println(welcome);
//            }
//
//            // Authentication
//            System.out.print("Enter your userID: ");
//            String userID = scanner.nextLine();
//            client.sendCommand(userID);
//
//            System.out.print("Enter your password: ");
//            String password = scanner.nextLine();
//            client.sendCommand(password);
//
//            // Read authentication response
//            String authResponse = readMultiLineResponse(client);
//            if (authResponse != null) {
//                System.out.println(authResponse);
//            }
//
//            if (authResponse == null || !authResponse.trim().equalsIgnoreCase("Authentication successful.")) {
//                System.out.println("Exiting application.");
//                return;
//            }
//
//            // Read role prompt
//            String rolePrompt = readMultiLineResponse(client);
//            if (rolePrompt != null && !rolePrompt.isEmpty()) {
//                System.out.println(rolePrompt);
//            }
//
//            // If customer, display available items
//            if (rolePrompt != null && rolePrompt.toLowerCase().contains("customer")) {
//                String initialResponse = readMultiLineResponse(client);
//                if (initialResponse != null && !initialResponse.isEmpty()) {
//                    System.out.println(initialResponse);
//                }
//            }
//
//            // Read Command Panel
//            String commandPanel = readMultiLineResponse(client);
//            if (commandPanel != null && !commandPanel.isEmpty()) {
//                System.out.println(commandPanel);
//            }
//
//            // Command loop
//            String response;
//            while (true) {
//                System.out.print("Enter a command: ");
//                String command = scanner.nextLine();
//                client.sendCommand(command);
//                response = readMultiLineResponse(client);
//                if (response != null) {
//                    System.out.println(response);
//                }
//
//                if (response != null && response.equalsIgnoreCase("Goodbye!")) {
//                    break;
//                }
//            }
//
//        } catch (IOException e) {
//            logger.log(Level.SEVERE, "Client error: ", e);
//            System.out.println("An error occurred: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Reads multi-line responses from the server until the delimiter '---END---' is encountered.
//     *
//     * @param client The client instance to read responses from.
//     * @return The accumulated response as a single String.
//     * @throws IOException If an I/O error occurs.
//     */
//    private static String readMultiLineResponse(Client client) throws IOException {
//        StringBuilder sb = new StringBuilder();
//        String line;
//        while ((line = client.readResponse()) != null) {
//            if (line.equals("---END---")) {
//                break;
//            }
//            sb.append(line).append("\n");
//        }
//        return sb.toString().trim();
//    }
//
//    /**
//     * Sets up the logger to log only severe messages to the console.
//     */
//    private static void setupLogger() {
//        Logger rootLogger = Logger.getLogger("");
//        rootLogger.setLevel(Level.SEVERE);
//
//        Handler[] handlers = rootLogger.getHandlers();
//        for (Handler handler : handlers) {
//            rootLogger.removeHandler(handler);
//        }
//
//        ConsoleHandler consoleHandler = new ConsoleHandler();
//        consoleHandler.setLevel(Level.SEVERE);
//        consoleHandler.setFormatter(new SimpleFormatter());
//        rootLogger.addHandler(consoleHandler);
//    }
//}



package SOMSClientJava;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.*;

/**
 * The client program for interacting with the SOMS server.
 */
public class ClientProgram {
    private static final Logger logger = Logger.getLogger(ClientProgram.class.getName());

    public static void main(String[] args) {
        setupLogger();

        try (Client client = new Client();
             Scanner scanner = new Scanner(System.in)) {

            logger.info("Client application started.");

            // Read and display the welcome message
            String welcome = readMultiLineResponse(client);
            if (welcome != null && !welcome.isEmpty()) {
                System.out.println(welcome);
            }

            // Read and display the UserID prompt
            String userIDPrompt = readMultiLineResponse(client);
            if (userIDPrompt != null && !userIDPrompt.isEmpty()) {
                System.out.println(userIDPrompt);
            }

            // Prompt for UserID
            System.out.print("Enter your userID: ");
            String userID = scanner.nextLine();
            client.sendCommand(userID);

            // Read and display the Password prompt
            String passwordPrompt = readMultiLineResponse(client);
            if (passwordPrompt != null && !passwordPrompt.isEmpty()) {
                System.out.println(passwordPrompt);
            }

            // Prompt for Password
            System.out.print("Enter your password: ");
            String password = scanner.nextLine();
            client.sendCommand(password);

            // Read authentication response
            String authResponse = readMultiLineResponse(client);
            if (authResponse != null && !authResponse.isEmpty()) {
                System.out.println(authResponse);
            }

            // Check if authentication was successful
            if (authResponse == null || !authResponse.toLowerCase().contains("authentication successful")) {
                System.out.println("Exiting application.");
                logger.info("Authentication failed. Exiting client.");
                return;
            }

            // Read role message
            String roleMessage = readMultiLineResponse(client);
            if (roleMessage != null && !roleMessage.isEmpty()) {
                System.out.println(roleMessage);
            }

            // Read Command Panel
            String commandPanel = readMultiLineResponse(client);
            if (commandPanel != null && !commandPanel.isEmpty()) {
                System.out.println(commandPanel);
            }

            // Command loop
            while (true) {
                System.out.print("Enter a command: ");
                String command = scanner.nextLine();
                client.sendCommand(command);

                // Read and display the server's response
                String response = readMultiLineResponse(client);
                if (response != null && !response.isEmpty()) {
                    System.out.println(response);
                }

                // Check for exit condition
                if (command.equalsIgnoreCase("exit")) {
                    logger.info("User initiated exit. Terminating client.");
                    break;
                }
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Client encountered an I/O error: ", e);
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Reads multi-line responses from the server until the delimiter '---END---' is encountered.
     *
     * @param client The client instance to read responses from.
     * @return The accumulated response as a single String.
     * @throws IOException If an I/O error occurs.
     */
    private static String readMultiLineResponse(Client client) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = client.readResponse()) != null) {
            if (line.equals("---END---")) {
                break;
            }
            sb.append(line).append("\n");
        }
        String response = sb.toString().trim();
        logger.info("Received response from server:\n" + response);
        return response;
    }

    /**
     * Sets up the logger to log only severe messages to the console.
     */
    private static void setupLogger() {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.SEVERE);

        // Remove default handlers
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }

        // Add a custom console handler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.SEVERE);
        consoleHandler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(consoleHandler);

        logger.info("Logger setup complete.");
    }
}