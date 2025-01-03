
package SOMSClientJava;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the socket connection to the SOMS server.
 */
public class Client implements Closeable {
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Constructs a new Client and connects to the server.
     *
     * @throws UnknownHostException If the IP address of the host could not be determined.
     * @throws IOException          If an I/O error occurs when creating the socket.
     */
    public Client() throws UnknownHostException, IOException {
        // Replace "localhost" and "12345" with your server's IP and port if different
        String serverAddress = "localhost"; // e.g., "192.168.1.100"
        int serverPort = 12345;

        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logger.info("Connected to SOMS Server at " + serverAddress + ":" + serverPort);
        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Unknown host: " + serverAddress, e);
            throw e;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error while connecting to server.", e);
            throw e;
        }
    }

    /**
     * Sends a command to the server.
     *
     * @param command The command string to send.
     */
    public void sendCommand(String command) {
        out.println(command);
        logger.info("Sent command to server: " + command);
    }

    /**
     * Reads a single line response from the server.
     *
     * @return The response line from the server.
     * @throws IOException If an I/O error occurs.
     */
    public String readResponse() throws IOException {
        String response = in.readLine();
        if (response != null) {
            logger.info("Received response from server: " + response);
        } else {
            logger.warning("Received null response from server.");
        }
        return response;
    }

    /**
     * Closes the client socket and associated streams.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        try {
            if (out != null) {
                out.close();
                logger.info("Output stream closed.");
            }
            if (in != null) {
                in.close();
                logger.info("Input stream closed.");
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                logger.info("Socket closed.");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while closing client resources.", e);
            throw e;
        }
    }
}