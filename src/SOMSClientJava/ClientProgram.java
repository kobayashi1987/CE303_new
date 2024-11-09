package SOMSClientJava;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.*;

public class ClientProgram {
    private static final Logger logger = Logger.getLogger(ClientProgram.class.getName());

    public static void main(String[] args) {
        setupLogger();

        try (Client client = new Client();
             Scanner scanner = new Scanner(System.in)) {

            logger.info("Connected to the server.");

            // Welcome message
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

            // Role prompt
            String rolePrompt = client.readResponse();
            if (rolePrompt != null && !rolePrompt.isEmpty()) {
                System.out.println(rolePrompt);
            }

            // Initial available items
            String initialResponse = readMultiLineResponse(client);
            if (initialResponse != null && !initialResponse.isEmpty()) {
                System.out.println(initialResponse);
            }

            // Command loop
            String response;
            while (true) {
                System.out.print("Enter a command: ");
                String command = scanner.nextLine();
                client.sendCommand(command);
                response = readMultiLineResponse(client);
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