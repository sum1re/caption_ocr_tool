package com.neo.caption.ocr.module.file

import com.appmattus.crypto.Algorithm
import com.neo.caption.ocr.common.BadRequestException
import com.neo.caption.ocr.common.ErrorCodeEnum
import com.neo.caption.ocr.common.Slf4j
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
     * Saves the contents of an [InputStream] to a specified path.
     *
     * @param savedPath The path where the file should be saved.
     * @param inputStream The input stream containing the data to be saved.
     *
     * @return The [XXHash3-64](https://xxhash.com) checksum of the saved file.
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
     * Saves a [Mat] object (image) to a specified path.
     *
     * @param savedPath The path where the image should be saved.
     * @param mat The Mat object containing the image data.
     * @param quality The desired image quality (default is 80).
     *
     * @see [toEncodeByteArray]
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
        if (!xxHash3.equals(checksum, ignoreCase = true)) {
            savedPath.deleteIfExists()
            throw BadRequestException(ErrorCodeEnum.FILE_COMBINE_FAILED_ERROR)
        }
        chunkSequence.forEach { it.deleteIfExists() }
        return savedPath
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun Path.calcXXHash3() = this.readBytes().let { Algorithm.XXH3_64().hash(it).toHexString() }

}