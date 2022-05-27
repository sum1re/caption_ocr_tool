package com.neo.caption.ocr.service.impl;

import com.neo.caption.ocr.constant.CacheKeyPrefix;
import com.neo.caption.ocr.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@CacheConfig(cacheNames = "cocr-cache")
public class CacheServiceImpl implements CacheService {

    /**
     * Data is fetched from cache.
     */
    @Override
    @Cacheable(key = "#prefix.name() + ':' + #key")
    public <T> T getCache(CacheKeyPrefix prefix, String key) {
        return null;
    }

    /**
     * Put data to cache.
     */
    @Override
    @CachePut(key = "#prefix.name() + ':' + #key")
    public <T> T putCache(CacheKeyPrefix prefix, String key, T object) {
        return object;
    }

    /**
     * Delete cache.
     */
    @Override
    @CacheEvict(key = "#prefix.name() + ':' + #key")
    public void removeCache(CacheKeyPrefix prefix, String key) {
    }

}
