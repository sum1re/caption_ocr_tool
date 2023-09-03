package com.neo.caption.ocr.service.impl

import com.appmattus.crypto.Algorithm
import com.neo.caption.ocr.annotation.Slf4j
import com.neo.caption.ocr.constant.ErrorCodeEnum
import com.neo.caption.ocr.domain.entity.FileChecksum
import com.neo.caption.ocr.domain.entity.FileChunk
import com.neo.caption.ocr.domain.vo.SavedFileVo
import com.neo.caption.ocr.exception.BadRequestException
import com.neo.caption.ocr.service.FileService
import org.opencv.videoio.VideoCapture
import org.springframework.stereotype.Service
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.io.path.*

@Slf4j
@Service
class FileServiceImpl : FileService {

    private val tempDirPrefix = "cocr_"

    /**
     * create temp directory and it will be deleted after application exited
     *
     * @return folder name, example: cocr_3691603552712666795
     */
    override fun createWorkingDirectory() = Files.createTempDirectory(tempDirPrefix)
        .let {
            it.toFile().deleteOnExit()
            it.name
        }

    /**
     * save chunk and return the xxHash for chunk
     */
    override fun saveFileChunk(fileChunk: FileChunk): SavedFileVo {
        val workingPath = getWorkingDir(fileChunk.projectId)
        val savedPath = workingPath.resolve(UUID.randomUUID().toString().substring(0, 8))
        fileChunk.multipartFile.transferTo(savedPath)
        return SavedFileVo(
            name = savedPath.name,
            size = savedPath.fileSize(),
            hash = savedPath.calcXXHash3()
        )
    }

    /**
     * combine chunks with the gaven list
     */
    override fun combineFileChunk(fileChecksum: FileChecksum): SavedFileVo {
        val workingPath = getWorkingDir(fileChecksum.projectId)
        val combinePath = workingPath.resolve("video.${fileChecksum.extension}")
        FileChannel.open(combinePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND).use {
            combine(fileChecksum.fileChunkName.map { chunk -> workingPath.resolve(chunk) }, it)
        }
        val xxHash3 = combinePath.calcXXHash3()
        if (xxHash3 != fileChecksum.hash) throw BadRequestException(ErrorCodeEnum.FILE_COMBINE_FAILED_ERROR)
        combinePath.toFile().deleteOnExit()
        return SavedFileVo(combinePath.name, combinePath.fileSize(), xxHash3)
    }

    override fun openVideoFile(projectId: String) =
        Files.find(getWorkingDir(projectId), 1, { path, _ -> path.name.startsWith("video.") })
            .findFirst()
            .orElseThrow { BadRequestException(ErrorCodeEnum.VIDEO_NOT_FOUND) }
            .let { VideoCapture(it.absolutePathString()) }
            .also { if (!it.isOpened) throw BadRequestException(ErrorCodeEnum.VIDEO_READ_ERROR) }

    /**
     * return the working dir
     * windows: %tmp%/cocr_xxx
     * linux: /tmp/cocr_xxx
     */
    private fun getWorkingDir(projectId: String): Path =
        Path.of(System.getProperty("java.io.tmpdir")).resolve(projectId)

    private fun combine(fileList: List<Path>, outFileChannel: FileChannel) {
        var start: Long = 0
        for (path in fileList) {
            FileChannel.open(path).use {
                outFileChannel.transferFrom(it, start, it.size())
                start += it.size().toInt()
                path.deleteExisting()
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun Path.calcXXHash3() = this.readBytes().let { Algorithm.XXH3_64().hash(it).toHexString() }

}