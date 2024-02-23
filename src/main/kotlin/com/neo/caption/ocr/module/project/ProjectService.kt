package com.neo.caption.ocr.module.project

import com.neo.caption.ocr.common.BadRequestException
import com.neo.caption.ocr.common.CACHE_PROJECT
import com.neo.caption.ocr.common.CACHE_TESS_CONFIG
import com.neo.caption.ocr.common.CacheableService
import com.neo.caption.ocr.common.ErrorCodeEnum
import com.neo.caption.ocr.module.cv.toByteArray
import com.neo.caption.ocr.module.file.FileChecksum
import com.neo.caption.ocr.module.file.FileService
import com.neo.caption.ocr.module.tesseract.AppTesseractService
import com.neo.caption.ocr.module.tesseract.TesseractConfig
import com.neo.caption.ocr.service.CacheService
import org.bytedeco.tesseract.TessBaseAPI
import org.opencv.core.Mat
import org.opencv.videoio.VideoCapture
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

@CacheableService
class ProjectService(
    private val fileService: FileService,
    private val appTesseractService: AppTesseractService,
    private val cacheService: CacheService,
) {

    fun initialProject(fileChecksum: FileChecksum): Project = fileService.createWorkingDirectory().let {
        Project(
            id = it.name.substringAfterLast("_"),
            workingDirectory = it,
            name = fileChecksum.name,
            hash = fileChecksum.hash,
            extension = fileChecksum.extension,
        )
    }.also { cacheService.put("$CACHE_PROJECT${it.id}", it) }

    @Cacheable(key = "'$CACHE_TESS_CONFIG' + #p0")
    fun initialTesseractConfig(projectId: String, tesseractConfig: TesseractConfig) = tesseractConfig

    fun doOCR(projectId: String, mat: Mat) = ocr(appTesseractService.getTessBaseApi(projectId), mat)

    fun doOCR(projectId: String, matList: List<Mat>) = appTesseractService.getTessBaseApi(projectId).run {
        matList.map { ocr(this, it) }
    }

    fun combineProjectVideo(projectId: String) {
        val project = cacheService.get<Project?>("$CACHE_PROJECT$projectId", null)
        require(project != null) { "unknown project: $projectId" }
        fileService.combineChunk(project.workingDirectory, project.extension, project.hash)
    }

    fun openProjectVideo(projectId: String) =
        VideoCapture(fileService.findVideoFile(projectId).absolutePathString())
            .also { if (!it.isOpened) throw BadRequestException(ErrorCodeEnum.VIDEO_READ_ERROR) }

    @CacheEvict(key = "'$CACHE_PROJECT' + #p0")
    fun closeProject(projectId: String) {
        appTesseractService.closeTessBaseApi(projectId)
    }

    fun projectMetadata(projectId: String): ProjectMetadata {
        TODO()
    }

    private fun ocr(tessBaseAPI: TessBaseAPI, mat: Mat): String {
        tessBaseAPI.SetImage(mat.toByteArray(), mat.cols(), mat.rows(), mat.channels(), mat.cols())
        return tessBaseAPI.GetUTF8Text().use { if (it.isNull) "" else it.string }
    }

}