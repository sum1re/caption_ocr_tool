package com.neo.caption.ocr.property

import org.springframework.boot.context.properties.ConfigurationProperties

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
)