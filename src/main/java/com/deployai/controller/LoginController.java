package com.deployai.controller;

import com.deployai.service.AuthService;
import com.deployai.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Por favor rellena todos los campos.");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Entrando...");
        errorLabel.setText("");

        new Thread(() -> {
            try {
                authService.login(email, password);
                Platform.runLater(() -> {
                    try {
                        SceneManager.getInstance().showChat();
                    } catch (Exception e) {
                        errorLabel.setText("Error al cargar el chat.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    errorLabel.setText("Email o contraseña incorrectos.");
                    loginButton.setDisable(false);
                    loginButton.setText("Entrar");
                });
            }
        }).start();
    }

    @FXML
    private void goToRegister() {
        try {
            SceneManager.getInstance().showRegister();
        } catch (Exception e) {
            errorLabel.setText("Error al navegar al registro.");
        }
    }
}