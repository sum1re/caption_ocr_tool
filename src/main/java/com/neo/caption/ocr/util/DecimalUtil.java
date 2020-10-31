package com.neo.caption.ocr.util;

import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.neo.caption.ocr.util.BaseUtil.v2s;

@Slf4j
public class DecimalUtil {

    public static BigDecimal add(List<String> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal bigDecimal = new BigDecimal(values.get(0));
        for (String value : Iterables.skip(values, 1)) {
            bigDecimal = bigDecimal.add(new BigDecimal(value));
        }
        return bigDecimal;
    }

    public static BigDecimal add(String... values) {
        return add(Arrays.asList(values));
    }

    public static BigDecimal add(Number... values) {
        return add(Arrays.stream(values).map(BaseUtil::v2s).collect(Collectors.toList()));
    }

    public static BigDecimal subtract(List<String> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal bigDecimal = new BigDecimal(values.get(0));
        for (String value : Iterables.skip(values, 1)) {
            bigDecimal = bigDecimal.subtract(new BigDecimal(value));
        }
        return bigDecimal;
    }

    public static BigDecimal subtract(String... values) {
        return subtract(Arrays.asList(values));
    }

    public static BigDecimal subtract(Number... values) {
        return subtract(Arrays.stream(values).map(BaseUtil::v2s).collect(Collectors.toList()));
    }

    public static BigDecimal multiply(List<String> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal bigDecimal = new BigDecimal(values.get(0));
        for (String value : Iterables.skip(values, 1)) {
            bigDecimal = bigDecimal.multiply(new BigDecimal(value));
        }
        return bigDecimal;
    }

    public static BigDecimal multiply(String... values) {
        return multiply(Arrays.asList(values));
    }

    public static BigDecimal multiply(Number... values) {
        return multiply(Arrays.stream(values).map(BaseUtil::v2s).collect(Collectors.toList()));
    }

    public static BigDecimal divide(int scale, RoundingMode roundingMode, List<String> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        if (scale < 0) {
            scale = 10;
        }
        BigDecimal bigDecimal = new BigDecimal(values.get(0));
        for (String value : Iterables.skip(values, 1)) {
            BigDecimal bd = new BigDecimal(value);
            if (bd.compareTo(BigDecimal.ZERO) == 0) {
                return bigDecimal;
            }
            bigDecimal = bigDecimal.divide(new BigDecimal(value), scale, roundingMode);
        }
        return bigDecimal;
    }

    public static BigDecimal divide(int scale, RoundingMode roundingMode, String... values) {
        return divide(scale, roundingMode, Arrays.asList(values));
    }

    public static BigDecimal divide(String... values) {
        return divide(5, RoundingMode.HALF_EVEN, values);
    }

    public static BigDecimal divide(int scale, RoundingMode roundingMode, Number... values) {
        return divide(scale, roundingMode,
                Arrays.stream(values).map(BaseUtil::v2s).collect(Collectors.toList()));
    }

    public static BigDecimal divide(Number... values) {
        return divide(5, RoundingMode.HALF_EVEN,
                Arrays.stream(values).map(BaseUtil::v2s).collect(Collectors.toList()));
    }

    public static BigDecimal remainder(String first, String second) {
        return remainder(new BigDecimal(first), new BigDecimal(second));
    }

    public static BigDecimal remainder(Number first, Number second) {
        return remainder(v2s(first), v2s(second));
    }

    public static BigDecimal remainder(BigDecimal first, BigDecimal second) {
        if (second.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return first.remainder(second);
    }

    public static BigDecimal round(int scale, RoundingMode roundingMode, String value) {
        if (isNullOrEmpty(value)) {
            return BigDecimal.ZERO;
        }
        if (scale < 0) {
            scale = 10;
        }
        return new BigDecimal(value).setScale(scale, roundingMode);
    }

    public static BigDecimal round(int scale, RoundingMode roundingMode, Number number) {
        return round(scale, roundingMode, number.toString());
    }

    public static BigDecimal round(String value) {
        return round(5, RoundingMode.HALF_EVEN, value);
    }

    public static BigDecimal round(Number number) {
        return round(5, RoundingMode.HALF_EVEN, v2s(number));
    }
}
