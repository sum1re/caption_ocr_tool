package com.neo.caption.ocr.domain.dto

data class FileChecksumDto(
    val hash: String,
    val extension: String,
    val fileChunkName: List<String>
)