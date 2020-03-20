package com.neo.caption.ocr.pojo;

import com.neo.caption.ocr.constant.ModuleType;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * Save the status of the ModuleNode
 */
@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleStatus implements Serializable {

    private static final long serialVersionUID = -1002414547440903751L;

    private int index;

    private ModuleType moduleType;

    private boolean enable;

    private boolean cache;

    /**
     * node attribute, such as:
     * spinner - value;
     * choiceBox - selected index;
     * checkBox - isSelected
     */
    private Map<String, Double> attrMap;

    /**
     * read value, such as:
     * spinner - value;
     * choiceBox - selected value in class;
     * checkBox - boolean
     */
    private Map<String, Object> paramMap;

}
