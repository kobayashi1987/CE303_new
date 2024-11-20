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

            logger.info("Connected to the server.");

            // Read and display the welcome message
            String welcome = readMultiLineResponse(client);
            if (welcome != null) {
                System.out.println(welcome);
            }

            // Read and display the UserID prompt
            String userIDPrompt = readMultiLineResponse(client);
            if (userIDPrompt != null) {
                System.out.println(userIDPrompt);
            }

            // Prompt for UserID
            String userID = scanner.nextLine();
            client.sendCommand(userID);

            // Read and display the Password prompt
            String passwordPrompt = readMultiLineResponse(client);
            if (passwordPrompt != null) {
                System.out.println(passwordPrompt);
            }

            // Prompt for Password
            String password = scanner.nextLine();
            client.sendCommand(password);

            // Read authentication response
            String authResponse = readMultiLineResponse(client);
            if (authResponse != null) {
                System.out.println(authResponse);
            }

            // Check if authentication was successful
            if (authResponse == null || !authResponse.toLowerCase().contains("authentication successful")) {
                System.out.println("Exiting application.");
                return;
            }

            // Read role message
            String roleMessage = readMultiLineResponse(client);
            if (roleMessage != null) {
                System.out.println(roleMessage);
            }

            // Read Additional Messages Based on Role
            // For Customer: Top Sellers and Available Items
            // For Seller: Command Panel only
            boolean isSeller = roleMessage.toLowerCase().contains("seller");

            if (!isSeller) {
                // For Customers: Read Top Sellers
                String topSellers = readMultiLineResponse(client);
                if (topSellers != null && !topSellers.isEmpty()) {
                    System.out.println(topSellers);
                }

                // Read Available Items
                String availableItems = readMultiLineResponse(client);
                if (availableItems != null && !availableItems.isEmpty()) {
                    System.out.println(availableItems);
                }
            }

            // Read Command Panel
            String commandPanel = readMultiLineResponse(client);
            if (commandPanel != null && !commandPanel.isEmpty()) {
                System.out.println(commandPanel);
            }

            // Prompt for Commands
            enterCommandLoop(client, scanner);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Client error: ", e);
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
        return sb.toString().trim();
    }

    /**
     * Handles the command loop where the user can enter commands.
     *
     * @param client  The client instance to communicate with the server.
     * @param scanner The scanner to read user input.
     * @throws IOException If an I/O error occurs.
     */
    private static void enterCommandLoop(Client client, Scanner scanner) throws IOException {
        while (true) {
            System.out.print("Enter a command: ");
            String command = scanner.nextLine();
            client.sendCommand(command);
            String response = readMultiLineResponse(client);
            if (response != null && !response.isEmpty()) {
                System.out.println(response);
            }

            if (response != null && response.equalsIgnoreCase("Goodbye!")) {
                break;
            }
        }
    }

    /**
     * Sets up the logger to log only severe messages to the console.
     */
    private static void setupLogger() {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.SEVERE);

        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.SEVERE);
        consoleHandler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(consoleHandler);
    }
}