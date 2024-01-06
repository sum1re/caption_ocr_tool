package com.neo.caption.ocr.module.tesseract

import com.neo.caption.ocr.common.RestEntityController
import com.neo.caption.ocr.domain.convert
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@RestEntityController("/api/tesseract")
class TesseractController(
    private val appTesseractService: AppTesseractService
) {

    @GetMapping
    fun get() = appTesseractService.appTesseractConfig.convert<TesseractConfigDto>()

    @PostMapping("reset")
    fun reset() = appTesseractService.resetConfig().convert<TesseractConfigDto>()

    @PostMapping
    fun update(tesseractConfigDto: TesseractConfigDto) =
        appTesseractService.updateTesseractConfig(tesseractConfigDto).convert<TesseractConfigDto>()

}