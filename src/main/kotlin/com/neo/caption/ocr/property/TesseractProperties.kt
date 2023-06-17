package com.neo.caption.ocr.property

import com.neo.caption.ocr.constant.OCREngineModeEnum
import com.neo.caption.ocr.constant.PageSegModeEnum
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cocr.tesseract")
data class TesseractProperties(
    val ocrEngineMode: OCREngineModeEnum,
    val pageSegMode: PageSegModeEnum,
    val language: List<String>,
    val vectors: List<TesseractVector>,
) {
    data class TesseractVector(val name: String, val value: String)

    fun linkedHashMap() = LinkedHashMap<String, String>(vectors.size).apply {
        vectors.forEach { this[it.name] = it.value }
    }

}