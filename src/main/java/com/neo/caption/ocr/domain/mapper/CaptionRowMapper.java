package com.neo.caption.ocr.domain.mapper;

import com.neo.caption.ocr.domain.dto.CaptionRowDto;
import com.neo.caption.ocr.domain.entity.CaptionRow;

import java.math.BigDecimal;
import java.util.List;

public interface CaptionRowMapper {

    /**
     * Convert entity object to data transfer object.
     *
     * <p>The start time is (frame index - 0.5) * frame time, special case: the start time of the first frame is 0.<br/>
     * The end time is (frame index + 0.5) * frame time.
     *
     * @param captionRow    entity object
     * @param frameDuration Duration per frame
     * @return data transfer object
     */
    CaptionRowDto toDto(CaptionRow captionRow, BigDecimal frameDuration);

    List<CaptionRowDto> toDto(List<CaptionRow>captionRowList, BigDecimal frameDuration);

}
