package com.yourorg.stegoapp.gui.controllers;

import com.yourorg.stegoapp.core.model.CryptoOptions;
import com.yourorg.stegoapp.core.model.StepConfig;
import com.yourorg.stegoapp.core.model.StepType;
import com.yourorg.stegoapp.core.model.ZeroWidthMode;
import com.yourorg.stegoapp.core.model.ZeroWidthOptions;
import com.yourorg.stegoapp.gui.MainApp;
import com.yourorg.stegoapp.service.StegoService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaFX controller for the pipeline (multi-step) encode/decode screen.
 * <p>
 * Users build an ordered pipeline of steps. Encoding applies the steps in order; decoding applies
 * the same steps in reverse order.
 * </p>
 */
public class PipelineController {

    @FXML private TextArea inputArea;
    @FXML private TextArea outputArea;
    @FXML private VBox stepsBox;
    @FXML private ComboBox<StepType> addStepCombo;

    private final StegoService service = new StegoService();
    private final List<StepRow> steps = new ArrayList<>();

    @FXML
    public void initialize() {
        // Populate the step picker. The visual order here is purely UI-related.
        addStepCombo.getItems().addAll(
                StepType.CRYPTO,
                StepType.ZERO_WIDTH,
                StepType.BASE64,
                StepType.EMOJI
        );

        addStepCombo.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(StepType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.name().replace('_', ' '));
            }
        });
        addStepCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(StepType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.name().replace('_', ' '));
            }
        });

        // Default: start with a single Zero-Width step.
        steps.add(StepRow.zeroWidth());
        renderSteps();
    }

    /**
     * Adds the currently selected step type to the end of the pipeline.
     */
    @FXML
    public void onAddStep() {
        StepType type = addStepCombo.getValue();
        if (type == null) {
            return;
        }
        steps.add(StepRow.of(type));
        addStepCombo.setValue(null);
        renderSteps();
    }

    @FXML
    public void onEncode() {
        run(true);
    }

    @FXML
    public void onDecode() {
        run(false);
    }

    private void run(boolean encode) {
        try {
            String input = inputArea.getText() == null ? "" : inputArea.getText();
            List<StepConfig> pipeline = buildPipeline();
            String result = encode ? service.encode(input, pipeline) : service.decode(input, pipeline);
            outputArea.setText(result);
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    private List<StepConfig> buildPipeline() {
        List<StepConfig> cfg = new ArrayList<>();
        for (StepRow row : steps) {
            cfg.add(row.toConfig());
        }
        return cfg;
    }

    private void renderSteps() {
        stepsBox.getChildren().clear();

        for (int i = 0; i < steps.size(); i++) {
            StepRow row = steps.get(i);
            int index = i;

            // Each step is rendered as a "card" with reorder/remove controls and step-specific options.
            VBox card = new VBox(10);
            card.getStyleClass().add("step-card");

            Label title = new Label(row.type.name().replace('_', ' '));
            title.getStyleClass().add("step-card-title");

            Button up = new Button("↑");
            up.getStyleClass().addAll("copy-button", "mini-button");
            up.setDisable(index == 0);
            up.setOnAction(e -> {
                if (index <= 0) return;
                StepRow tmp = steps.get(index - 1);
                steps.set(index - 1, steps.get(index));
                steps.set(index, tmp);
                renderSteps();
            });

            Button down = new Button("↓");
            down.getStyleClass().addAll("copy-button", "mini-button");
            down.setDisable(index == steps.size() - 1);
            down.setOnAction(e -> {
                if (index >= steps.size() - 1) return;
                StepRow tmp = steps.get(index + 1);
                steps.set(index + 1, steps.get(index));
                steps.set(index, tmp);
                renderSteps();
            });

            Button remove = new Button("Remove");
            remove.getStyleClass().addAll("back-button", "mini-button");
            remove.setOnAction(e -> {
                steps.remove(index);
                renderSteps();
            });

            Region spacer = new Region();
            HBox header = new HBox(10, title, spacer, up, down, remove);
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            card.getChildren().add(header);
            card.getChildren().addAll(row.optionsNodes());
            stepsBox.getChildren().add(card);
        }
    }

    @FXML
    public void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/selection.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) inputArea.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);

            stage.setWidth(MainApp.SELECTION_WIDTH);
            stage.setHeight(MainApp.SELECTION_HEIGHT);
            stage.setMinWidth(MainApp.SELECTION_WIDTH);
            stage.setMinHeight(MainApp.SELECTION_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onCopy() {
        Clipboard cb = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(outputArea.getText() == null ? "" : outputArea.getText());
        cb.setContent(content);
    }

    private static final class StepRow {
        private final StepType type;

        // CRYPTO
        private PasswordField passwordField;

        // ZERO_WIDTH
        private ToggleGroup zwGroup;
        private TextArea coverTextArea;
        private RadioButton zwRaw;
        private RadioButton zwCover;

        private StepRow(StepType type) {
            this.type = type;
        }

        static StepRow of(StepType type) {
            return switch (type) {
                case ZERO_WIDTH -> zeroWidth();
                case CRYPTO -> crypto();
                case BASE64, EMOJI -> new StepRow(type);
            };
        }

        static StepRow crypto() {
            StepRow row = new StepRow(StepType.CRYPTO);
            row.passwordField = new PasswordField();
            row.passwordField.setPromptText("Password");
            return row;
        }

        static StepRow zeroWidth() {
            StepRow row = new StepRow(StepType.ZERO_WIDTH);
            row.zwGroup = new ToggleGroup();
            row.zwRaw = new RadioButton("Raw (zero-width only)");
            row.zwCover = new RadioButton("Embed in cover text");
            row.zwRaw.setToggleGroup(row.zwGroup);
            row.zwCover.setToggleGroup(row.zwGroup);
            row.zwRaw.setSelected(true);

            row.coverTextArea = new TextArea();
            row.coverTextArea.setPromptText("Cover (innocent) text");
            row.coverTextArea.setDisable(true);
            row.coverTextArea.setPrefRowCount(3);

            row.zwGroup.selectedToggleProperty().addListener((obs, oldT, newT) ->
                    row.coverTextArea.setDisable(newT == row.zwRaw)
            );

            return row;
        }

        List<javafx.scene.Node> optionsNodes() {
            return switch (type) {
                case BASE64, EMOJI -> List.of();
                case CRYPTO -> List.of(new Label("Password:"), passwordField);
                case ZERO_WIDTH -> List.of(zwRaw, zwCover, coverTextArea);
            };
        }

        StepConfig toConfig() {
            return switch (type) {
                case BASE64, EMOJI -> StepConfig.of(type);
                case CRYPTO -> new StepConfig(type, new CryptoOptions(passwordField.getText() == null ? "" : passwordField.getText()));
                case ZERO_WIDTH -> {
                    if (zwGroup.getSelectedToggle() == zwCover) {
                        String cover = coverTextArea.getText() == null ? "" : coverTextArea.getText();
                        yield new StepConfig(type, new ZeroWidthOptions(ZeroWidthMode.EMBED_IN_COVER, cover));
                    }
                    yield StepConfig.of(type);
                }
            };
        }
    }
}
