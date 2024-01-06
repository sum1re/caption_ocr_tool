package com.neo.caption.ocr.module.app

import com.neo.caption.ocr.common.AppInfoProperties
import com.neo.caption.ocr.common.RestEntityController
import com.neo.caption.ocr.domain.BaseDto
import com.neo.caption.ocr.domain.convert
import com.neo.caption.ocr.domain.response
import com.neo.caption.ocr.service.LoaderService
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping

data class AppInfoDto(
    val name: String,
    val appLicense: String,
    val version: String,
    val buildTimestamp: String,
) : BaseDto

@Component
@CacheConfig(cacheNames = ["system"])
class AppInfoConverter : Converter<AppInfoProperties, AppInfoDto> {
    @Cacheable("cocr")
    override fun convert(source: AppInfoProperties) = AppInfoDto(
        name = source.name,
        appLicense = source.appLicense,
        version = source.version,
        buildTimestamp = source.buildTimestamp,
    )
}

@RestEntityController("/api/info")
class AppInfoController(
    private val loaderService: LoaderService
) {
    @GetMapping
    fun info() = response { loaderService.captionOCRTool().convert<AppInfoDto>() }
}