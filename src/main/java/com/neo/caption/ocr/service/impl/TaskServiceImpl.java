package com.neo.caption.ocr.service.impl;

import com.neo.caption.ocr.constant.CacheKeyPrefix;
import com.neo.caption.ocr.domain.dto.CaptionRowDto;
import com.neo.caption.ocr.domain.dto.TaskAttributeDto;
import com.neo.caption.ocr.domain.entity.TaskAttribute;
import com.neo.caption.ocr.domain.mapper.CaptionRowMapper;
import com.neo.caption.ocr.domain.mapper.TaskAttributeMapper;
import com.neo.caption.ocr.service.CacheService;
import com.neo.caption.ocr.service.TaskService;
import com.neo.caption.ocr.service.VideoService;
import com.neo.caption.ocr.util.DecimalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static org.opencv.videoio.Videoio.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final CacheService cacheService;
    private final VideoService videoService;
    private final CaptionRowMapper captionRowMapper;
    private final TaskAttributeMapper taskAttributeMapper;

    @Override
    public TaskAttribute initTask(@NotNull TaskAttributeDto taskAttributeDto) {
        var hash = taskAttributeDto.getHash();
        var videoCapture = videoService.openVideoFile(hash);
        var taskId = UUID.randomUUID().toString().substring(0, 8).toLowerCase();
        var width = videoCapture.get(CAP_PROP_FRAME_WIDTH);
        var height = videoCapture.get(CAP_PROP_FRAME_HEIGHT);
        var fps = videoCapture.get(CAP_PROP_FPS);
        var taskAttribute = taskAttributeMapper.toEntity(taskAttributeDto);
        taskAttribute.setId(taskId)
                .setFps(fps);
        videoCapture.release();
        log.info("video info [hash: {}, width: {}, height: {}, fps: {}]", hash, width, height, fps);
        log.info("task  info [hash: {}, task_id: {}]", hash, taskId);
        return taskAttribute;
    }

    @Override
    @Async
    public void runTask(TaskAttribute taskAttribute) {
        var list = videoService.processVideo(taskAttribute);
        var frameDuration = DecimalUtil.divide(1000, taskAttribute.getFps());
        var dtoList = captionRowMapper.toDto(list, frameDuration);
        cacheService.putCache(CacheKeyPrefix.OCR_RESULT, taskAttribute.getId(), dtoList);
    }

    @Override
    public List<CaptionRowDto> getOCRResult(String taskId) {
        return cacheService.getCache(CacheKeyPrefix.OCR_RESULT, taskId);
    }

    @Override
    public void removeOCRResult(String taskId) {
        cacheService.removeCache(CacheKeyPrefix.OCR_RESULT, taskId);
    }

}
