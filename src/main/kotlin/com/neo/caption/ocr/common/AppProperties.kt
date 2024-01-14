package com.neo.caption.ocr.common

import com.neo.caption.ocr.domain.BaseEntity
import com.neo.caption.ocr.module.tesseract.OCREngineModeEnum
import com.neo.caption.ocr.module.tesseract.PageSegModeEnum
import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.UUID

@ConfigurationProperties(prefix = "cocr.info")
data class AppInfoProperties(
    val artifact: String,
    val group: String,
    val name: String,
    val appLicense: String,
    val javaVersion: String,
    val springBootVersion: String,
    val version: String,
    val buildTimestamp: String,
) : BaseEntity

@ConfigurationProperties(prefix = "cocr.cors")
data class CorsProperties(
    val originPatterns: List<String>,
    val allowedHeader: List<String>,
    val allowedMethods: List<String>
)

@ConfigurationProperties(prefix = "cocr.ocr")
data class OCRProperties(
    val minBlackPixelThreshold: Double,
    val maxWhitePixelThreshold: Double,
    val ssimThreshold: Double,
    val invertThreshold: Double,
)

@ConfigurationProperties(prefix = "cocr.tesseract")
data class TesseractProperties(
    val uuid: UUID,
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
