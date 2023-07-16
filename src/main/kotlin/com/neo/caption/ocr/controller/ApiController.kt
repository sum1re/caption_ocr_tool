package com.neo.caption.ocr.controller

import com.neo.caption.ocr.annotation.RestEntityController
import com.neo.caption.ocr.domain.dto.FileChecksumDto
import com.neo.caption.ocr.domain.dto.FileChunkDto
import com.neo.caption.ocr.domain.dto.TaskConfigDto
import com.neo.caption.ocr.domain.mapper.TaskMapper
import com.neo.caption.ocr.domain.vo.BaseVo
import com.neo.caption.ocr.domain.vo.RestVo
import com.neo.caption.ocr.service.AppInfoService
import com.neo.caption.ocr.service.FileService
import com.neo.caption.ocr.service.TaskService
import com.neo.caption.ocr.service.TesseractService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@RestEntityController("/api")
class ApiController(
    private val appInfoService: AppInfoService,
    private val fileService: FileService,
    private val tesseractService: TesseractService,
    private val taskService: TaskService,
    private val taskMapper: TaskMapper,
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

    @GetMapping("/v1/tesseract/language")
    fun getSupportLanguage() = RestVo(tesseractService.supportedLanguage)

    @GetMapping("/v1/tesseract/default-config")
    fun getDefaultConfig() = tesseractService.getDefaultConfig().toRestVo()

    @PostMapping("/v1/task")
    fun initialTask(taskConfigDto: TaskConfigDto) =
        taskService.initTask(taskConfigDto).let { taskMapper.toVo(it) }.toRestVo()

    @GetMapping("/v1/task/result/{taskId}")
    fun getCaptionRow(@PathVariable taskId: String) =
        taskService.getCaptionRowVoList(taskId).toRestVo()

    @GetMapping("/v1/task/schedule/{taskId}")
    fun getSchedule(@PathVariable taskId: String) =
        taskService.getSchedule(taskId).toRestVo()

    @GetMapping("/v1/task/{identity}")
    fun startTask(@PathVariable identity: String): RestVo<Nothing?> {
        taskService.runTask(identity)
        return RestVo(null)
    }

    @DeleteMapping("/v1/task/close/{taskId}")
    fun deleteTask(@PathVariable taskId: String): RestVo<Nothing?> {
        taskService.closeTask(taskId)
        return RestVo(null)
    }

    @DeleteMapping("/v1/task/remove/{identity}")
    fun removeTask(@PathVariable identity: String): RestVo<Nothing?> {
        taskService.removeTask(identity)
        return RestVo(null)
    }

    private fun <T : BaseVo> T.toRestVo() = RestVo(this)

    private fun <T : BaseVo> List<T>.toRestVo() = RestVo(this)

}