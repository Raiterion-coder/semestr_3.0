package org.example.dem;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatClientController {
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;
    @FXML
    private Label userCountLabel;
    @FXML
    private Button logoutButton;
    @FXML
    private ComboBox<String> userComboBox;
    @FXML
    private Button backToGeneralButton;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private List<String> onlineUsers = new ArrayList<>();
    private String selectedRecipient = null;

    @FXML
    public void initialize() {
        logoutButton.setOnAction(event -> logout());
        userComboBox.setOnAction(event -> updateRecipient());
        backToGeneralButton.setOnAction(event -> backToGeneralChat());
    }

    public void connectToServer() {
        try {
            socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(new JSONObject().put("type", "login").put("username", username).toString());

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        JSONObject jsonMessage = new JSONObject(message);
                        String type = jsonMessage.getString("type");
                        if (type.equals("user_count")) {
                            String finalMessage1 = message;
                            Platform.runLater(() -> updateUserCount(jsonMessage.getInt("count")));
                        } else if (type.equals("user_list")) {
                            String finalMessage2 = message;
                            Platform.runLater(() -> updateUserList(jsonMessage.getString("users")));
                        } else if (type.equals("message")) {
                            String finalMessage = message;
                            Platform.runLater(() -> chatArea.appendText(jsonMessage.getString("content") + "\n"));
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
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("type", "message");
            jsonMessage.put("content", message);
            if (selectedRecipient != null && !selectedRecipient.isEmpty()) {
                jsonMessage.put("recipient", selectedRecipient);
            }
            out.println(jsonMessage.toString());
            messageField.clear();
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private void updateUserCount(int userCount) {
        userCountLabel.setText("Users Online: " + userCount);
    }

    private void updateUserList(String userList) {
        onlineUsers.clear();
        onlineUsers.addAll(List.of(userList.split(",")));
        List<String> filteredUsers = onlineUsers.stream()
                .filter(user -> !user.equals(username))
                .collect(Collectors.toList());
        userComboBox.getItems().setAll(filteredUsers);
    }

    private void updateRecipient() {
        selectedRecipient = userComboBox.getValue();
        if (selectedRecipient != null && !selectedRecipient.isEmpty()) {
            messageField.setPromptText("Type your message to " + selectedRecipient + "...");
        } else {
            messageField.setPromptText("Type your message here...");
            selectedRecipient = null;
        }
    }

    @FXML
    private void backToGeneralChat() {
        selectedRecipient = null;
        userComboBox.setValue(null);
        messageField.setPromptText("Type your message here...");
    }

    @FXML
    private void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Exit");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("You will be disconnected from the chat.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                closeConnection();
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.close();
            }
        });
    }

    private void closeConnection() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void login(String username, String password) {
        boolean isLoggedIn = UserManager.loginUser(username, password);
        if (isLoggedIn) {
            System.out.println("Login successful!");
            setUsername(username);
            connectToServer();
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    public void register(String username, String password) {
        boolean isRegistered = UserManager.registerUser(username, password);
        if (isRegistered) {
            System.out.println("Registration successful!");
            setUsername(username);
            connectToServer();
        } else {
            System.out.println("Username already exists!");
        }
    }
}
