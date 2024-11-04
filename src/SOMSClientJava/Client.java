package SOMSClientJava;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client implements AutoCloseable {
    private final Scanner reader;
    private final PrintWriter writer;
    private final Gson gson = new GsonBuilder().create();

    public Client(int clientId) throws Exception {
        Socket socket = new Socket("localhost", 8888);
        reader = new Scanner(socket.getInputStream());
        writer = new PrintWriter(socket.getOutputStream(), true);

        writer.println(clientId);
        String response = gson.fromJson(reader.nextLine(), String.class);
        if (!"SUCCESS".equalsIgnoreCase(response)) {
            throw new Exception("Login failed: " + response);
        }
    }

    public int[] getAccountNumbers() {
        writer.println("accounts");
        return gson.fromJson(reader.nextLine(), int[].class);
    }

    public int getBalance(int accountNumber) throws Exception {
        writer.println("balance " + accountNumber);
        String response = reader.nextLine();
        if (response.contains("ERROR")) {
            throw new Exception(gson.fromJson(response, String.class));
        }
        return gson.fromJson(response, Integer.class);
    }

    public void transfer(int fromAccount, int toAccount, int amount) throws Exception {
        writer.println("transfer " + fromAccount + " " + toAccount + " " + amount);
        String response = gson.fromJson(reader.nextLine(), String.class);
        if (!"SUCCESS".equalsIgnoreCase(response)) {
            throw new Exception("Transfer failed: " + response);
        }
    }

    @Override
    public void close() {
        reader.close();
        writer.close();
    }
}