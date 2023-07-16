package com.neo.caption.ocr.domain.vo

data class TaskScheduleVo(
    val current: Int = 0,
    val isFinished: Boolean = false
) : BaseVo()
