package org.example.dem;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                ClientHandler client = new ClientHandler(socket);
                clients.add(client);
                new Thread(client).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
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
                broadcastMessage(clientName + " has joined the chat.");

                String message;
                while ((message = in.readLine()) != null) {
                    broadcastMessage(clientName + ": " + message);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                closeConnections();
                clients.remove(this);
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
                ex.printStackTrace();
            }
        }
    }
}
