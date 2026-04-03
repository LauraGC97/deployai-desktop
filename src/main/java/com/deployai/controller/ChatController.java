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
    @FXML private Label avatarLabel;
    @FXML private Label chatTitleLabel;

    private final ChatService chatService = new ChatService();
    private Integer currentConversationId = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Usuario
        if (SessionManager.getInstance().getCurrentUser() != null) {
            String username = SessionManager.getInstance().getCurrentUser().getUsername();
            if (username != null) {
                usernameLabel.setText(username);
                avatarLabel.setText(username.substring(0, 1).toUpperCase());
            }
        }

        // Ctrl+Enter para enviar
        messageInput.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    if (event.isControlDown()) sendMessage();
                    break;
                default:
                    break;
            }
        });

        // Click en conversación
        conversationList.setOnMouseClicked(event -> {
            Conversation selected = conversationList.getSelectionModel().getSelectedItem();
            if (selected != null) loadConversation(selected);
        });

        // Estilo de celdas del sidebar
        conversationList.setCellFactory(lv -> new ListCell<Conversation>() {
            @Override
            protected void updateItem(Conversation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item.getTitle());
                    setStyle(
                        "-fx-background-color: transparent;" +
                        "-fx-text-fill: #6B7280;" +
                        "-fx-font-family: 'JetBrains Mono';" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 9 12 9 12;" +
                        "-fx-background-radius: 8;"
                    );
                }
            }
        });

        // Estado vacío
        showEmptyState();
        loadConversations();
    }

    @FXML
    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty()) return;

        messageInput.clear();
        sendButton.setDisable(true);

        addMessageBubble(text, true);

        // Indicador typing
        HBox typingWrapper = new HBox();
        typingWrapper.setAlignment(Pos.CENTER_LEFT);
        typingWrapper.setPadding(new Insets(4, 24, 4, 24));
        Label typing = new Label("Deploy AI está escribiendo...");
        typing.setStyle(
            "-fx-font-family: 'JetBrains Mono';" +
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #8B9BB4;" +
            "-fx-background-color: #10121C;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 10 16 10 16;" +
            "-fx-border-color: rgba(255,255,255,0.06);" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;"
        );
        typingWrapper.getChildren().add(typing);
        messagesContainer.getChildren().add(typingWrapper);
        scrollToBottom();

        new Thread(() -> {
            try {
                com.google.gson.JsonObject fullResponse = chatService.sendMessageFull(text, currentConversationId);
                String reply = fullResponse.has("reply") ? fullResponse.get("reply").getAsString() : "";
                if (fullResponse.has("conversationId")) {
                    currentConversationId = fullResponse.get("conversationId").getAsInt();
                }

                Platform.runLater(() -> {
                    messagesContainer.getChildren().remove(typingWrapper);
                    addMessageBubble(reply, false);
                    sendButton.setDisable(false);
                    loadConversations();
                    scrollToBottom();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    messagesContainer.getChildren().remove(typingWrapper);
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
        chatTitleLabel.setText(">_ chat");
        conversationList.getSelectionModel().clearSelection();
        showEmptyState();
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
                Platform.runLater(() -> conversationList.getItems().setAll(conversations));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadConversation(Conversation conversation) {
        currentConversationId = conversation.getId();
        chatTitleLabel.setText(">_ " + conversation.getTitle());
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

    private void showEmptyState() {
        VBox empty = new VBox(12);
        empty.setAlignment(Pos.CENTER);
        empty.setStyle("-fx-padding: 60 24 60 24;");

        Label icon = new Label("⬡");
        icon.setStyle("-fx-font-size: 48px; -fx-text-fill: #1a2030;");

        Label title = new Label("¿En qué puedo ayudarte?");
        title.setStyle(
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #F0F9FF;"
        );

        Label sub = new Label("Escribe tu pregunta de programación\ny Deploy AI te responderá al instante.");
        sub.setStyle("-fx-font-size: 14px; -fx-text-fill: #8B9BB4; -fx-text-alignment: center;");
        sub.setWrapText(true);

        empty.getChildren().addAll(icon, title, sub);
        messagesContainer.getChildren().add(empty);
    }

    private void addMessageBubble(String content, boolean isUser) {
        HBox wrapper = new HBox();
        wrapper.setPadding(new Insets(0, 0, 0, 0));

        // Avatar
        VBox avatar = new VBox();
        avatar.setAlignment(Pos.CENTER);
        avatar.setMinWidth(34);
        avatar.setMaxWidth(34);
        avatar.setMinHeight(34);
        avatar.setMaxHeight(34);

        Label avatarText = new Label(isUser ? getUserInitial() : "AI");

        if (isUser) {
            avatar.setStyle(
                "-fx-background-color: rgba(34,211,238,0.12);" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: rgba(34,211,238,0.2);" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;"
            );
            avatarText.setStyle(
                "-fx-font-family: 'JetBrains Mono';" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #22D3EE;"
            );
        } else {
            avatar.setStyle(
                "-fx-background-color: rgba(168,85,247,0.12);" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: rgba(168,85,247,0.2);" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;"
            );
            avatarText.setStyle(
                "-fx-font-family: 'JetBrains Mono';" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #A855F7;"
            );
        }
        avatar.getChildren().add(avatarText);

        // Burbuja
        VBox bubble = new VBox();
        bubble.setMaxWidth(700);
        bubble.setPadding(new Insets(14, 18, 14, 18));

        Text text = new Text(content);
        text.setWrappingWidth(640);
        TextFlow flow = new TextFlow(text);

        if (isUser) {
            bubble.setStyle(
                "-fx-background-color: rgba(34,211,238,0.07);" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: rgba(34,211,238,0.15);" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;"
            );
            text.setStyle(
                "-fx-fill: #E0F9FF;" +
                "-fx-font-family: 'JetBrains Mono';" +
                "-fx-font-size: 13px;"
            );
            wrapper.setAlignment(Pos.CENTER_RIGHT);
            bubble.getChildren().add(flow);
            wrapper.setSpacing(14);
            wrapper.getChildren().addAll(bubble, avatar);
        } else {
            bubble.setStyle(
                "-fx-background-color: #10121C;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: rgba(255,255,255,0.06);" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;"
            );
            text.setStyle(
                "-fx-fill: #F0F9FF;" +
                "-fx-font-family: 'JetBrains Mono';" +
                "-fx-font-size: 13px;"
            );
            wrapper.setAlignment(Pos.CENTER_LEFT);
            bubble.getChildren().add(flow);
            wrapper.setSpacing(14);
            wrapper.getChildren().addAll(avatar, bubble);
        }

        messagesContainer.getChildren().add(wrapper);
    }

    private String getUserInitial() {
        if (SessionManager.getInstance().getCurrentUser() != null) {
            String username = SessionManager.getInstance().getCurrentUser().getUsername();
            if (username != null && !username.isEmpty()) {
                return username.substring(0, 1).toUpperCase();
            }
        }
        return "U";
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }
}