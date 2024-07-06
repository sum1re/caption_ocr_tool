package com.neo.caption.ocr.common

import com.neo.caption.ocr.module.ocr.TesseractEngineEnum
import com.neo.caption.ocr.module.ocr.TesseractPageSegModeEnum
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID

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
    val ocrEngineMode: TesseractEngineEnum,
    val pageSegMode: TesseractPageSegModeEnum,
    val language: MutableList<String> = mutableListOf(),
    val vectors: MutableList<TesseractVector> = mutableListOf(),
) {
    data class TesseractVector(val name: String, val value: String)
}

@ConfigurationProperties(prefix = "cocr.common")
data class CommonProperties(
    val workingDirectory: Path = Paths.get(System.getProperty("user.home"), "cocr")
)

@Component
@ConfigurationPropertiesBinding
class StringToPathConverter : Converter<String, Path> {
    override fun convert(source: String): Path = Paths.get(source)
}