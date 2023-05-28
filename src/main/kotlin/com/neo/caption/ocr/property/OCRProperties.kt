package com.neo.caption.ocr.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cocr.ocr")
data class OCRProperties(
    val minBlackPixelThreshold: Double,
    val maxWhitePixelThreshold: Double,
    val ssimThreshold: Double,
    val invertThreshold: Double,
)
