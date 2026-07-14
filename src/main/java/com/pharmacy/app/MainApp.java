package com.pharmacy.app;

import com.pharmacy.app.dao.UserDAO;
import com.pharmacy.app.util.DatabaseManager;
import com.pharmacy.app.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Connect to database and create tables if needed
        DatabaseManager.getConnection();

        // 2. Create default admin account if this is the first ever run
        UserDAO.seedDefaultAdminIfNeeded();

        // 3. Set up scene switching, then show the login screen
        SceneManager.init(primaryStage);
        SceneManager.switchTo("/com/pharmacy/app/view/login.fxml", "Pharmacy Management System - Login");
    }

    @Override
    public void stop() {
        // Clean shutdown - close DB connection when app closes
        DatabaseManager.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}