package com.neo.caption.ocr.domain.dto

data class FileChecksumDto(
    val hash: String,
    val identity: String,
    val extension: String,
    val fileChunkName: List<String>
)