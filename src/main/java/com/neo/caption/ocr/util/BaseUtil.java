package com.neo.caption.ocr.util;

import com.google.common.base.Strings;
import org.springframework.scheduling.annotation.Async;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BaseUtil {

    public static <T> String v2s(T t) {
        return String.valueOf(t);
    }

    public static Double s2d(String value) {
        return s2d(value, 0D);
    }

    public static Double s2d(String value, Double def) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return def;
        }
    }

    public static Integer s2i(String value) {
        return s2i(value, 0);
    }

    public static Integer s2i(String value, Integer def) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return def;
        }
    }

    public static Boolean s2b(String value) {
        return s2b(value, false);
    }

    public static Boolean s2b(String value, Boolean def) {
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return def;
        }
    }

}
