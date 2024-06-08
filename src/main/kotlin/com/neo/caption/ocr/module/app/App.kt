package com.neo.caption.ocr.module.app

import com.neo.caption.ocr.domain.BaseDto
import org.jetbrains.exposed.dao.id.UUIDTable

enum class CompareAlgorithm {
    SSIM, PSNR
}

enum class MatRetentionPolicy {
    FIRST_IN,
    LAST_IN,
    WHITEST, // least black pixels
    BLACKEST, // most black pixels
    BALANCED // medium
}

data class AppInfoDto(
    val name: String,
    val appLicense: String,
    val version: String,
    val buildTimestamp: String,
) : BaseDto


object AppConfigTable : UUIDTable("COCR_CONFIG") {
    val filterInterval = short("FILTER_INTERVAL")
    val compareAlgorithm = customEnumeration(
        name = "COMPARE_ALGORITHM",
        sql = "ENUM(${CompareAlgorithm.entries.joinToString(",") { "'$it'" }})",
        fromDb = { CompareAlgorithm.valueOf(it as String) },
        toDb = { it.name }
    )
    val similarThreshold = text("SIMILAR_THRESHOLD")
    val pixelThreshold = integer("PIXEL_THRESHOLD")
    val matRetentionPolicy = customEnumeration(
        name = "MAT_RETENTION_POLICY",
        sql = "ENUM(${MatRetentionPolicy.entries.joinToString(",") { "'$it'" }})",
        fromDb = { MatRetentionPolicy.valueOf(it as String) },
        toDb = { it.name }
    )
}

data class AppConfig(
    val filterInterval: Int,
    val compareAlgorithm: CompareAlgorithm,
    val similarThreshold: Map<CompareAlgorithm, Number>,
    val pixelThreshold: Int,
    val matRetentionPolicy: MatRetentionPolicy
)