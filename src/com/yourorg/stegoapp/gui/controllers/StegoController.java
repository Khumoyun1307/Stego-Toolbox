package com.yourorg.stegoapp.gui.controllers;

import com.yourorg.stegoapp.core.ZeroWidthStep;
import com.yourorg.stegoapp.service.StegoService;
import com.yourorg.stegoapp.core.model.Step;
import com.yourorg.stegoapp.core.model.StepType;
import com.yourorg.stegoapp.gui.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Collections;

/**
 * Controller for the stego step encode/decode screen.
 * <p>
 * Handles user input, options, and invokes the StegoService for encoding/decoding.
 * </p>
 */
public class StegoController {

    @FXML private Label    titleLabel;
    @FXML private TextArea inputArea;
    @FXML private TextArea outputArea;
    @FXML private VBox     optionsPane;

    private StepType stepType;
    private Step     stepModel;
    private final StegoService service = new StegoService();

    /**
     * Called by SelectionController immediately after FXML load.
     * Sets up the UI for the selected step type.
     *
     * @param type The selected StepType
     */
    public void setStep(StepType type) {
        this.stepType  = type;
        this.stepModel = new Step(type);
        titleLabel.setText(type.name().replace('_', ' '));

        optionsPane.getChildren().clear();
        switch (type) {
            case CRYPTO:
                PasswordField pwd = new PasswordField();
                pwd.setPromptText("Password");
                pwd.textProperty().addListener((obs,o,n) -> stepModel.setPassword(n));
                optionsPane.getChildren().addAll(new Label("Password:"), pwd);
                break;

            case ZERO_WIDTH:
                // Mode toggle: empty vs cover
                ToggleGroup tg = new ToggleGroup();
                RadioButton emptyRb = new RadioButton("Empty (no cover)");
                RadioButton coverRb = new RadioButton("Embed in cover text");
                emptyRb.setToggleGroup(tg);
                coverRb.setToggleGroup(tg);
                emptyRb.setSelected(true);

                TextArea coverTa = new TextArea();
                coverTa.setPromptText("Cover (innocent) text");
                coverTa.setDisable(true);

                tg.selectedToggleProperty().addListener((obs,oldT,newT) ->
                        coverTa.setDisable(newT == emptyRb)
                );

                optionsPane.getChildren().addAll(emptyRb, coverRb, coverTa);
                break;

            default:
                // no extra options
        }
    }

    /**
     * Handles the encode button action.
     * Encodes the input using the selected step and options.
     */
    @FXML
    public void onEncode() {
        try {
            String in = inputArea.getText();
            if (stepType == StepType.ZERO_WIDTH) {
                RadioButton firstRb = (RadioButton) optionsPane.getChildren().get(0);
                ToggleGroup tg = firstRb.getToggleGroup();
                RadioButton sel = (RadioButton) tg.getSelectedToggle();

                if (sel == optionsPane.getChildren().get(1)) {
                    TextArea coverTa = (TextArea) optionsPane.getChildren().get(2);
                    String cover  = coverTa.getText();
                    String hidden = new ZeroWidthStep().encode(in);
                    outputArea.setText(cover + hidden);
                    return;
                }
                // else fall through for pure empty
            }

            String out = service.encode(in, Collections.singletonList(stepModel));
            outputArea.setText(out);

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    /**
     * Handles the decode button action.
     * Decodes the input using the selected step and options.
     */
    @FXML
    public void onDecode() {
        try {
            String in = inputArea.getText();
            if (stepType == StepType.ZERO_WIDTH) {
                String zwOnly = in.chars()
                        .filter(c -> c == 0x200B || c == 0x200C)
                        .collect(StringBuilder::new,
                                StringBuilder::appendCodePoint,
                                StringBuilder::append)
                        .toString();
                String decoded = new ZeroWidthStep().decode(zwOnly);
                outputArea.setText(decoded);
                return;
            }

            String out = service.decode(in, Collections.singletonList(stepModel));
            outputArea.setText(out);

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    /**
     * Handles the back button action.
     * Returns to the selection screen.
     */
    @FXML
    public void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/selection.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) inputArea.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);

            // restore fixed main menu size
            stage.setResizable(false);
            stage.setWidth(MainApp.SELECTION_WIDTH);
            stage.setHeight(MainApp.SELECTION_HEIGHT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the copy button action.
     * Copies the output to the system clipboard.
     */
    @FXML
    public void onCopy() {
        Clipboard cb = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(outputArea.getText() == null ? "" : outputArea.getText());
        cb.setContent(content);
    }
}
