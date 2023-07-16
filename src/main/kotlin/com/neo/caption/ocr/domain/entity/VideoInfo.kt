package com.neo.caption.ocr.domain.entity

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

data class VideoInfo(
    val width: Int,
    val height: Int,
    val fps: Double,
    val totalFrame: Int,
    val frameDuration: BigDecimal = BigDecimal("1000").divide(
        BigDecimal(fps.toString()),
        MathContext(5, RoundingMode.HALF_EVEN)
    )
)
