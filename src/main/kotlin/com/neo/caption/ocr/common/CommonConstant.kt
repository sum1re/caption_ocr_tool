package com.neo.caption.ocr.common

import com.neo.caption.ocr.domain.CommonError
import com.neo.caption.ocr.domain.ErrorResponse
import org.springframework.http.HttpStatus

const val languageSeparator: String = "+"
const val CHUNK_PREFIX = "chunk"
const val MAT_EXTENSION = "webp"
const val COCR_PROJECT_CHAIN = "cocrProjectFlow"
const val COCR_BATCH_OCR_CHAIN = "cocrBatchOcrFlow"

enum class ErrorCodeEnum(val code: Int, val message: String, val httpStatus: HttpStatus) {

    /**
     * code: 1xxx
     * usually cause by user
     */
    BANNED_ACCOUNT(1006, "Your account has benn banned", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(1007, "You do not have permission to access", HttpStatus.FORBIDDEN),
    INVALID_URL(1008, "404", HttpStatus.NOT_FOUND),
    INVALID_PARAMETER(1034, "invalid param", HttpStatus.BAD_REQUEST),
    INVALID_MAT_CHANNEL(1035, "invalid channel", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(1200, "file is broken", HttpStatus.BAD_REQUEST),
    VIDEO_NOT_FOUND(1201, "video file not found", HttpStatus.BAD_REQUEST),
    DEFAULT_BAD_REQUEST(1999, "default error", HttpStatus.BAD_REQUEST),

    /**
     * code: 10xxxx
     * usually cause by service
     */
    SERVER_IO_ERROR(10100, "IO Error", HttpStatus.INTERNAL_SERVER_ERROR),
    LOSS_TESSERACT_DATA(10101, "miss tess data", HttpStatus.INTERNAL_SERVER_ERROR),
    VIDEO_READ_ERROR(10102, "failed to open video file", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_COMBINE_FAILED_ERROR(10103, "failed to combine file chunk", HttpStatus.INTERNAL_SERVER_ERROR),
    UNKNOWN_ERROR(10999, "issue to sum1re/caption_ocr_tool", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    fun toResponse(message: String = this.message): ErrorResponse = ErrorResponse(CommonError(this.code, message))

}

/**
 * https://tus.io/protocols/resumable-upload#headers
 */
object TusHeader {
    const val LOCATION = "Location"
    const val TUS_CHECKSUM_ALGORITHM = "Tus-Checksum-Algorithm"
    const val TUS_EXTENSION = "Tus-Extension"
    const val TUS_MAX_SIZE = "Tus-Max-Size"
    const val TUS_RESUMABLE = "Tus-Resumable"
    const val TUS_VERSION = "Tus-Version"
    const val UPLOAD_CHECKSUM = "Upload-Checksum"
    const val UPLOAD_DEFER_LENGTH = "Upload-Defer-Length"
    const val UPLOAD_LENGTH = "Upload-Length"
    const val UPLOAD_METADATA = "Upload-Metadata"
    const val UPLOAD_OFFSET = "Upload-Offset"
}

enum class TusVersion(val code: String) {
    V_1_0_0("1.0.0")
}

enum class TusResumable(val code: String) {
    V_1_0_0("1.0.0")
}

enum class TusChecksumAlgorithm {
    MD5, SHA1, SHA256, SHA384, SHA512, XXH3_64
}

/**
 * https://tus.io/protocols/resumable-upload#protocol-extensions
 */
enum class TusExtension(val code: String) {
    CREATION("creation"),
    TERMINATION("termination"),
    CHECKSUM("checksum"),
    EXPIRATION("expiration"),
    CONCATENATION("concatenation"),
    CREATION_WITH_UPLOAD("creation with upload"),
}