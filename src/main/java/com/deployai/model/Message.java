package com.deployai.model;

public class Message {
    private int id;
    private String role;
    private String content;
    private String createdAt;
    private int conversationId;

    public Message() {}

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getConversationId() { return conversationId; }
    public void setConversationId(int conversationId) { this.conversationId = conversationId; }

    public boolean isUser() { return "user".equals(role); }
    public boolean isAssistant() { return "assistant".equals(role); }
}