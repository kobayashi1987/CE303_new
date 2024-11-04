package SOMSClientJava;

import java.io.*;
import java.net.Socket;

/**
 * Client manages the client-side socket connection, sending commands to the server,
 * and receiving responses.
 */
public class Client implements AutoCloseable {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;
    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;

    public Client() throws IOException {
        this.socket = new Socket(HOST, PORT);
        this.writer = new PrintWriter(socket.getOutputStream(), true); // autoFlush=true
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /**
     * Sends a command to the server.
     *
     * @param command The command to send.
     */
    public void sendCommand(String command) {
        writer.println(command);
    }

    /**
     * Reads a single-line response from the server.
     *
     * @return The server's response.
     * @throws IOException If an I/O error occurs.
     */
    public String readResponse() throws IOException {
        return reader.readLine();
    }

    @Override
    public void close() {
        try {
            if (writer != null)
                writer.close();
            if (reader != null)
                reader.close();
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            System.err.println("Error closing client resources: " + e.getMessage());
        }
    }
}