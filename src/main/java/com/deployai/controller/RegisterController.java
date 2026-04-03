package com.deployai.controller;

import com.deployai.service.AuthService;
import com.deployai.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button registerButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Por favor rellena todos los campos.");
            return;
        }

        if (password.length() < 6) {
            errorLabel.setText("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        registerButton.setDisable(true);
        registerButton.setText("Creando cuenta...");
        errorLabel.setText("");

        new Thread(() -> {
            try {
                authService.register(email, password, username);
                Platform.runLater(() -> {
                    try {
                        SceneManager.getInstance().showChat();
                    } catch (Exception e) {
                        errorLabel.setText("Error al cargar el chat.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    errorLabel.setText("Error al crear la cuenta. El email ya existe.");
                    registerButton.setDisable(false);
                    registerButton.setText("Crear cuenta");
                });
            }
        }).start();
    }

    @FXML
    private void goToLogin() {
        try {
            SceneManager.getInstance().showLogin();
        } catch (Exception e) {
            errorLabel.setText("Error al navegar al login.");
        }
    }
}