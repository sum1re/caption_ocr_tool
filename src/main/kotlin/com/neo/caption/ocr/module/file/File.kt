package com.neo.caption.ocr.module.file

import com.neo.caption.ocr.domain.BaseData
import com.neo.caption.ocr.domain.BaseDto
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

data class FileChecksum(
    val name: String,
    val hash: String,
    val extension: String,
    val size: Long
) : BaseData

data class FileChecksumDto(
    val name: String,
    val hash: String,
    val size: Long
) : BaseDto

data class UploadChunk(
    val projectId: String,
    val multipartFile: MultipartFile,
    val chunkIndex: Int
)

@Component
class FileChecksumDtoToFileChecksumConverter : Converter<FileChecksumDto, FileChecksum> {
    override fun convert(source: FileChecksumDto): FileChecksum {
        require(source.name.isNotBlank() && source.name.contains(".")) { "Invalid file name" }
        return FileChecksum(
            name = source.name.substringBeforeLast("."),
            hash = source.hash,
            extension = source.name.substringAfterLast(".", ""),
            size = source.size,
        )
    }

}