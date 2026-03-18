/*
 * *
 *  MIT License
 *
 *  Copyright (c) 2026 Andy Bailey
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 * /
 */

package org.tlr.fastdecimal.valuetype;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValueFastDecimalTest {

    private static final int SCALE_DIGITS = 4;

    @ParameterizedTest
    @CsvSource({
            "1.2345, 2.3456",
            "10.0000, 5.0000",
            "-1.0000, 1.0000",
            "0.0000, 0.0000",
            "9999.9999, 0.0001"
    })
    public void testAdd(String val1, String val2) {
        ValueFastDecimal d1 = ValueFastDecimal.of(val1);
        ValueFastDecimal d2 = ValueFastDecimal.of(val2);
        ValueFastDecimal result = d1.add(d2);

        BigDecimal expected = new BigDecimal(val1).add(new BigDecimal(val2)).setScale(SCALE_DIGITS, RoundingMode.HALF_UP);
        assertEquals(expected, result.toBigDecimal(SCALE_DIGITS));
    }

    @ParameterizedTest
    @CsvSource({
            "5.0000, 3.0000",
            "1.2345, 1.2345",
            "0.0000, 5.0000",
            "-1.0000, -2.0000"
    })
    public void testSubtract(String val1, String val2) {
        ValueFastDecimal d1 = ValueFastDecimal.of(val1);
        ValueFastDecimal d2 = ValueFastDecimal.of(val2);
        ValueFastDecimal result = d1.subtract(d2);

        BigDecimal expected = new BigDecimal(val1).subtract(new BigDecimal(val2)).setScale(SCALE_DIGITS, RoundingMode.HALF_UP);
        assertEquals(expected, result.toBigDecimal(SCALE_DIGITS));
    }

    @ParameterizedTest
    @CsvSource({
            "2.0000, 3.0000",
            "1.5000, 2.0000",
            "0.5000, 0.5000",
            "-1.0000, 5.0000",
            "0.0000, 123.4567"
    })
    public void testMultiply(String val1, String val2) {
        ValueFastDecimal d1 = ValueFastDecimal.of(val1);
        ValueFastDecimal d2 = ValueFastDecimal.of(val2);
        ValueFastDecimal result = d1.multiply(d2);

        BigDecimal expected = new BigDecimal(val1).multiply(new BigDecimal(val2)).setScale(SCALE_DIGITS, RoundingMode.HALF_UP);
        assertEquals(expected, result.toBigDecimal(SCALE_DIGITS));
    }

    @ParameterizedTest
    @CsvSource({
            "6.0000, 2.0000",
            "1.0000, 3.0000",
            "10.0000, 5.0000",
            "-10.0000, 2.0000",
            "1.2345, 0.1234"
    })
    public void testDivide(String val1, String val2) {
        ValueFastDecimal d1 = ValueFastDecimal.of(val1);
        ValueFastDecimal d2 = ValueFastDecimal.of(val2);
        ValueFastDecimal result = d1.divide(d2);

        BigDecimal expected = new BigDecimal(val1).divide(new BigDecimal(val2), SCALE_DIGITS, RoundingMode.HALF_UP);
        assertEquals(expected, result.toBigDecimal(SCALE_DIGITS));
    }

    @Test
    public void testToString() {
        String val = "123.4567";
        ValueFastDecimal d = ValueFastDecimal.of(val);
        assertEquals("123.4567", d.toString());
    }
}
