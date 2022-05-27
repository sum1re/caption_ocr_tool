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

    /**
     * Convert millisecond to H:mm:ss.SS<br/>
     * e.g. 82033 -> 0:01:22.33
     *
     * @param time unit: millisecond
     * @return String
     */
    public static String convertTime(BigDecimal time) {
        if (time == null || time.compareTo(BigDecimal.ZERO) <= 0) {
            return "0:00:00.00";
        }
        var millisecond = DecimalUtil.divide(time, 1000D);
        var hour = BigDecimal.ZERO;
        var minute = BigDecimal.ZERO;
        var second = DecimalUtil.round(0, RoundingMode.DOWN, millisecond);
        if (second.compareTo(new BigDecimal("60")) >= 0) {
            minute = DecimalUtil.divide(second, 60D);
            second = DecimalUtil.remainder(second, 60D);
        }
        if (minute.compareTo(new BigDecimal("60")) >= 0) {
            hour = DecimalUtil.divide(minute, 60D);
            minute = DecimalUtil.remainder(minute, 60D);
        }
        var ms = DecimalUtil.remainder(millisecond, BigDecimal.ONE);
        var msStr = DecimalUtil.round(5, RoundingMode.DOWN, ms).toString();
        var msDotIndex = msStr.indexOf('.');
        var subMs = msStr.substring(msDotIndex + 1, Math.min(msDotIndex + 3, msStr.length()));
        return String.format("%1$01d:%2$02d:%3$02d.%4$s",
                hour.intValue(),
                minute.intValue(),
                second.intValue(),
                Strings.padEnd(subMs, 2, '0'));
    }

}
