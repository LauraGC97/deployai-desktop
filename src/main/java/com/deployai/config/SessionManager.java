package com.deployai.config;

import com.deployai.model.User;

public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null && currentUser.getToken() != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getToken() {
        return isLoggedIn() ? currentUser.getToken() : null;
    }
}