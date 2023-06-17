package com.neo.caption.ocr.constant

/**
 * Tesseract page segmentation
 * @see <a href="https://tesseract-ocr.github.com.io/tessapi/5.x/a00008.html#a4d1f965486ce272064ffdbd7a618234c">TessPageSegMod</a>
 */
enum class PageSegModeEnum(val code: Int) {

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

}