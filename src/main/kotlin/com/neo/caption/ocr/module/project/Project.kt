package com.neo.caption.ocr.module.project

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.neo.caption.ocr.common.TEMP_DIR_PREFIX
import com.neo.caption.ocr.common.TimelineSerialize
import com.neo.caption.ocr.domain.BaseData
import com.neo.caption.ocr.domain.BaseDto
import com.neo.caption.ocr.module.cv.CropRange
import com.neo.caption.ocr.module.cv.CropRangeDto
import com.neo.caption.ocr.module.tesseract.TesseractConfig
import org.opencv.core.Mat
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.nio.file.Path

data class Project(
    val id: String,
    val workingDirectory: Path,
    val hash: String,
    val name: String,
    val extension: String,
) : BaseData

data class ProjectDto(
    val id: String,
) : BaseDto

data class ProjectMetadata(
    val cropRange: CropRange,
    val chain: String, // TODO: dynamical chain
    val tesseractConfig: TesseractConfig,
    val width: Int,
    val height: Int,
    val fps: Double,
    val totalFrame: Int,
    val frameDuration: BigDecimal = BigDecimal("1000").divide(
        BigDecimal(fps.toString()),
        MathContext(5, RoundingMode.HALF_EVEN)
    )
) : BaseData

data class ProjectMetadataDto(
    val cropRange: CropRangeDto,
    val chain: String, // TODO: dynamical chain
) : BaseDto

data class CaptionRow(
    val projectId: String,
    val start: Int,
    val end: Int,
    val mat: Mat,
    val caption: String,
) : BaseData {
    fun clone() = this.copy(mat = this.mat.clone())
}

data class CaptionRowDto(
    @JsonSerialize(using = TimelineSerialize::class) val start: BigDecimal,
    @JsonSerialize(using = TimelineSerialize::class) val end: BigDecimal,
    val img: String,
    val caption: String,
) : BaseDto

@Component
class ProjectToProjectDtoConverter : Converter<Project, ProjectDto> {
    override fun convert(source: Project) = ProjectDto(id = source.id)
}

@Component
class CaptionRowToCaptionRowDtoConverter(
    private val projectService: ProjectService
) : Converter<CaptionRow, CaptionRowDto> {
    override fun convert(source: CaptionRow): CaptionRowDto {
        val frameDuration = projectService.projectMetadata(source.projectId).frameDuration
        return CaptionRowDto(
            start = source.start.let { if (it == 0) BigDecimal.ZERO else it.subtract(frameDuration) },
            end = source.end.add(frameDuration),
            img = "/${TEMP_DIR_PREFIX}${source.projectId}/${source.start}.webp",
            caption = source.caption,
        )
    }
}

private fun Int.add(frameDuration: BigDecimal) =
    BigDecimal(this.toString()).add(BigDecimal("0.5")).multiply(frameDuration)

private fun Int.subtract(frameDuration: BigDecimal) =
    BigDecimal(this.toString()).subtract(BigDecimal("0.5")).multiply(frameDuration)