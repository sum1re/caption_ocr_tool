package com.neo.caption.ocr.domain.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskAttribute {

    private int upperLeftX;
    private int upperLeftY;
    private int lowerRightX;
    private int lowerRightY;
    private String hash;
    private String language;
    private String id;
    private double fps;

}
