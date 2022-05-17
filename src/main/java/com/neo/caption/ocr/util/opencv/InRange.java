package com.neo.caption.ocr.util.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public record InRange(
        int low1, int low2, int low3, int up1, int up2, int up3) implements Process {

    @Override
    public void process(Mat mat) {
        var lowerScalar = new Scalar(low1, low2, low3);
        var upperScalar = new Scalar(up1, up2, up3);
        Core.inRange(mat, lowerScalar, upperScalar, mat);
    }

}
