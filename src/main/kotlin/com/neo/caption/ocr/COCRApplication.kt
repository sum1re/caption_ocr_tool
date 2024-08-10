package com.neo.caption.ocr

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan("com.neo.caption.ocr.common")
class COCRApplication

fun main(args: Array<String>) {
    runApplication<COCRApplication>(*args)
}
