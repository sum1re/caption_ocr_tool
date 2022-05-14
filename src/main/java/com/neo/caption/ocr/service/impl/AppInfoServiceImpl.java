package com.neo.caption.ocr.service.impl;

import com.neo.caption.ocr.domain.dto.AppInfoDto;
import com.neo.caption.ocr.property.AppInfoProperties;
import com.neo.caption.ocr.service.AppInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@CacheConfig(cacheNames = "app-info")
public class AppInfoServiceImpl implements AppInfoService {

    private final AppInfoProperties appInfoProperties;

    @Override
    @Cacheable
    public AppInfoDto getInfo() {
        return new AppInfoDto(
                appInfoProperties.getName(),
                appInfoProperties.getAppLicense(),
                appInfoProperties.getVersion(),
                appInfoProperties.getBuildTimestamp());
    }

}
