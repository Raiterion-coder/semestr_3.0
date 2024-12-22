package org.example.dem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Server is listening on port {}", PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("New client connected");
                ClientHandler client = new ClientHandler(socket);
                clients.add(client);
                new Thread(client).start();
            }
        } catch (IOException ex) {
            logger.error("Server error: {}", ex.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                clientName = in.readLine();
                logger.info("{} has joined the chat.", clientName);
                broadcastMessage(clientName + " has joined the chat.");

                String message;
                while ((message = in.readLine()) != null) {
                    logger.info("Received message from {}: {}", clientName, message);
                    broadcastMessage(clientName + ": " + message);
                }
            } catch (IOException ex) {
                logger.error("Client error: {}", ex.getMessage());
            } finally {
                closeConnections();
                clients.remove(this);
                logger.info("{} has left the chat.", clientName);
                broadcastMessage(clientName + " has left the chat.");
            }
        }

        private void broadcastMessage(String message) {
            for (ClientHandler client : clients) {
                client.out.println(message);
            }
        }

        private void closeConnections() {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                logger.error("Error closing connections: {}", ex.getMessage());
            }
        }
    }
}
