package com.neo.caption.ocr.util.opencv;

import org.opencv.core.Mat;

public interface Process {

    void process(Mat mat);

    default Mat cloneProcess(Mat mat) {
        var clone = mat.clone();
        process(clone);
        return clone;
    }

}
