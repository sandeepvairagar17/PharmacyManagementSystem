package com.pharmacy.app.controller;

import com.pharmacy.app.util.BackupManager;
import com.pharmacy.app.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.Optional;

public class BackupController {

    @FXML private ListView<String> backupListView;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        refreshList();
    }

    private void refreshList() {
        backupListView.setItems(FXCollections.observableArrayList(BackupManager.listBackups()));
    }

    @FXML
    private void handleBackupNow() {
        try {
            String path = BackupManager.backupNow();
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("Backup created successfully: " + path);
            refreshList();
        } catch (RuntimeException e) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Backup failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        refreshList();
        statusLabel.setText("");
    }

    @FXML
    private void handleRestore() {
        String selected = backupListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Select a backup from the list first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Restore");
        confirm.setHeaderText("This will REPLACE all current data with the backup: " + selected);
        confirm.setContentText("Any sales, inventory changes, or other data added after this backup was made will be permanently lost. Continue?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            BackupManager.restoreFromBackup(selected);
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("Restore complete. Returning to login...");

            // Data (including user accounts) may have changed - safest to force re-login
            SceneManager.switchTo("/com/pharmacy/app/view/login.fxml", "Pharmacy Management System - Login");

        } catch (RuntimeException e) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Restore failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo("/com/pharmacy/app/view/dashboard.fxml", "Pharmacy Management System - Dashboard");
    }
}