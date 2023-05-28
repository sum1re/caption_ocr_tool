package com.neo.caption.ocr.service

import com.neo.caption.ocr.domain.vo.AppInfoVo

interface AppInfoService {

    fun getInfo(): AppInfoVo

}