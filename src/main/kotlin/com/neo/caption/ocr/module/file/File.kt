package com.neo.caption.ocr.module.file

import java.util.UUID
import java.util.concurrent.locks.ReentrantLock
import kotlin.io.encoding.Base64

class UploadLock(private val lock: ReentrantLock) : AutoCloseable {
    override fun close() {
        lock.unlock()
    }
}

data class UploadInfo(
    val id: UUID,
    val offset: Long = 0,
    val length: Long? = null,
    val encodedMetadata: String? = null,
    val ownerKey: String? = null,
    val creationTimestamp: Long = System.currentTimeMillis(),
    val expirationTimestamp: Long? = null,
    val creatorIpAddresses: String? = null,
    val uploadType: String? = null,
    val concatenationPartIds: List<String>? = null,
    val uploadConcatHeaderValue: String? = null,
    val checksum: String? = null,
    val checksumAlgorithm: String? = null,
)

fun UploadInfo.hasMetadata() = !encodedMetadata.isNullOrBlank()

fun UploadInfo.hasLength() = length != null

fun UploadInfo.isUploadInProgress() = length == null || offset != length

fun UploadInfo.isExpired() = expirationTimestamp != null && expirationTimestamp < System.currentTimeMillis()

fun UploadInfo.getMetadata(): Map<String, String> {
    val metadata = mutableMapOf<String, String>()
    if (encodedMetadata.isNullOrBlank()) return metadata
    encodedMetadata.split(",").forEach { pair ->
        val parts = pair.trim().split("\\s+".toRegex())
        if (parts.isNotEmpty()) {
            val key = parts[0]
            if (parts.size > 1) {
                metadata[key] = Base64.decode(parts[1]).decodeToString()
            } else {
                metadata[key] = ""
            }
        }
    }
    return metadata
}

fun UploadInfo.getFileName() = getMetadata().let { it["filename"] ?: it["name"] ?: id.toString() }
