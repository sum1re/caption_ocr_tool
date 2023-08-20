package com.neo.caption.ocr.service.impl

import com.neo.caption.ocr.annotation.Slf4j
import com.neo.caption.ocr.constant.ErrorCodeEnum
import com.neo.caption.ocr.domain.dto.FileChecksumDto
import com.neo.caption.ocr.domain.dto.FileChunkDto
import com.neo.caption.ocr.domain.vo.SavedDirVo
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
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteExisting
import kotlin.io.path.fileSize
import kotlin.io.path.name

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
            SavedDirVo(it.name)
        }

    /**
     * save chunk and return the xxhash for chunk
     */
    override fun saveFileChunk(fileChunkDto: FileChunkDto): SavedFileVo {
        val workingPath = getWorkingDir(fileChunkDto.identity)
        val savedPath = workingPath.resolve(UUID.randomUUID().toString().substring(0, 8))
        fileChunkDto.multipartFile.transferTo(savedPath)
        // TODO: use xxhash to rename chunk
        return SavedFileVo(savedPath.name, savedPath.fileSize())
    }

    /**
     * combine chunks with the gaven list
     */
    override fun combineFileChunk(fileChecksumDto: FileChecksumDto): SavedFileVo {
        val workingPath = getWorkingDir(fileChecksumDto.identity)
        val combinePath = workingPath.resolve("video.${fileChecksumDto.extension}")
        FileChannel.open(combinePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND).use {
            combine(fileChecksumDto.fileChunkName.map { chunk -> workingPath.resolve(chunk) }, it)
        }
        // TODO check combineFile with the gaven xxhash
        combinePath.toFile().deleteOnExit()
        return SavedFileVo(combinePath.name, combinePath.fileSize())
    }

    override fun openVideoFile(identify: String) =
        Files.find(getWorkingDir(identify), 1, { path, _ -> path.name.startsWith("video.") })
            .findFirst()
            .orElseThrow { BadRequestException(ErrorCodeEnum.VIDEO_NOT_FOUND) }
            .let { VideoCapture(it.absolutePathString()) }
            .also { if (!it.isOpened) throw BadRequestException(ErrorCodeEnum.VIDEO_READ_ERROR) }


    /**
     * return the working dir
     * windows: %tmp%/cocr_xxx
     * linux: /tmp/cocr_xxx
     */
    private fun getWorkingDir(identity: String): Path = Path.of(System.getProperty("java.io.tmpdir")).resolve(identity)

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

}