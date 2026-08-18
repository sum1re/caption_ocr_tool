package com.neo.caption.ocr.common

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.CacheConfig
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
@RestController
@RequestMapping
@Slf4j
annotation class RestEntityController(
    @get:AliasFor(annotation = RequestMapping::class) val value: String
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Slf4j {

    companion object {
        val <reified T> T.logging: KLogger
            inline get() = KotlinLogging.logger(T::class.java.name)
    }

}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Slf4j
@Service
@CacheConfig(cacheNames = ["cocr"])
@Transactional(rollbackFor = [Exception::class])
annotation class CommonService