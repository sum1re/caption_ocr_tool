package com.neo.caption.ocr.module.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.neo.caption.ocr.common.CommonService
import org.springframework.boot.info.BuildProperties
import org.springframework.cache.annotation.Cacheable

@CommonService
class AppService(
    private val buildProperties: BuildProperties,
    private val objectMapper: ObjectMapper
) {
    @Cacheable(key = "'app:info'")
    fun getAppInfoDto(): AppInfoDto = AppInfoDto(
        name = buildProperties.name,
        appLicense = buildProperties["license"],
        version = buildProperties.version,
        buildTimestamp = buildProperties.time.epochSecond.toString(),
    )
}