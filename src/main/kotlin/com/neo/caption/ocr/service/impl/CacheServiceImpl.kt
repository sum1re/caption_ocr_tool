package com.neo.caption.ocr.service.impl

import com.neo.caption.ocr.annotation.Slf4j
import com.neo.caption.ocr.constant.CachePrefixEnum
import com.neo.caption.ocr.service.CacheService
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Slf4j
@Service
@CacheConfig(cacheNames = ["cocr"])
class CacheServiceImpl : CacheService {

    /**
     * Data is fetched from cache.
     */
    @Cacheable(key = "#prefix.name() + ':' + #key")
    override fun <T> getCache(prefix: CachePrefixEnum, key: String): T? {
        return null
    }

    /**
     * Put data to cache.
     */
    @CachePut(key = "#prefix.name() + ':' + #key")
    override fun <T> putCache(prefix: CachePrefixEnum, key: String, data: T) = data

    /**
     * Delete cache.
     */
    @CacheEvict(key = "#prefix.name() + ':' + #key")
    override fun removeCache(prefix: CachePrefixEnum, key: String) {}

}