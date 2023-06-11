package com.neo.caption.ocr.domain.dto

import org.springframework.web.multipart.MultipartFile

data class FileChunkDto(val identity: String, val index: Int, val multipartFile: MultipartFile)