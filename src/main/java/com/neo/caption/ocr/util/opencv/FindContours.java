package com.neo.caption.ocr.util.opencv;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public record FindContours(List<MatOfPoint> contours, Mat hierarchy, int mode, int method) implements Process {

    @Override
    public void process(Mat mat) {
        Imgproc.findContours(mat, contours(), hierarchy(), mode(), method());
    }

}
