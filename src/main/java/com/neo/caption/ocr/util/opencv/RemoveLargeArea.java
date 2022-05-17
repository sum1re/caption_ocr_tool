package com.neo.caption.ocr.util.opencv;

import com.neo.caption.ocr.util.OpenCVUtil;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_NONE;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;

public record RemoveLargeArea(double maxArea) implements Process {

    @Override
    public void process(Mat mat) {
        var contours = new ArrayList<MatOfPoint>(4);
        var hierarchy = new Mat();
        var findContours = new FindContours(contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_NONE);
        findContours.process(mat);
        var rectList = new ArrayList<Rect>(contours.size());
        for (var matOfPoint : contours) {
            var area = Imgproc.contourArea(matOfPoint);
            if (area > maxArea()) {
                rectList.add(Imgproc.boundingRect(matOfPoint));
            }
        }
        var roi = new Mat();
        for (var rect : rectList) {
            roi = mat.submat(rect);
            byte[] bytes = OpenCVUtil.mat2ByteArrayByMatOfByte(roi);
            var len = bytes.length;
            for (var i = 0; i < len; i++) {
                bytes[i] = 0;
            }
            roi.put(0, 0, bytes);
            Core.addWeighted(roi, 1, Mat.zeros(roi.rows(), roi.cols(), roi.depth()), 0, 0, roi);
        }
        //Core.bitwise_not(mat, mat);
        OpenCVUtil.release(roi, hierarchy);
    }

}
