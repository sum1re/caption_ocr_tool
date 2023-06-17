package com.neo.caption.ocr.constant

enum class OCREngineModeEnum(val code: Int) {

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