package com.neo.caption.ocr.controller

import com.neo.caption.ocr.annotation.RestEntityController
import com.neo.caption.ocr.domain.vo.BaseVo
import com.neo.caption.ocr.domain.vo.RestVo
import com.neo.caption.ocr.service.AppInfoService
import org.springframework.web.bind.annotation.GetMapping

@RestEntityController("/api")
class ApiController(
    private val appInfoService: AppInfoService
) {

    @GetMapping("/info")
    fun getAppInfo() = appInfoService.getInfo().toRestVo()

    private fun <T : BaseVo> T.toRestVo() = RestVo(this)

}