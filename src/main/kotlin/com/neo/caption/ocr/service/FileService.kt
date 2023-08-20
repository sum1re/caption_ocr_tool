package com.neo.caption.ocr.service

import com.neo.caption.ocr.domain.dto.FileChecksumDto
import com.neo.caption.ocr.domain.dto.FileChunkDto
import com.neo.caption.ocr.domain.vo.SavedFileVo
import com.neo.caption.ocr.domain.vo.SavedDirVo
import org.opencv.videoio.VideoCapture

interface FileService {

    fun createWorkingDirectory(): SavedDirVo

    fun saveFileChunk(fileChunkDto: FileChunkDto): SavedFileVo

    fun combineFileChunk(fileChecksumDto: FileChecksumDto): SavedFileVo

    fun openVideoFile(identify: String): VideoCapture

}