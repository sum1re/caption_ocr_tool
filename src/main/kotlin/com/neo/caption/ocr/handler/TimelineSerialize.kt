package com.neo.caption.ocr.handler

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class TimelineSerialize : JsonSerializer<BigDecimal>() {

    override fun serialize(value: BigDecimal, gen: JsonGenerator, serializers: SerializerProvider) {
        convertTime(value).let { gen.writeString(it) }
    }

    /**
     * Convert millisecond to H:mm:ss.SS<br/>
     * e.g. 82033 -> 0:01:22.33
     *
     * @param time unit: millisecond
     * @return String
     */
    private fun convertTime(time: BigDecimal): String {
        if (time <= BigDecimal.ZERO) {
            return "0:00:00.00"
        }
        val sixty = BigDecimal("60")
        val thousand = BigDecimal("1000")
        var hour = BigDecimal.ZERO
        var minute = BigDecimal.ZERO
        var second = time.divide(thousand, MathContext(0, RoundingMode.DOWN))
        val millisecond = time.remainder(thousand)
        if (second >= sixty) {
            minute = second.divide(sixty, MathContext(5, RoundingMode.HALF_EVEN))
            second = second.remainder(sixty)
        }
        if (minute >= sixty) {
            hour = minute.divide(sixty, MathContext(5, RoundingMode.HALF_EVEN))
            minute = minute.remainder(sixty)
        }
        return "${hour.toInt()}:" +
                "${minute.toFormatInt()}:" +
                "${second.toFormatInt()}." +
                millisecond.toString().padEnd(2, '0').take(2)
    }

    private fun BigDecimal.toFormatInt() = "%02d".format(this.toInt())

}