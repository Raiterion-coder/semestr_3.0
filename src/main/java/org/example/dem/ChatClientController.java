package org.example.dem;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;

public class ChatClientController {
    @FXML
    private TextArea chatArea;  // область для отображения сообщений
    @FXML
    private TextField messageField;  // поле для ввода сообщения
    @FXML
    private Button sendButton;  // кнопка отправки сообщения
    @FXML
    private Button logoutButton;  // кнопка выхода
    @FXML
    private Label userCountLabel;  // метка для отображения количества пользователей онлайн

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    @FXML
    public void initialize() {
        // инициализация - можно добавить дополнительные настройки, если необходимо
    }

    // Метод для подключения к серверу
    public void connectToServer() {
        try {
            // Создаем соединение с сервером (localhost, порт 12345)
            socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Отправляем имя пользователя на сервер
            out.println(username);

            // Запускаем поток для получения сообщений от сервера
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

    // Метод для отправки сообщения
    @FXML
    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            out.println(message);
            messageField.clear();  // очищаем поле ввода
        }
    }

    // Устанавливаем имя пользователя
    public void setUsername(String username) {
        this.username = username;
    }

    // Обновляем количество пользователей, которые в сети
    private void updateUserCount(String userCount) {
        userCountLabel.setText("Users Online: " + userCount);
    }

    // Метод для выхода из чата с подтверждением
    @FXML
    private void logout() {
        // Создаем окно подтверждения выхода
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm Exit");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("You will be disconnected from the chat.");

        // Показать окно подтверждения
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Закрыть соединение и выйти из чата
                closeConnection();
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.close();  // Закрыть окно чата
            }
        });
    }

    // Метод для закрытия соединения с сервером
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
}
