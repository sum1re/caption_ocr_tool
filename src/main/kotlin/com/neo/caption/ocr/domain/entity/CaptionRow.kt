package com.neo.caption.ocr.domain.entity

data class CaptionRow(
    val start: Int,
    val end: Int,
    val caption: String
)
