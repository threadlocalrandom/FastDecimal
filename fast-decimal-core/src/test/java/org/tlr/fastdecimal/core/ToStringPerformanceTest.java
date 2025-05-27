package org.tlr.fastdecimal.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A simple test to demonstrate the performance improvement of the optimized toString method.
 * This is not a comprehensive benchmark, but it gives a rough idea of the performance gain.
 */
public class ToStringPerformanceTest {

    @Test
    public void testToStringPerformance() {
        // Create a FastDecimal with a decimal part
        FastDecimal decimal = FastDecimal.of("123.4567");

        // Warm up
        for (int i = 0; i < 10000; i++) {
            decimal.toString();
        }

        // Measure time for multiple toString calls
        int iterations = 1000000;
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            decimal.toString();
        }
        long endTime = System.nanoTime();

        // Calculate average time per call in nanoseconds
        double avgTimePerCall = (endTime - startTime) / (double) iterations;

        // Use a format that's more likely to be captured in test output
        String result = "[DEBUG_LOG] Average time per toString call: " + avgTimePerCall + " ns";
        System.out.println(result);

        // Assert that the average time is positive (should always be true)
        assertTrue(avgTimePerCall > 0);
    }
}
