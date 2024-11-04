package SOMSClientJava;

import java.util.Scanner;

public class ClientProgram {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter client ID: ");
            int clientId = Integer.parseInt(scanner.nextLine().trim());

            try (Client client = new Client(clientId)) {
                System.out.println("Login successful.");

                while (true) {
                    int[] accounts = client.getAccountNumbers();
                    System.out.println("Your accounts:");
                    for (int account : accounts) {
                        System.out.printf("  Account %d: Balance %d%n", account, client.getBalance(account));
                    }

                    System.out.print("Enter from account (-1 to quit): ");
                    int fromAccount = Integer.parseInt(scanner.nextLine().trim());
                    if (fromAccount < 0) break;

                    System.out.print("Enter to account: ");
                    int toAccount = Integer.parseInt(scanner.nextLine().trim());

                    System.out.print("Enter amount to transfer: ");
                    int amount = Integer.parseInt(scanner.nextLine().trim());

                    client.transfer(fromAccount, toAccount, amount);
                    System.out.println("Transfer successful.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}