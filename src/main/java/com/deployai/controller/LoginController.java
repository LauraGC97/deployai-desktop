package com.deployai.controller;

import com.deployai.service.AuthService;
import com.deployai.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final AuthService authService = new AuthService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Enter en email → salta a password
        emailField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.ENTER) {
                event.consume();
                passwordField.requestFocus();
            }
        });

        // Enter en password → hace login
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                handleLogin();
            }
        });

        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
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
                    loginButton.setText("Iniciar sesión →");
                });
            }
        }).start();

        // Aplicar gradiente al botón via código
        loginButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #A855F7 0%, #22D3EE 100%);" +
            "-fx-text-fill: #080A10;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'JetBrains Mono';" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 12 20 12 20;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;"
        );
    }

    @FXML
    private void goToRegister() {
        try {
            SceneManager.getInstance().showRegister();
        } catch (Exception e) {
            showError("Error al navegar al registro.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}