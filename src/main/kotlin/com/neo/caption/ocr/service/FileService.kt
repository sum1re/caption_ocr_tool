package com.neo.caption.ocr.service

import com.neo.caption.ocr.domain.dto.FileChecksumDto
import com.neo.caption.ocr.domain.dto.FileChunkDto
import com.neo.caption.ocr.domain.vo.SavedFileVo
import com.neo.caption.ocr.domain.vo.SavedDirVo
import java.nio.file.Path

interface FileService {

    fun createWorkingDirectory(): SavedDirVo

    fun saveFileChunk(fileChunkDto: FileChunkDto): SavedFileVo

    fun combineFileChunk(fileChecksumDto: FileChecksumDto): SavedFileVo

    fun getVideoFile(identify: String): Path

}