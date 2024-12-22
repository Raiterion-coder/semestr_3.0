package org.example.dem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class UserManager {
    private static final String FILE_NAME = "users.json";

    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes());
            byte[] hashedBytes = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static boolean registerUser(String username, String password) {
        JSONArray usersArray = readUsersFromFile();

        for (int i = 0; i < usersArray.length(); i++) {
            JSONObject userObject = usersArray.getJSONObject(i);
            if (userObject.getString("username").equals(username)) {
                return false;
            }
        }

        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);

        JSONObject newUser = new JSONObject();
        newUser.put("username", username);
        newUser.put("password", hashedPassword);
        newUser.put("salt", salt);

        usersArray.put(newUser);
        saveUsersToFile(usersArray);

        return true;
    }

    public static boolean loginUser(String username, String password) {
        JSONArray usersArray = readUsersFromFile();

        for (int i = 0; i < usersArray.length(); i++) {
            JSONObject userObject = usersArray.getJSONObject(i);
            if (userObject.getString("username").equals(username)) {
                String storedSalt = userObject.getString("salt");
                String storedHashedPassword = userObject.getString("password");
                String inputHashedPassword = hashPassword(password, storedSalt);

                if (storedHashedPassword.equals(inputHashedPassword)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static JSONArray readUsersFromFile() {
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) {
                return new JSONArray();
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            reader.close();
            return new JSONArray(jsonContent.toString());
        } catch (IOException e) {
            throw new RuntimeException("Error reading users from file", e);
        }
    }

    private static void saveUsersToFile(JSONArray usersArray) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME));
            writer.write(usersArray.toString(4));
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Error writing users to file", e);
        }
    }
}
