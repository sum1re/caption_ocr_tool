package com.neo.caption.ocr.controller;

import javafx.application.Platform;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public interface BaseController extends Initializable {

    void init();

    void destroy();

    void delay();

    void bindListener();

    default void bindHotKey() {
    }

    default void initialize(URL location, ResourceBundle resources) {
        init();
        Platform.runLater(() -> {
            delay();
            bindListener();
            destroy();
            bindHotKey();
        });
    }
}
