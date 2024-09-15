package com.neo.caption.ocr.module.project

import com.neo.caption.ocr.common.CHUNK_PREFIX
import com.neo.caption.ocr.common.CommonProperties
import com.neo.caption.ocr.common.CommonService
import com.neo.caption.ocr.domain.Page
import com.neo.caption.ocr.domain.Pageable
import com.neo.caption.ocr.domain.toCaptionRow
import com.neo.caption.ocr.domain.toProjectMetadata
import com.neo.caption.ocr.module.liteflow.AstModelService
import com.neo.caption.ocr.module.liteflow.FilterContext
import com.neo.caption.ocr.module.liteflow.LiteflowChainId
import com.neo.caption.ocr.module.liteflow.LiteflowService
import com.neo.caption.ocr.module.liteflow.then
import com.neo.caption.ocr.module.ocr.OcrService
import com.neo.caption.ocr.service.CacheService
import com.yomahub.liteflow.builder.el.ELBus.node
import com.yomahub.liteflow.builder.el.ELWrapper
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.opencv.core.Mat
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.extension

@CommonService
@Transactional
class ProjectService(
    private val commonProperties: CommonProperties,
    private val cacheService: CacheService,
    private val liteflowService: LiteflowService,
    private val astModelService: AstModelService,
    private val ocrService: OcrService,
) {

    fun generateWorkingDirectory(projectId: UUID): Path = projectId.toWorkingDirectory()

    fun generateChunkPath(projectId: UUID, index: Int): Path =
        projectId.toWorkingDirectory().resolve("${index.toChunkIndex()}.$CHUNK_PREFIX")

    fun generateVideoPath(projectId: UUID, extension: String? = null): Path {
        return if (extension.isNullOrEmpty()) ProjectMetadataTable.select(ProjectMetadataTable.extension)
            .where { ProjectMetadataTable.projectId eq projectId }.singleOrNull()
            ?.let { generateVideoPath(projectId, it[ProjectMetadataTable.extension]) }
            ?: throw RuntimeException("Failed to generate project video path")
        else projectId.toWorkingDirectory().resolve("video.$extension")
    }

    fun generateMatPath(projectId: UUID, matId: Long): Path = projectId.toWorkingDirectory().resolve("${matId}.webp")

    fun generateMatUrl(projectId: UUID, matId: Long): String = "${projectId}/${matId}.webp"

    // CRUD for Project
    fun insertProject(): UUID {
        return ProjectTable.insertAndGetId {}.value.also {
            it.toWorkingDirectory().createDirectory()
            insertProjectMetadata(it)
        }
    }

    fun findProjectDtoList(): List<ProjectDto> {
        return ProjectTable.join(
            otherTable = ProjectMetadataTable,
            joinType = JoinType.INNER,
            onColumn = ProjectTable.id,
            otherColumn = ProjectMetadataTable.projectId,
        )
            .select(ProjectTable.id, ProjectTable.createdAt, ProjectMetadataTable.name)
            .orderBy(ProjectTable.createdAt to SortOrder.DESC)
            .map { ProjectDto(it[ProjectTable.id].value, it[ProjectMetadataTable.name]) }
    }

    fun findProjectByProjectId(projectId: UUID): Project =
        ProjectTable.select(ProjectTable.astModelId, ProjectTable.ocrProfileId)
            .where { ProjectTable.id eq projectId }
            .singleOrNull()
            ?.let {
                Project(
                    id = projectId,
                    ocrProfile = ocrService.findOcrProfileByOcrProfileId(it[ProjectTable.ocrProfileId], true),
                    astModel = astModelService.findAstModelById(it[ProjectTable.astModelId]),
                    projectMetadata = findProjectMetadataByProjectId(projectId)
                )
            }
            ?: throw RuntimeException("Failed to find project $projectId")

    fun updateProject(projectId: UUID, astModelId: UUID? = null, ocrProfileId: UUID? = null) {
        ProjectTable.update({ ProjectTable.id eq projectId }) {
            if (astModelId != null) it[this.astModelId] = astModelId
            if (ocrProfileId != null) it[this.ocrProfileId] = ocrProfileId
        }
    }

    @OptIn(ExperimentalPathApi::class)
    fun deleteProject(projectId: UUID) {
        liteflowService.remove(projectId)
        liteflowService.removeChain(LiteflowChainId(projectId))
        ProjectTable.deleteWhere { id eq projectId }
        ProjectMetadataTable.deleteWhere { ProjectMetadataTable.projectId eq projectId }
        CaptionRowTable.deleteWhere { CaptionRowTable.projectId eq projectId }
        projectId.toWorkingDirectory().deleteRecursively()
    }

    // CRUD for ProjectMetadata
    fun insertProjectMetadata(projectId: UUID) {
        ProjectMetadataTable.insert {
            it[this.projectId] = projectId
            it[this.name] = "tmp-$projectId"
        }
    }

    fun findProjectMetadataByProjectId(projectId: UUID): ProjectMetadata {
        return ProjectMetadataTable.selectAll()
            .where { ProjectMetadataTable.projectId eq projectId }
            .singleOrNull()
            ?.toProjectMetadata()
            ?: throw RuntimeException("NotFound")
    }

    fun updateProjectMetadataByVideoFile(projectId: UUID, videoFile: Path, filename: String) {
        require(videoFile.exists()) { "videoPath does not exist, ${videoFile.absolutePathString()}" }
        val videoCapture = VideoCapture(videoFile.absolutePathString())
        val fps = videoCapture.get(Videoio.CAP_PROP_FPS).toString()
        require(videoCapture.isOpened) { "Failed to access video file" }
        ProjectMetadataTable.update({ ProjectMetadataTable.projectId eq projectId }) {
            it[this.name] = filename
            it[this.extension] = videoFile.extension
            it[this.width] = videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH).toInt()
            it[this.height] = videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT).toInt()
            it[this.fps] = fps
            it[this.totalFrames] = videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT).toInt()
            it[this.frameDuration] = BigDecimal("1000")
                .divide(BigDecimal(fps), MathContext(5, RoundingMode.HALF_EVEN))
        }
        videoCapture.release()
    }

    // CRUD for CaptionRow
    fun insertCaptionRow(captionRow: CaptionRow): Long {
        return CaptionRowTable.insertAndGetId {
            it[this.projectId] = captionRow.projectId
            it[this.caption] = captionRow.caption
            it[this.start] = captionRow.start
            it[this.end] = captionRow.end
        }.value
    }

    fun findCaptionRowByProjectId(projectId: UUID, pageable: Pageable): Page<CaptionRow> {
        val query = CaptionRowTable.selectAll()
            .where { CaptionRowTable.projectId eq projectId }
        val count = query.count()
        return query.limit(pageable.size, pageable.offset)
            .orderBy(CaptionRowTable.start to SortOrder.ASC)
            .map { it.toCaptionRow() }
            .let { Page(content = it, page = pageable.page, size = pageable.size, totalElements = count) }
    }

    fun updateCaption(captionRowId: Long, caption: String) {
        CaptionRowTable.update({ CaptionRowTable.id eq captionRowId }) {
            it[this.caption] = caption
        }
    }

    fun deleteCaptionRow(projectId: UUID, captionRowId: Long) {
        CaptionRowTable.deleteWhere { this.id eq captionRowId }
        generateMatPath(projectId, captionRowId).deleteIfExists()
    }

    fun deleteCaptionRow(projectId: UUID, captionRowIdList: Collection<Long>) {
        CaptionRowTable.deleteWhere { (this.projectId eq projectId) and (this.id inList captionRowIdList.toSet()) }
        captionRowIdList.map { generateMatPath(projectId, it) }.forEach { it.deleteIfExists() }
    }

    fun previewProjectFilter(projectId: UUID, mat: Mat, el: ELWrapper): PreviewResultDto {
        try {
            val chainId = LiteflowChainId(projectId)
            liteflowService.createChain(chainId, el.then(node("isMatSingleChannel")))
            val context = FilterContext(mutableListOf(), Mat())
            val response = liteflowService.executeSync(chainId, context)
            require(response.isSuccess) { throw response.cause }
            val result: Mat = context.matStack.last().clone()
            context.matStack.clear()
            return PreviewResultDto(true, result.channels(), null, result)
        } catch (t: Throwable) {
            return PreviewResultDto(false, null, t.message ?: t::class.simpleName!!, null)
        }
    }

    private fun UUID.toWorkingDirectory() = commonProperties.workingDirectory.resolve(this.toString())

    private fun Int.toChunkIndex() = this.toString().padStart(5, '0')

}