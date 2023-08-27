package com.neo.caption.ocr.domain.entity

data class TaskConfig(
    val projectId: String = "",
    val taskId: String = "",
    val videoAbsolutePath: String = "",
    val cropRange: CropRange? = null,
    val videoInfo: VideoInfo? = null,
    val tesseractConfig: TesseractConfig? = null,
)
