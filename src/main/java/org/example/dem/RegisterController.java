package org.example.dem;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegisterController {
    @FXML
    private TextField newUsernameField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private Button backButton;
    @FXML
    private Button registerButton;

    private ObjectMapper objectMapper = new ObjectMapper();
    private File userFile = new File("users.json");
    private Stage primaryStage;
    private Runnable onBack;

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
    private void back() {
        Stage registerStage = (Stage) backButton.getScene().getWindow();
        registerStage.close();

        if (onBack != null) {
            onBack.run();
        }
    }

    @FXML
    private void register() {
        String username = newUsernameField.getText();
        String password = newPasswordField.getText();

        try {
            Map<String, String> users = readUsersFromFile();
            if (users.containsKey(username)) {
                System.out.println("Username already exists!");
            } else {
                String salt = PasswordUtils.generateSalt();
                String hashedPassword = PasswordUtils.hashPassword(password, salt);

                users.put(username, hashedPassword + ":" + salt);
                objectMapper.writeValue(userFile, users);
                System.out.println("Registration successful!");

                Stage registerStage = (Stage) registerButton.getScene().getWindow();
                registerStage.close();

                if (onBack != null) {
                    onBack.run();
                }
            }
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

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }
}
