package com.neo.caption.ocr.annotation

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