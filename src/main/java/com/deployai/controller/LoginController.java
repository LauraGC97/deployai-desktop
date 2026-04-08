package com.deployai.controller;

import com.deployai.service.AuthService;
import com.deployai.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private StackPane logoContainer;

    private final AuthService authService = new AuthService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // WebView para el logo — mismo SVG animado que el splash
        WebView logoView = new WebView();
        logoView.setPrefWidth(120);
        logoView.setPrefHeight(120);
        logoView.setPageFill(javafx.scene.paint.Color.TRANSPARENT);
        logoView.getEngine().loadContent(getLogoHtml());
        logoContainer.getChildren().add(logoView);

        emailField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.ENTER) {
                event.consume();
                passwordField.requestFocus();
            }
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                handleLogin();
            }
        });

        loginButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #A855F7 0%, #22D3EE 100%);" +
            "-fx-text-fill: #080A10; -fx-font-weight: bold; -fx-font-family: 'JetBrains Mono';" +
            "-fx-font-size: 15px; -fx-background-radius: 8; -fx-padding: 14 20 14 20;" +
            "-fx-cursor: hand; -fx-border-width: 0;"
        );

        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private String getLogoHtml() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>" +
        "* { margin:0; padding:0; box-sizing:border-box; }" +
        "body { background:transparent; display:flex; align-items:center; justify-content:center; height:100vh; overflow:hidden; }" +
        ".ring-outer { animation:rotSlow 18s linear infinite; transform-origin:75px 75px; }" +
        ".ring-inner { animation:rotSlowR 12s linear infinite; transform-origin:75px 75px; }" +
        ".core-pulse { animation:breathe 4s ease-in-out infinite; }" +
        "@keyframes rotSlow  { to{transform:rotate(360deg);}  }" +
        "@keyframes rotSlowR { to{transform:rotate(-360deg);} }" +
        "@keyframes breathe  { 0%,100%{opacity:.2} 50%{opacity:.8} }" +
        "</style></head><body>" +
        "<svg width='110' height='110' viewBox='-10 -10 170 170' fill='none' style='overflow:visible'>" +
        "  <circle class='core-pulse' cx='75' cy='75' r='68' fill='none' stroke='#A855F7' stroke-width='1' opacity='.15'/>" +
        "  <g class='ring-outer'>" +
        "    <polygon points='75,8 133,41 133,109 75,142 17,109 17,41' fill='none' stroke='#22D3EE' stroke-width='1.5' opacity='.7'/>" +
        "    <circle cx='75'  cy='8'   r='3' fill='#22D3EE'/>" +
        "    <circle cx='133' cy='41'  r='3' fill='#22D3EE'/>" +
        "    <circle cx='133' cy='109' r='3' fill='#22D3EE'/>" +
        "    <circle cx='75'  cy='142' r='3' fill='#22D3EE'/>" +
        "    <circle cx='17'  cy='109' r='3' fill='#22D3EE'/>" +
        "    <circle cx='17'  cy='41'  r='3' fill='#22D3EE'/>" +
        "  </g>" +
        "  <g class='ring-inner'>" +
        "    <polygon points='75,28 112,49 112,101 75,122 38,101 38,49' fill='none' stroke='#A855F7' stroke-width='1' stroke-dasharray='4 5' opacity='.5'/>" +
        "  </g>" +
        "  <circle cx='75' cy='75' r='26' fill='#0E1018' stroke='#FDE047' stroke-width='1.8'/>" +
        "  <path d='M65 62 L65 88 L75 88 C85 88 91 82 91 75 C91 68 85 62 75 62 Z' fill='none' stroke='#FDE047' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'/>" +
        "  <line x1='65' y1='62' x2='65' y2='88' stroke='#FDE047' stroke-width='2.5' stroke-linecap='round'/>" +
        "  <circle cx='75' cy='75' r='3' fill='#22D3EE'/>" +
        "</svg>" +
        "</body></html>";
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor rellena todos los campos.");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Entrando...");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        new Thread(() -> {
            try {
                authService.login(email, password);
                Platform.runLater(() -> {
                    try {
                        SceneManager.getInstance().showChat();
                    } catch (Exception e) {
                        showError("Error al cargar el chat.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Email o contraseña incorrectos.");
                    loginButton.setDisable(false);
                    loginButton.setText("Entrar →");
                });
            }
        }).start();
    }

    @FXML
    private void goToRegister() {
        try { SceneManager.getInstance().showRegister(); }
        catch (Exception e) { showError("Error al navegar al registro."); }
    }

    @FXML
    private void goBack() {
        try { SceneManager.getInstance().showSplash(); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}