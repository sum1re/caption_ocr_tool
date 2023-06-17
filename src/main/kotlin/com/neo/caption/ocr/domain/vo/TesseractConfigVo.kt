package com.neo.caption.ocr.domain.vo

import com.neo.caption.ocr.constant.OCREngineModeEnum
import com.neo.caption.ocr.constant.PageSegModeEnum

data class TesseractConfigVo(
    val oemMode: OCREngineModeEnum,
    val psmMode: PageSegModeEnum,
    val language: List<String>,
    val vectors: LinkedHashMap<String, String>
) : BaseVo()
