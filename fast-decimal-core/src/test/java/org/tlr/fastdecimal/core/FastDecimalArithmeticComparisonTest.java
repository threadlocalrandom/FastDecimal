/*
 * *
 *  MIT License
 *
 *  Copyright (c) 2025 Andy Bailey
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

package org.tlr.fastdecimal.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Comparison tests for add, subtract, and multiply using BigDecimal to compute expected values.
 */
@DisplayName("FastDecimal vs BigDecimal Add/Subtract/Multiply Comparison")
class FastDecimalArithmeticComparisonTest {

    private static BigDecimal toBD(String s) {
        return new BigDecimal(s).setScale(4, RoundingMode.HALF_UP);
    }

    private static String normalizeToPlainString(BigDecimal bd) {
        return bd.toPlainString();
    }

    static Stream<Arguments> provideArithmeticCases() {
        return Stream.of(
                // Addition cases
                Arguments.of("1.23", "4.56", Operation.ADD),
                Arguments.of("-1.23", "4.56", Operation.ADD),
                Arguments.of("1.23", "-4.56", Operation.ADD),
                Arguments.of("-1.23", "-4.56", Operation.ADD),
                Arguments.of("0", "0", Operation.ADD),
                Arguments.of("9999.9999", "0.0001", Operation.ADD),

                // Subtraction cases
                Arguments.of("4.56", "1.23", Operation.SUBTRACT),
                Arguments.of("1.23", "4.56", Operation.SUBTRACT),
                Arguments.of("-1.23", "4.56", Operation.SUBTRACT),
                Arguments.of("1.23", "-4.56", Operation.SUBTRACT),
                Arguments.of("-1.23", "-4.56", Operation.SUBTRACT),
                Arguments.of("0.0000", "0.0000", Operation.SUBTRACT),

                // Multiplication cases
                Arguments.of("2.5", "3", Operation.MULTIPLY),
                Arguments.of("-2.5", "3", Operation.MULTIPLY),
                Arguments.of("2.5", "-3", Operation.MULTIPLY),
                Arguments.of("-2.5", "-3", Operation.MULTIPLY),
                Arguments.of("0.0001", "9999.9999", Operation.MULTIPLY),
                Arguments.of("123.4567", "0.0000", Operation.MULTIPLY)
        );
    }

    enum Operation {ADD, SUBTRACT, MULTIPLY}

    private static String expected(BigDecimal a, BigDecimal b, Operation op) {
        return switch (op) {
            case ADD -> normalizeToPlainString(a.add(b));
            case SUBTRACT -> normalizeToPlainString(a.subtract(b));
            case MULTIPLY -> normalizeToPlainString(a.multiply(b).setScale(4, RoundingMode.HALF_UP));
        };
    }

    @ParameterizedTest
    @MethodSource("provideArithmeticCases")
    @DisplayName("Predefined cases: FastDecimal equals BigDecimal for add/subtract/multiply")
    void testPredefined(String aStr, String bStr, Operation op) {
        FastDecimal a = FastDecimal.of(aStr);
        FastDecimal b = FastDecimal.of(bStr);

        FastDecimal result = switch (op) {
            case ADD -> a.add(b);
            case SUBTRACT -> a.subtract(b);
            case MULTIPLY -> a.multiply(b);
        };

        BigDecimal bdA = toBD(aStr);
        BigDecimal bdB = toBD(bStr);
        String expectedStr = expected(bdA, bdB, op);

        assertEquals(expectedStr, result.toString(),
                () -> "Mismatch for " + op + " with " + aStr + " and " + bStr +
                        "\nExpected: " + expectedStr + "\nActual:   " + result);
    }

    @Test
    @DisplayName("Randomized cases: compare against BigDecimal")
    void testRandomized() {
        Random rnd = new Random(12345);
        for (int i = 0; i < 500; i++) {
            String aStr = randomFastDecimalString(rnd);
            String bStr = randomFastDecimalString(rnd);

            FastDecimal a = FastDecimal.of(aStr);
            FastDecimal b = FastDecimal.of(bStr);

            // Test three operations per pair
            for (Operation op : Operation.values()) {
                FastDecimal fdRes = switch (op) {
                    case ADD -> a.add(b);
                    case SUBTRACT -> a.subtract(b);
                    case MULTIPLY -> a.multiply(b);
                };

                BigDecimal bdA = toBD(aStr);
                BigDecimal bdB = toBD(bStr);
                String expectedStr = expected(bdA, bdB, op);

                assertEquals(expectedStr, fdRes.toString(),
                        () -> "Random mismatch for " + op + " with " + aStr + " and " + bStr +
                                "\nExpected: " + expectedStr + "\nActual:   " + fdRes);
            }
        }
    }

    private static String randomFastDecimalString(Random rnd) {
        // Generate values within a range to avoid overflow in multiply when scaled by 10,000
        // Max absolute whole part up to 1_000_000 to be safe (though FastDecimal may handle larger)
        long whole = rnd.nextInt(10000) - 5000; // [-5000, 4999]
        int frac = rnd.nextInt(10000); // 0..9999
        boolean negative = rnd.nextBoolean();
        if (whole == 0 && frac == 0) negative = false; // keep zero positive for variety
        String s = (negative ? "-" : "") + Math.abs(whole) + "." + String.format(java.util.Locale.ROOT, "%04d", frac);
        // Occasionally return integer-like strings without decimals to add variety
        if (rnd.nextDouble() < 0.2) {
            s = (negative ? "-" : "") + Math.abs(whole);
        }
        return s;
    }
}
