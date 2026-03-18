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

package org.tlr.fastdecimal.vector;

import org.junit.jupiter.api.Test;
import org.tlr.fastdecimal.core.FastDecimal;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Comprehensive division tests for VectorFastDecimal.
 * These tests use FastDecimal.divide as the source of truth.
 */
public class VectorFastDecimalDivisionTest {

    @Test
    public void testDivideSmallArray() {
        testDivision(10, false);
    }

    @Test
    public void testDivideLargeArray() {
        testDivision(1000, false);
    }

    @Test
    public void testDivideParallelSmallArray() {
        // Should fall back to serial divide
        testDivision(10, true);
    }

    @Test
    public void testDivideParallelLargeArray() {
        // Should use parallel processing
        testDivision(2000, true);
    }

    @Test
    public void testDivideEdgeCases() {
        FastDecimal[] a = {
                FastDecimal.ZERO,
                FastDecimal.ONE,
                FastDecimal.TEN,
                FastDecimal.of("-1.0"),
                FastDecimal.of("0.0001"),
                FastDecimal.of("999999.9999"),
                FastDecimal.of("-999999.9999"),
                FastDecimal.of("1.0"),
                FastDecimal.of("-1.0")
        };
        FastDecimal[] b = {
                FastDecimal.of("5.5"),
                FastDecimal.ONE,
                FastDecimal.of("0.1"),
                FastDecimal.of("-1.0"),
                FastDecimal.of("10000"),
                FastDecimal.of("0.0001"),
                FastDecimal.of("-1"),
                FastDecimal.of("3.0"),
                FastDecimal.of("3.0")
        };

        verifyDivision(a, b, false);
        verifyDivision(a, b, true);
    }

    @Test
    public void testDivisionByZero() {
        FastDecimal[] a = {FastDecimal.ONE};
        FastDecimal[] b = {FastDecimal.ZERO};

        assertThrows(ArithmeticException.class, () -> VectorFastDecimal.divide(a, b),
                "Division by zero should throw an ArithmeticException");
        assertThrows(ArithmeticException.class, () -> VectorFastDecimal.divideParallel(a, b),
                "Division by zero should throw an ArithmeticException");
    }

    private void testDivision(int size, boolean parallel) {
        FastDecimal[] a = createRandomArray(size, 12345L);
        FastDecimal[] b = createRandomNonZeroArray(size, 67890L);

        verifyDivision(a, b, parallel);
    }

    private void verifyDivision(FastDecimal[] a, FastDecimal[] b, boolean parallel) {
        // Compute expected results using regular FastDecimal.divide
        FastDecimal[] expected = new FastDecimal[a.length];
        for (int i = 0; i < a.length; i++) {
            expected[i] = a[i].divide(b[i]);
        }

        // Compute actual results using VectorFastDecimal
        FastDecimal[] actual;
        if (parallel) {
            actual = VectorFastDecimal.divideParallel(a, b);
        } else {
            actual = VectorFastDecimal.divide(a, b);
        }

        // Verify results
        assertArrayEquals(expected, actual, "Vector division should match FastDecimal.divide results" + (parallel ? " (parallel)" : ""));
    }

    private FastDecimal[] createRandomArray(int size, long seed) {
        Random random = new Random(seed);
        FastDecimal[] array = new FastDecimal[size];
        for (int i = 0; i < size; i++) {
            // Random values between -1000 and 1000 with 4 decimal places
            double value = (random.nextDouble() * 2000) - 1000;
            array[i] = FastDecimal.of(String.format("%.4f", value));
        }
        return array;
    }

    private FastDecimal[] createRandomNonZeroArray(int size, long seed) {
        Random random = new Random(seed);
        FastDecimal[] array = new FastDecimal[size];
        for (int i = 0; i < size; i++) {
            // Random values between -100 and 100, but ensuring it's not too close to zero
            double value;
            do {
                value = (random.nextDouble() * 200) - 100;
            } while (Math.abs(value) < 0.001);
            array[i] = FastDecimal.of(String.format("%.4f", value));
        }
        return array;
    }
}
