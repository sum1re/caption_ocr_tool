package com.neo.caption.ocr.util.opencv;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public record AdaptiveBinarization(
        int adaptiveMethod, int thresholdType, int blockSize, double constant) implements Process {

    @Override
    public void process(Mat mat) {
        Imgproc.adaptiveThreshold(mat, mat, 255, adaptiveMethod(), thresholdType(), blockSize(), constant());
    }

}
