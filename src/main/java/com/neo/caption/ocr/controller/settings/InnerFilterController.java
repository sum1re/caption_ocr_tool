package com.neo.caption.ocr.controller.settings;

import com.neo.caption.ocr.service.PreferencesService;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import static com.neo.caption.ocr.constant.PrefKey.*;

@Controller
@Slf4j
@Lazy
public class InnerFilterController {

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

    private final PreferencesService preferencesService;

    protected InnerFilterController(PreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    protected void init() {
        spinner_mpc.getValueFactory().setValue(MIN_PIXEL_COUNT.intValue());
        spinner_ssim.getValueFactory().setValue(MIN_SSIM_THRESHOLD.doubleValue());
        spinner_psnr.getValueFactory().setValue(MIN_PSNR_THRESHOLD.doubleValue());
        choice_similarity_type.getSelectionModel().select(SIMILARITY_TYPE.intValue());
        choice_storage_policy.getSelectionModel().select(STORAGE_POLICY.intValue());
    }

    protected void bindListener() {
        spinner_mpc.valueProperty().addListener((ov, a, b) -> preferencesService.put(MIN_PIXEL_COUNT, b));
        spinner_ssim.valueProperty().addListener((ov, a, b) -> preferencesService.put(MIN_SSIM_THRESHOLD, b));
        spinner_psnr.valueProperty().addListener((ov, a, b) -> preferencesService.put(MIN_PSNR_THRESHOLD, b));
        choice_similarity_type.getSelectionModel().selectedItemProperty()
                .addListener((ov, a, b) -> preferencesService.put(SIMILARITY_TYPE, getChoiceSelected(choice_similarity_type)));
        choice_storage_policy.getSelectionModel().selectedItemProperty()
                .addListener((ov, a, b) -> preferencesService.put(STORAGE_POLICY, getChoiceSelected(choice_storage_policy)));
    }

    private int getChoiceSelected(ChoiceBox<String> choiceBox) {
        return choiceBox.getSelectionModel().getSelectedIndex();
    }

}
