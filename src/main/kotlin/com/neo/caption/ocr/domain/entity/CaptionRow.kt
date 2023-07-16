package com.neo.caption.ocr.domain.entity

import org.opencv.core.Mat

data class CaptionRow(
    val start: Int,
    val end: Int,
    val mat: Mat,
    val caption: String
)