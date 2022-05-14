package com.neo.caption.ocr.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "cocr.info")
public class AppInfoProperties {

    private String artifact;
    private String group;
    private String name;
    private String appLicense;
    private String javaVersion;
    private String springBootVersion;
    private String version;
    private String buildTimestamp;

}
