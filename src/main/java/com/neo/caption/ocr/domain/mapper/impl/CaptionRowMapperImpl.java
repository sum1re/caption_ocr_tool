package com.neo.caption.ocr.domain.mapper.impl;

import com.neo.caption.ocr.domain.dto.CaptionRowDto;
import com.neo.caption.ocr.domain.entity.CaptionRow;
import com.neo.caption.ocr.domain.mapper.CaptionRowMapper;
import com.neo.caption.ocr.util.DecimalUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class CaptionRowMapperImpl implements CaptionRowMapper {

    @Override
    public CaptionRowDto toDto(@NotNull CaptionRow captionRow, BigDecimal frameDuration) {
        var startCount = captionRow.getStartIndex() == 0
                ? BigDecimal.ZERO
                : DecimalUtil.subtract(captionRow.getStartIndex(), 0.5);
        var endCount = DecimalUtil.add(captionRow.getEndIndex(), 0.5);
        var startTime = DecimalUtil.multiply(frameDuration, startCount);
        var endTime = DecimalUtil.multiply(frameDuration, endCount);
        return new CaptionRowDto(startTime, endTime, captionRow.getCaption());
    }

    @Override
    public List<CaptionRowDto> toDto(List<CaptionRow> captionRowList, BigDecimal frameDuration) {
        if (captionRowList == null) {
            return null;
        }
        var list = new ArrayList<CaptionRowDto>(captionRowList.size());
        for (var captionRow : captionRowList) {
            list.add(toDto(captionRow, frameDuration));
        }
        return list;
    }

}
