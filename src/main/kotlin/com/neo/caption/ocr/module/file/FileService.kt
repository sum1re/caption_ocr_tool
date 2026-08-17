package com.neo.caption.ocr.module.file

import com.neo.caption.ocr.common.BadRequestException
import com.neo.caption.ocr.common.CommonProperties
import com.neo.caption.ocr.common.CommonService
import com.neo.caption.ocr.common.ErrorCodeEnum
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.time.Clock

@Suppress("SqlResolve")
@CommonService
class FileService(
    private val commonProperties: CommonProperties,
    private val mapper: JsonMapper,
    private val jdbcTemplate: JdbcTemplate,
) {

    private val locks = ConcurrentHashMap<UUID, ReentrantLock>()

    private fun acquireLock(uri: UUID): UploadLock {
        val lock = locks.computeIfAbsent(uri) { ReentrantLock() }
        return if (lock.tryLock())
            UploadLock(lock)
        else
            throw BadRequestException(ErrorCodeEnum.FILE_UPLOAD_FAILED, "Resource is locked", HttpStatus.CONFLICT)
    }

    fun getDataFile(id: UUID): Path = commonProperties.storeDirectory.resolve("$id.data")

    fun initialUpload(uploadLength: Long?, uploadDeferLength: String?, uploadMetadata: String?): UploadInfo {
        if (uploadLength == null && uploadDeferLength != "1") {
            throw BadRequestException(ErrorCodeEnum.FILE_UPLOAD_FAILED, "Missing Upload-Length or Upload-Defer-Length")
        }
        if (uploadLength != null && uploadLength < 0) {
            throw BadRequestException(ErrorCodeEnum.FILE_UPLOAD_FAILED, "Upload-Length cannot be negative")
        }

        val info = UploadInfo(
            id = UUID.randomUUID(),
            length = uploadLength,
            encodedMetadata = uploadMetadata,
            expirationTimestamp = System.currentTimeMillis() + commonProperties.uploadExpirationPeriod
        )
        insertUploadInfo(info)
        return info
    }

    fun appendUpload(
        id: UUID,
        uploadOffset: Long,
        contentType: String,
        uploadChecksum: String?,
        contentLength: Long?,
        inputStream: InputStream,
    ): UploadInfo {
        require(contentType == "application/offset+octet-stream") {
            throw BadRequestException(
                ErrorCodeEnum.FILE_UPLOAD_FAILED,
                "Content-Type must be application/offset+octet-stream",
                HttpStatus.BAD_REQUEST
            )
        }

        acquireLock(id).use {
            var info = findUploadInfoById(id)
            if (info.isExpired()) {
                deleteUploadData(id)
                throw BadRequestException(ErrorCodeEnum.FILE_UPLOAD_FAILED, "Upload expired", HttpStatus.NOT_FOUND)
            }

            if (uploadOffset != info.offset) {
                throw BadRequestException(
                    ErrorCodeEnum.FILE_UPLOAD_FAILED,
                    "Upload-Offset mismatch. Expected ${info.offset}, got $uploadOffset",
                    HttpStatus.CONFLICT
                )
            }
            if (!info.isUploadInProgress()) {
                throw BadRequestException(
                    ErrorCodeEnum.FILE_UPLOAD_FAILED,
                    "Upload already completed",
                    HttpStatus.CONTENT_TOO_LARGE
                )
            }

            if (uploadChecksum != null) {
                val parts = uploadChecksum.split(" ", limit = 2)
                if (parts.size != 2) throw BadRequestException(
                    ErrorCodeEnum.FILE_UPLOAD_FAILED,
                    "Invalid Upload-Checksum header",
                    HttpStatus.BAD_REQUEST
                )
                val algorithm = parts[0]
                if (!algorithm.equals("SHA1", ignoreCase = true)) {
                    throw BadRequestException(
                        ErrorCodeEnum.FILE_UPLOAD_FAILED,
                        "Algorithm $algorithm not supported",
                        HttpStatus.BAD_REQUEST
                    )
                }
            }

            val bytesToRead = contentLength ?: -1L
            try {
                val readCount = appendBytes(id, inputStream, bytesToRead)
                info = info.copy(offset = (info.offset + readCount))
                insertUploadInfo(info)
            } catch (_: Exception) {
                throw BadRequestException(
                    ErrorCodeEnum.SERVER_IO_ERROR,
                    "Error writing to storage",
                    HttpStatus.INTERNAL_SERVER_ERROR
                )
            }

            return info
        }
    }

    fun findUploadInfoById(id: UUID): UploadInfo {
        return jdbcTemplate.query(
            "SELECT * FROM upload_info WHERE id = ?",
            { rs, _ ->
                val partIds = rs.getString("concatenation_part_ids").takeIf { it != null }?.let {
                    mapper.readValue<Array<String>>(it).toList()
                }
                val length = rs.getObject("upload_length") as? Long
                val exp = rs.getObject("expiration_timestamp") as? Long
                UploadInfo(
                    id = rs.getString("id").let { UUID.fromString(it) },
                    offset = rs.getLong("upload_offset"),
                    length = length,
                    encodedMetadata = rs.getString("encoded_metadata"),
                    ownerKey = rs.getString("owner_key"),
                    creationTimestamp = rs.getLong("creation_timestamp"),
                    expirationTimestamp = exp,
                    creatorIpAddresses = rs.getString("creator_ip_addresses"),
                    uploadType = rs.getString("upload_type"),
                    concatenationPartIds = partIds,
                    uploadConcatHeaderValue = rs.getString("upload_concat_header_value"),
                    checksum = rs.getString("checksum"),
                    checksumAlgorithm = rs.getString("checksum_algorithm")
                )
            },
            id
        )
            .firstOrNull() ?: throw BadRequestException(
            ErrorCodeEnum.FILE_UPLOAD_FAILED,
            "Upload not found",
            HttpStatus.NOT_FOUND
        )
    }

    fun removeLastNumberOfBytes(id: UUID, bytesCount: Long) {
        if (bytesCount <= 0) return
        val dataFile = getDataFile(id)
        if (Files.exists(dataFile)) {
            val currentSize = Files.size(dataFile)
            if (currentSize > bytesCount) {
                Files.newByteChannel(dataFile, StandardOpenOption.WRITE).use { channel ->
                    channel.truncate(currentSize - bytesCount)
                }
            } else {
                Files.deleteIfExists(dataFile)
            }
        }
    }

    fun deleteUploadData(id: UUID) {
        acquireLock(id).use {
            dropUploadInfoById(id)
            Files.deleteIfExists(getDataFile(id))
        }
    }

    fun cleanupExpired() {
        val now = Clock.System.now().toEpochMilliseconds()
        jdbcTemplate.query(
            "SELECT id FROM upload_info WHERE expiration_timestamp < ?",
            { rs, _ -> rs.getString("id") },
            now
        ).map { UUID.fromString(it) }.forEach(this::deleteUploadData)
    }

    private fun dropUploadInfoById(id: UUID) {
        jdbcTemplate.update("DELETE FROM upload_info WHERE id = ?", id)
    }

    private fun insertUploadInfo(info: UploadInfo) {
        val partIdsStr = info.concatenationPartIds?.let { mapper.writeValueAsString(it) }
        val exists = jdbcTemplate.queryForObject<Int>("SELECT COUNT(1) FROM upload_info WHERE id = ?", info.id) ?: 0
        if (exists > 0) {
            jdbcTemplate.update(
                """
                UPDATE upload_info SET 
                    upload_offset = ?, upload_length = ?, encoded_metadata = ?, owner_key = ?,
                    creation_timestamp = ?, expiration_timestamp = ?, creator_ip_addresses = ?,
                    upload_type = ?, concatenation_part_ids = ?, upload_concat_header_value = ?,
                    checksum = ?, checksum_algorithm = ?
                WHERE id = ?
                """.trimIndent(),
                info.offset, info.length, info.encodedMetadata, info.ownerKey,
                info.creationTimestamp, info.expirationTimestamp, info.creatorIpAddresses,
                info.uploadType, partIdsStr, info.uploadConcatHeaderValue,
                info.checksum, info.checksumAlgorithm, info.id
            )
        } else {
            jdbcTemplate.update(
                """
                INSERT INTO upload_info (
                    id, upload_offset, upload_length, encoded_metadata, owner_key,
                    creation_timestamp, expiration_timestamp, creator_ip_addresses,
                    upload_type, concatenation_part_ids, upload_concat_header_value,
                    checksum, checksum_algorithm
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                info.id, info.offset, info.length, info.encodedMetadata, info.ownerKey,
                info.creationTimestamp, info.expirationTimestamp, info.creatorIpAddresses,
                info.uploadType, partIdsStr, info.uploadConcatHeaderValue,
                info.checksum, info.checksumAlgorithm
            )
        }
    }

    private fun appendBytes(id: UUID, inputStream: InputStream, length: Long): Long {
        val dataFile = getDataFile(id)
        if (!Files.exists(dataFile)) {
            Files.createFile(dataFile)
        }
        var bytesReadTotal = 0L
        Files.newOutputStream(dataFile, StandardOpenOption.APPEND).use { os ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                os.write(buffer, 0, bytesRead)
                bytesReadTotal += bytesRead
                if (length in 1..bytesReadTotal) break
            }
        }
        return bytesReadTotal
    }
}
