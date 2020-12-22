package com.neo.caption.ocr.service;

import com.neo.caption.ocr.exception.ModuleException;
import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.List;
import java.util.Map;

public interface OpenCVService {

    List<Mat> spliceMatList(int size);

    byte[] mat2ByteArrayByGet(Mat mat);

    Image mat2Image(Mat mat, boolean isCompressImage);

    byte[] mat2ByteArrayByMatOfByte(Mat mat);

    Map<String, Integer> getPixelColor(int x, int y);

    void setVideoOriMat(Mat mat);

    Mat replaceRoiImage(Mat mat) throws ModuleException;

    Mat filter(Mat mat) throws ModuleException;

    int countBlackPixel(Mat mat);

    int countWhitePixel(Mat mat);

    double psnr(Mat I1, Mat I2);

    Scalar meanSSIM(Mat i1, Mat i2);
}
