package com.neo.caption.ocr.domain.vo;

import java.util.List;

public record RestVo<T>(
        Boolean success, List<RestErrorVo> errors, T result, RestPageVo resultInfo
) {
}
