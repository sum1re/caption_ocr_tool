package com.neo.caption.ocr.module.project

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.neo.caption.ocr.common.MatSerialize
import com.neo.caption.ocr.common.TimelineSerialize
import com.neo.caption.ocr.domain.BaseData
import com.neo.caption.ocr.domain.BaseDto
import com.neo.caption.ocr.domain.default
import com.neo.caption.ocr.module.liteflow.AstModel
import com.neo.caption.ocr.module.ocr.OcrProfile
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.opencv.core.Mat
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

object ProjectTable : UUIDTable("PROJECT") {
    val createdAt = long("CREATED_AT").clientDefault { Instant.now().epochSecond }
    val astModelId = uuid("AST_MODEL_ID").default()
    val ocrProfileId = uuid("OCR_PROFILE_ID").default()
}

object ProjectMetadataTable : UUIDTable("PROJECT_METADATA") {
    val projectId = uuid("PROJECT_ID").uniqueIndex()
    val name = varchar("FILE_NAME", 255).default("")
    val hash = varchar("FILE_HASH", 32).default("")
    val extension = varchar("FILE_EXTENSION", 255).default("")
    val width = integer("VIDEO_WIDTH").default(0)
    val height = integer("VIDEO_HEIGHT").default(0)
    val fps = double("VIDEO_FPS").default(1.0)
    val totalFrames = integer("TOTAL_FRAMES").default(0)
    val frameDuration = decimal("FRAME_DURATION", 2, 5).default(BigDecimal.ZERO)
}

object CaptionRowTable : LongIdTable("CAPTION_ROW") {
    val projectId = uuid("PROJECT_ID")
    val caption = varchar("CAPTION", 255).default("")
    val start = decimal("START_TIME", 10, 5).default(BigDecimal.ZERO)
    val end = decimal("END_TIME", 10, 5).default(BigDecimal.ZERO)
}

data class Project(
    val id: UUID,
    val ocrProfile: OcrProfile?,
    val astModel: AstModel,
    val projectMetadata: ProjectMetadata,
) : BaseData

data class ProjectMetadata(
    val name: String,
    val hash: String,
    val extension: String,
    val width: Int,
    val height: Int,
    val fps: Double,
    val totalFrames: Int,
    val frameDuration: BigDecimal
) : BaseData

data class CaptionRow(
    val id: Long = 0,
    val projectId: UUID,
    val caption: String = "",
    val start: BigDecimal,
    val end: BigDecimal,
) : BaseData

data class ProjectDto(
    val id: UUID,
    val name: String,
)

data class CaptionRowDto(
    val id: Long,
    @JsonSerialize(using = TimelineSerialize::class) val start: BigDecimal,
    @JsonSerialize(using = TimelineSerialize::class) val end: BigDecimal,
    val img: String,
    val caption: String,
) : BaseDto

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PreviewResultDto(
    val isMetRequire: Boolean,
    val channel: Int?,
    val causedBy: String?,
    @JsonSerialize(using = MatSerialize::class) val mat: Mat?
) : BaseDto

@Component
class CaptionRowToCaptionRowDtoConverter(
    private val projectService: ProjectService
) : Converter<CaptionRow, CaptionRowDto> {
    override fun convert(source: CaptionRow): CaptionRowDto = CaptionRowDto(
        id = source.id,
        start = source.start,
        end = source.end,
        img = projectService.generateMatUrl(source.projectId, source.id),
        caption = source.caption,
    )
}
