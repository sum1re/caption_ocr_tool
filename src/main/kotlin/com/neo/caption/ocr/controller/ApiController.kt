package com.neo.caption.ocr.controller

import com.neo.caption.ocr.annotation.RestEntityController
import com.neo.caption.ocr.domain.dto.FileChecksumDto
import com.neo.caption.ocr.domain.dto.TaskConfigDto
import com.neo.caption.ocr.domain.entity.FileChunk
import com.neo.caption.ocr.domain.toEntity
import com.neo.caption.ocr.domain.toVo
import com.neo.caption.ocr.domain.vo.BaseVo
import com.neo.caption.ocr.domain.vo.RestVo
import com.neo.caption.ocr.service.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestEntityController("/api")
class ApiController(
    private val appInfoService: AppInfoService,
    private val fileService: FileService,
    private val tesseractService: TesseractService,
    private val projectService: ProjectService,
    private val taskService: TaskService,
    private val videoService: VideoService,
) {

    @GetMapping("/v1/info")
    fun getAppInfo() = appInfoService.getInfo().toRestVo()

    @PostMapping("/v1/file/{projectId}")
    fun uploadFileChunk(@PathVariable projectId: String, @RequestPart multipartFile: MultipartFile) =
        FileChunk(projectId, multipartFile).let { fileService.saveFileChunk(it) }.toRestVo()

    @PatchMapping("/v1/file/{projectId}")
    fun combineFileChunk(@PathVariable projectId: String, fileChecksumDto: FileChecksumDto) =
        fileChecksumDto.toEntity(projectId).let { fileService.combineFileChunk(it) }.toRestVo()

    @GetMapping("/v1/tesseract/option")
    fun getTesseractOptions() =
        tesseractService.getTesseractOption().toRestVo()

    @GetMapping("/v1/tesseract/config")
    fun getTesseractConfig() =
        tesseractService.getDefaultConfig().toRestVo()

    @PostMapping("/v1/project")
    fun initialProject() =
        projectService.initProject().toRestVo()

    @PutMapping("/v1/project/{projectId}")
    fun initialProjectTask(@PathVariable projectId: String, taskConfigDto: TaskConfigDto) =
        taskService.initTask(projectId, taskConfigDto).run { this.toVo() }.toRestVo()

    @GetMapping("/v1/project/{projectId}")
    fun startProjectTask(@PathVariable projectId: String) =
        videoService.processVideo(projectId).run { RestVo(null) }

    @DeleteMapping("/v1/project/{projectId}")
    fun removeProject(@PathVariable projectId: String) =
        projectService.closeProject(projectId).run { RestVo(null) }

    @GetMapping("/v1/task/{taskId}/result")
    fun getCaptionRow(@PathVariable taskId: String) =
        taskService.getCaptionRowVoList(taskId).toRestVo()

    @GetMapping("/v1/task/{taskId}/schedule")
    fun getSchedule(@PathVariable taskId: String) =
        taskService.getSchedule(taskId).toRestVo()

    @DeleteMapping("/v1/task/{taskId}")
    fun removeTask(@PathVariable taskId: String) =
        taskService.closeTask(taskId).run { RestVo(null) }

    private fun <T : BaseVo> T.toRestVo() = RestVo(this)

    private fun <T : BaseVo> List<T>.toRestVo() = RestVo(this)

}