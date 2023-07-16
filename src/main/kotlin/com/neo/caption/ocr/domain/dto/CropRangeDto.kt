package com.neo.caption.ocr.domain.dto

data class CropRangeDto(
    val upperLeftX: Int,
    val upperLeftY: Int,
    val lowerRightX: Int,
    val lowerRightY: Int
)