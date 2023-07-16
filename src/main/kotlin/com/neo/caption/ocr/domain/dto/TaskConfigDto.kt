package com.neo.caption.ocr.domain.dto

data class TaskConfigDto(
    val identity: String,
    val tesseractConfigDto: TesseractConfigDto,
    val cropRangeDto: CropRangeDto,
)
