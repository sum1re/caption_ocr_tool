package com.neo.caption.ocr.util;

import com.neo.caption.ocr.constant.LayoutName;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;

@Slf4j
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

    public static URL fxmlURL(LayoutName layoutName) {
        return BaseUtil.class.getResource("/layout/" + layoutName.getName() + ".fxml");
    }

    public static String cssURL(LayoutName layoutName) {
        return BaseUtil.class.getResource("/css/" + layoutName.getName() + ".css").toExternalForm();
    }

    public static long getTimeGen() {
        return System.currentTimeMillis();
    }

    /**
     * convert millisecond to HH:mm:ss.SS
     *
     * @param time unit: millisecond
     * @return String
     */
    public static String convertTime(Double time) {
        BigDecimal millisecond = DecimalUtil.divide(time, 1000D);
        BigDecimal hour = BigDecimal.ZERO;
        BigDecimal minute = BigDecimal.ZERO;
        BigDecimal second = DecimalUtil.round(0, RoundingMode.DOWN, millisecond);
        if (second.compareTo(new BigDecimal("60")) >= 0) {
            minute = DecimalUtil.divide(second, 60D);
            second = DecimalUtil.remainder(second, 60D);
        }
        if (minute.compareTo(new BigDecimal("60")) >= 0) {
            hour = DecimalUtil.divide(minute, 60D);
            minute = DecimalUtil.remainder(minute, 60D);
        }
        String ms = DecimalUtil.round(5, RoundingMode.DOWN, DecimalUtil.remainder(millisecond, BigDecimal.ONE)).toString();
        return String.format("%1$02d:%2$02d:%3$02d.%4$s",
                hour.intValue(),
                minute.intValue(),
                second.intValue(),
                ms.substring(ms.indexOf(".") + 1));
    }

}
