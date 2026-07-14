package com.pharmacy.app.controller;

import com.pharmacy.app.model.User;
import com.pharmacy.app.util.SceneManager;
import com.pharmacy.app.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Button manageUsersButton;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getFullName() + " (" + user.getRole() + ")");

            // Only Admins can manage staff accounts
            boolean isAdmin = user.getRole() == User.Role.ADMIN;
            manageUsersButton.setVisible(isAdmin);
            manageUsersButton.setManaged(isAdmin);
        }
    }

    @FXML
    private void handleBilling() {
        SceneManager.switchTo("/com/pharmacy/app/view/billing.fxml", "Billing / New Sale");
    }

    @FXML
    private void handleInventory() {
        SceneManager.switchTo("/com/pharmacy/app/view/inventory.fxml", "Inventory");
    }

    @FXML
    private void handlePatients() {
        SceneManager.switchTo("/com/pharmacy/app/view/patients.fxml", "Patients");
    }

    @FXML
    private void handlePrescriptions() {
        SceneManager.switchTo("/com/pharmacy/app/view/prescriptions.fxml", "Prescriptions");
    }

    @FXML
    private void handleReports() {
        SceneManager.switchTo("/com/pharmacy/app/view/reports.fxml", "Reports");
    }

    @FXML
    private void handleBackup() {
        SceneManager.switchTo("/com/pharmacy/app/view/backup.fxml", "Backup & Restore");
    }

    @FXML
    private void handleManageUsers() {
        SceneManager.switchTo("/com/pharmacy/app/view/manage_users.fxml", "Manage Users");
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        SceneManager.switchTo("/com/pharmacy/app/view/login.fxml", "Pharmacy Management System - Login");
    }
}