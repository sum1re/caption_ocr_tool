package com.neo.caption.ocr.view;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import lombok.extern.slf4j.Slf4j;

import java.util.ResourceBundle;

import static com.neo.caption.ocr.constant.PrefKey.EXPORT_ON_ONE_PAGE;
import static com.neo.caption.ocr.constant.PrefKey.EXPORT_PER_PAGE;
import static com.neo.caption.ocr.util.BaseUtil.v2s;

@Slf4j
public class OCRExportDialog extends Dialog<Integer> {

    private final GridPane gridPane;
    private final Spinner<Integer> spinner;
    private final CheckBox checkBox;

    private final ResourceBundle resourceBundle;
    private final int max;

    public OCRExportDialog(ResourceBundle resourceBundle, int max) {
        this.resourceBundle = resourceBundle;
        this.max = max;
        final DialogPane dialogPane = getDialogPane();

        this.spinner = new Spinner<>();
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, max, EXPORT_PER_PAGE.intValue(), 1);
        spinner.setValueFactory(valueFactory);
        spinner.setEditable(true);

        GridPane.setHgrow(spinner, Priority.ALWAYS);
        GridPane.setFillWidth(spinner, true);

        this.gridPane = new GridPane();
        this.gridPane.setHgap(8D);
        this.gridPane.setVgap(8D);
        this.gridPane.setStyle("-fx-font-size: 14");
        this.gridPane.setMaxWidth(Double.MAX_VALUE);
        this.gridPane.setAlignment(Pos.CENTER_LEFT);

        this.checkBox = new CheckBox();
        checkBox.selectedProperty().addListener((ov, a, b) -> spinner.setDisable(b));
        checkBox.setSelected(EXPORT_ON_ONE_PAGE.booleanValue());
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        updateGrid();

        setResultConverter((dialogButton) -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonBar.ButtonData.OK_DONE ? getSpinnerValue() : null;
        });
    }

    public final int getSpinnerValue() {
        if (checkBox.isSelected()) {
            return max;
        }
        return spinner.getValue();
    }

    private void updateGrid() {
        insertNodeToGrid("alert.content.export.spinner", spinner);
        insertNodeToGrid("alert.content.export.max", new Label(v2s(max)));
        insertNodeToGrid("alert.content.export.aio", checkBox);

        getDialogPane().setContent(gridPane);
    }

    private void insertNodeToGrid(String resourceKey, Node node) {
        final int row = gridPane.getRowCount();
        // left col
        gridPane.add(new Label(resourceBundle.getString(resourceKey)), 0, row);
        // right col
        gridPane.add(node, 1, row);
    }

}
