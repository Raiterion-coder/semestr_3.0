package org.example.dem;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.*;

public class ChatClientController {
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;
    @FXML
    private Label userCountLabel;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    @FXML
    public void initialize() {
        // Do not connect to the server here
    }

    public void connectToServer() {
        try {
            socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Send the client name to the server
            out.println(username);

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.startsWith("USER_COUNT:")) {
                            String finalMessage1 = message;
                            Platform.runLater(() -> updateUserCount(finalMessage1.substring(11)));
                        } else {
                            String finalMessage = message;
                            Platform.runLater(() -> chatArea.appendText(finalMessage + "\n"));
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }).start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            out.println(message);
            messageField.clear();
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private void updateUserCount(String userCount) {
        userCountLabel.setText("Users Online: " + userCount);
    }
}
