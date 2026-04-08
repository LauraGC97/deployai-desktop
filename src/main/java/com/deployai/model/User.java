package com.deployai.model;

public class User {
    private int id;
    private String email;
    private String username;
    private String token;

    public User() {}

    public User(int id, String email, String username, String token) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.token = token;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}