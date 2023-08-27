package com.neo.caption.ocr.domain.vo

import com.neo.caption.ocr.constant.OCREngineModeEnum
import com.neo.caption.ocr.constant.PageSegModeEnum

data class TesseractOptionVo(
    val oemModeList: List<OCREngineModeEnum>,
    val psmModeList: List<PageSegModeEnum>,
    val supportedLanguage: List<String>,
    val vectorKeyList: List<String>
) : BaseVo()
