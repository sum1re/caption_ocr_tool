package com.neo.caption.ocr.view;

import com.neo.caption.ocr.pojo.ModuleStatus;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static com.neo.caption.ocr.constant.LayoutName.LAYOUT_MODULE_NODE;
import static com.neo.caption.ocr.util.BaseUtil.fxmlURL;
import static com.neo.caption.ocr.util.BaseUtil.v2s;

@Getter
@Slf4j
public class ModuleNode extends TitledPane implements Initializable {

    @FXML
    public VBox vb_param;
    @FXML
    public CheckBox check_enable;
    @FXML
    public CheckBox check_cache;
    @FXML
    public Button btn_del;
    @FXML
    public Label indexLabel;
    @FXML
    public Label nameLabel;

    private final String nodeTypeHeadName;
    private final ModuleStatus moduleStatus;
    private int index;

    public ModuleNode(ModuleStatus moduleStatus, String nodeTypeHeadName, ResourceBundle resourceBundle) {
        this.moduleStatus = moduleStatus;
        this.nodeTypeHeadName = nodeTypeHeadName;
        this.index = moduleStatus.getIndex();
        try {
            FXMLLoader loader = new FXMLLoader(fxmlURL(LAYOUT_MODULE_NODE), resourceBundle);
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        this.nameLabel.setText(resources.getString(nodeTypeHeadName));
        this.indexLabel.setText(v2s(moduleStatus.getIndex()));
        check_enable.setSelected(moduleStatus.isEnable());
        check_cache.setSelected(moduleStatus.isCache());
        if (!moduleStatus.isEnable()) {
            this.setOpacity(0.5);
        }
    }

    public void setIndex(int index) {
        this.index = index;
        this.indexLabel.setText(v2s(index));
        moduleStatus.setIndex(index);
    }

    public ModuleNode setDelAction(EventHandler<ActionEvent> eventHandler) {
        btn_del.setOnAction(eventHandler);
        return this;
    }

    public ModuleNode setEnableListener(ChangeListener<Boolean> changeListener) {
        check_enable.selectedProperty().addListener(changeListener);
        return this;
    }

    public ModuleNode setCacheListener(ChangeListener<Boolean> cacheListener) {
        check_cache.selectedProperty().addListener(cacheListener);
        return this;
    }

    public ModuleNode setDragDetected(EventHandler<MouseEvent> eventHandler) {
        this.setOnDragDetected(eventHandler);
        return this;
    }

    public ModuleNode setDragOver(EventHandler<DragEvent> eventHandler) {
        this.setOnDragOver(eventHandler);
        return this;
    }

    public ModuleNode setDragDropped(EventHandler<DragEvent> eventHandler) {
        this.setOnDragDropped(eventHandler);
        return this;
    }

    public void setModuleParam(List<Node> nodeList) {
        vb_param.getChildren().addAll(nodeList);
    }

    @Override
    public String toString() {
        return "ModuleNode{" +
                "moduleStatus=" + moduleStatus +
                '}';
    }
}
