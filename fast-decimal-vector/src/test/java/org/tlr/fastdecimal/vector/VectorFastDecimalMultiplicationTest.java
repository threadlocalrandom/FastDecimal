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

/**
 * Comprehensive multiplication tests for VectorFastDecimal.
 * These tests use FastDecimal.multiply as the source of truth.
 */
public class VectorFastDecimalMultiplicationTest {

    private static final int PARALLEL_THRESHOLD = 16 * 8; // Approximation based on VectorFastDecimal

    @Test
    public void testMultiplySmallArray() {
        testMultiplication(10, false);
    }

    @Test
    public void testMultiplyLargeArray() {
        // Large enough to trigger parallel logic if multiplyParallel is used,
        // or at least to test many vector chunks in serial multiply.
        testMultiplication(1000, false);
    }

    @Test
    public void testMultiplyParallelSmallArray() {
        // Should fall back to serial multiply
        testMultiplication(10, true);
    }

    @Test
    public void testMultiplyParallelLargeArray() {
        // Should use parallel processing
        testMultiplication(2000, true);
    }

    @Test
    public void testMultiplyEdgeCases() {
        FastDecimal[] a = {
                FastDecimal.ZERO,
                FastDecimal.ONE,
                FastDecimal.TEN,
                FastDecimal.of("-1.0"),
                FastDecimal.of("0.0001"),
                FastDecimal.of("999999.9999"),
                FastDecimal.of("-999999.9999")
        };
        FastDecimal[] b = {
                FastDecimal.of("5.5"),
                FastDecimal.ZERO,
                FastDecimal.of("0.1"),
                FastDecimal.of("-1.0"),
                FastDecimal.of("10000"),
                FastDecimal.of("0.0001"),
                FastDecimal.of("-1")
        };

        verifyMultiplication(a, b, false);
        verifyMultiplication(a, b, true);
    }

    private void testMultiplication(int size, boolean parallel) {
        FastDecimal[] a = createRandomArray(size, 12345L);
        FastDecimal[] b = createRandomArray(size, 67890L);

        verifyMultiplication(a, b, parallel);
    }

    private void verifyMultiplication(FastDecimal[] a, FastDecimal[] b, boolean parallel) {
        // Compute expected results using regular FastDecimal.multiply
        FastDecimal[] expected = new FastDecimal[a.length];
        for (int i = 0; i < a.length; i++) {
            expected[i] = a[i].multiply(b[i]);
        }

        // Compute actual results using VectorFastDecimal
        FastDecimal[] actual;
        if (parallel) {
            actual = VectorFastDecimal.multiplyParallel(a, b);
        } else {
            actual = VectorFastDecimal.multiply(a, b);
        }

        // Verify results
        assertArrayEquals(expected, actual, "Vector multiplication should match FastDecimal.multiply results" + (parallel ? " (parallel)" : ""));
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
}
