package com.neo.caption.ocr.module.file

import com.neo.caption.ocr.common.CommonProperties
import com.neo.caption.ocr.common.RestEntityController
import com.neo.caption.ocr.common.Slf4j
import com.neo.caption.ocr.common.TusChecksumAlgorithm
import com.neo.caption.ocr.common.TusExtension
import com.neo.caption.ocr.common.TusHeader
import com.neo.caption.ocr.common.TusResumable
import com.neo.caption.ocr.common.TusVersion
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
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
        @RequestHeader(TusHeader.UPLOAD_DEFER_LENGTH, required = false) uploadDeferLength: String?,
        @RequestHeader(TusHeader.UPLOAD_METADATA, required = false) uploadMetadata: String?,
    ): ResponseEntity<Void> {
        val info = fileService.initialUpload(uploadLength, uploadDeferLength, uploadMetadata)

        return ResponseEntity.status(HttpStatus.CREATED)
            .header(TusHeader.TUS_RESUMABLE, TusResumable.V_1_0_0.code)
            .header(TusHeader.LOCATION, "/api/file/${info.id}")
            .build()
    }

    @PatchMapping("/{id}")
    fun uploadPatch(
        @PathVariable id: UUID,
        @RequestHeader(HttpHeaders.CONTENT_TYPE) contentType: String,
        @RequestHeader(HttpHeaders.CONTENT_LENGTH, required = false) contentLength: Long?,
        @RequestHeader(TusHeader.UPLOAD_OFFSET) uploadOffset: Long,
        @RequestHeader(TusHeader.UPLOAD_CHECKSUM, required = false) uploadChecksum: String?,
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
            .header(TusHeader.TUS_RESUMABLE, TusResumable.V_1_0_0.code)
            .header(TusHeader.UPLOAD_OFFSET, info.offset.toString())
            .build()
    }

    @RequestMapping("/{id}", method = [RequestMethod.HEAD])
    fun uploadHead(@PathVariable("id") id: UUID): ResponseEntity<Void> {
        val info = fileService.findUploadInfoById(id)

        val builder = ResponseEntity.status(HttpStatus.OK)
            .header(TusHeader.TUS_RESUMABLE, TusResumable.V_1_0_0.code)
            .header(TusHeader.UPLOAD_OFFSET, info.offset.toString())

        info.length?.let {
            builder.header(TusHeader.UPLOAD_LENGTH, it.toString())
        }
        info.encodedMetadata?.let {
            builder.header(TusHeader.UPLOAD_METADATA, it)
        }

        return builder.build()
    }

    @DeleteMapping("/{id}")
    fun uploadDelete(@PathVariable("id") id: UUID): ResponseEntity<Void> {
        fileService.deleteUploadData(id)

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .header(TusHeader.TUS_RESUMABLE, TusResumable.V_1_0_0.code)
            .build()
    }

    @RequestMapping(value = ["", "/", "/**"], method = [RequestMethod.OPTIONS])
    fun uploadOptions(): ResponseEntity<Void> {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .header(TusHeader.TUS_RESUMABLE, TusResumable.V_1_0_0.code)
            .header(TusHeader.TUS_VERSION, TusVersion.V_1_0_0.code)
            .header(TusHeader.TUS_MAX_SIZE, appProperties.uploadMaxSize.toString())
            .header(
                TusHeader.TUS_EXTENSION,
                TusExtension.CHECKSUM.code,
                TusExtension.CONCATENATION.code,
                TusExtension.CREATION.code,
                TusExtension.EXPIRATION.code,
                TusExtension.TERMINATION.code,
            )
            .header(TusHeader.TUS_CHECKSUM_ALGORITHM, TusChecksumAlgorithm.SHA1.name)
            .build()
    }

}