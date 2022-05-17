package com.neo.caption.ocr.util.opencv;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public record Rectangle(
        int upperLeftX, int upperLeftY, int lowerRightX, int lowerRightY, Scalar scalar) implements Process {

    @Override
    public void process(Mat mat) {
        var upperLeftPoint = new Point(upperLeftX, upperLeftY);
        var lowerRightPoint = new Point(lowerRightX, lowerRightY);
        Imgproc.rectangle(mat, upperLeftPoint, lowerRightPoint, scalar());
    }

}
