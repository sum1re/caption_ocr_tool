package com.neo.caption.ocr.annotation

import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Slf4j {

    companion object {
        @Suppress("UnusedReceiverParameter")
        val <reified T> T.log: KLogger
            inline get() = KotlinLogging.logger(T::class.java.name)
    }

}
