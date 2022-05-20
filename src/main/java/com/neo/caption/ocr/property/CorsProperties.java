package com.neo.caption.ocr.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    private String[] allowedOriginPatterns;
    private String allowedHeader;
    private String allowedMethods;

}
