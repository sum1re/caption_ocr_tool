package com.neo.caption.ocr.domain.mapper;

import com.neo.caption.ocr.domain.dto.TaskAttributeDto;
import com.neo.caption.ocr.domain.entity.TaskAttribute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper
public interface TaskAttributeMapper {

    @Mappings({
            @Mapping(target = "upperLeftX", ignore = true),
            @Mapping(target = "upperLeftY", ignore = true),
            @Mapping(target = "lowerRightX", ignore = true),
            @Mapping(target = "lowerRightY", ignore = true),
            @Mapping(target = "hash", ignore = true),
            @Mapping(target = "language", ignore = true)
    })
    TaskAttributeDto toDto(TaskAttribute taskInfo);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "fps", ignore = true)
    })
    TaskAttribute toEntity(TaskAttributeDto taskAttributeDto);

}
