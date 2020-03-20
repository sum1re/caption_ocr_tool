package com.neo.caption.ocr.service;

import com.neo.caption.ocr.exception.TessException;
import javafx.scene.control.ProgressBar;
import org.opencv.core.Mat;

public interface OCRService {

    void apiInit() throws TessException;

    String doOCR(Mat mat);

    Integer doOCR(ProgressBar jfxProgressBar);

    boolean isReady();
}
