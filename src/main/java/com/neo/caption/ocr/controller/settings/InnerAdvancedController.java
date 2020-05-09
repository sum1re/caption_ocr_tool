package com.neo.caption.ocr.controller.settings;

import ch.qos.logback.classic.Level;
import com.neo.caption.ocr.service.LogService;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@Lazy
public class InnerAdvancedController {

    @FXML
    public ChoiceBox<Level> choice_log_level;

    private final LogService logService;

    protected InnerAdvancedController(LogService logService) {
        this.logService = logService;
    }

    protected void bindListener() {
        choice_log_level.getSelectionModel()
                .selectedItemProperty()
                .addListener(this::onLogLevelModify);
    }

    private void onLogLevelModify(ObservableValue<?> ov, Level a, Level b) {
        logService.modifyLogLevel(b);
    }

}
