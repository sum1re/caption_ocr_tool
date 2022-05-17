package com.neo.caption.ocr.util.opencv;

import org.opencv.core.Mat;

public record CvtType(int type) implements Process {

    @Override
    public void process(Mat mat) {
        mat.convertTo(mat, type());
    }

}
