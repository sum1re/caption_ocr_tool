package com.neo.caption.ocr.module.app

import com.neo.caption.ocr.domain.BaseDto

data class AppInfoDto(
    val name: String,
    val appLicense: String,
    val version: String,
    val buildTimestamp: String,
) : BaseDto
