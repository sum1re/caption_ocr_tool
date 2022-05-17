package com.neo.caption.ocr.util.opencv;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public record Morphology(
        int morphShape, Size kernelSize, Point elementAnchorPoint, int morphologyType,
        Point anchorPoint, int i, int borderType) implements Process {

    @Override
    public void process(Mat mat) {
        var kernel = Imgproc.getStructuringElement(morphShape(), kernelSize(), elementAnchorPoint());
        Imgproc.morphologyEx(mat, mat, morphologyType(), kernel, anchorPoint(), i(), borderType());
    }

}
