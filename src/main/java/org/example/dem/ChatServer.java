package org.example.dem;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);
    private static int userCount = 0;

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
                String message = in.readLine();
                JSONObject jsonMessage = new JSONObject(message);
                clientName = jsonMessage.getString("username");
                userCount++;
                logger.info("{} has joined the chat.", clientName);
                broadcastMessage(new JSONObject().put("type", "message").put("content", clientName + " has joined the chat.").toString());
                broadcastUserCount();
                broadcastUserList();

                while ((message = in.readLine()) != null) {
                    jsonMessage = new JSONObject(message);
                    String type = jsonMessage.getString("type");
                    if (type.equals("message")) {
                        String content = jsonMessage.getString("content");
                        String recipient = jsonMessage.optString("recipient", null);
                        if (recipient != null && !recipient.isEmpty()) {
                            sendPrivateMessage(recipient, new JSONObject().put("type", "message").put("content", clientName + " (private): " + content).toString());
                        } else {
                            broadcastMessage(new JSONObject().put("type", "message").put("content", clientName + ": " + content).toString());
                        }
                    }
                }
            } catch (IOException ex) {
                logger.error("Client error: {}", ex.getMessage());
            } finally {
                closeConnections();
                clients.remove(this);
                userCount--;
                logger.info("{} has left the chat.", clientName);
                broadcastMessage(new JSONObject().put("type", "message").put("content", clientName + " has left the chat.").toString());
                broadcastUserCount();
                broadcastUserList();
            }
        }

        private void broadcastMessage(String message) {
            for (ClientHandler client : clients) {
                client.out.println(message);
            }
        }

        private void sendPrivateMessage(String recipient, String message) {
            for (ClientHandler client : clients) {
                if (client.clientName.equals(recipient)) {
                    client.out.println(message);
                    break;
                }
            }
        }

        private void broadcastUserCount() {
            for (ClientHandler client : clients) {
                client.out.println(new JSONObject().put("type", "user_count").put("count", userCount).toString());
            }
        }

        private void broadcastUserList() {
            StringBuilder userList = new StringBuilder();
            for (ClientHandler client : clients) {
                if (userList.length() > 0) {
                    userList.append(",");
                }
                userList.append(client.clientName);
            }
            for (ClientHandler client : clients) {
                client.out.println(new JSONObject().put("type", "user_list").put("users", userList.toString()).toString());
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
