package com.neo.caption.ocr.util;

import com.neo.caption.ocr.constant.ModuleType;
import com.neo.caption.ocr.service.impl.OpenCVServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.Videoio;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
@Slf4j
public class FieldUtil {

    /**
     * Reflecting the string's variable of the same name, .
     *
     * @param aClass class
     * @param name   string to reflect
     * @param def    the default value will be returned when throwing Exception.
     * @return NoSuchFieldException will return NULL, and other Exception will return the default value
     */
    public Object getValue(Class<?> aClass, String name, Object def) {
        try {
            Field field = aClass.getField(name);
            return field.get(aClass);
        } catch (NoSuchFieldException e) {
            //e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return def;
        }
    }

    /**
     * @param name variable name, must in Imgproc
     */
    public Integer getValueFromImgproc(String name, Integer def) {
        return (Integer) getValue(Imgproc.class, name, def);
    }

    public Integer getValueFromVideoio(String name, Integer def) {
        return (Integer) getValue(Videoio.class, name, def);
    }

    public Integer getValueFromCvType(String name, Integer def) {
        return (Integer) getValue(CvType.class, name, def);
    }

    public Integer getValueFromCore(String name, Integer def) {
        return (Integer) getValue(Core.class, name, def);
    }

    public Integer getValueFromOperationType(String name, Integer def) {
        return (Integer) getValue(OpenCVServiceImpl.OperationType.class, name, def);
    }

    /**
     * Reflect ModuleType by the fxId of the JFXButton.
     *
     * @param btnFxId the same name with ModuleType
     */
    public ModuleType reflectFxId(String btnFxId) {
        try {
            Field field = ModuleType.class.getField(btnFxId);
            return (ModuleType) field.get(ModuleType.class);
        } catch (Exception e) {
            e.printStackTrace();
            return ModuleType.CVT_COLOR;
        }
    }

    public Object reflectAttrChoiceBox(String name) {
        Object value = getValueFromImgproc(name, 0);
        if (value != null) {
            return value;
        }
        value = getValueFromCore(name, 0);
        if (value != null) {
            return value;
        }
        value = getValueFromCvType(name, 0);
        if (value != null) {
            return value;
        }
        value = getValueFromOperationType(name, 0);
        if (value != null) {
            return value;
        }
        try {
            return Integer.valueOf(name);
        } catch (NumberFormatException e) {
            return Boolean.valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
