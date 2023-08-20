package com.neo.caption.ocr.domain.entity

import org.opencv.core.Mat

data class CaptionRow(
    var start: Int,
    var end: Int,
    val mat: Mat,
    var caption: String
) {
    fun clone() = this.copy(mat = Mat().also { this.mat.copyTo(it) })
}