package com.neo.caption.ocr.module.file

import com.appmattus.crypto.Algorithm
import com.neo.caption.ocr.common.BadRequestException
import com.neo.caption.ocr.common.ErrorCodeEnum
import com.neo.caption.ocr.common.Slf4j
import com.neo.caption.ocr.common.TEMP_DIR_PREFIX
import com.neo.caption.ocr.module.cv.toEncodeByteArray
import org.opencv.core.Mat
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.PathWalkOption
import kotlin.io.path.deleteIfExists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.outputStream
import kotlin.io.path.readBytes
import kotlin.io.path.walk

@Slf4j
@Service
class FileService {

    /**
     * Create temp directory and it will be deleted after application exited
     *
     * @return folder name, example: cocr_3691603552712666795
     */
    fun saveInputStream(savedPath: Path, inputStream: InputStream): String {
        return savedPath.outputStream(
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
            .use { inputStream.copyTo(it) }
            .let { savedPath.calcXXHash3() }
    }

    /**
     * Save chunk to working directory
     */
    fun saveMat(savedPath: Path, mat: Mat, quality: Int = 80) {
        Files.write(
            savedPath,
            mat.toEncodeByteArray(ext = ".${savedPath.extension}", quality = quality),
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    /**
     * Combine chunks with the gaven directory
     */
    @OptIn(ExperimentalPathApi::class)
    fun combineChunk(chunkDirectory: Path, chunkExtension: String, savedPath: Path, checksum: String): Path {
        val chunkSequence = chunkDirectory.walk(PathWalkOption.BREADTH_FIRST)
            .filter { !it.isDirectory() && it.extension == chunkExtension }
            .sorted()
        FileChannel.open(savedPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND).use { savedChannel ->
            var start: Long = 0
            chunkSequence.forEach { chunk ->
                FileChannel.open(chunk).use {
                    savedChannel.transferFrom(it, start, it.size())
                    start += it.size()
                }
            }
        }
        val xxHash3 = savedPath.calcXXHash3()
        if (xxHash3 != checksum) {
            savedPath.deleteIfExists()
            throw BadRequestException(ErrorCodeEnum.FILE_COMBINE_FAILED_ERROR)
        }
        chunkSequence.forEach { it.deleteIfExists() }
        return savedPath
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun Path.calcXXHash3() = this.readBytes().let { Algorithm.XXH3_64().hash(it).toHexString() }

}