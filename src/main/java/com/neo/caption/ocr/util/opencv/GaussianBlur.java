package com.neo.caption.ocr.util.opencv;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public record GaussianBlur(
        Size kernelSize, double sigmaX, double sigmaY, int borderType) implements Process {

    @Override
    public void process(Mat mat) {
        Imgproc.GaussianBlur(mat, mat, kernelSize(), sigmaX(), sigmaY(), borderType());
    }

}
