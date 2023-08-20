package com.neo.caption.ocr.service.impl

import com.neo.caption.ocr.annotation.Slf4j
import com.neo.caption.ocr.annotation.Slf4j.Companion.log
import com.neo.caption.ocr.checkRange
import com.neo.caption.ocr.domain.dto.TaskConfigDto
import com.neo.caption.ocr.domain.entity.CropRange
import com.neo.caption.ocr.domain.entity.TaskConfig
import com.neo.caption.ocr.domain.entity.VideoInfo
import com.neo.caption.ocr.domain.mapper.CropRangeMapper
import com.neo.caption.ocr.domain.mapper.TesseractConfigMapper
import com.neo.caption.ocr.domain.vo.CaptionRowVo
import com.neo.caption.ocr.domain.vo.TaskScheduleVo
import com.neo.caption.ocr.service.FileService
import com.neo.caption.ocr.service.TaskService
import com.neo.caption.ocr.use
import org.opencv.core.Mat
import org.opencv.videoio.Videoio
import org.springframework.cache.annotation.*
import org.springframework.stereotype.Service
import java.util.*

@Slf4j
@Service
@CacheConfig(cacheNames = ["task::"])
class TaskServiceImpl(
    private val fileService: FileService,
    private val cropRangeMapper: CropRangeMapper,
    private val tesseractConfigMapper: TesseractConfigMapper,
) : TaskService {

    /**
     * initial task, return necessary config for task
     * it will also check whether [CropRange] is valid
     */
    @Cacheable(key = "#p0.identity")
    override fun initTask(taskConfigDto: TaskConfigDto): TaskConfig {
        val videoCapture = fileService.openVideoFile(taskConfigDto.identity)
        val videoInfo = VideoInfo(
            width = videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH).toInt(),
            height = videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT).toInt(),
            fps = videoCapture.get(Videoio.CAP_PROP_FPS),
            totalFrame = videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT).toInt()
        )
        val cropRange = cropRangeMapper.toEntity(
            taskConfigDto.cropRangeDto,
            videoInfo.width - 1,
            videoInfo.height - 1
        )
        Mat().use {
            try {
                videoCapture.read(it)
                it.checkRange(
                    cropRange.upperLeftX,
                    cropRange.upperLeftY,
                    cropRange.lowerRightX,
                    cropRange.lowerRightY
                )
            } catch (e: Throwable) {
                throw e
            } finally {
                videoCapture.release()
            }
        }
        return TaskConfig(
            identity = taskConfigDto.identity,
            taskId = UUID.randomUUID().toString().substring(0, 8),
            cropRange = cropRange,
            videoInfo = videoInfo,
            tesseractConfig = tesseractConfigMapper.toEntity(taskConfigDto.tesseractConfigDto)
        )
            .also { log.info { it.toString() } }
    }

    /**
     * only VideoService can invoke it after [initTask]
     */
    @Cacheable(key = "#p0")
    override fun getTaskConfig(identity: String) = TaskConfig()

    @Caching(evict = [CacheEvict(key = "#p0 + 'row'"), CacheEvict(key = "#p0 + 'schedule'")])
    override fun closeTask(taskId: String) {
        // remove List<CaptionRowVo> and CurrentFrame from cache
    }

    @CacheEvict(key = "#p0")
    override fun removeTask(identity: String) {
        // remove TaskConfig (create by initTask()) from cache
    }

    /**
     * return [CaptionRowVo] list from cache, if cache don't have list it will return empty list
     */
    @Cacheable(key = "#p0 + 'row'")
    override fun getCaptionRowVoList(taskId: String): List<CaptionRowVo> {
        return emptyList()
    }

    /**
     * put [CaptionRowVo] list to cache, ignore return
     */
    @CachePut(key = "#p0 + 'row'")
    override fun updateCaptionRowVoList(taskId: String, captionRowVoList: List<CaptionRowVo>) = captionRowVoList

    /**
     * return the schedule index from cache, if cache don't have the key it will return default
     */
    @Cacheable(key = "#p0 + 'schedule'")
    override fun getSchedule(taskId: String) = TaskScheduleVo()

    /**
     * put schedule to cache, ignore return
     */
    @CachePut(key = "#p0 + 'schedule'")
    override fun updateSchedule(taskId: String, taskScheduleVo: TaskScheduleVo) = taskScheduleVo

}