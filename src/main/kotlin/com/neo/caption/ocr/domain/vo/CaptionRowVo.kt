package com.neo.caption.ocr.domain.vo

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.neo.caption.ocr.handler.TimelineSerialize
import java.math.BigDecimal

data class CaptionRowVo(
    @JsonSerialize(using = TimelineSerialize::class)
    val start: BigDecimal,
    @JsonSerialize(using = TimelineSerialize::class)
    val end: BigDecimal,
    val caption: String,
) : BaseVo()
