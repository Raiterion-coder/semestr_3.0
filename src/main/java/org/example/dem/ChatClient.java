package org.example.dem;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.start();
    }

    public void start() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Send the client name to the server
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your name: ");
            String clientName = scanner.nextLine();
            out.println(clientName);

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }).start();

            while (true) {
                String message = scanner.nextLine();
                out.println(message);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
