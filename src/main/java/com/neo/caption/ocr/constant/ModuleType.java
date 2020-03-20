package com.neo.caption.ocr.constant;

public enum ModuleType {

    CROP,

    CVT_COLOR,
    CVT_DEPTH,

    BILATERAL_FILTER,
    BOX_FILTER,
    GAUSSIAN_BLUR,
    MEDIAN_BLUR,

    EQUALIZE_HIST,

    ADAPTIVE_BINARIZATION,
    FIXED_BINARIZATION,

    MORPHOLOGY,

    HLS_IN_RANGE,
    HSV_IN_RANGE,

    ARITHMETIC_OPERATION,
    INVERT_BINARIZATION,
    MAX_CCL,
    MIN_CCL,

    ;

    public final String toLowerCase() {
        return this.name().toLowerCase();
    }

}
