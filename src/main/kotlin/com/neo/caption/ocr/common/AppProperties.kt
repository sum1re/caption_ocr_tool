package com.neo.caption.ocr.common

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.nio.file.Paths

@ConfigurationProperties(prefix = "cocr.cors")
data class CorsProperties(
    val originPatterns: List<String>,
    val allowedHeader: List<String>,
    val allowedMethods: List<String>,
)

@ConfigurationProperties(prefix = "cocr.common")
data class CommonProperties(
    val workingDirectory: Path = Paths.get(System.getProperty("user.home"), "cocr"),
    val storeDirectory: Path = workingDirectory.resolve("store"),
    val uploadExpirationPeriod: Long = 86_400_000L, // 1 day
    val uploadMaxSize: Long = 1_073_741_824, // 1 GiB
)

@Component
@ConfigurationPropertiesBinding
class StringToPathConverter : Converter<String, Path> {
    override fun convert(source: String): Path = Paths.get(source)
}