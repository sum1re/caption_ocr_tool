package com.neo.caption.ocr.module.ocr

enum class OcrTypeEnum {
    TESSERACT, OCR_SPACE
}

enum class TesseractEngineEnum(val code: Int) {

    /**
     * run tesseract only, fastest
     */
    @Deprecated(message = "deprecated by tesseract", replaceWith = ReplaceWith("OEM_LSTM_ONLY"))
    OEM_TESSERACT_ONLY(0),

    /**
     * lstm line recognizer
     */
    OEM_LSTM_ONLY(1),

    /**
     * run lstm line recognizer, but allow fallback to Tesseract when things get difficult
     */
    @Deprecated(message = "deprecated by tesseract", replaceWith = ReplaceWith("OEM_LSTM_ONLY"))
    OEM_TESSERACT_LSTM_COMBINED(2),

    /**
     * Specify this mode when calling init, to indicate that any of the above
     * modes should be automatically inferred from the variables in the language-specific config,
     * command-line configs, or if not specified in any of the above should be set
     * to the default OEM_TESSERACT_ONLY
     */
    OEM_DEFAULT(3)

}

/**
 * Tesseract page segmentation
 * @see <a href="https://tesseract-ocr.github.com.io/tessapi/5.x/a00008.html#a4d1f965486ce272064ffdbd7a618234c">TessPageSegMod</a>
 */
enum class TesseractPageSegModeEnum(val code: Int) {

    /**
     * Orientation and script detection (OSD) only.
     */
    PSM_OSD_ONLY(0),

    /**
     * Automatic page segmentation with OSD.
     */
    PSM_AUTO_OSD(1),

    /**
     * Automatic page segmentation, but no OSD, or OCR.
     */
    PSM_AUTO_ONLY(2),

    /**
     * Fully automatic page segmentation, but no OSD.
     */
    PSM_AUTO(3),

    /**
     * Assume a single column of text of variable sizes.
     */
    PSM_SINGLE_COLUMN(4),

    /**
     * Assume a single uniform block of vertically aligned text.
     */
    PSM_SINGLE_BLOCK_VERT_TEXT(5),

    /**
     * Assume a single uniform block of text.
     */
    PSM_SINGLE_BLOCK(6),

    /**
     * Treat the image as a single text line.
     */
    PSM_SINGLE_LINE(7),

    /**
     * Treat the image as a single word.
     */
    PSM_SINGLE_WORD(8),

    /**
     * Treat the image as a single word in a circle.
     */
    PSM_SINGLE_CIRCLE_WORD(9),

    /**
     * Treat the image as a single character.
     */
    PSM_SINGLE_CHAR(10),

    /**
     * Sparse text. Find as much text as possible in no particular order.
     */
    PSM_SPARSE_TEXT(11),

    /**
     * Sparse text with OSD.
     */
    PSM_SPARSE_OSD(12),

    /**
     * Raw line. Treat the image as a single text line, bypassing hacks that are Tesseract-specific.
     */
    PSM_RAW_LINE(13),
    ;

    fun toVector() = "tessedit_pageseg_mode" to this.code.toString()
}

enum class OcrSpaceEngineEnum(val code: Int) {
    ENGINE_1(1), ENGINE_2(2)
}