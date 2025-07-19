package com.yourorg.stegoapp.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX application entry point for the Stego App.
 * <p>
 * Loads the selection screen and applies the dark theme.
 * </p>
 */
public class MainApp extends Application {
    /** Width of the selection window. */
    public static final double SELECTION_WIDTH = 600;
    /** Height of the selection window. */
    public static final double SELECTION_HEIGHT = 500;

    /**
     * Starts the JavaFX application and loads the selection screen.
     *
     * @param stage The primary stage for this application
     * @throws Exception if FXML or resources cannot be loaded
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/selection.fxml")
        );
        Scene scene = new Scene(loader.load());
        // Load dark theme
        scene.getStylesheets().add(
                getClass().getResource("/styles/dark-theme.css").toExternalForm()
        );

        stage.setTitle("Stego App");
        stage.setScene(scene);
        stage.setWidth(SELECTION_WIDTH);
        stage.setHeight(SELECTION_HEIGHT);
        stage.setResizable(false);  // fixed for selection screen
        stage.show();
    }

    /**
     * Main entry point.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
