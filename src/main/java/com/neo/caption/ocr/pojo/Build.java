package com.neo.caption.ocr.pojo;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class Build {

    private final BuildProperties buildProperties;

    public Build(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @PostConstruct
    public void init() {
        Info.ARTIFACT_ID.set(buildProperties.getArtifact());
        Info.GROUP_ID.set(buildProperties.getGroup());
        Info.NAME.set(buildProperties.getName());
        Info.DESCRIPTION.set(get("description", ""));
        Info.VERSION.set(buildProperties.getVersion());
        Info.VERSION_CODE.set(get("versionCode", "1908242156.18504"));
        Info.TIME.set(buildProperties.getTime().toString());
    }

    private String get(String key, String def) {
        String value = buildProperties.get(key);
        return Strings.isNullOrEmpty(value) ? def : value;
    }

    public enum Info {

        ARTIFACT_ID,
        GROUP_ID,
        NAME,
        DESCRIPTION,
        VERSION,
        VERSION_CODE,
        TIME,
        ;
        private String value;

        public String value() {
            return value;
        }

        public void set(String value) {
            this.value = value;
        }
    }

}
