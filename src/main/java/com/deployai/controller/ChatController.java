package com.deployai.controller;

import com.deployai.config.SessionManager;
import com.deployai.model.Conversation;
import com.deployai.model.Message;
import com.deployai.service.ChatService;
import com.deployai.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatController implements Initializable {

    @FXML private ListView<Conversation> conversationList;
    @FXML private VBox messagesContainer;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;
    @FXML private ScrollPane scrollPane;
    @FXML private Label usernameLabel;
    @FXML private Label avatarLabel;
    @FXML private Label chatTitleLabel;
    @FXML private Label modeBadge;
    @FXML private Button modeGenerate;
    @FXML private Button modeAnalyze;
    @FXML private Button modeExplain;

    private final ChatService chatService = new ChatService();
    private Integer currentConversationId = null;
    private final List<Message> currentMessages = new ArrayList<>();
    private String currentMode = "generate";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (SessionManager.getInstance().getCurrentUser() != null) {
            String name = SessionManager.getInstance().getCurrentUser().getUsername();
            if (name != null) {
                usernameLabel.setText(name);
                avatarLabel.setText(name.substring(0, 1).toUpperCase());
            }
        }

        sendButton.setText("➤");
        sendButton.setAlignment(Pos.CENTER);
        sendButton.setPadding(new Insets(0));
        sendButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #A855F7 0%, #22D3EE 100%);" +
            "-fx-text-fill: #080A10; -fx-font-weight: bold; -fx-font-size: 18px;" +
            "-fx-background-radius: 10; -fx-min-width: 50; -fx-min-height: 50;" +
            "-fx-max-width: 50; -fx-max-height: 50; -fx-cursor: hand; -fx-border-width: 0; -fx-padding: 0;"
        );

        messageInput.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    if (!event.isShiftDown()) { event.consume(); sendMessage(); }
                    break;
                default: break;
            }
        });

        messageInput.textProperty().addListener((obs, oldVal, newVal) -> {
            int lines = newVal.split("\n", -1).length;
            messageInput.setPrefRowCount(Math.min(Math.max(lines, 1), 6));
        });

        conversationList.setOnMouseClicked(event -> {
            Conversation selected = conversationList.getSelectionModel().getSelectedItem();
            if (selected != null) loadConversation(selected);
        });

        conversationList.setCellFactory(lv -> new ListCell<Conversation>() {
            @Override
            protected void updateItem(Conversation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox row = new HBox();
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setSpacing(8);

                    Label title = new Label(item.getTitle());
                    title.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 13px; -fx-text-fill: #6B7280;");
                    title.setMaxWidth(180);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button deleteBtn = new Button("✕");
                    deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4B5563; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 2 4 2 4; -fx-border-color: transparent;");
                    deleteBtn.setVisible(false);

                    row.setOnMouseEntered(e -> {
                        deleteBtn.setVisible(true);
                        title.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 13px; -fx-text-fill: #F0F9FF;");
                    });
                    row.setOnMouseExited(e -> {
                        deleteBtn.setVisible(false);
                        title.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 13px; -fx-text-fill: #6B7280;");
                    });

                    deleteBtn.setOnAction(e -> {
                        chatService.deleteConversation(item.getId());
                        refreshConversationList();
                        if (currentConversationId != null && currentConversationId.equals(item.getId())) {
                            newConversation();
                        }
                        e.consume();
                    });

                    deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: rgba(248,81,73,0.15); -fx-text-fill: #f85149; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 2 4 2 4; -fx-background-radius: 4; -fx-border-color: transparent;"));
                    deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4B5563; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 2 4 2 4; -fx-border-color: transparent;"));

                    row.getChildren().addAll(title, spacer, deleteBtn);
                    setText(null);
                    setGraphic(row);

                    setStyle("-fx-background-color: transparent; -fx-padding: 6 8 6 8; -fx-background-radius: 8; -fx-cursor: hand;");

                    setOnMouseEntered(e -> { if (!isSelected()) setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-padding: 6 8 6 8; -fx-background-radius: 8; -fx-cursor: hand;"); });
                    setOnMouseExited(e -> { if (!isSelected()) setStyle("-fx-background-color: transparent; -fx-padding: 6 8 6 8; -fx-background-radius: 8; -fx-cursor: hand;"); });
                    selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                        if (isNowSelected) {
                            setStyle("-fx-background-color: rgba(168,85,247,0.08); -fx-padding: 6 8 6 8; -fx-background-radius: 8; -fx-cursor: hand;");
                            title.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 13px; -fx-text-fill: #A855F7;");
                        } else {
                            setStyle("-fx-background-color: transparent; -fx-padding: 6 8 6 8; -fx-background-radius: 8; -fx-cursor: hand;");
                            title.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 13px; -fx-text-fill: #6B7280;");
                        }
                    });
                }
            }
        });

        showEmptyState();
        refreshConversationList();
        updateModeUI();

        Platform.runLater(() -> {
            refreshConversationList();
            conversationList.refresh();
        });
    }

    // ── MODOS ────────────────────────────────────────────────────────────────

    @FXML private void setModeGenerate() { currentMode = "generate"; updateModeUI(); }
    @FXML private void setModeAnalyze()  { currentMode = "analyze";  updateModeUI(); }
    @FXML private void setModeExplain()  { currentMode = "explain";  updateModeUI(); }

    private void updateModeUI() {
        String active = "-fx-background-color: rgba(34,211,238,0.08); -fx-text-fill: #22D3EE;" +
            "-fx-font-family: 'JetBrains Mono'; -fx-font-size: 13px; -fx-background-radius: 8;" +
            "-fx-padding: 10 12 10 12; -fx-border-color: rgba(34,211,238,0.2);" +
            "-fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;";
        String inactive = "-fx-background-color: transparent; -fx-text-fill: #8B9BB4;" +
            "-fx-font-family: 'JetBrains Mono'; -fx-font-size: 13px; -fx-background-radius: 8;" +
            "-fx-padding: 10 12 10 12; -fx-border-color: transparent;" +
            "-fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;";

        modeGenerate.setStyle(currentMode.equals("generate") ? active : inactive);
        modeAnalyze.setStyle(currentMode.equals("analyze")  ? active : inactive);
        modeExplain.setStyle(currentMode.equals("explain")  ? active : inactive);

        switch (currentMode) {
            case "generate":
                modeBadge.setText("⚡ Generar");
                modeBadge.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-text-fill: #22D3EE; -fx-background-color: rgba(34,211,238,0.08); -fx-background-radius: 99; -fx-padding: 4 12 4 12; -fx-border-color: rgba(34,211,238,0.2); -fx-border-radius: 99; -fx-border-width: 1;");
                messageInput.setPromptText("Describe el código que quieres generar...");
                break;
            case "analyze":
                modeBadge.setText("✦ Analizar");
                modeBadge.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-text-fill: #A855F7; -fx-background-color: rgba(168,85,247,0.08); -fx-background-radius: 99; -fx-padding: 4 12 4 12; -fx-border-color: rgba(168,85,247,0.2); -fx-border-radius: 99; -fx-border-width: 1;");
                messageInput.setPromptText("Pega tu código para analizarlo...");
                break;
            case "explain":
                modeBadge.setText("◆ Explicar");
                modeBadge.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-text-fill: #FDE047; -fx-background-color: rgba(253,224,71,0.08); -fx-background-radius: 99; -fx-padding: 4 12 4 12; -fx-border-color: rgba(253,224,71,0.2); -fx-border-radius: 99; -fx-border-width: 1;");
                messageInput.setPromptText("¿Qué concepto quieres que te explique?");
                break;
        }
    }

    private String getModeSystemPrompt() {
        switch (currentMode) {
            case "generate": return "Eres Deploy AI, un experto generador de código. Genera código limpio, bien comentado y siguiendo las mejores prácticas. Siempre usa bloques de código con el lenguaje especificado.";
            case "analyze":  return "Eres Deploy AI, un experto analizador de código. Analiza el código proporcionado, identifica bugs, problemas de rendimiento, malas prácticas y sugiere mejoras concretas.";
            case "explain":  return "Eres Deploy AI, un experto explicando conceptos de programación. Explica de forma clara con ejemplos de código prácticos.";
            default: return "Eres Deploy AI, un asistente experto en programación.";
        }
    }

    // ── CHAT ─────────────────────────────────────────────────────────────────

    @FXML
    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty()) return;

        messageInput.clear();
        sendButton.setDisable(true);

        // Quitar estado vacío
        messagesContainer.getChildren().removeIf(node ->
            node instanceof VBox && ((VBox) node).getChildren().size() >= 2
            && ((VBox) node).getChildren().get(0) instanceof Label
            && ((Label)((VBox) node).getChildren().get(0)).getText().equals("⬡")
        );

        Message userMsg = new Message("user", text);
        currentMessages.add(userMsg);

        if (currentConversationId == null) {
            Conversation conv = chatService.createConversation(text);
            currentConversationId = conv.getId();
            chatTitleLabel.setText(">_ " + conv.getTitle());
            refreshConversationList();
        }

        addMessageBubble(text, true);

        // Typing indicator ligero — solo texto, sin WebView
        HBox typingWrapper = new HBox();
        typingWrapper.setAlignment(Pos.CENTER_LEFT);
        typingWrapper.setSpacing(18);
        VBox typingAvatar = buildAvatar(false);
        Label typing = new Label("Deploy AI está escribiendo...");
        typing.setStyle(
            "-fx-font-family: 'JetBrains Mono'; -fx-font-size: 14px; -fx-text-fill: #8B9BB4;" +
            "-fx-background-color: #10121C; -fx-background-radius: 12;" +
            "-fx-padding: 14 20 14 20; -fx-border-color: rgba(255,255,255,0.06);" +
            "-fx-border-radius: 12; -fx-border-width: 1;"
        );
        typingWrapper.getChildren().addAll(typingAvatar, typing);
        messagesContainer.getChildren().add(typingWrapper);
        scrollToBottom();

        List<Message> messagesCopy = currentMessages.size() > 1
            ? new ArrayList<>(currentMessages)
            : List.of(new Message("user", getModeSystemPrompt() + "\n\nUsuario: " + text));

        int convId = currentConversationId;

        new Thread(() -> {
            try {
                String reply = chatService.sendMessage(new ArrayList<>(messagesCopy));
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
                    if (!currentMessages.isEmpty()) currentMessages.remove(currentMessages.size() - 1);
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
        try { SceneManager.getInstance().showLogin(); } catch (Exception e) { e.printStackTrace(); }
    }

    private void refreshConversationList() {
        conversationList.getItems().setAll(chatService.getConversations());
    }

    private void loadConversation(Conversation conversation) {
        currentConversationId = conversation.getId();
        chatTitleLabel.setText(">_ " + conversation.getTitle());
        currentMessages.clear();
        messagesContainer.getChildren().clear();
        List<Message> messages = chatService.getMessages(conversation.getId());
        currentMessages.addAll(messages);
        for (Message m : messages) addMessageBubble(m.getContent(), m.isUser());
        scrollToBottom();
    }

    private void showEmptyState() {
        VBox empty = new VBox(16);
        empty.setAlignment(Pos.CENTER);
        empty.prefWidthProperty().bind(messagesContainer.widthProperty());
        empty.prefHeightProperty().bind(scrollPane.heightProperty().subtract(80));

        Label icon = new Label("⬡");
        icon.setStyle("-fx-font-size: 56px; -fx-text-fill: #1a2030;");

        Label title = new Label("¿En qué puedo ayudarte?");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #F0F9FF;");

        Label sub = new Label("Selecciona un modo y escribe tu pregunta.\nDeploy AI te responderá al instante.");
        sub.setStyle("-fx-font-size: 15px; -fx-text-fill: #8B9BB4; -fx-text-alignment: center;");
        sub.setWrapText(true);

        empty.getChildren().addAll(icon, title, sub);
        messagesContainer.getChildren().add(empty);
    }

    // ── RENDERIZADO NATIVO (sin WebView) ─────────────────────────────────────

    private void addMessageBubble(String content, boolean isUser) {
        HBox wrapper = new HBox();
        wrapper.setSpacing(18);
        VBox avatar = buildAvatar(isUser);

        if (isUser) {
            VBox bubble = new VBox();
            bubble.setMaxWidth(820);
            bubble.setPadding(new Insets(16, 20, 16, 20));
            bubble.setStyle(
                "-fx-background-color: rgba(34,211,238,0.07);" +
                "-fx-background-radius: 12; -fx-border-color: rgba(34,211,238,0.15);" +
                "-fx-border-radius: 12; -fx-border-width: 1;"
            );
            Text text = new Text(content);
            text.setWrappingWidth(760);
            text.setFill(Color.web("#E0F9FF"));
            text.setFont(Font.font("JetBrains Mono", 16));
            bubble.getChildren().add(new TextFlow(text));
            wrapper.setAlignment(Pos.CENTER_RIGHT);
            wrapper.getChildren().addAll(bubble, avatar);
        } else {
            // Renderizado nativo de markdown
            VBox bubble = buildMarkdownBubble(content);
            wrapper.setAlignment(Pos.CENTER_LEFT);
            wrapper.getChildren().addAll(avatar, bubble);
        }

        messagesContainer.getChildren().add(wrapper);
    }

    private VBox buildAvatar(boolean isUser) {
        VBox avatar = new VBox();
        avatar.setAlignment(Pos.CENTER);
        avatar.setMinWidth(40); avatar.setMaxWidth(40);
        avatar.setMinHeight(40); avatar.setMaxHeight(40);

        Label lbl = new Label(isUser ? getUserInitial() : "AI");
        if (isUser) {
            avatar.setStyle("-fx-background-color: rgba(34,211,238,0.12); -fx-background-radius: 8; -fx-border-color: rgba(34,211,238,0.2); -fx-border-radius: 8; -fx-border-width: 1;");
            lbl.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #22D3EE;");
        } else {
            avatar.setStyle("-fx-background-color: rgba(168,85,247,0.12); -fx-background-radius: 8; -fx-border-color: rgba(168,85,247,0.2); -fx-border-radius: 8; -fx-border-width: 1;");
            lbl.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #A855F7;");
        }
        avatar.getChildren().add(lbl);
        return avatar;
    }

    /**
     * Convierte markdown a nodos JavaFX nativos — sin WebView, rápido y ligero.
     */
    private VBox buildMarkdownBubble(String content) {
        VBox bubble = new VBox(8);
        bubble.setMaxWidth(820);
        bubble.setPadding(new Insets(16, 20, 16, 20));
        bubble.setStyle(
            "-fx-background-color: #10121C; -fx-background-radius: 12;" +
            "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 12; -fx-border-width: 1;"
        );

        // Dividir por bloques de código
        Pattern codePattern = Pattern.compile("```(\\w*)\\n([\\s\\S]*?)```", Pattern.MULTILINE);
        Matcher matcher = codePattern.matcher(content);

        int lastEnd = 0;
        while (matcher.find()) {
            // Texto antes del bloque de código
            if (matcher.start() > lastEnd) {
                String textPart = content.substring(lastEnd, matcher.start()).trim();
                if (!textPart.isEmpty()) {
                    bubble.getChildren().addAll(buildTextNodes(textPart));
                }
            }
            // Bloque de código
            String lang = matcher.group(1);
            String code = matcher.group(2);
            bubble.getChildren().add(buildCodeBlock(lang, code));
            lastEnd = matcher.end();
        }

        // Texto restante
        if (lastEnd < content.length()) {
            String remaining = content.substring(lastEnd).trim();
            if (!remaining.isEmpty()) {
                bubble.getChildren().addAll(buildTextNodes(remaining));
            }
        }

        return bubble;
    }

    private List<javafx.scene.Node> buildTextNodes(String text) {
        List<javafx.scene.Node> nodes = new ArrayList<>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            TextFlow flow = new TextFlow();
            flow.setMaxWidth(780);
            flow.setPrefWidth(780);

            // Headings
            if (line.startsWith("### ")) {
                Text t = styledText(line.substring(4), "#FDE047", 16, true);
                flow.getChildren().add(t);
            } else if (line.startsWith("## ")) {
                Text t = styledText(line.substring(3), "#A855F7", 16, true);
                flow.getChildren().add(t);
            } else if (line.startsWith("# ")) {
                Text t = styledText(line.substring(2), "#22D3EE", 18, true);
                flow.getChildren().add(t);
            } else if (line.startsWith("- ") || line.startsWith("* ")) {
                Text bullet = styledText("• ", "#A855F7", 16, false);
                flow.getChildren().add(bullet);
                flow.getChildren().addAll(parseInline(line.substring(2)));
            } else {
                flow.getChildren().addAll(parseInline(line));
            }

            nodes.add(flow);
        }
        return nodes;
    }

    private List<Text> parseInline(String line) {
        List<Text> texts = new ArrayList<>();
        // Negrita **text**
        Pattern bold = Pattern.compile("\\*\\*([^*]+)\\*\\*");
        // Cursiva *text*
        Pattern italic = Pattern.compile("\\*([^*]+)\\*");
        // Código inline `code`
        Pattern code = Pattern.compile("`([^`]+)`");

        String remaining = line;
        while (!remaining.isEmpty()) {
            int boldIdx  = findFirst(bold, remaining);
            int italicIdx = findFirst(italic, remaining);
            int codeIdx  = findFirst(code, remaining);

            int minIdx = minPositive(boldIdx, italicIdx, codeIdx);

            if (minIdx == -1) {
                texts.add(styledText(remaining, "#F0F9FF", 16, false));
                break;
            }

            // Texto antes del match
            if (minIdx > 0) {
                texts.add(styledText(remaining.substring(0, minIdx), "#F0F9FF", 16, false));
            }

            if (minIdx == boldIdx) {
                Matcher m = bold.matcher(remaining);
                m.find();
                texts.add(styledText(m.group(1), "#F0F9FF", 16, true));
                remaining = remaining.substring(m.end());
            } else if (minIdx == italicIdx) {
                Matcher m = italic.matcher(remaining);
                m.find();
                Text t = styledText(m.group(1), "#A855F7", 15, false);
                t.setFont(Font.font("JetBrains Mono", FontPosture.ITALIC, 16));
                texts.add(t);
                remaining = remaining.substring(m.end());
            } else {
                Matcher m = code.matcher(remaining);
                m.find();
                texts.add(styledText(m.group(1), "#22D3EE", 15, false));
                remaining = remaining.substring(m.end());
            }
        }
        return texts;
    }

    private VBox buildCodeBlock(String lang, String code) {
        VBox block = new VBox(0);
        block.setStyle(
            "-fx-background-color: #070810; -fx-background-radius: 10;" +
            "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 10; -fx-border-width: 1;"
        );

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(8, 16, 8, 16));
        header.setStyle(
            "-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 10 10 0 0;" +
            "-fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 0 1 0;"
        );
        Label langLabel = new Label(lang.isEmpty() ? "code" : lang.toUpperCase());
        langLabel.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-text-fill: #6B7280;");

        Button copyBtn = new Button("📋 copiar");
        copyBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.04); -fx-text-fill: #6B7280;" +
            "-fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-background-radius: 6;" +
            "-fx-padding: 3 10 3 10; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 6; -fx-border-width: 1; -fx-cursor: hand;"
        );
        final String codeToCopy = code;
        copyBtn.setOnAction(e -> {
            javafx.scene.input.Clipboard cb = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
            cc.putString(codeToCopy);
            cb.setContent(cc);
            copyBtn.setText("✓ copiado");
            copyBtn.setStyle("-fx-background-color: rgba(16,185,129,0.08); -fx-text-fill: #10B981; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 3 10 3 10; -fx-border-color: rgba(16,185,129,0.2); -fx-border-radius: 6; -fx-border-width: 1;");
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException ex) {}
                Platform.runLater(() -> {
                    copyBtn.setText("📋 copiar");
                    copyBtn.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-text-fill: #6B7280; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 3 10 3 10; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 6; -fx-border-width: 1; -fx-cursor: hand;");
                });
            }).start();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(langLabel, spacer, copyBtn);

        // Código con syntax highlighting nativo
        VBox codeContainer = new VBox(2);
        codeContainer.setPadding(new Insets(16, 20, 16, 20));
        codeContainer.setStyle("-fx-background-color: transparent;");

        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            HBox lineBox = new HBox(0);
            lineBox.setAlignment(Pos.CENTER_LEFT);

            // Número de línea
            Label lineNum = new Label(String.format("%02d", i + 1));
            lineNum.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 13px; -fx-text-fill: #3D4F6A; -fx-min-width: 32; -fx-padding: 0 12 0 0;");

            // Línea coloreada
            TextFlow lineFlow = buildHighlightedLine(lines[i]);
            lineFlow.setPrefWidth(720);
            HBox.setHgrow(lineFlow, Priority.ALWAYS);

            lineBox.getChildren().addAll(lineNum, lineFlow);
            codeContainer.getChildren().add(lineBox);
        }

        block.getChildren().addAll(header, codeContainer);
        return block;
    }

    private TextFlow buildHighlightedLine(String line) {
        TextFlow flow = new TextFlow();

        // Comentarios — toda la línea
        if (line.trim().startsWith("//") || line.trim().startsWith("#") || line.trim().startsWith("--")) {
            flow.getChildren().add(styledText(line, "#4B5563", 13, false));
            return flow;
        }

        // Tokenizar la línea
        java.util.List<Text> tokens = tokenizeLine(line);
        flow.getChildren().addAll(tokens);
        return flow;
    }

    private java.util.List<Text> tokenizeLine(String line) {
        java.util.List<Text> result = new ArrayList<>();

        java.util.Set<String> keywords = new java.util.HashSet<>(java.util.Arrays.asList(
            "const","let","var","function","return","if","else","for","while","do",
            "class","import","export","from","new","this","try","catch","throw",
            "async","await","public","private","protected","static","void","int",
            "long","double","float","String","boolean","true","false","null",
            "undefined","extends","implements","interface","enum","switch","case",
            "break","continue","typeof","instanceof","SELECT","FROM","WHERE",
            "JOIN","INSERT","UPDATE","DELETE","CREATE","TABLE","AND","OR","ON",
            "AS","BY","ORDER","GROUP","HAVING","LIMIT","INTO","VALUES","SET",
            "DROP","ALTER","def","print","elif","not","in","is","lambda","pass"
        ));

        int i = 0;
        StringBuilder current = new StringBuilder();

        while (i < line.length()) {
            char c = line.charAt(i);

            // String con comillas dobles
            if (c == '"') {
                if (current.length() > 0) { result.add(styledText(current.toString(), "#F0F9FF", 13, false)); current = new StringBuilder(); }
                StringBuilder str = new StringBuilder("\"");
                i++;
                while (i < line.length() && line.charAt(i) != '"') { str.append(line.charAt(i)); i++; }
                str.append('"');
                result.add(styledText(str.toString(), "#FDE047", 13, false));
                i++;
                continue;
            }

            // String con comillas simples
            if (c == '\'') {
                if (current.length() > 0) { result.add(styledText(current.toString(), "#F0F9FF", 13, false)); current = new StringBuilder(); }
                StringBuilder str = new StringBuilder("'");
                i++;
                while (i < line.length() && line.charAt(i) != '\'') { str.append(line.charAt(i)); i++; }
                str.append('\'');
                result.add(styledText(str.toString(), "#FDE047", 13, false));
                i++;
                continue;
            }

            // Comentario inline //
            if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
                if (current.length() > 0) { result.add(styledText(current.toString(), "#F0F9FF", 13, false)); current = new StringBuilder(); }
                result.add(styledText(line.substring(i), "#4B5563", 13, false));
                break;
            }

            // Número
            if (Character.isDigit(c) && (current.length() == 0 || !Character.isLetterOrDigit(current.charAt(current.length()-1)))) {
                if (current.length() > 0) { result.add(styledText(current.toString(), "#F0F9FF", 13, false)); current = new StringBuilder(); }
                StringBuilder num = new StringBuilder();
                while (i < line.length() && (Character.isDigit(line.charAt(i)) || line.charAt(i) == '.')) { num.append(line.charAt(i)); i++; }
                result.add(styledText(num.toString(), "#F97316", 13, false));
                continue;
            }

            // Palabra
            if (Character.isLetter(c) || c == '_') {
                if (current.length() > 0 && !Character.isLetterOrDigit(current.charAt(current.length()-1)) && current.charAt(current.length()-1) != '_') {
                    result.add(styledText(current.toString(), "#F0F9FF", 13, false));
                    current = new StringBuilder();
                }
                current.append(c);
                i++;
                while (i < line.length() && (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '_')) {
                    current.append(line.charAt(i)); i++;
                }
                String word = current.toString();
                current = new StringBuilder();
                if (keywords.contains(word)) {
                    result.add(styledText(word, "#A855F7", 13, true));
                } else {
                    // Funciones (seguidas de '(')
                    if (i < line.length() && line.charAt(i) == '(') {
                        result.add(styledText(word, "#22D3EE", 13, false));
                    } else {
                        result.add(styledText(word, "#F0F9FF", 13, false));
                    }
                }
                continue;
            }

            current.append(c);
            i++;
        }

        if (current.length() > 0) result.add(styledText(current.toString(), "#F0F9FF", 13, false));
        return result;
    }


    // ── HELPERS ──────────────────────────────────────────────────────────────

    private Text styledText(String content, String color, double size, boolean bold) {
        Text t = new Text(content);
        t.setFill(Color.web(color));
        if (bold) t.setFont(Font.font("JetBrains Mono", FontWeight.BOLD, size));
        else       t.setFont(Font.font("JetBrains Mono", size));
        return t;
    }

    private int findFirst(Pattern p, String text) {
        Matcher m = p.matcher(text);
        return m.find() ? m.start() : -1;
    }

    private int minPositive(int... values) {
        int min = -1;
        for (int v : values) {
            if (v >= 0 && (min == -1 || v < min)) min = v;
        }
        return min;
    }

    private String getUserInitial() {
        if (SessionManager.getInstance().getCurrentUser() != null) {
            String name = SessionManager.getInstance().getCurrentUser().getUsername();
            if (name != null && !name.isEmpty()) return name.substring(0, 1).toUpperCase();
        }
        return "U";
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }
}