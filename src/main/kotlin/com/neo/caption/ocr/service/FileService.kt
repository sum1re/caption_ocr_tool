package com.neo.caption.ocr.service

import com.neo.caption.ocr.domain.entity.FileChecksum
import com.neo.caption.ocr.domain.entity.FileChunk
import com.neo.caption.ocr.domain.vo.SavedFileVo
import org.opencv.videoio.VideoCapture

interface FileService {

    fun createWorkingDirectory(): String

    fun saveFileChunk(fileChunk: FileChunk): SavedFileVo

    fun combineFileChunk(fileChecksum: FileChecksum): SavedFileVo

    fun openVideoFile(projectId: String): VideoCapture

}