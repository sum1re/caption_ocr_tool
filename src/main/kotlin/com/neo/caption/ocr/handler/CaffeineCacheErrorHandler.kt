package com.neo.caption.ocr.handler

import com.neo.caption.ocr.annotation.Slf4j
import com.neo.caption.ocr.annotation.Slf4j.Companion.log
import org.springframework.cache.Cache
import org.springframework.cache.interceptor.CacheErrorHandler
import java.lang.RuntimeException

@Slf4j
class CaffeineCacheErrorHandler : CacheErrorHandler {

    override fun handleCacheGetError(exception: RuntimeException, cache: Cache, key: Any) {
        log.error { "CacheError [option: GET, key: $key, cause: ${exception.cause}]" }
    }

    override fun handleCachePutError(exception: RuntimeException, cache: Cache, key: Any, value: Any?) {
        log.error { "CacheError [option: PUT, key: $key, value: $value, cause: ${exception.cause}]" }
    }

    override fun handleCacheEvictError(exception: RuntimeException, cache: Cache, key: Any) {
        log.error { "CacheError [option: EVICT, key: $key, cause: ${exception.cause}]" }
    }

    override fun handleCacheClearError(exception: RuntimeException, cache: Cache) {
        log.error { "CacheError [option: CLEAR, cause: ${exception.cause}]" }
    }

}