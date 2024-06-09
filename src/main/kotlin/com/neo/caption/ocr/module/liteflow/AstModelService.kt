package com.neo.caption.ocr.module.liteflow

import com.neo.caption.ocr.common.CommonService
import com.neo.caption.ocr.domain.toAstEdge
import com.neo.caption.ocr.domain.toAstEntity
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

@CommonService
class AstModelService {
    // CRUD AstModel
    fun insertAstModel(astModel: AstModelDto) {
        val id = AstModelTable.insertAndGetId { it[this.friendlyName] = astModel.friendlyName }.value
        insertAstEntity(id, astModel.entityList)
        insertAstEdge(id, astModel.edgeList)
    }

    fun findAstModelDtoList(): List<AstModelDto> {
        return AstModelTable.selectAll().orderBy(AstModelTable.createdAt to SortOrder.ASC).map {
            AstModelDto(
                id = it[AstModelTable.id].value,
                friendlyName = it[AstModelTable.friendlyName],
                entityList = emptyList(),
                edgeList = emptyList()
            )
        }
    }

    fun findAstModelById(astModelId: UUID, fullModel: Boolean = true): AstModel {
        return AstModelTable.selectAll().where { AstModelTable.id eq astModelId }.singleOrNull()
            ?.let {
                AstModel(
                    id = it[AstModelTable.id].value,
                    friendlyName = it[AstModelTable.friendlyName],
                    entityList = if (fullModel) findAstEntityByAstModelId(astModelId) else emptyList(),
                    edgeList = if (fullModel) findAstEdgeByAstModelId(astModelId) else emptyList(),
                )
            }
            ?: throw RuntimeException("$astModelId not found")
    }

    fun findAstEntityByAstModelId(astModelId: UUID): List<AstEntity> = AstEntityTable.selectAll()
        .where { AstEntityTable.astModelId eq astModelId }.map { it.toAstEntity() }

    fun findAstEdgeByAstModelId(astModelId: UUID): List<AstEdge> = AstEdgeTable.selectAll()
        .where { AstEdgeTable.astModelId eq astModelId }.map { it.toAstEdge() }

    // as easy as possible just delete and re-insert
    fun updateAstModel(astModel: AstModelDto) {
        deleteAstEntityByAstModelId(astModel.id)
        deleteAstEdgeByAstModelId(astModel.id)
        insertAstEntity(astModel.id, astModel.entityList)
        insertAstEdge(astModel.id, astModel.edgeList)
    }

    fun deleteAstModelById(astModelId: UUID) {
        deleteAstEntityByAstModelId(astModelId)
        deleteAstEdgeByAstModelId(astModelId)
        AstModelTable.deleteWhere { AstModelTable.id eq astModelId }
    }

    private fun insertAstEntity(astModelId: UUID, astEntityList: List<AstEntityDto>) {
        AstEntityTable.batchInsert(astEntityList) {
            this[AstEntityTable.astModelId] = astModelId
            this[AstEntityTable.name] = it.name
            this[AstEntityTable.label] = it.label
            this[AstEntityTable.entityType] = it.entityType
            this[AstEntityTable.x] = it.x
            this[AstEntityTable.y] = it.y
            this[AstEntityTable.data] = it.data
        }
    }

    private fun insertAstEdge(astModelId: UUID, edgeList: List<AstEdgeDto>) {
        AstEdgeTable.batchInsert(edgeList) {
            this[AstEdgeTable.astModelId] = astModelId
            this[AstEdgeTable.sourceId] = it.source
            this[AstEdgeTable.targetId] = it.target
            this[AstEdgeTable.ifFlag] = it.ifFlag
            this[AstEdgeTable.switchTag] = it.switchTag
        }
    }

    private fun deleteAstEntityByAstModelId(astModelId: UUID) {
        AstEntityTable.deleteWhere { AstEntityTable.astModelId eq astModelId }
    }

    private fun deleteAstEdgeByAstModelId(astModelId: UUID) {
        AstEdgeTable.deleteWhere { AstEdgeTable.astModelId eq astModelId }
    }
}