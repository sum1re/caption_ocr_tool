package com.neo.caption.ocr.util.opencv;

import org.opencv.core.CvException;
import org.opencv.core.Mat;

public record Crop(
        int upperLeftX, int upperLeftY, int lowerRightX, int lowerRightY) implements Process {

    /**
     * Crop must return a new mat, this method is deprecated.
     *
     * @deprecated Should not invoke the method, it will throw a runtime exception.
     * <p> Use {@link Crop#cloneProcess(Mat)} to replace.
     */
    @Override
    @Deprecated
    public void process(Mat mat) {
        throw new RuntimeException("Should not invoke the method, use cloneProcess to replace.");
    }

    @Override
    public Mat cloneProcess(Mat mat) {
        return mat.submat(upperLeftY(), lowerRightY(), upperLeftX(), lowerRightX());
    }

    public void validateParam(Mat mat) {
        if (lowerRightX() <= upperLeftX() || Math.abs(lowerRightX - upperLeftX) > mat.cols()) {
            throw new CvException("lowerRightX <= upperLeftX && range less than mat.cols()");
        }
        if (lowerRightY() <= upperLeftY() || Math.abs(lowerRightY - upperLeftY) > mat.rows()) {
            throw new CvException("lowerRightY <= upperLeftY && range less than mat.cols()");
        }
    }

}
