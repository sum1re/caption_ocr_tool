package com.neo.caption.ocr.controller

import com.neo.caption.ocr.annotation.RestEntityController
import com.neo.caption.ocr.domain.dto.FileChecksumDto
import com.neo.caption.ocr.domain.dto.FileChunkDto
import com.neo.caption.ocr.domain.vo.BaseVo
import com.neo.caption.ocr.domain.vo.RestVo
import com.neo.caption.ocr.service.AppInfoService
import com.neo.caption.ocr.service.FileService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@RestEntityController("/api")
class ApiController(
    private val appInfoService: AppInfoService,
    private val fileService: FileService
) {

    @GetMapping("/v1/info")
    fun getAppInfo() = appInfoService.getInfo().toRestVo()

    @GetMapping("/v1/file")
    fun initialFile() = fileService.createWorkingDirectory().toRestVo()

    @PutMapping("/v1/file/{identity}/{index}")
    fun uploadFileChunk(
        @PathVariable identity: String,
        @PathVariable index: Int,
        @RequestPart multipartFile: MultipartFile
    ) =
        FileChunkDto(identity, index, multipartFile).let { fileService.saveFileChunk(it) }.toRestVo()

    @PatchMapping("/v1/file")
    fun combineFileChunk(fileChecksumDto: FileChecksumDto) =
        fileService.combineFileChunk(fileChecksumDto).toRestVo()

    private fun <T : BaseVo> T.toRestVo() = RestVo(this)

}