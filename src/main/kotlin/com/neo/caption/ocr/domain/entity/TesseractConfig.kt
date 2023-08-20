package com.neo.caption.ocr.domain.entity

import org.bytedeco.tesseract.StringVector

data class TesseractConfig(
    val oem: Int,
    val language: String,
    val vectorKey: StringVector,
    val vectorValue: StringVector,
)
