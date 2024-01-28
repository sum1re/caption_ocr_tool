package com.neo.caption.ocr.domain

import com.neo.caption.ocr.common.Slf4j
import jakarta.annotation.PostConstruct
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Component

@Slf4j
@Component
class InnerExtension(
    val conversionService: ConversionService
) {
    @PostConstruct
    private fun init() {
        instant = this
    }
}

lateinit var instant: InnerExtension

inline fun <reified T> BaseData.convert(): T = instant.conversionService.convert(this, T::class.java)!!

fun response(action: () -> Any?): RestVo<Any?> =
    when (val result = action()) {
        is Collection<*> -> RestVo(result)
        else -> RestVo(result)
    }