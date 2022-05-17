package com.neo.caption.ocr.util.opencv;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public record CvtColor(int color) implements Process {

    @Override
    public void process(Mat mat) {
        Imgproc.cvtColor(mat, mat, color());
    }

}
