package com.neo.caption.ocr.domain

import com.neo.caption.ocr.module.liteflow.AstEdge
import com.neo.caption.ocr.module.liteflow.AstEdgeTable
import com.neo.caption.ocr.module.liteflow.AstEntity
import com.neo.caption.ocr.module.liteflow.AstEntityTable
import com.neo.caption.ocr.module.ocr.OcrProfileTable.default
import com.neo.caption.ocr.module.project.CaptionRow
import com.neo.caption.ocr.module.project.CaptionRowTable
import com.neo.caption.ocr.module.project.ProjectMetadata
import com.neo.caption.ocr.module.project.ProjectMetadataTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import java.util.UUID

fun Column<UUID>.default() = this.default(UUID(0, 0))

fun ResultRow.toAstEntity(): AstEntity = AstEntity(
    id = this[AstEntityTable.id].value,
    name = this[AstEntityTable.name],
    label = this[AstEntityTable.label],
    entityType = this[AstEntityTable.entityType],
    x = this[AstEntityTable.x],
    y = this[AstEntityTable.y],
    data = this[AstEntityTable.data],
)

fun ResultRow.toAstEdge(): AstEdge = AstEdge(
    source = this[AstEdgeTable.sourceId],
    target = this[AstEdgeTable.targetId],
    ifFlag = this[AstEdgeTable.ifFlag],
    switchTag = this[AstEdgeTable.switchTag],
)

fun ResultRow.toProjectMetadata(): ProjectMetadata = ProjectMetadata(
    name = this[ProjectMetadataTable.name],
    hash = this[ProjectMetadataTable.hash],
    extension = this[ProjectMetadataTable.extension],
    width = this[ProjectMetadataTable.width],
    height = this[ProjectMetadataTable.height],
    fps = this[ProjectMetadataTable.fps],
    totalFrames = this[ProjectMetadataTable.totalFrames],
    frameDuration = this[ProjectMetadataTable.frameDuration],
)

fun ResultRow.toCaptionRow(): CaptionRow = CaptionRow(
    id = this[CaptionRowTable.id].value,
    caption = this[CaptionRowTable.caption],
    start = this[CaptionRowTable.start],
    end = this[CaptionRowTable.end],
    projectId = this[CaptionRowTable.projectId],
)
