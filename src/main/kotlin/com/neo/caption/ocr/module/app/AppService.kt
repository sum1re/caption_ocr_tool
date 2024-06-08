package com.neo.caption.ocr.module.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.neo.caption.ocr.common.CommonService
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import org.springframework.boot.info.BuildProperties
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import java.util.UUID

@CommonService
class AppService(
    private val buildProperties: BuildProperties,
    private val objectMapper: ObjectMapper
) {
    @Cacheable(key = "'app:info'")
    fun getAppInfoDto(): AppInfoDto = AppInfoDto(
        name = buildProperties.name,
        appLicense = buildProperties["license"],
        version = buildProperties.version,
        buildTimestamp = buildProperties.time.epochSecond.toString(),
    )

    //CRUD for AppConfig
    fun insertAppConfig() {
        AppConfigTable.upsert {
            it[this.id] = UUID(0, 0)
            it[this.filterInterval] = 2
            it[this.compareAlgorithm] = CompareAlgorithm.SSIM
            it[this.similarThreshold] = objectMapper.writeValueAsString(
                mapOf(
                    CompareAlgorithm.SSIM to 0.95,
                    CompareAlgorithm.PSNR to 40
                )
            )
            it[this.pixelThreshold] = 500
            it[this.matRetentionPolicy] = MatRetentionPolicy.BALANCED
        }
    }

    @Cacheable(key = "'app:config'")
    fun getAppConfig(): AppConfig = AppConfigTable.selectAll().limit(1, 0).singleOrNull()?.let {
        AppConfig(
            filterInterval = it[AppConfigTable.filterInterval].toInt(),
            compareAlgorithm = it[AppConfigTable.compareAlgorithm],
            similarThreshold = objectMapper.readValue<Map<CompareAlgorithm, Number>>(it[AppConfigTable.similarThreshold]),
            pixelThreshold = it[AppConfigTable.pixelThreshold],
            matRetentionPolicy = it[AppConfigTable.matRetentionPolicy],
        )
    } ?: insertAppConfig().let { getAppConfig() } // re-create and query again

    @CacheEvict(key = "'app:profile'", allEntries = true)
    fun updateAppConfig(appConfig: AppConfig) {
        AppConfigTable.update({ AppConfigTable.id eq UUID(0, 0) }) {
            it[this.filterInterval] = appConfig.filterInterval.toShort()
            it[this.similarThreshold] = objectMapper.writeValueAsString(appConfig.similarThreshold)
            it[this.pixelThreshold] = appConfig.pixelThreshold
            it[this.matRetentionPolicy] = appConfig.matRetentionPolicy
        }
    }

}