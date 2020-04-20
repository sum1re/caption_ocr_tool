package com.neo.caption.ocr.controller;

import ch.qos.logback.classic.Level;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.neo.caption.ocr.constant.PrefKey;
import com.neo.caption.ocr.pojo.AppHolder;
import com.neo.caption.ocr.service.FileService;
import com.neo.caption.ocr.service.LogService;
import com.neo.caption.ocr.service.PreferencesService;
import com.neo.caption.ocr.service.StageService;
import com.neo.caption.ocr.stage.StageBroadcast;
import com.neo.caption.ocr.view.Toast;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.neo.caption.ocr.constant.PrefKey.*;

@Controller
@Slf4j
@Lazy
public class SettingsController implements BaseController {

    // Application profile
    @FXML
    public TabPane root;
    @FXML
    public TextArea text_cds;
    @FXML
    public TextArea text_dcf;
    @FXML
    public Spinner<Integer> spinner_efs;
    @FXML
    public Spinner<Integer> spinner_fi;
    @FXML
    public Spinner<Integer> spinner_cpp;
    @FXML
    public CheckBox check_sim;
    @FXML
    public CheckBox check_tra;
    @FXML
    public CheckBox check_jpn;
    @FXML
    public CheckBox check_eng;
    @FXML
    public CheckBox check_compress;
    // Filter Global Profile
    @FXML
    public Spinner<Integer> spinner_mpc;
    @FXML
    public Spinner<Double> spinner_ssim;
    @FXML
    public Spinner<Double> spinner_psnr;
    @FXML
    public ChoiceBox<String> choice_similarity_type;
    @FXML
    public ChoiceBox<String> choice_storage_policy;
    // Personalise
    @FXML
    public Button background_image;
    @FXML
    public Slider slider_opacity;
    @FXML
    public CheckBox check_dark;
    @FXML
    public Button background_del;
    @FXML
    public ChoiceBox<Level> choice_log_level;

    private final StageService stageService;
    private final FileService fileService;
    private final LogService logService;
    private final StageBroadcast stageBroadcast;
    private final PreferencesService preferencesService;
    private final Joiner joiner;
    private final Splitter splitter;
    private final AppHolder appHolder;
    private final ResourceBundle resourceBundle;

    private Stage stage;
    private List<String> languageList;

