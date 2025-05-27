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
 * Test class to verify that the ToStringBenchmark works correctly.
 * This test runs a minimal subset of the benchmarks with minimal iterations
 * to ensure the benchmark infrastructure is working.
 */
public class ToStringBenchmarkTest {

    @Test
    public void testToStringBenchmarkRuns() throws Exception {
        // Configure a more comprehensive benchmark run
        Options options = new OptionsBuilder()
                .include(ToStringBenchmark.class.getSimpleName() + ".toString.*")
                .warmupIterations(2)
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(3)
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
        System.out.println("[DEBUG_LOG] ToString Benchmark test results:");
        results.forEach(result -> {
            String benchmark = result.getPrimaryResult().getLabel();
            double score = result.getPrimaryResult().getScore();
            System.out.println("[DEBUG_LOG] " + benchmark + ": " + score + " ns/op");
        });

        // Group and compare FastDecimal vs BigDecimal results
        System.out.println("[DEBUG_LOG] Performance comparison (FastDecimal vs BigDecimal):");

        // Compare small number toString performance
        double smallFastDecimalScore = results.stream()
                .filter(r -> r.getPrimaryResult().getLabel().contains("toStringSmallFastDecimal"))
                .findFirst()
                .map(r -> r.getPrimaryResult().getScore())
                .orElse(0.0);

        double smallBigDecimalScore = results.stream()
                .filter(r -> r.getPrimaryResult().getLabel().contains("toStringSmallBigDecimal"))
                .findFirst()
                .map(r -> r.getPrimaryResult().getScore())
                .orElse(0.0);

        if (smallFastDecimalScore > 0 && smallBigDecimalScore > 0) {
            double ratio = smallBigDecimalScore / smallFastDecimalScore;
            String message = "Small numbers - FastDecimal is " +
                    String.format("%.2f", ratio) + "x " +
                    (ratio > 1 ? "faster" : "slower") + " than BigDecimal";
            System.out.println("[DEBUG_LOG] " + message);

            // We're just logging the results, not asserting anything
            // The actual performance may vary depending on the environment
        }

        // Compare medium number toString performance
        double mediumFastDecimalScore = results.stream()
                .filter(r -> r.getPrimaryResult().getLabel().contains("toStringMediumFastDecimal"))
                .findFirst()
                .map(r -> r.getPrimaryResult().getScore())
                .orElse(0.0);

        double mediumBigDecimalScore = results.stream()
                .filter(r -> r.getPrimaryResult().getLabel().contains("toStringMediumBigDecimal"))
                .findFirst()
                .map(r -> r.getPrimaryResult().getScore())
                .orElse(0.0);

        if (mediumFastDecimalScore > 0 && mediumBigDecimalScore > 0) {
            double ratio = mediumBigDecimalScore / mediumFastDecimalScore;
            String message = "Medium numbers - FastDecimal is " +
                    String.format("%.2f", ratio) + "x " +
                    (ratio > 1 ? "faster" : "slower") + " than BigDecimal";
            System.out.println("[DEBUG_LOG] " + message);

            // We're just logging the results, not asserting anything
            // The actual performance may vary depending on the environment
        }

        // Compare large number toString performance
        double largeFastDecimalScore = results.stream()
                .filter(r -> r.getPrimaryResult().getLabel().contains("toStringLargeFastDecimal"))
                .findFirst()
                .map(r -> r.getPrimaryResult().getScore())
                .orElse(0.0);

        double largeBigDecimalScore = results.stream()
                .filter(r -> r.getPrimaryResult().getLabel().contains("toStringLargeBigDecimal"))
                .findFirst()
                .map(r -> r.getPrimaryResult().getScore())
                .orElse(0.0);

        if (largeFastDecimalScore > 0 && largeBigDecimalScore > 0) {
            double ratio = largeBigDecimalScore / largeFastDecimalScore;
            String message = "Large numbers - FastDecimal is " +
                    String.format("%.2f", ratio) + "x " +
                    (ratio > 1 ? "faster" : "slower") + " than BigDecimal";
            System.out.println("[DEBUG_LOG] " + message);

            // We're just logging the results, not asserting anything
            // The actual performance may vary depending on the environment
        }

        // Compare decimal number toString performance
        double decimalFastDecimalScore = results.stream()
                .filter(r -> r.getPrimaryResult().getLabel().contains("toStringDecimalFastDecimal"))
                .findFirst()
                .map(r -> r.getPrimaryResult().getScore())
                .orElse(0.0);

        double decimalBigDecimalScore = results.stream()
                .filter(r -> r.getPrimaryResult().getLabel().contains("toStringDecimalBigDecimal"))
                .findFirst()
                .map(r -> r.getPrimaryResult().getScore())
                .orElse(0.0);

        if (decimalFastDecimalScore > 0 && decimalBigDecimalScore > 0) {
            double ratio = decimalBigDecimalScore / decimalFastDecimalScore;
            String message = "Decimal numbers - FastDecimal is " +
                    String.format("%.2f", ratio) + "x " +
                    (ratio > 1 ? "faster" : "slower") + " than BigDecimal";
            System.out.println("[DEBUG_LOG] " + message);

            // We're just logging the results, not asserting anything
            // The actual performance may vary depending on the environment
        }

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
