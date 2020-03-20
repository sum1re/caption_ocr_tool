package com.neo.caption.ocr.view;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.neo.caption.ocr.constant.LayoutName.LAYOUT_BAT_NODE;
import static com.neo.caption.ocr.util.BaseUtil.fxmlURL;

@Getter
@Setter
@Accessors(chain = true)
public final class BatNode extends ToggleButton implements Initializable {

    @FXML
    public Label file_name;
    @FXML
    public Label status;
    @FXML
    public ProgressBar progress_bar;

    private File file;

    private boolean finish;
    private boolean valid;
    private boolean error;

    public BatNode() {
        this.finish = false;
        this.error = false;
        try {
            FXMLLoader loader = new FXMLLoader(fxmlURL(LAYOUT_BAT_NODE));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @FXML
    public final void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public StringProperty statusProperty() {
        return status.textProperty();
    }

    public BatNode setFile(File file) {
        this.file = file;
        file_name.setText(file.getName());
        return this;
    }
}
