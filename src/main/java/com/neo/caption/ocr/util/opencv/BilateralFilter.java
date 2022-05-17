package com.neo.caption.ocr.util.opencv;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public record BilateralFilter(
        int diameter, double sigmaColor, double sigmaSpace, int borderType) implements Process {

    @Override
    public void process(Mat mat) {
        var src = mat.clone();
        Imgproc.bilateralFilter(src, mat, diameter(), sigmaColor(), sigmaSpace(), borderType());
        src.release();
    }

}
