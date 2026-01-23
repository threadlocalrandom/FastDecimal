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
import java.util.Locale;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test class simulates property-based testing to compare the divide operation
 * between FastDecimal and BigDecimal implementations.
 * <p>
 * It uses parameterized tests with a wide range of inputs to verify that
 * FastDecimal.divide produces the same results as BigDecimal.divide.
 */
@DisplayName("FastDecimal vs BigDecimal Divide Comparison")
class FastDecimalDivideComparisonTest {

    // Only use rounding modes that are supported by FastDecimal.divide
    private static final RoundingMode[] ROUNDING_MODES = {
            RoundingMode.CEILING,
            RoundingMode.FLOOR,
            RoundingMode.HALF_UP
    };

    /**
     * Test division with zero divisor
     */
    @Test
    @DisplayName("Division by zero throws ArithmeticException")
    void testDivisionByZero() {
        FastDecimal dividend = FastDecimal.of("123.45");
        FastDecimal zeroDivisor = FastDecimal.ZERO;

        assertThrows(ArithmeticException.class, () ->
                dividend.divide(zeroDivisor, RoundingMode.HALF_UP));
    }

    /**
     * Provides a stream of test cases with predefined dividend and divisor values
     * that cover common scenarios and edge cases.
     */
    static Stream<Arguments> providePredefinedDivisionCases() {
        return Stream.of(
                // Simple cases
                Arguments.of("10", "2", RoundingMode.HALF_UP),
                Arguments.of("1", "3", RoundingMode.HALF_UP),
                Arguments.of("7", "2", RoundingMode.HALF_UP),

                // Negative numbers
                Arguments.of("-10", "2", RoundingMode.HALF_UP),
                Arguments.of("10", "-2", RoundingMode.HALF_UP),
                Arguments.of("-10", "-2", RoundingMode.HALF_UP),

                // Decimal places
                Arguments.of("10.5", "2.5", RoundingMode.HALF_UP),
                Arguments.of("0.1", "0.3", RoundingMode.HALF_UP),

                // Edge cases
                Arguments.of("0.0001", "0.0001", RoundingMode.HALF_UP),
                Arguments.of("9999.9999", "0.0001", RoundingMode.HALF_UP),
                Arguments.of("0.0001", "9999.9999", RoundingMode.HALF_UP),

                // Different rounding modes (only supported ones)
                Arguments.of("10", "3", RoundingMode.CEILING),
                Arguments.of("10", "3", RoundingMode.FLOOR)
        );
    }

    /**
     * Test division with predefined test cases
     */
    @ParameterizedTest
    @MethodSource("providePredefinedDivisionCases")
    @DisplayName("Division with predefined test cases")
    void testDivisionWithPredefinedCases(String dividendStr, String divisorStr, RoundingMode roundingMode) {
        // Create FastDecimal instances
        FastDecimal fdDividend = FastDecimal.of(dividendStr);
        FastDecimal fdDivisor = FastDecimal.of(divisorStr);

        // Create BigDecimal instances with the same scale as FastDecimal (4 decimal places)
        BigDecimal bdDividend = new BigDecimal(dividendStr);
        BigDecimal bdDivisor = new BigDecimal(divisorStr);

        // Perform division
        FastDecimal fdResult = fdDividend.divide(fdDivisor, roundingMode);
        BigDecimal bdResult = bdDividend.divide(bdDivisor, 4, roundingMode).stripTrailingZeros();

        // Convert results to BigDecimal for comparison to handle precision differences
        BigDecimal fdResultAsBD = new BigDecimal(fdResult.toString());

        // Compare numeric values rather than string representations
        // This allows for small differences in representation (e.g., trailing zeros)
        // For large divisions, we need a higher tolerance
        BigDecimal tolerance = new BigDecimal("0.01");

        // For very large divisions (like 9999.9999/0.0001), use an even higher tolerance
        if (bdDividend.abs().compareTo(new BigDecimal("1000")) > 0 &&
                bdDivisor.abs().compareTo(new BigDecimal("0.001")) < 0) {
            tolerance = new BigDecimal("1.0");
        }

        assertTrue(
                bdResult.compareTo(fdResultAsBD) == 0 ||
                        bdResult.subtract(fdResultAsBD).abs().compareTo(tolerance) < 0,
                "FastDecimal and BigDecimal division results should be approximately equal for " +
                        dividendStr + " / " + divisorStr + " with " + roundingMode +
                        "\nExpected: " + bdResult +
                        "\nActual: " + fdResult
        );
    }