    public SettingsController(StageService stageService, FileService fileService, LogService logService,
                              StageBroadcast stageBroadcast, PreferencesService preferencesService,
                              @Qualifier("plus") Joiner joiner, @Qualifier("plusSplitter") Splitter splitter,
                              AppHolder appHolder, ResourceBundle resourceBundle) {
        this.stageService = stageService;
        this.fileService = fileService;
        this.logService = logService;
        this.stageBroadcast = stageBroadcast;
        this.preferencesService = preferencesService;
        this.joiner = joiner;
        this.splitter = splitter;
        this.appHolder = appHolder;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public void init() {
        text_cds.setText(DEFAULT_STYLE.stringValue());
        text_dcf.setText(DIGITAL_CONTAINER_FORMAT.stringValue());
        spinner_efs.getValueFactory().setValue(EDITOR_FONT_SIZE.intValue());
        spinner_fi.getValueFactory().setValue(FRAME_INTERVAL.intValue());
        spinner_cpp.getValueFactory().setValue(COUNT_PRE_PAGE.intValue());
        check_compress.setSelected(COMPRESS_IMAGE.booleanValue());
        this.languageList = new ArrayList<>(splitter.splitToList(TESS_LANG.stringValue()));
        for (String s : languageList) {
            switch (s) {
                case "chi_sim":
                    check_sim.setSelected(true);
                    break;
                case "chi_tra":
                    check_tra.setSelected(true);
                    break;
                case "jpn":
                    check_jpn.setSelected(true);
                    break;
                case "eng":
                    check_eng.setSelected(true);
            }
        }
    }

    @Override
    public void destroy() {
        stage.setOnHiding(windowEvent -> {
            saveTextArea(text_cds, DEFAULT_STYLE.stringValue(), PrefKey.DEFAULT_STYLE);
            saveTextArea(text_dcf, DIGITAL_CONTAINER_FORMAT.stringValue(), PrefKey.DIGITAL_CONTAINER_FORMAT);
            if (!languageList.isEmpty()) {
                String lang = joiner.join(languageList);
                if (lang.equals(TESS_LANG.stringValue())) {
                    return;
                }
                preferencesService.put(TESS_LANG, lang);
                stageBroadcast.sendTessLangBroadcast();
            }
            stageService.remove(stage);
        });
    }

    @Override
    public void delay() {
        this.stage = stageService.add(root);
        if (!isNullOrEmpty(BACKGROUND_IMAGE.stringValue())) {
            background_image.setText(BACKGROUND_IMAGE.stringValue());
            background_del.setVisible(true);
            background_del.setManaged(true);
        }
        slider_opacity.setValue(BACKGROUND_OPACITY.intValue());
        check_dark.setSelected(DARK_THEME.booleanValue());
        spinner_mpc.getValueFactory().setValue(MIN_PIXEL_COUNT.intValue());
        spinner_ssim.getValueFactory().setValue(MIN_SSIM_THRESHOLD.doubleValue());
        spinner_psnr.getValueFactory().setValue(MIN_PSNR_THRESHOLD.doubleValue());
        choice_similarity_type.getSelectionModel().select(SIMILARITY_TYPE.intValue());
        choice_storage_policy.getSelectionModel().select(STORAGE_POLICY.intValue());
    }

    @Override
    public void bindListener() {
        spinner_efs.valueProperty().addListener(this::onEditorSizeModify);
        spinner_fi.valueProperty().addListener((ov, a, b) -> preferencesService.put(FRAME_INTERVAL, b));
        spinner_cpp.valueProperty().addListener((ov, a, b) -> preferencesService.put(COUNT_PRE_PAGE, b));
        check_compress.selectedProperty().addListener(this::onCompressModify);
        spinner_mpc.valueProperty().addListener((ov, a, b) -> preferencesService.put(MIN_PIXEL_COUNT, b));
        spinner_ssim.valueProperty().addListener((ov, a, b) -> preferencesService.put(MIN_SSIM_THRESHOLD, b));
        spinner_psnr.valueProperty().addListener((ov, a, b) -> preferencesService.put(MIN_PSNR_THRESHOLD, b));
        choice_similarity_type.getSelectionModel().selectedItemProperty()
                .addListener((ov, a, b) -> preferencesService.put(SIMILARITY_TYPE, getChoiceSelected(choice_similarity_type)));
        choice_storage_policy.getSelectionModel().selectedItemProperty()
                .addListener((ov, a, b) -> preferencesService.put(STORAGE_POLICY, getChoiceSelected(choice_storage_policy)));
        check_sim.selectedProperty().addListener((ov, a, b) -> onCheckBoxClick(b, "chi_sim"));
        check_tra.selectedProperty().addListener((ov, a, b) -> onCheckBoxClick(b, "chi_tra"));
        check_jpn.selectedProperty().addListener((ov, a, b) -> onCheckBoxClick(b, "jpn"));
        check_eng.selectedProperty().addListener((ov, a, b) -> onCheckBoxClick(b, "eng"));
        slider_opacity.valueProperty().addListener(this::onOpacityModify);
        check_dark.selectedProperty().addListener(this::onThemeModify);
        choice_log_level.getSelectionModel().selectedItemProperty()
                .addListener(this::onLogLevelModify);
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
                File file = fileService.openFileDialog(stage, "Background Image", appHolder.getPngFilter());
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

    private void onCheckBoxClick(boolean isSelected, String value) {
        if (isSelected) {
            if (!languageList.contains(value)) {
                languageList.add(value);
            }
            return;
        }
        languageList.remove(value);
    }

    private void onEditorSizeModify(ObservableValue<?> ov, Integer a, Integer b) {
        preferencesService.put(EDITOR_FONT_SIZE, b);
        stageBroadcast.sendEditorBroadcast(b);
    }

    private void onOpacityModify(ObservableValue<?> ov, Number a, Number b) {
        preferencesService.put(BACKGROUND_OPACITY, b.intValue());
        stageBroadcast.sendBackgroundImageBroadcast();
    }

    private void onCompressModify(ObservableValue<?> ov, Boolean a, Boolean b) {
        preferencesService.put(COMPRESS_IMAGE, b);
        Toast.makeToast(stage, resourceBundle.getString("snackbar.modify.compress"));
    }

    private void onThemeModify(ObservableValue<?> ov, Boolean a, Boolean b) {
        preferencesService.put(DARK_THEME, b);
        Toast.makeToast(stage, resourceBundle.getString("snackbar.modify.theme"));
    }

    private void onLogLevelModify(ObservableValue<?> ov, Level a, Level b) {
        logService.modifyLogLevel(b);
    }

    private void saveTextArea(TextArea textArea, String ori, PrefKey prefKey) {
        String result = textArea.getText();
        if (!ori.equals(result)) {
            if (isNullOrEmpty(result)) {
                preferencesService.remove(prefKey);
            } else if (prefKey == PrefKey.DEFAULT_STYLE) {
                preferencesService.put(DEFAULT_STYLE, result);
            } else if (prefKey == PrefKey.DIGITAL_CONTAINER_FORMAT) {
                preferencesService.put(DIGITAL_CONTAINER_FORMAT, result);
                stageBroadcast.sendDigitalBroadcast();
            }
        }
    }

    private int getChoiceSelected(ChoiceBox<String> choiceBox) {
        return choiceBox.getSelectionModel().getSelectedIndex();
    }
}
