package com.neo.caption.ocr.service;

import com.neo.caption.ocr.view.MatNode;
import javafx.scene.layout.FlowPane;

public interface MatNodeService {

    void handleDeleteAndMergeTag(FlowPane flowPane);

    void markDeleteTag(MatNode matNode);

    void markMergeTag(MatNode matNode);

    void markSaveTag(MatNode matNode);

    void removeAllTag(MatNode matNode);

    void removeMergeBeginTag();

    String getMatNodeFormatterTime(MatNode matNode);

    Integer getCaretPosition(String string, int caretPosition);
}
