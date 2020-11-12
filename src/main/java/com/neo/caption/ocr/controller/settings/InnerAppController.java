package com.neo.caption.ocr.controller.settings;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.neo.caption.ocr.constant.PrefKey;
import com.neo.caption.ocr.service.PreferencesService;
import com.neo.caption.ocr.stage.StageBroadcast;
import com.neo.caption.ocr.view.Toast;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.ResourceBundle;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.neo.caption.ocr.constant.PrefKey.*;

@Controller
@Slf4j
@Lazy
public class InnerAppController {

    @FXML
    public TextArea text_cds;
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

    private final Joiner joiner;
    private final Splitter splitter;
    private final StageBroadcast stageBroadcast;
    private final PreferencesService preferencesService;
    private final ResourceBundle resourceBundle;
    private final SettingsController settingsController;

    protected InnerAppController(@Qualifier("plus") Joiner joiner, @Qualifier("plusSplitter") Splitter splitter,
                                 StageBroadcast stageBroadcast, PreferencesService preferencesService,
                                 ResourceBundle resourceBundle, SettingsController settingsController) {
        this.joiner = joiner;
        this.splitter = splitter;
        this.stageBroadcast = stageBroadcast;
        this.preferencesService = preferencesService;
        this.resourceBundle = resourceBundle;
        this.settingsController = settingsController;
    }

    protected void init() {
        text_cds.setText(DEFAULT_STYLE.stringValue());
        spinner_efs.getValueFactory().setValue(EDITOR_FONT_SIZE.intValue());
        spinner_fi.getValueFactory().setValue(FRAME_INTERVAL.intValue());
        spinner_cpp.getValueFactory().setValue(COUNT_PRE_PAGE.intValue());
        check_compress.setSelected(COMPRESS_IMAGE.booleanValue());
        settingsController.setLanguageList(new ArrayList<>(splitter.splitToList(TESS_LANG.stringValue())));
        for (String s : settingsController.getLanguageList()) {
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

    protected void destroy() {
        saveTextArea(text_cds, DEFAULT_STYLE.stringValue(), PrefKey.DEFAULT_STYLE);
        if (!settingsController.getLanguageList().isEmpty()) {
            String lang = joiner.join(settingsController.getLanguageList());
            if (lang.equals(TESS_LANG.stringValue())) {
                return;
            }
            preferencesService.put(TESS_LANG, lang);
            stageBroadcast.sendTessLangBroadcast();
        }
    }

    protected void bindListener() {
        spinner_efs.valueProperty().addListener(this::onEditorSizeModify);
        spinner_fi.valueProperty().addListener((ov, a, b) -> preferencesService.put(FRAME_INTERVAL, b));
        spinner_cpp.valueProperty().addListener((ov, a, b) -> preferencesService.put(COUNT_PRE_PAGE, b));
        check_compress.selectedProperty().addListener(this::onCompressModify);
        check_sim.selectedProperty().addListener((ov, a, b) -> onCheckBoxClick(b, "chi_sim"));
        check_tra.selectedProperty().addListener((ov, a, b) -> onCheckBoxClick(b, "chi_tra"));
        check_jpn.selectedProperty().addListener((ov, a, b) -> onCheckBoxClick(b, "jpn"));
        check_eng.selectedProperty().addListener((ov, a, b) -> onCheckBoxClick(b, "eng"));
    }

    private void onEditorSizeModify(ObservableValue<?> ov, Integer a, Integer b) {
        preferencesService.put(EDITOR_FONT_SIZE, b);
        stageBroadcast.sendEditorBroadcast(b);
    }

    private void onCompressModify(ObservableValue<?> ov, Boolean a, Boolean b) {
        preferencesService.put(COMPRESS_IMAGE, b);
        Toast.makeToast(settingsController.getStage(), resourceBundle.getString("snackbar.modify.compress"));
    }

    private void onCheckBoxClick(boolean isSelected, String value) {
        if (isSelected) {
            if (!settingsController.getLanguageList().contains(value)) {
                settingsController.getLanguageList().add(value);
            }
            return;
        }
        settingsController.getLanguageList().remove(value);
    }

    private void saveTextArea(TextArea textArea, String ori, PrefKey prefKey) {
        String result = textArea.getText();
        if (!ori.equals(result)) {
            if (isNullOrEmpty(result)) {
                preferencesService.remove(prefKey);
            } else if (prefKey == PrefKey.DEFAULT_STYLE) {
                preferencesService.put(DEFAULT_STYLE, result);
            }
        }
    }
}
