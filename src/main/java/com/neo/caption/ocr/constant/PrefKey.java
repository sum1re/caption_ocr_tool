package com.neo.caption.ocr.constant;

public enum PrefKey {

    // int data
    MIN_PIXEL_COUNT,
    SIMILARITY_TYPE,
    STORAGE_POLICY,
    COUNT_PRE_PAGE,
    FRAME_INTERVAL,
    EDITOR_FONT_SIZE,
    BACKGROUND_OPACITY,

    // double data
    MIN_SSIM_THRESHOLD,
    MIN_PSNR_THRESHOLD,

    // string data
    FILE_CHOOSE_DIR,
    DEFAULT_STYLE,
    TESS_LANG,
    BACKGROUND_IMAGE,
    MODULE_PROFILE_NAME,

    // boolean data
    DARK_THEME,
    COMPRESS_IMAGE,

    // other data
    MODULE_PROFILE_STATUS_LIST,
    MODULE_PROFILE_DEFAULT,

    /**
     * Read only
     */
    @Deprecated MODULE_PROFILE_INDEX,
    @Deprecated MODULE_PROFILE_FIXED_BINARY,
    @Deprecated MODULE_PROFILE_ADAPTIVE_BINARY,
    @Deprecated MODULE_PROFILE_HLS_COLOR,
    @Deprecated MODULE_PROFILE_HSV_COLOR,
    @Deprecated MODULE_PROFILE_CUSTOMIZE,
    @Deprecated DIGITAL_CONTAINER_FORMAT,

    /**
     * Has been replaced with MODULE_PROFILE_CUSTOMIZE.
     */
    @Deprecated MODULE_STATUS_LIST,

    ;

    private Object value;

    public Object value() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String stringValue() {
        return (String) value;
    }

    public int intValue() {
        return (int) value;
    }

    public double doubleValue() {
        return (double) value;
    }

    public boolean booleanValue() {
        return (boolean) value;
    }

    public final String toLowerCase() {
        return this.name().toLowerCase();
    }
}
