package com.neo.caption.ocr.service.impl

import com.neo.caption.ocr.domain.vo.AppInfoVo
import com.neo.caption.ocr.property.AppInfoProperties
import com.neo.caption.ocr.service.AppInfoService
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
@CacheConfig(cacheNames = ["app-info"])
class AppInfoServiceImpl(
    val appInfoProperties: AppInfoProperties
) : AppInfoService {

    @Cacheable
    override fun getInfo(): AppInfoVo {
        return AppInfoVo(
            appInfoProperties.name,
            appInfoProperties.appLicense,
            appInfoProperties.version,
            appInfoProperties.buildTimestamp
        )
    }

}