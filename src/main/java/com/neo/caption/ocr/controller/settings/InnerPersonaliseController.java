package com.neo.caption.ocr.controller.settings;

import com.neo.caption.ocr.pojo.AppHolder;
import com.neo.caption.ocr.service.FileService;
import com.neo.caption.ocr.service.PreferencesService;
import com.neo.caption.ocr.stage.StageBroadcast;
import com.neo.caption.ocr.view.Toast;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.util.ResourceBundle;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.neo.caption.ocr.constant.FileType.*;
import static com.neo.caption.ocr.constant.PrefKey.*;

@Controller
@Slf4j
@Lazy
public class InnerPersonaliseController {

    @FXML
    public Button background_image;
    @FXML
    public Slider slider_opacity;
    @FXML
    public CheckBox check_dark;
    @FXML
    public Button background_del;

    private final StageBroadcast stageBroadcast;
    private final PreferencesService preferencesService;
    private final AppHolder appHolder;
    private final ResourceBundle resourceBundle;
    private final FileService fileService;
    private final SettingsController settingsController;

    public InnerPersonaliseController(StageBroadcast stageBroadcast, PreferencesService preferencesService,
                                      AppHolder appHolder, ResourceBundle resourceBundle, FileService fileService,
                                      SettingsController settingsController) {
        this.stageBroadcast = stageBroadcast;
        this.preferencesService = preferencesService;
        this.appHolder = appHolder;
        this.resourceBundle = resourceBundle;
        this.fileService = fileService;
        this.settingsController = settingsController;
    }

    protected void init() {
        if (!isNullOrEmpty(BACKGROUND_IMAGE.stringValue())) {
            background_image.setText(BACKGROUND_IMAGE.stringValue());
            background_del.setVisible(true);
            background_del.setManaged(true);
        }
        slider_opacity.setValue(BACKGROUND_OPACITY.intValue());
        check_dark.setSelected(DARK_THEME.booleanValue());
        settingsController.getFxUtil().modifyToggleText(check_dark);
    }

    protected void bindListener() {
        slider_opacity.valueProperty().addListener(this::onOpacityModify);
        check_dark.selectedProperty().addListener(this::onThemeModify);
    }

    @FXML
    public void onClick(ActionEvent actionEvent) {
        switch (((Button) actionEvent.getSource()).getId()) {
            case "background_del":
                preferencesService.remove(BACKGROUND_IMAGE);
                background_image.setText("...");
                slider_opacity.setValue(100);
                background_del.setVisible(false);
                background_del.setManaged(false);
                break;
            case "background_image":
                File file = fileService.openFileDialog(settingsController.getStage(),
                        "Background Image",
                        appHolder.getExtFilter(BMP, JPEG, PNG, ALL));
                if (file == null) {
                    break;
                }
                preferencesService.put(BACKGROUND_IMAGE, file.getAbsolutePath());
                background_image.setText(file.getAbsolutePath());
                stageBroadcast.sendBackgroundImageBroadcast();
                if (!background_del.isVisible()) {
                    background_del.setVisible(true);
                    background_del.setManaged(true);
                }
                break;
        }
    }

    private void onOpacityModify(ObservableValue<?> ov, Number a, Number b) {
        preferencesService.put(BACKGROUND_OPACITY, b.intValue());
        stageBroadcast.sendBackgroundImageBroadcast();
    }

    private void onThemeModify(ObservableValue<?> ov, Boolean a, Boolean b) {
        preferencesService.put(DARK_THEME, b);
        Toast.makeToast(settingsController.getStage(), resourceBundle.getString("snackbar.modify.theme"));
        settingsController.getFxUtil().modifyToggleText(check_dark);
    }

}
