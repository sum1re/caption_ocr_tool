package com.neo.caption.ocr.domain.entity

data class CropRange(
    val upperLeftX: Int,
    val upperLeftY: Int,
    val lowerRightX: Int,
    val lowerRightY: Int
)
