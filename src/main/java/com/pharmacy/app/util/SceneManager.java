package com.pharmacy.app.util;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Handles switching the content of the main application window between screens.
 * Explicitly sizes the window to the screen's VISUAL bounds (which excludes the
 * taskbar) every time a screen loads - this avoids both the "window shrinks on
 * navigation" bug and the "content hidden behind the taskbar" bug, since relying
 * on setMaximized() timing alone was unreliable.
 */
public class SceneManager {

    private static Stage primaryStage;

    public static void init(Stage stage) {
        primaryStage = stage;
        primaryStage.setResizable(true);
        applyFullWorkAreaSize();
    }

    public static void switchTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);

            applyFullWorkAreaSize();
            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load screen: " + fxmlPath, e);
        }
    }

    /**
     * Sizes and positions the window to exactly match the screen's usable work area
     * (i.e. the full screen MINUS the taskbar), so nothing is ever hidden behind it.
     */
    private static void applyFullWorkAreaSize() {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}