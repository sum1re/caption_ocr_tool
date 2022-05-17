package com.neo.caption.ocr.util.opencv;

import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public record EqualizeHist(int channelIndex) implements Process {

    @Override
    public void process(Mat mat) {
        var list = new ArrayList<Mat>();
        Core.split(mat, list);
        if (list.isEmpty() || channelIndex() >= list.size()) {
            throw new CvException("Failed to split mat or the channelIndex is greater than the number of mat channel");
        }
        if (channelIndex() == -1) {
            for (var matChannel : list) {
                Imgproc.equalizeHist(matChannel, matChannel);
            }
        } else {
            Imgproc.equalizeHist(list.get(channelIndex), list.get(channelIndex));
        }
        Core.merge(list, mat);
    }

}
