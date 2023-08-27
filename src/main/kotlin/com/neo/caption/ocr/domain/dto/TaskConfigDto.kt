package com.neo.caption.ocr.domain.dto

data class TaskConfigDto(
    val tesseractConfigDto: TesseractConfigDto,
    val cropRangeDto: CropRangeDto,
)
