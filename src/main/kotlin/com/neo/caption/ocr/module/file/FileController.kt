package com.neo.caption.ocr.module.file

import com.neo.caption.ocr.common.CommonProperties
import com.neo.caption.ocr.common.RestEntityController
import com.neo.caption.ocr.common.Slf4j
import com.neo.caption.ocr.common.TusHeader
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID

@RestEntityController("/api/file")
@Slf4j
class FileController(
    private val fileService: FileService,
    private val appProperties: CommonProperties,
) {

    @PostMapping(value = ["", "/"])
    fun uploadPost(
        @RequestHeader(TusHeader.UPLOAD_LENGTH, required = false) uploadLength: Long?,
        @RequestHeader("Upload-Defer-Length", required = false) uploadDeferLength: String?,
        @RequestHeader("Upload-Metadata", required = false) uploadMetadata: String?,
    ): ResponseEntity<Void> {
        val info = fileService.initialUpload(uploadLength, uploadDeferLength, uploadMetadata)

        return ResponseEntity.status(HttpStatus.CREATED)
            .header("Tus-Resumable", "1.0.0")
            .header("Location", "/api/file/${info.id}")
            .build()
    }

    @PatchMapping("/{id}")
    fun uploadPatch(
        @PathVariable id: UUID,
        @RequestHeader("Upload-Offset") uploadOffset: Long,
        @RequestHeader("Content-Type") contentType: String,
        @RequestHeader("Content-Length", required = false) contentLength: Long?,
        @RequestHeader("Upload-Checksum", required = false) uploadChecksum: String?,
        request: HttpServletRequest,
    ): ResponseEntity<Void> {
        val info = fileService.appendUpload(
            id, uploadOffset, contentType, uploadChecksum, contentLength, request.inputStream
        )

        val uploadInfo = fileService.findUploadInfoById(id)
        if (!uploadInfo.isUploadInProgress()) {
            val extension = uploadInfo.getFileName().substringAfterLast(".")
            Files.move(
                fileService.getDataFile(uploadInfo.id),
                appProperties.storeDirectory.resolve("${uploadInfo.id}.$extension"),
                StandardCopyOption.REPLACE_EXISTING
            )
            fileService.deleteUploadData(id)
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .header("Tus-Resumable", "1.0.0")
            .header("Upload-Offset", info.offset.toString())
            .build()
    }

    @RequestMapping("/{id}", method = [RequestMethod.HEAD])
    fun uploadHead(@PathVariable("id") id: UUID): ResponseEntity<Void> {
        val info = fileService.findUploadInfoById(id)

        val builder = ResponseEntity.status(HttpStatus.OK)
            .header("Tus-Resumable", "1.0.0")
            .header("Upload-Offset", info.offset.toString())

        info.length?.let {
            builder.header(TusHeader.UPLOAD_LENGTH, it.toString())
        }
        info.encodedMetadata?.let {
            builder.header("Upload-Metadata", it)
        }

        return builder.build()
    }

    @DeleteMapping("/{id}")
    fun uploadDelete(@PathVariable("id") id: UUID): ResponseEntity<Void> {
        fileService.deleteUploadData(id)

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .header("Tus-Resumable", "1.0.0")
            .build()
    }

    @RequestMapping(value = ["", "/", "/**"], method = [RequestMethod.OPTIONS])
    fun uploadOptions(): ResponseEntity<Void> {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .header("Tus-Resumable", "1.0.0")
            .header("Tus-Version", "1.0.0")
            .header("Tus-Max-Size", "1073741824")
            .header("Tus-Extension", "creation,termination,checksum,expiration,concatenation")
            .header("Tus-Checksum-Algorithm", "SHA1")
            .build()
    }

}