package com.neo.caption.ocr.service;

import javafx.scene.Node;
import javafx.stage.Stage;

public interface StageService {

    Stage add(Node node);

    void add(Stage stage);

    void remove(Stage stage);

    Stage getFocusedStage();
}
