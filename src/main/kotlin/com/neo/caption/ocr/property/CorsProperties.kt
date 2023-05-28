package com.neo.caption.ocr.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cocr.cors")
data class CorsProperties(
    val originPatterns: List<String>,
    val allowedHeader: List<String>,
    val allowedMethods: List<String>
)
