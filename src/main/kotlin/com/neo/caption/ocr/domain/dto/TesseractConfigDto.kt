package com.neo.caption.ocr.domain.dto

import com.neo.caption.ocr.constant.OCREngineModeEnum
import com.neo.caption.ocr.constant.PageSegModeEnum

data class TesseractConfigDto(
    val oemMode: OCREngineModeEnum,
    val psmMode: PageSegModeEnum,
    val language: List<String>,
    val vectors: LinkedHashMap<String, String>
)