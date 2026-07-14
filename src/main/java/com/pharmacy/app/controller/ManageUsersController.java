package com.pharmacy.app.controller;

import com.pharmacy.app.dao.UserDAO;
import com.pharmacy.app.model.User;
import com.pharmacy.app.util.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class ManageUsersController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField fullNameField;
    @FXML private ComboBox<User.Role> roleBox;
    @FXML private Label errorLabel;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> fullNameCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, String> activeCol;
    @FXML private TableColumn<User, Void> actionCol;

    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        roleBox.setItems(FXCollections.observableArrayList(User.Role.PHARMACIST, User.Role.CASHIER, User.Role.ADMIN));
        roleBox.getSelectionModel().select(User.Role.CASHIER);

        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().toString()));
        activeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isActive() ? "Yes" : "No"));

        addActionButtonToTable();

        userTable.setItems(userList);
        loadUsers();
    }

    private void loadUsers() {
        userList.setAll(UserDAO.getAllUsers());
    }

    private void addActionButtonToTable() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = col -> new TableCell<>() {
            private final Button toggleBtn = new Button();
            {
                toggleBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    UserDAO.setActive(user.getId(), !user.isActive());
                    loadUsers();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    toggleBtn.setText(user.isActive() ? "Deactivate" : "Activate");
                    setGraphic(toggleBtn);
                }
            }
        };
        actionCol.setCellFactory(cellFactory);
    }

    @FXML
    private void handleCreateUser() {
        errorLabel.setText("");
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String fullName = fullNameField.getText().trim();
        User.Role role = roleBox.getValue();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || role == null) {
            errorLabel.setText("All fields are required.");
            return;
        }
        if (password.length() < 4) {
            errorLabel.setText("Password should be at least 4 characters.");
            return;
        }

        try {
            UserDAO.createUser(username, password, fullName, role);
        } catch (RuntimeException e) {
            errorLabel.setText("Failed to create user (username may already exist).");
            return;
        }

        usernameField.clear();
        passwordField.clear();
        fullNameField.clear();
        loadUsers();
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo("/com/pharmacy/app/view/dashboard.fxml", "Pharmacy Management System - Dashboard");
    }
}