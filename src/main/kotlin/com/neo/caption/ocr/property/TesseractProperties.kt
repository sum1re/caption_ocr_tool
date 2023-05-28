package com.neo.caption.ocr.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cocr.tesseract")
data class TesseractProperties(
    /**
     * 0    Legacy engine only.<br/>
     * 1    Neural nets LSTM engine only. (Default)<br/>
     * 2    Legacy + LSTM engines.<br/>
     * 3    Based on what is available.
     */
    val ocrEngineMode: Int,
    /**
     * 0    Orientation and script detection (OSD) only.<br/>
     * 1    Automatic page segmentation with OSD.<br/>
     * 2    Automatic page segmentation, but no OSD, or OCR. (not implemented)<br/>
     * 3    Fully automatic page segmentation, but no OSD.<br/>
     * 4    Assume a single column of text of variable sizes.<br/>
     * 5    Assume a single uniform block of vertically aligned text.<br/>
     * 6    Assume a single uniform block of text.<br/>
     * 7    Treat the image as a single text line. (Default)<br/>
     * 8    Treat the image as a single word.<br/>
     * 9    Treat the image as a single word in a circle.<br/>
     * 10   Treat the image as a single character.<br/>
     * 11   Sparse text. Find as much text as possible in no particular order.<br/>
     * 12   Sparse text with OSD.<br/>
     * 13   Raw line. Treat the image as a single text line.
     */
    val pageSegMode: Int,
    val supportedLanguage: String,
    val configs: List<Config>,
) {
    data class Config(val name: String, val value: String)
}