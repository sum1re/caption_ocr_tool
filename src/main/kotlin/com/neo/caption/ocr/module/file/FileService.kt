package com.neo.caption.ocr.module.file

import com.appmattus.crypto.Algorithm
import com.neo.caption.ocr.common.BadRequestException
import com.neo.caption.ocr.common.ErrorCodeEnum
import com.neo.caption.ocr.common.Slf4j
import org.springframework.stereotype.Service
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.PathWalkOption
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.io.path.walk

@Slf4j
@Service
class FileService {

    private val tempDirPrefix = "cocr_"

    /**
     * Create temp directory and it will be deleted after application exited
     *
     * @return folder name, example: cocr_3691603552712666795
     */
    fun createWorkingDirectory(): Path = Files.createTempDirectory(tempDirPrefix).also {
        it.toFile().deleteOnExit()
    }

    /**
     * Save chunk to working directory
     */
    fun saveChunk(uploadChunk: UploadChunk) {
        val savedPath = getWorkingDirectory(uploadChunk.projectId)
            .resolve(uploadChunk.chunkIndex.toString().padStart(5, '0'))
        uploadChunk.multipartFile.transferTo(savedPath)
    }

    /**
     * Combine chunks with the gaven directory
     */
    @OptIn(ExperimentalPathApi::class)
    fun combineChunk(workingDirectory: Path, extension: String, checksum: String) {
        val savedPath = workingDirectory.resolve("video.$extension")
        FileChannel.open(savedPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND).use { savedChannel ->
            var start: Long = 0
            workingDirectory.walk(PathWalkOption.BREADTH_FIRST).filter { !it.isDirectory() }.sorted().forEach { chunk ->
                FileChannel.open(chunk).use {
                    savedChannel.transferFrom(it, start, it.size())
                    start += it.size()
                    chunk.deleteIfExists()
                }
            }
        }
        val xxHash3 = savedPath.calcXXHash3()
        if (xxHash3 != checksum) {
            savedPath.deleteIfExists()
            throw BadRequestException(ErrorCodeEnum.FILE_COMBINE_FAILED_ERROR)
        }
        savedPath.toFile().deleteOnExit()
    }

    /**
     * Find and return the video file.
     */
    fun findVideoFile(projectId: String) =
        Files.find(getWorkingDirectory(projectId), 1, { path, _ -> path.name.startsWith("video.") })
            .findFirst()
            .orElseThrow { BadRequestException(ErrorCodeEnum.VIDEO_NOT_FOUND) }

    /**
     * Return the working dir
     * windows: %tmp%/cocr_xxx
     * linux: /tmp/cocr_xxx
     */
    private fun getWorkingDirectory(projectId: String): Path =
        Path.of(System.getProperty("java.io.tmpdir")).resolve("$tempDirPrefix$projectId")

    @OptIn(ExperimentalStdlibApi::class)
    private fun Path.calcXXHash3() = this.readBytes().let { Algorithm.XXH3_64().hash(it).toHexString() }

}