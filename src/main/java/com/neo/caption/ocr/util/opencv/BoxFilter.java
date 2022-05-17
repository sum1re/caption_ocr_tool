package com.neo.caption.ocr.util.opencv;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public record BoxFilter(
        Size kernelSize, Point anchorPoint, boolean normalize, int borderType) implements Process {

    @Override
    public void process(Mat mat) {
        // -1 to use mat.depth()
        Imgproc.boxFilter(mat, mat, -1, kernelSize(), anchorPoint(), normalize(), borderType());
    }

}
