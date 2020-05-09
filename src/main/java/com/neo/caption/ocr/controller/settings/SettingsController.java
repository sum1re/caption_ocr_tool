package com.neo.caption.ocr.controller.settings;

import com.neo.caption.ocr.controller.BaseController;
import com.neo.caption.ocr.service.StageService;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Slf4j
@Lazy
public class SettingsController implements BaseController {

    @FXML
    public TabPane root;
    @FXML
    public InnerAppController innerAppController;
    @FXML
    public InnerPersonaliseController innerPersonaliseController;
    @FXML
    public InnerFilterController innerFilterController;
    @FXML
    public InnerAdvancedController innerAdvancedController;

    private final StageService stageService;

    private Stage stage;
    private List<String> languageList;

    public SettingsController(StageService stageService) {
        this.stageService = stageService;
    }

    @Override
    public void init() {
        innerAppController.init();
    }

    @Override
    public void destroy() {
        stage.setOnHiding(windowEvent -> {
            innerAppController.destroy();
            stageService.remove(stage);
        });
    }

    @Override
    public void delay() {
        stage = stageService.add(root);
        innerPersonaliseController.init();
        innerFilterController.init();
    }

    @Override
    public void bindListener() {
        innerAppController.bindListener();
        innerPersonaliseController.bindListener();
        innerFilterController.bindListener();
        innerAdvancedController.bindListener();
    }

    protected Stage getStage() {
        return stage;
    }

    protected List<String> getLanguageList() {
        return languageList;
    }

    protected void setLanguageList(List<String> languageList) {
        this.languageList = languageList;
    }
}
