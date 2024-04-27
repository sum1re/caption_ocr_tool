package com.neo.caption.ocr.service

import com.neo.caption.ocr.common.CommonService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable

@CommonService
class CacheService {

    @Cacheable(key = "#p0")
    fun <T> get(key: String, value: T): T = value

    @CachePut(key = "#p0")
    fun <T> put(key: String, value: T): T = value

    @CacheEvict(key = "#p0")
    fun evict(key: String) = Unit

}