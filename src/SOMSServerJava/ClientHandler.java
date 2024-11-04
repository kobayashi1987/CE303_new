package SOMSServerJava;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final SOMS soms;
    private final Gson gson = new GsonBuilder().create();

    public ClientHandler(Socket socket, SOMS soms) {
        this.socket = socket;
        this.soms = soms;
    }

    @Override
    public void run() {
        int clientId = 0;
        try (Scanner reader = new Scanner(socket.getInputStream());
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            clientId = Integer.parseInt(reader.nextLine().trim());
            List<Integer> accounts = soms.getListOfAccounts(clientId);
            if (accounts.isEmpty()) {
                writer.println(gson.toJson("ERROR: Client ID not found or no accounts associated."));
                socket.close();
                return;
            }
            writer.println(gson.toJson("SUCCESS"));

            while (true) {
                String command = reader.nextLine().trim();
                String[] commandParts = command.split(" ");

                try {
                    switch (commandParts[0].toLowerCase()) {
                        case "accounts":
                            writer.println(gson.toJson(accounts));
                            break;

                        case "balance":
                            if (commandParts.length < 2) {
                                writer.println(gson.toJson("ERROR: Missing account number for balance check."));
                                break;
                            }
                            int accountNumber = Integer.parseInt(commandParts[1]);
                            writer.println(gson.toJson(soms.getAccountBalance(clientId, accountNumber)));
                            break;

                        case "transfer":
                            if (commandParts.length < 4) {
                                writer.println(gson.toJson("ERROR: Insufficient parameters for transfer."));
                                break;
                            }
                            int fromAccount = Integer.parseInt(commandParts[1]);
                            int toAccount = Integer.parseInt(commandParts[2]);
                            int amount = Integer.parseInt(commandParts[3]);
                            soms.transfer(clientId, fromAccount, toAccount, amount);
                            writer.println(gson.toJson("SUCCESS"));
                            break;

                        default:
                            writer.println(gson.toJson("ERROR: Unknown command."));
                            break;
                    }
                } catch (Exception e) {
                    writer.println(gson.toJson("ERROR: " + e.getMessage()));
                }
            }
        } catch (Exception e) {
            System.out.println("Client " + clientId + " disconnected.");
        }
    }
}