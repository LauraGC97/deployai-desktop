package com.deployai.service;

import com.deployai.config.AppConfig;
import com.deployai.config.SessionManager;
import com.deployai.model.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

public class AuthService {

    private final ApiClient apiClient = ApiClient.getInstance();

    public User login(String email, String password) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        String response = apiClient.post(AppConfig.ENDPOINT_LOGIN, body);
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        User user = new User();
        user.setToken(json.get("token").getAsString());

        if (json.has("user")) {
            JsonObject userData = json.getAsJsonObject("user");
            user.setId(userData.get("id").getAsInt());
            user.setEmail(userData.get("email").getAsString());
            if (userData.has("username")) {
                user.setUsername(userData.get("username").getAsString());
            }
        }

        SessionManager.getInstance().login(user);
        return user;
    }

    public User register(String email, String password, String username) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);
        body.addProperty("username", username);

        String response = apiClient.post(AppConfig.ENDPOINT_REGISTER, body);
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        User user = new User();
        user.setToken(json.get("token").getAsString());

        if (json.has("user")) {
            JsonObject userData = json.getAsJsonObject("user");
            user.setId(userData.get("id").getAsInt());
            user.setEmail(userData.get("email").getAsString());
            if (userData.has("username")) {
                user.setUsername(userData.get("username").getAsString());
            }
        }

        SessionManager.getInstance().login(user);
        return user;
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }
}