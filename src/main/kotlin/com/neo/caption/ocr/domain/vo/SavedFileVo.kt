package com.neo.caption.ocr.domain.vo

data class SavedFileVo(
    val name: String,
    val size: Long,
    val hash: String,
) : BaseVo()
