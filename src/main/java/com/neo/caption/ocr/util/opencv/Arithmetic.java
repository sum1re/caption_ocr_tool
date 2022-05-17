package com.neo.caption.ocr.util.opencv;

import org.jetbrains.annotations.NotNull;
import org.opencv.core.Core;
import org.opencv.core.Mat;

public record Arithmetic(Mat dst) {

    public void add(Mat src) {
        Core.add(src, dst(), src);
    }

    @NotNull
    public Mat cloneAdd(Mat src) {
        var mat = new Mat();
        Core.add(src, dst(), mat);
        return mat;
    }

    public void subtract(Mat src) {
        Core.subtract(src, dst(), src);
    }

    public void multiply(Mat src) {
        Core.multiply(src, dst(), src);
    }

    public void divide(Mat src) {
        Core.divide(src, dst(), src);
    }

    public void absDiff(Mat src) {
        Core.absdiff(src, dst(), src);
    }

    public void bitwiseNOT(Mat src) {
        Core.bitwise_not(src, dst());
    }

    public void bitwiseAND(Mat src) {
        Core.bitwise_and(src, dst(), src);
    }

    public void bitwiseOR(Mat src) {
        Core.bitwise_or(src, dst(), src);
    }

    public void bitwiseXOR(Mat src) {
        Core.bitwise_xor(src, dst(), src);
    }

    public void max(Mat src) {
        Core.max(src, dst(), src);
    }

    public void min(Mat src) {
        Core.min(src, dst(), src);
    }

}
