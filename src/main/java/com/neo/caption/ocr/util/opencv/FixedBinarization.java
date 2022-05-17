package com.neo.caption.ocr.util.opencv;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public record FixedBinarization(double threshold, int type) implements Process {

    @Override
    public void process(Mat mat) {
        Imgproc.threshold(mat, mat, threshold(), 255, type());
    }

}
