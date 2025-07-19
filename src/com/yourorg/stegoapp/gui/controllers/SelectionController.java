package com.yourorg.stegoapp.gui.controllers;

import com.yourorg.stegoapp.core.model.StepType;
import com.yourorg.stegoapp.gui.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

/**
 * Controller for the step selection screen.
 * <p>
 * Dynamically creates buttons for each StepType and handles navigation to the step page.
 * </p>
 */
public class SelectionController {

    @FXML
    private TilePane stepGrid;

    /**
     * Initializes the selection screen by creating a button for each StepType.
     */
    @FXML
    public void initialize() {
        for (StepType type : StepType.values()) {
            Button btn = new Button(type.name().replace('_', ' '));
            btn.setMinSize(120, 60);
            btn.getStyleClass().add("step-button");
            btn.setOnAction(e -> openStepPage(type));
            stepGrid.getChildren().add(btn);
        }
    }

    /**
     * Opens the stego step page for the selected StepType.
     *
     * @param type The selected StepType
     */
    private void openStepPage(StepType type) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/stego.fxml")
            );
            Parent root = loader.load();
            StegoController ctrl = loader.getController();
            ctrl.setStep(type);

            Stage stage = (Stage) stepGrid.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            stage.setWidth(MainApp.SELECTION_WIDTH);   // resize window to fit new content

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
