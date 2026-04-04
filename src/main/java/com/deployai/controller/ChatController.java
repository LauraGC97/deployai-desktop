package com.deployai.controller;

import com.deployai.config.SessionManager;
import com.deployai.model.Conversation;
import com.deployai.model.Message;
import com.deployai.service.ChatService;
import com.deployai.util.MarkdownRenderer;
import com.deployai.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ArrayList;
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
    private final List<Message> currentMessages = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Nombre de usuario
        if (SessionManager.getInstance().getCurrentUser() != null) {
            String name = SessionManager.getInstance().getCurrentUser().getUsername();
            if (name != null) {
                usernameLabel.setText(name);
                avatarLabel.setText(name.substring(0, 1).toUpperCase());
            }
        }

        // Estilo del botón enviar
        sendButton.setText("➤");
        sendButton.setAlignment(Pos.CENTER);
        sendButton.setPadding(new Insets(0));
        sendButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #A855F7 0%, #22D3EE 100%);" +
            "-fx-text-fill: #080A10;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 10;" +
            "-fx-min-width: 44;" +
            "-fx-min-height: 44;" +
            "-fx-max-width: 44;" +
            "-fx-max-height: 44;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;" +
            "-fx-padding: 0;"
        );

        // Enter envía, Shift+Enter hace salto de línea
        messageInput.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    if (!event.isShiftDown()) {
                        event.consume();
                        sendMessage();
                    }
                    break;
                default:
                    break;
            }
        });

        // Auto-expandir el TextArea
        messageInput.textProperty().addListener((obs, oldVal, newVal) -> {
            int lines = newVal.split("\n", -1).length;
            int rows = Math.min(Math.max(lines, 1), 6);
            messageInput.setPrefRowCount(rows);
        });

        // Click en conversación
        conversationList.setOnMouseClicked(event -> {
            Conversation selected = conversationList.getSelectionModel().getSelectedItem();
            if (selected != null) loadConversation(selected);
        });

        // Estilo celdas sidebar
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

        showEmptyState();
        refreshConversationList();
    }

    @FXML
    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty()) return;

        messageInput.clear();
        sendButton.setDisable(true);

        Message userMsg = new Message("user", text);
        currentMessages.add(userMsg);

        if (currentConversationId == null) {
            Conversation conv = chatService.createConversation(text);
            currentConversationId = conv.getId();
            chatTitleLabel.setText(">_ " + conv.getTitle());
            refreshConversationList();
        }

        addMessageBubble(text, true);

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

        List<Message> messagesCopy = new ArrayList<>(currentMessages);
        int convId = currentConversationId;

        new Thread(() -> {
            try {
                String reply = chatService.sendMessage(messagesCopy);
                Message aiMsg = new Message("assistant", reply);
                currentMessages.add(aiMsg);
                chatService.updateConversation(convId, currentMessages);

                Platform.runLater(() -> {
                    messagesContainer.getChildren().remove(typingWrapper);
                    addMessageBubble(reply, false);
                    sendButton.setDisable(false);
                    scrollToBottom();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    messagesContainer.getChildren().remove(typingWrapper);
                    addMessageBubble("Error al conectar con el servidor. Inténtalo de nuevo.", false);
                    sendButton.setDisable(false);
                    currentMessages.remove(currentMessages.size() - 1);
                });
            }
        }).start();
    }

    @FXML
    private void newConversation() {
        currentConversationId = null;
        currentMessages.clear();
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

    private void refreshConversationList() {
        List<Conversation> conversations = chatService.getConversations();
        conversationList.getItems().setAll(conversations);
    }

    private void loadConversation(Conversation conversation) {
        currentConversationId = conversation.getId();
        chatTitleLabel.setText(">_ " + conversation.getTitle());
        currentMessages.clear();
        messagesContainer.getChildren().clear();

        List<Message> messages = chatService.getMessages(conversation.getId());
        currentMessages.addAll(messages);
        for (Message m : messages) {
            addMessageBubble(m.getContent(), m.isUser());
        }
        scrollToBottom();
    }

    private void showEmptyState() {
        VBox empty = new VBox(12);
        empty.setAlignment(Pos.CENTER);
        empty.setStyle("-fx-padding: 60 24 60 24;");

        Label icon = new Label("⬡");
        icon.setStyle("-fx-font-size: 48px; -fx-text-fill: #1a2030;");

        Label title = new Label("¿En qué puedo ayudarte?");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #F0F9FF;");

        Label sub = new Label("Escribe tu pregunta de programación\ny Deploy AI te responderá al instante.");
        sub.setStyle("-fx-font-size: 14px; -fx-text-fill: #8B9BB4;");
        sub.setWrapText(true);

        empty.getChildren().addAll(icon, title, sub);
        messagesContainer.getChildren().add(empty);
    }

    private void addMessageBubble(String content, boolean isUser) {
        HBox wrapper = new HBox();
        wrapper.setPadding(new Insets(0, 0, 0, 0));

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

        if (isUser) {
            VBox bubble = new VBox();
            bubble.setMaxWidth(700);
            bubble.setPadding(new Insets(14, 18, 14, 18));
            bubble.setStyle(
                "-fx-background-color: rgba(34,211,238,0.07);" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: rgba(34,211,238,0.15);" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;"
            );
            Text text = new Text(content);
            text.setWrappingWidth(620);
            text.setStyle("-fx-fill: #E0F9FF; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 13px;");
            bubble.getChildren().add(new TextFlow(text));
            wrapper.setAlignment(Pos.CENTER_RIGHT);
            wrapper.setSpacing(14);
            wrapper.getChildren().addAll(bubble, avatar);
        } else {
            VBox bubble = new VBox();
            bubble.setStyle(
                "-fx-background-color: #10121C;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: rgba(255,255,255,0.06);" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 4;"
            );

            WebView webView = new WebView();
            webView.setPrefWidth(680);
            webView.setMaxWidth(680);
            webView.setPrefHeight(50);
            webView.setPageFill(javafx.scene.paint.Color.TRANSPARENT);

            WebEngine engine = webView.getEngine();
            engine.loadContent(MarkdownRenderer.toHtml(content));

            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    Object heightObj = engine.executeScript("document.body.scrollHeight");
                    if (heightObj instanceof Integer) {
                        webView.setPrefHeight(((Integer) heightObj).doubleValue() + 28);
                    }
                }
            });

            bubble.getChildren().add(webView);
            wrapper.setAlignment(Pos.CENTER_LEFT);
            wrapper.setSpacing(14);
            wrapper.getChildren().addAll(avatar, bubble);
        }

        messagesContainer.getChildren().add(wrapper);
    }

    private String getUserInitial() {
        if (SessionManager.getInstance().getCurrentUser() != null) {
            String name = SessionManager.getInstance().getCurrentUser().getUsername();
            if (name != null && !name.isEmpty()) {
                return name.substring(0, 1).toUpperCase();
            }
        }
        return "U";
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }
}