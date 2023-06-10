package com.neo.caption.ocr

import com.neo.caption.ocr.service.LoaderService
import jakarta.annotation.PostConstruct
import org.bytedeco.opencv.opencv_java
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan("com.neo.caption.ocr.property")
class COCRApplication(
    private val loaderService: LoaderService
) : InitializingBean {

    @PostConstruct
    fun init() {
        loaderService.loadLib(opencv_java::class.java)
    }

    override fun afterPropertiesSet() {
        loaderService.printSystemInfo()
    }

}

fun main(args: Array<String>) {
    runApplication<COCRApplication>(*args)
}
