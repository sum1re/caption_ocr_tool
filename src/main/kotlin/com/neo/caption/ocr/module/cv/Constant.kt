package com.neo.caption.ocr.module.cv

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.imgproc.Imgproc

enum class AdaptiveMethod(val value: Int) {
    ADAPTIVE_THRESH_MEAN_C(Imgproc.ADAPTIVE_THRESH_MEAN_C),
    ADAPTIVE_THRESH_GAUSSIAN_C(Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C)
}

enum class ThresholdType(val value: Int) {
    THRESH_BINARY(Imgproc.THRESH_BINARY),
    THRESH_BINARY_INV(Imgproc.THRESH_BINARY_INV)
}

enum class Operation {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    ABS_DIFF,
    BITWISE_AND,
    BITWISE_OR,
    BITWISE_NOT,
    BITWISE_XOR,
    MAX,
    MIN
}

enum class Border(val value: Int) {
    BORDER_CONSTANT(Core.BORDER_CONSTANT),
    BORDER_REPLICATE(Core.BORDER_REPLICATE),
    BORDER_REFLECT(Core.BORDER_REFLECT),
    BORDER_WRAP(Core.BORDER_WRAP),
    BORDER_REFLECT_101(Core.BORDER_REFLECT_101),
    BORDER_TRANSPARENT(Core.BORDER_TRANSPARENT),
    BORDER_DEFAULT(Core.BORDER_DEFAULT),
    BORDER_ISOLATED(Core.BORDER_ISOLATED)
}

enum class ColorType(val value: Int) {
    COLOR_BGR2GRAY(Imgproc.COLOR_RGB2GRAY),
    COLOR_BGR2HLS(Imgproc.COLOR_BGR2HLS),
    COLOR_BGR2HLS_FULL(Imgproc.COLOR_BGR2HLS_FULL),
    COLOR_BGR2HSV(Imgproc.COLOR_BGR2HSV),
    COLOR_BGR2HSV_FULL(Imgproc.COLOR_BGR2HSV_FULL),
    COLOR_GRAY2BGR(Imgproc.COLOR_GRAY2BGR),
    COLOR_HLS2BGR(Imgproc.COLOR_HLS2BGR),
    COLOR_HLS2BGR_FULL(Imgproc.COLOR_HLS2BGR_FULL),
    COLOR_HSV2BGR(Imgproc.COLOR_HSV2BGR),
    COLOR_HSV2BGR_FULL(Imgproc.COLOR_HSV2BGR_FULL)
}

enum class Depth(val value: Int) {
    CV_8S(CvType.CV_8S),
    CV_8U(CvType.CV_8U),
    CV_16F(CvType.CV_16F),
    CV_16S(CvType.CV_16S),
    CV_16U(CvType.CV_16U),
    CV_32F(CvType.CV_32F),
    CV_32S(CvType.CV_32S),
    CV_64F(CvType.CV_64F)
}

enum class MorphType(val value: Int) {
    MORPH_ERODE(Imgproc.MORPH_ERODE),
    MORPH_DILATE(Imgproc.MORPH_DILATE),
    MORPH_OPEN(Imgproc.MORPH_OPEN),
    MORPH_CLOSE(Imgproc.MORPH_CLOSE),
    MORPH_GRADIENT(Imgproc.MORPH_GRADIENT),
    MORPH_TOPHAT(Imgproc.MORPH_TOPHAT),
    MORPH_BLACKHAT(Imgproc.MORPH_BLACKHAT),
    MORPH_HITMISS(Imgproc.MORPH_HITMISS)
}

enum class MorphShape(val value: Int) {
    MORPH_RECT(Imgproc.MORPH_RECT), MORPH_CROSS(Imgproc.MORPH_CROSS), MORPH_ELLIPSE(Imgproc.MORPH_ELLIPSE)
}