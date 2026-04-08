package com.deployai.model;

import java.util.List;

public class Conversation {
    private int id;
    private String title;
    private String createdAt;
    private List<Message> messages;

    public Conversation() {}

    public Conversation(int id, String title, String createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    @Override
    public String toString() { return title != null ? title : "Conversación #" + id; }
}