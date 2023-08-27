package com.neo.caption.ocr.domain.entity

import org.springframework.web.multipart.MultipartFile

data class FileChunk(val projectId: String, val multipartFile: MultipartFile)