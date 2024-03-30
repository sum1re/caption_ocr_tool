package com.neo.caption.ocr

import com.neo.caption.ocr.common.Slf4j
import com.neo.caption.ocr.common.Slf4j.Companion.log
import com.neo.caption.ocr.service.LoaderService
import jakarta.annotation.PostConstruct
import org.bytedeco.opencv.opencv_java
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan("com.neo.caption.ocr.common")
@Slf4j
class COCRApplication(
    private val loaderService: LoaderService,
    private val buildProperties: BuildProperties
) : InitializingBean {

    @PostConstruct
    fun init() {
        loaderService.loadLib(opencv_java::class.java)
    }

    override fun afterPropertiesSet() {
        val system = loaderService.systemInformation()
        log.debug { "System: [$system]" }
        log.debug { "JavaCPP: [${loaderService.javacpp()}]" }
        log.info {
            """
                |Caption OCR Tool service (v${buildProperties.version}) is ready!
                |Open http://${system.ip}:${system.port} or http://127.0.0.1:${system.port} in your browser.
            """.trimMargin("|")
        }
    }

}

fun main(args: Array<String>) {
    runApplication<COCRApplication>(*args)
}
