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

    public String sendMessage(String userMessage, Integer conversationId) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("message", userMessage);
        if (conversationId != null) {
            body.addProperty("conversationId", conversationId);
        }

        String response = apiClient.postAuth(AppConfig.ENDPOINT_CHAT, body);
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        return json.has("reply") ? json.get("reply").getAsString() : "";
    }

    public JsonObject sendMessageFull(String userMessage, Integer conversationId) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("message", userMessage);
        if (conversationId != null) {
            body.addProperty("conversationId", conversationId);
        }

        String response = apiClient.postAuth(AppConfig.ENDPOINT_CHAT, body);
        return JsonParser.parseString(response).getAsJsonObject();
    }

    public List<Conversation> getConversations() throws IOException {
        String response = apiClient.getAuth(AppConfig.ENDPOINT_HISTORY);
        JsonArray array = JsonParser.parseString(response).getAsJsonArray();

        List<Conversation> conversations = new ArrayList<>();
        for (JsonElement el : array) {
            JsonObject obj = el.getAsJsonObject();
            Conversation c = new Conversation(
                    obj.get("id").getAsInt(),
                    obj.has("title") ? obj.get("title").getAsString() : "Sin título",
                    obj.has("created_at") ? obj.get("created_at").getAsString() : ""
            );
            conversations.add(c);
        }
        return conversations;
    }

    public List<Message> getMessages(int conversationId) throws IOException {
        String response = apiClient.getAuth(AppConfig.ENDPOINT_HISTORY + "/" + conversationId + "/messages");
        JsonArray array = JsonParser.parseString(response).getAsJsonArray();

        List<Message> messages = new ArrayList<>();
        for (JsonElement el : array) {
            JsonObject obj = el.getAsJsonObject();
            Message m = new Message(
                    obj.get("role").getAsString(),
                    obj.get("content").getAsString()
            );
            m.setId(obj.get("id").getAsInt());
            messages.add(m);
        }
        return messages;
    }
}