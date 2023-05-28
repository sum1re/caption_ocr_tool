package com.neo.caption.ocr.domain.vo

data class AppInfoVo(
    val name: String,
    val appLicense: String,
    val version: String,
    val buildTimestamp: String,
) : BaseVo()