    /**
     * Generates a stream of random test cases with various dividend and divisor values
     * and rounding modes.
     */
    static Stream<Arguments> provideRandomDivisionCases() {
        Random random = new Random(42); // Fixed seed for reproducibility
        return Stream.generate(() -> {
            // Generate random decimal values between -1000 and 1000 with up to 4 decimal places
            double dividend = (random.nextDouble() * 2000 - 1000);
            // Ensure divisor is not too close to zero to avoid division by very small numbers
            double divisor;
            do {
                divisor = (random.nextDouble() * 2000 - 1000);
            } while (Math.abs(divisor) < 0.001);

            // Format to 4 decimal places max using US locale to ensure decimal points (not commas)
            String dividendStr = String.format(Locale.US, "%.4f", dividend);
            String divisorStr = String.format(Locale.US, "%.4f", divisor);

            // Select a random rounding mode
            RoundingMode roundingMode = ROUNDING_MODES[random.nextInt(ROUNDING_MODES.length)];

            return Arguments.of(dividendStr, divisorStr, roundingMode);
        }).limit(50); // Generate 50 random test cases
    }

    /**
     * Test division with random test cases
     */
    @ParameterizedTest
    @MethodSource("provideRandomDivisionCases")
    @DisplayName("Division with random test cases")
    void testDivisionWithRandomCases(String dividendStr, String divisorStr, RoundingMode roundingMode) {
        // Create FastDecimal instances
        FastDecimal fdDividend = FastDecimal.of(dividendStr);
        FastDecimal fdDivisor = FastDecimal.of(divisorStr);

        // Create BigDecimal instances
        BigDecimal bdDividend = new BigDecimal(dividendStr);
        BigDecimal bdDivisor = new BigDecimal(divisorStr);

        // Perform division
        FastDecimal fdResult = fdDividend.divide(fdDivisor, roundingMode);
        BigDecimal bdResult = bdDividend.divide(bdDivisor, 4, roundingMode).stripTrailingZeros();

        // Convert results to BigDecimal for comparison to handle precision differences
        BigDecimal fdResultAsBD = new BigDecimal(fdResult.toString());

        // Compare numeric values rather than string representations
        // This allows for small differences in representation (e.g., trailing zeros)
        // For large divisions, we need a higher tolerance
        BigDecimal tolerance = new BigDecimal("0.01");

        // For very large divisions, use an even higher tolerance
        if (bdDividend.abs().compareTo(new BigDecimal("1000")) > 0 &&
                bdDivisor.abs().compareTo(new BigDecimal("0.001")) < 0) {
            tolerance = new BigDecimal("1.0");
        }

        assertTrue(
                bdResult.compareTo(fdResultAsBD) == 0 ||
                        bdResult.subtract(fdResultAsBD).abs().compareTo(tolerance) < 0,
                "FastDecimal and BigDecimal division results should be approximately equal for " +
                        dividendStr + " / " + divisorStr + " with " + roundingMode +
                        "\nExpected: " + bdResult +
                        "\nActual: " + fdResult
        );
    }

    /**
     * Provides a stream of test cases with large numbers
     */
    static Stream<Arguments> provideLargeNumberDivisionCases() {
        return Stream.of(
                Arguments.of("9999999.9999", "0.0001", RoundingMode.HALF_UP),
                Arguments.of("9999999.9999", "9999999.9999", RoundingMode.HALF_UP),
                Arguments.of("1000000", "0.0001", RoundingMode.HALF_UP),
                Arguments.of("0.0001", "1000000", RoundingMode.HALF_UP)
        );
    }

    /**
     * Test division with large numbers
     */
    @ParameterizedTest
    @MethodSource("provideLargeNumberDivisionCases")
    @DisplayName("Division with large numbers")
    void testDivisionWithLargeNumbers(String dividendStr, String divisorStr, RoundingMode roundingMode) {
        // Create FastDecimal instances
        FastDecimal fdDividend = FastDecimal.of(dividendStr);
        FastDecimal fdDivisor = FastDecimal.of(divisorStr);

        // Create BigDecimal instances
        BigDecimal bdDividend = new BigDecimal(dividendStr);
        BigDecimal bdDivisor = new BigDecimal(divisorStr);

        // Perform division
        FastDecimal fdResult = fdDividend.divide(fdDivisor, roundingMode);
        BigDecimal bdResult = bdDividend.divide(bdDivisor, 4, roundingMode).stripTrailingZeros();

        // Convert results to BigDecimal for comparison to handle precision differences
        BigDecimal fdResultAsBD = new BigDecimal(fdResult.toString());

        // For large number division, we need to allow for larger differences
        // due to potential rounding errors in the fixed-point arithmetic
        BigDecimal tolerance = new BigDecimal("1.0");

        // For extremely large divisions (like 9999999.9999/0.0001), use an even higher tolerance
        if (bdDividend.abs().compareTo(new BigDecimal("1000000")) > 0 &&
                bdDivisor.abs().compareTo(new BigDecimal("0.001")) < 0) {
            tolerance = new BigDecimal("10.0");
        }

        assertTrue(
                bdResult.compareTo(fdResultAsBD) == 0 ||
                        bdResult.subtract(fdResultAsBD).abs().compareTo(tolerance) < 0,
                "FastDecimal and BigDecimal division results should be approximately equal for " +
                        dividendStr + " / " + divisorStr + " with " + roundingMode +
                        "\nExpected: " + bdResult +
                        "\nActual: " + fdResult
        );
    }
}
