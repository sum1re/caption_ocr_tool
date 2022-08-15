package com.neo.caption.ocr.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CaptionRow implements Cloneable {

    private int startIndex;
    private int endIndex;
    private String caption;

    @Override
    public CaptionRow clone() {
        try {
            return (CaptionRow) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
