package com.deployai.service;

import com.deployai.config.AppConfig;
import com.deployai.config.SessionManager;
import com.deployai.model.Conversation;
import com.deployai.model.Message;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ChatService {

    private final ApiClient apiClient = ApiClient.getInstance();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final List<Conversation> localConversations = new ArrayList<>();
    private int nextId = 1;

    public ChatService() {
        loadFromDisk();
    }

    // ── ENVÍO DE MENSAJES ────────────────────────────────────────────────────

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

    // ── GESTIÓN DE CONVERSACIONES ────────────────────────────────────────────

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
        saveToDisk();
        return c;
    }

    public void updateConversation(int id, List<Message> messages) {
        for (Conversation c : localConversations) {
            if (c.getId() == id) {
                c.setMessages(new ArrayList<>(messages));
                break;
            }
        }
        saveToDisk();
    }

    public void deleteConversation(int id) {
        localConversations.removeIf(c -> c.getId() == id);
        saveToDisk();
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

    // ── PERSISTENCIA EN DISCO ─────────────────────────────────────────────────

    private Path getStorageFile() {
        String userId = "guest";
        if (SessionManager.getInstance().getCurrentUser() != null) {
            userId = String.valueOf(SessionManager.getInstance().getCurrentUser().getId());
        }
        String home = System.getProperty("user.home");
        Path dir = Paths.get(home, ".deployai");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dir.resolve("conversations_" + userId + ".json");
    }

    private void saveToDisk() {
        try {
            Path file = getStorageFile();
            // Serializar conversaciones con sus mensajes
            JsonArray array = new JsonArray();
            for (Conversation c : localConversations) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", c.getId());
                obj.addProperty("title", c.getTitle());
                obj.addProperty("createdAt", c.getCreatedAt());

                JsonArray msgs = new JsonArray();
                if (c.getMessages() != null) {
                    for (Message m : c.getMessages()) {
                        JsonObject msgObj = new JsonObject();
                        msgObj.addProperty("role", m.getRole());
                        msgObj.addProperty("content", m.getContent());
                        msgs.add(msgObj);
                    }
                }
                obj.add("messages", msgs);
                array.add(obj);
            }

            JsonObject root = new JsonObject();
            root.add("conversations", array);
            root.addProperty("nextId", nextId);

            Files.writeString(file, gson.toJson(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromDisk() {
        try {
            Path file = getStorageFile();
            if (!Files.exists(file)) return;

            String content = Files.readString(file);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();

            if (root.has("nextId")) {
                nextId = root.get("nextId").getAsInt();
            }

            if (root.has("conversations")) {
                JsonArray array = root.getAsJsonArray("conversations");
                for (JsonElement el : array) {
                    JsonObject obj = el.getAsJsonObject();
                    Conversation c = new Conversation(
                        obj.get("id").getAsInt(),
                        obj.get("title").getAsString(),
                        obj.has("createdAt") ? obj.get("createdAt").getAsString() : ""
                    );

                    List<Message> messages = new ArrayList<>();
                    if (obj.has("messages")) {
                        for (JsonElement msgEl : obj.getAsJsonArray("messages")) {
                            JsonObject msgObj = msgEl.getAsJsonObject();
                            messages.add(new Message(
                                msgObj.get("role").getAsString(),
                                msgObj.get("content").getAsString()
                            ));
                        }
                    }
                    c.setMessages(messages);
                    localConversations.add(c);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}