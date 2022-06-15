package com.neo.caption.ocr.service;

import com.neo.caption.ocr.domain.dto.CaptionRowDto;
import com.neo.caption.ocr.domain.dto.TaskAttributeDto;
import com.neo.caption.ocr.domain.entity.TaskAttribute;

import java.util.List;

public interface TaskService {

    TaskAttribute initTask(TaskAttributeDto taskAttributeDto);

    void runTask(TaskAttribute taskAttribute);

    List<CaptionRowDto> getOCRResult(String taskId);

    void removeOCRResult(String taskId);

}
