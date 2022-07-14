package com.neo.caption.ocr.domain.entity;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Rect;

import java.util.Arrays;

import static com.google.common.collect.Range.closed;

public class CRect extends Rect {

    public int brx;
    public int bry;

    public CRect(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.brx = x + width;
        this.bry = y + height;
    }

    @Override
    public void set(double[] values) {
        super.set(values);
        this.brx = x + width;
        this.bry = y + height;
    }

    @Contract("_ -> new")
    public static @NotNull CRect bound(@NotNull Rect rect) {
        return new CRect(rect.x, rect.y, rect.width, rect.height);
    }

    public boolean isXConnected(@NotNull CRect otherRect) {
        return closed(x, brx).isConnected(closed(otherRect.x, otherRect.brx));
    }

    public boolean isYConnected(@NotNull CRect otherRect) {
        return closed(y, bry).isConnected(closed(otherRect.y, otherRect.bry));
    }

    public boolean isXContains(CRect otherRect, Integer offset) {
        if (offset == null) {
            offset = 0;
        }
        if (offset < 0) {
            offset = Math.abs(offset);
        }
        return closed(x - offset, brx + offset).containsAll(Arrays.asList(otherRect.x, otherRect.brx)) ||
                closed(otherRect.x - offset, otherRect.brx + offset).containsAll(Arrays.asList(x, brx));
    }

    public boolean isYContains(CRect otherRect, Integer offset) {
        if (offset == null) {
            offset = 0;
        }
        if (offset < 0) {
            offset = Math.abs(offset);
        }
        return closed(y - offset, bry + offset).containsAll(Arrays.asList(otherRect.y, otherRect.bry)) ||
                closed(otherRect.y - offset, otherRect.bry + offset).containsAll(Arrays.asList(y, bry));
    }

}
