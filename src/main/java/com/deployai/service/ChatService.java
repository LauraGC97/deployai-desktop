package com.deployai.service;

import com.deployai.config.AppConfig;
import com.deployai.model.Conversation;
import com.deployai.model.Message;
import com.google.gson.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatService {

    private final ApiClient apiClient = ApiClient.getInstance();

    public String sendMessage(List<Message> messages) throws IOException {
        JsonArray messagesArray = new JsonArray();
        for (Message m : messages) {
            JsonObject msg = new JsonObject();
            msg.addProperty("role", m.getRole());
            msg.addProperty("content", m.getContent());
            messagesArray.add(msg);
        }

        JsonObject body = new JsonObject();
        body.add("messages", messagesArray);

        String response = apiClient.postAuth(AppConfig.ENDPOINT_CHAT, body);
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        return json.has("content") ? json.get("content").getAsString() : "";
    }

    private final List<Conversation> localConversations = new ArrayList<>();
    private int nextId = 1;

    public List<Conversation> getConversations() {
        return new ArrayList<>(localConversations);
    }

    public Conversation createConversation(String firstMessage) {
        String title = firstMessage.length() > 40
                ? firstMessage.substring(0, 40) + "..."
                : firstMessage;
        Conversation c = new Conversation(nextId++, title, "");
        c.setMessages(new ArrayList<>());
        localConversations.add(0, c);
        return c;
    }

    public void updateConversation(int id, List<Message> messages) {
        for (Conversation c : localConversations) {
            if (c.getId() == id) {
                c.setMessages(new ArrayList<>(messages));
                break;
            }
        }
    }

    public List<Message> getMessages(int conversationId) {
        for (Conversation c : localConversations) {
            if (c.getId() == conversationId) {
                return c.getMessages() != null
                        ? new ArrayList<>(c.getMessages())
                        : new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }
}