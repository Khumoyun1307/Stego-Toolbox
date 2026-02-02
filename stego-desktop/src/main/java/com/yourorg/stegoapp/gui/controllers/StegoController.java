package com.yourorg.stegoapp.gui.controllers;

import com.yourorg.stegoapp.core.model.CryptoOptions;
import com.yourorg.stegoapp.core.model.StepConfig;
import com.yourorg.stegoapp.service.StegoService;
import com.yourorg.stegoapp.core.model.StepType;
import com.yourorg.stegoapp.core.model.ZeroWidthMode;
import com.yourorg.stegoapp.core.model.ZeroWidthOptions;
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

import java.util.List;

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
    private final StegoService service = new StegoService();

    private PasswordField cryptoPassword;

    private RadioButton zwRaw;
    private RadioButton zwCover;
    private TextArea coverText;

    /**
     * Called by SelectionController immediately after FXML load.
     * Sets up the UI for the selected step type.
     *
     * @param type The selected StepType
     */
    public void setStep(StepType type) {
        this.stepType  = type;
        titleLabel.setText(type.name().replace('_', ' '));

        optionsPane.getChildren().clear();
        switch (type) {
            case CRYPTO:
                cryptoPassword = new PasswordField();
                cryptoPassword.setPromptText("Password");
                optionsPane.getChildren().addAll(new Label("Password:"), cryptoPassword);
                break;

            case ZERO_WIDTH:
                // Mode toggle: empty vs cover
                ToggleGroup tg = new ToggleGroup();
                zwRaw = new RadioButton("Raw (zero-width only)");
                zwCover = new RadioButton("Embed in cover text");
                zwRaw.setToggleGroup(tg);
                zwCover.setToggleGroup(tg);
                zwRaw.setSelected(true);

                coverText = new TextArea();
                coverText.setPromptText("Cover (innocent) text");
                coverText.setDisable(true);
                coverText.setPrefRowCount(3);

                tg.selectedToggleProperty().addListener((obs,oldT,newT) ->
                        coverText.setDisable(newT == zwRaw)
                );

                optionsPane.getChildren().addAll(zwRaw, zwCover, coverText);
                break;

            default:
                // no extra options
                cryptoPassword = null;
                zwRaw = null;
                zwCover = null;
                coverText = null;
        }
    }

    /**
     * Handles the encode button action.
     * Encodes the input using the selected step and options.
     */
    @FXML
    public void onEncode() {
        try {
            String in = inputArea.getText() == null ? "" : inputArea.getText();
            StepConfig cfg = buildStepConfig();
            String out = service.encode(in, List.of(cfg));
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
            String in = inputArea.getText() == null ? "" : inputArea.getText();
            StepConfig cfg = buildStepConfig();
            String out = service.decode(in, List.of(cfg));
            outputArea.setText(out);

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    private StepConfig buildStepConfig() {
        // Map the current UI state to the typed core model expected by StegoService.
        return switch (stepType) {
            case BASE64, EMOJI -> StepConfig.of(stepType);
            case CRYPTO -> new StepConfig(
                    StepType.CRYPTO,
                    new CryptoOptions(cryptoPassword == null || cryptoPassword.getText() == null ? "" : cryptoPassword.getText())
            );
            case ZERO_WIDTH -> {
                if (zwCover != null && zwCover.isSelected()) {
                    String cover = coverText == null || coverText.getText() == null ? "" : coverText.getText();
                    yield new StepConfig(StepType.ZERO_WIDTH, new ZeroWidthOptions(ZeroWidthMode.EMBED_IN_COVER, cover));
                }
                yield StepConfig.of(StepType.ZERO_WIDTH);
            }
        };
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

            stage.setMinWidth(MainApp.SELECTION_WIDTH);
            stage.setMinHeight(MainApp.SELECTION_HEIGHT);
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
