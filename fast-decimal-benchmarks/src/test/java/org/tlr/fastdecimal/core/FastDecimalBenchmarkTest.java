package org.tlr.fastdecimal.core;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class to verify that the FastDecimalBenchmark works correctly.
 * This test runs a minimal subset of the benchmarks with minimal iterations
 * to ensure the benchmark infrastructure is working.
 */
public class FastDecimalBenchmarkTest {

    @Test
    public void testBenchmarkRuns() throws Exception {
        // Configure a minimal benchmark run
        Options options = new OptionsBuilder()
                .include(FastDecimalBenchmark.class.getSimpleName() + ".createFastDecimalFromString")
                .include(FastDecimalBenchmark.class.getSimpleName() + ".createBigDecimalFromString")
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(1))
                .forks(1)
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.NANOSECONDS)
                .build();

        // Run the benchmark
        Collection<RunResult> results = new Runner(options).run();

        // Verify that we got results
        assertFalse(results.isEmpty(), "Benchmark should produce results");

        // Print the results for debugging
        System.out.println("[DEBUG_LOG] Benchmark test results:");
        results.forEach(result -> {
            String benchmark = result.getPrimaryResult().getLabel();
            double score = result.getPrimaryResult().getScore();
            System.out.println("[DEBUG_LOG] " + benchmark + ": " + score + " ns/op");
        });

        // Verify that FastDecimal is included in the results
        boolean hasFastDecimalResult = results.stream()
                .anyMatch(r -> r.getPrimaryResult().getLabel().contains("FastDecimal"));
        assertTrue(hasFastDecimalResult, "Results should include FastDecimal benchmark");

        // Verify that BigDecimal is included in the results
        boolean hasBigDecimalResult = results.stream()
                .anyMatch(r -> r.getPrimaryResult().getLabel().contains("BigDecimal"));
        assertTrue(hasBigDecimalResult, "Results should include BigDecimal benchmark");
    }
}