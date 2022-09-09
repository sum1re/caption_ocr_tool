package com.neo.caption.ocr.databind;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.neo.caption.ocr.util.BaseUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigDecimal;

public class TimelineSerialize extends JsonSerializer<BigDecimal> {

    @Override
    public void serialize(
            BigDecimal time, @NotNull JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeString(BaseUtil.convertTime(time));
    }

}
