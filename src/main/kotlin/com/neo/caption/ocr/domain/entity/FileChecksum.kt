package com.neo.caption.ocr.domain.entity

data class FileChecksum(
    val hash: String,
    val projectId: String,
    val extension: String,
    val fileChunkName: List<String>
)
