package org.example.dem;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginRegisterController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;

    private ObjectMapper objectMapper = new ObjectMapper();
    private File userFile = new File("users.json");
    private Stage primaryStage;

    @FXML
    public void initialize() {
        if (!userFile.exists()) {
            try {
                userFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            Map<String, String> users = readUsersFromFile();
            if (users.containsKey(username) && users.get(username).equals(password)) {
                System.out.println("Login successful!");
                openChatWindow(username);
                closeLoginWindow();
            } else {
                System.out.println("Invalid username or password!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void register() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dem/register.fxml"));
            Parent root = loader.load();
            RegisterController controller = loader.getController();
            controller.setPrimaryStage((Stage) registerButton.getScene().getWindow());

            // Закрыть окно логина
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();

            // Передать метод возврата в RegisterController
            controller.setOnBack(() -> {
                // Когда нажата кнопка back, откроем окно логина обратно
                loginStage.show();
            });

            Stage registerStage = new Stage();
            registerStage.initModality(Modality.APPLICATION_MODAL);
            registerStage.setTitle("Register");
            registerStage.setScene(new Scene(root, 300, 200));
            registerStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Map<String, String> readUsersFromFile() throws IOException {
        if (userFile.length() == 0) {
            return new HashMap<>();
        }
        return objectMapper.readValue(userFile, HashMap.class);
    }

    private void openChatWindow(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dem/chat_client.fxml"));
            Parent root = loader.load();
            ChatClientController controller = loader.getController();
            controller.setUsername(username);
            controller.connectToServer(); // Connect to the server after setting the username

            Stage chatStage = new Stage();
            chatStage.setTitle("Chat");
            chatStage.setScene(new Scene(root, 400, 300));
            chatStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeLoginWindow() {
        Stage loginStage = (Stage) loginButton.getScene().getWindow();
        loginStage.close();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
