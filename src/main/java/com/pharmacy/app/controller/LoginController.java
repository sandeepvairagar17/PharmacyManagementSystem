package com.pharmacy.app.controller;

import com.pharmacy.app.dao.UserDAO;
import com.pharmacy.app.model.User;
import com.pharmacy.app.util.SceneManager;
import com.pharmacy.app.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        User user = UserDAO.authenticate(username, password);

        if (user == null) {
            errorLabel.setText("Invalid username or password.");
            passwordField.clear();
            return;
        }

        // Success - store session and move to dashboard
        SessionManager.login(user);
        SceneManager.switchTo("/com/pharmacy/app/view/dashboard.fxml", "Pharmacy Management System - Dashboard");
    }
}