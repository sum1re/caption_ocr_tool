package com.neo.caption.ocr.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskAttributeDto {

    private Integer upperLeftX;

    private Integer upperLeftY;

    private Integer lowerRightX;

    private Integer lowerRightY;

    private String hash;

    private String language;

    private String id;

}