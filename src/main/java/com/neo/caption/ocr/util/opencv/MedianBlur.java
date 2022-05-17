package com.neo.caption.ocr.util.opencv;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public record MedianBlur(int kernelSize) implements Process {

    @Override
    public void process(Mat mat) {
        Imgproc.medianBlur(mat, mat, kernelSize);
    }

}
