package com.neo.caption.ocr.module.app

import com.neo.caption.ocr.common.RestEntityController
import com.neo.caption.ocr.domain.BaseDto
import com.neo.caption.ocr.domain.response
import org.springframework.boot.info.BuildProperties
import org.springframework.web.bind.annotation.GetMapping

data class AppInfoDto(
    val name: String,
    val appLicense: String,
    val version: String,
    val buildTimestamp: String,
) : BaseDto

@RestEntityController("/api/info")
class AppInfoController(
    private val buildProperties: BuildProperties
) {
    @GetMapping
    fun info() = response {
        AppInfoDto(
            name = buildProperties.name,
            appLicense = buildProperties["license"],
            version = buildProperties.version,
            buildTimestamp = buildProperties.time.epochSecond.toString(),
        )
    }
}