package com.neo.caption.ocr.common

import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging
import org.springframework.core.annotation.AliasFor
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
        @Suppress("UnusedReceiverParameter")
        val <reified T> T.log: KLogger
            inline get() = KotlinLogging.logger(T::class.java.name)
    }

}