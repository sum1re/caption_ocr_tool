package com.neo.caption.ocr.pojo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Data
@Slf4j
public class ModuleNodeAttribute implements Serializable {

    private static final long serialVersionUID = 1515836868757933765L;

    /**
     * Only used in the Json file to explain the meaning of the Tag,
     * which must have Tag.name and Tag.description fields in the 'language' file.
     */
    private transient String paramName;

    /**
     * Tag of the parameter, which is set to the key of the map.
     */
    private String paramTag;

    /**
     * the node type of the parameter
     */
    private ModuleParamNodeType paramNodeType;

    /**
     * Whether the spinner's editor supports editing. Default: true
     */
    private Boolean editable;

    /**
     * default value of the parameter or default index of the options
     */
    private Double paramDefaultValue;

    /**
     * max value of the parameter
     */
    private Double paramMaxValue;

    /**
     * min value of the parameter
     */
    private Double paramMinValue;

    /**
     * increment of the parameter
     */
    private Double paramIncrement;

    /**
     * options of the parameter
     */
    private String[] paramOptions;

    public Boolean getEditable() {
        return this.editable == null ? true : editable;
    }

    public enum ModuleParamNodeType {

        DEPRECATED,
        EMPTY,
        SPINNER,
        CHOICE_BOX,
        CHECK_BOX

    }
}
