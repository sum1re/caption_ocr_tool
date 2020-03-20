package com.neo.caption.ocr.service.impl;

import com.neo.caption.ocr.pojo.AppHolder;
import com.neo.caption.ocr.service.StageService;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class StageServiceImpl implements StageService {

    private final AppHolder appHolder;

    public StageServiceImpl(AppHolder appHolder) {
        this.appHolder = appHolder;
    }

    @Override
    public Stage add(Node node) {
        Stage stage = (Stage) node.getScene().getWindow();
        add(stage);
        return stage;
    }

    @Override
    public void add(Stage stage) {
        if (stage == null) {
            return;
        }
        appHolder.getStageList().add(stage);
    }

    @Override
    public void remove(Stage stage) {
        if (stage == null) {
            return;
        }
        appHolder.getStageList().remove(stage);
    }

    @Override
    public Stage getFocusedStage() {
        Optional<Stage> optional = appHolder.getStageList()
                .stream()
                .filter(Window::isFocused)
                .findFirst();
        if (optional.isEmpty()) {
            return null;
        }
        return optional.get();
    }
}
