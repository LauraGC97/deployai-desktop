package com.deployai.controller;

import com.deployai.config.SessionManager;
import com.deployai.model.Conversation;
import com.deployai.model.Message;
import com.deployai.service.ChatService;
import com.deployai.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    @FXML private ListView<Conversation> conversationList;
    @FXML private VBox messagesContainer;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;
    @FXML private ScrollPane scrollPane;
    @FXML private Label usernameLabel;
    @FXML private Label chatTitleLabel;

    private final ChatService chatService = new ChatService();
    private Integer currentConversationId = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Mostrar nombre de usuario
        if (SessionManager.getInstance().getCurrentUser() != null) {
            String username = SessionManager.getInstance().getCurrentUser().getUsername();
            if (username != null) usernameLabel.setText(username);
        }

        // Enviar con Ctrl+Enter
        messageInput.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    if (event.isControlDown()) sendMessage();
                    break;
                default:
                    break;
            }
        });

        // Click en conversación del sidebar
        conversationList.setOnMouseClicked(event -> {
            Conversation selected = conversationList.getSelectionModel().getSelectedItem();
            if (selected != null) loadConversation(selected);
        });

        // Cargar conversaciones del sidebar
        loadConversations();
    }

    @FXML
    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty()) return;

        messageInput.clear();
        sendButton.setDisable(true);

        // Mostrar mensaje del usuario
        addMessageBubble(text, true);

        // Indicador de escritura
        Label typing = new Label("Deploy AI está escribiendo...");
        typing.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px; -fx-padding: 0 0 0 16;");
        messagesContainer.getChildren().add(typing);
        scrollToBottom();

        new Thread(() -> {
            try {
                String reply = chatService.sendMessage(text, currentConversationId);

                // Obtener conversationId si es nueva conversación
                com.google.gson.JsonObject fullResponse = chatService.sendMessageFull(text, currentConversationId);
                if (fullResponse.has("conversationId")) {
                    currentConversationId = fullResponse.get("conversationId").getAsInt();
                }

                Platform.runLater(() -> {
                    messagesContainer.getChildren().remove(typing);
                    addMessageBubble(reply, false);
                    sendButton.setDisable(false);
                    loadConversations();
                    scrollToBottom();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    messagesContainer.getChildren().remove(typing);
                    addMessageBubble("Error al conectar con el servidor. Inténtalo de nuevo.", false);
                    sendButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void newConversation() {
        currentConversationId = null;
        messagesContainer.getChildren().clear();
        chatTitleLabel.setText("Nueva conversación");
        conversationList.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            SceneManager.getInstance().showLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadConversations() {
        new Thread(() -> {
            try {
                List<Conversation> conversations = chatService.getConversations();
                Platform.runLater(() -> {
                    conversationList.getItems().setAll(conversations);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadConversation(Conversation conversation) {
        currentConversationId = conversation.getId();
        chatTitleLabel.setText(conversation.getTitle());
        messagesContainer.getChildren().clear();

        new Thread(() -> {
            try {
                List<Message> messages = chatService.getMessages(conversation.getId());
                Platform.runLater(() -> {
                    for (Message m : messages) {
                        addMessageBubble(m.getContent(), m.isUser());
                    }
                    scrollToBottom();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void addMessageBubble(String content, boolean isUser) {
        HBox wrapper = new HBox();
        wrapper.setPadding(new Insets(4, 16, 4, 16));

        VBox bubble = new VBox(6);
        bubble.setMaxWidth(600);
        bubble.setPadding(new Insets(12, 16, 12, 16));

        Text text = new Text(content);
        text.setWrappingWidth(560);
        TextFlow flow = new TextFlow(text);

        if (isUser) {
            wrapper.setAlignment(Pos.CENTER_RIGHT);
            bubble.setStyle("-fx-background-color: #7c3aed; -fx-background-radius: 12 12 0 12;");
            text.setStyle("-fx-fill: white; -fx-font-size: 13px;");
        } else {
            wrapper.setAlignment(Pos.CENTER_LEFT);
            bubble.setStyle("-fx-background-color: #161b22; -fx-background-radius: 12 12 12 0; -fx-border-color: #30363d; -fx-border-radius: 12 12 12 0;");
            text.setStyle("-fx-fill: #e6edf3; -fx-font-size: 13px;");
        }

        bubble.getChildren().add(flow);
        wrapper.getChildren().add(bubble);
        messagesContainer.getChildren().add(wrapper);
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }
}