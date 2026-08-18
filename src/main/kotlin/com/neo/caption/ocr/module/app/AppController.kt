package com.neo.caption.ocr.module.app

import com.neo.caption.ocr.common.RestEntityController
import com.neo.caption.ocr.support.response
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@RestEntityController("/api/app")
class AppController(
    private val appService: AppService,
) {
    @GetMapping("/info")
    fun info() = response {
        appService.getAppInfoDto()
    }

    @GetMapping("/config")
    fun config() = response {}

    @PutMapping("/config")
    fun updateConfig(@RequestBody config: AppConfig) = response {
    }
}