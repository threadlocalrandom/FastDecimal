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

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark to compare the performance of BigDecimal.toString() and FastDecimal.toString().
 * This benchmark measures the time taken to convert decimal numbers to strings.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class ToStringBenchmark {

    // Small numbers (few digits)
    private FastDecimal smallFastDecimal;
    private BigDecimal smallBigDecimal;

    // Medium numbers (moderate number of digits)
    private FastDecimal mediumFastDecimal;
    private BigDecimal mediumBigDecimal;

    // Large numbers (many digits)
    private FastDecimal largeFastDecimal;
    private BigDecimal largeBigDecimal;

    // Numbers with decimal parts
    private FastDecimal decimalFastDecimal;
    private BigDecimal decimalBigDecimal;

    @Setup
    public void setup() {
        // Initialize small numbers
        smallFastDecimal = FastDecimal.of("12.34");
        smallBigDecimal = new BigDecimal("12.34");

        // Initialize medium numbers
        mediumFastDecimal = FastDecimal.of("1234567.89");
        mediumBigDecimal = new BigDecimal("1234567.89");

        // Initialize large numbers
        largeFastDecimal = FastDecimal.of("9876543210.12345");
        largeBigDecimal = new BigDecimal("9876543210.12345");

        // Initialize numbers with decimal parts
        decimalFastDecimal = FastDecimal.of("123.4567");
        decimalBigDecimal = new BigDecimal("123.4567");
    }

    @Benchmark
    public void toStringSmallFastDecimal(Blackhole blackhole) {
        // Call toString multiple times to get more realistic results
        for (int i = 0; i < 100; i++) {
            blackhole.consume(smallFastDecimal.toString());
        }
    }

    @Benchmark
    public void toStringSmallBigDecimal(Blackhole blackhole) {
        // Call toString multiple times to get more realistic results
        for (int i = 0; i < 100; i++) {
            blackhole.consume(smallBigDecimal.toString());
        }
    }

    @Benchmark
    public void toStringMediumFastDecimal(Blackhole blackhole) {
        // Call toString multiple times to get more realistic results
        for (int i = 0; i < 100; i++) {
            blackhole.consume(mediumFastDecimal.toString());
        }
    }

    @Benchmark
    public void toStringMediumBigDecimal(Blackhole blackhole) {
        // Call toString multiple times to get more realistic results
        for (int i = 0; i < 100; i++) {
            blackhole.consume(mediumBigDecimal.toString());
        }
    }

    @Benchmark
    public void toStringLargeFastDecimal(Blackhole blackhole) {
        // Call toString multiple times to get more realistic results
        for (int i = 0; i < 100; i++) {
            blackhole.consume(largeFastDecimal.toString());
        }
    }

    @Benchmark
    public void toStringLargeBigDecimal(Blackhole blackhole) {
        // Call toString multiple times to get more realistic results
        for (int i = 0; i < 100; i++) {
            blackhole.consume(largeBigDecimal.toString());
        }
    }

    @Benchmark
    public void toStringDecimalFastDecimal(Blackhole blackhole) {
        // Call toString multiple times to get more realistic results
        for (int i = 0; i < 100; i++) {
            blackhole.consume(decimalFastDecimal.toString());
        }
    }

    @Benchmark
    public void toStringDecimalBigDecimal(Blackhole blackhole) {
        // Call toString multiple times to get more realistic results
        for (int i = 0; i < 100; i++) {
            blackhole.consume(decimalBigDecimal.toString());
        }
    }

    /**
     * Main method to run the benchmark from the command line.
     *
     * @param args command line arguments (not used)
     * @throws RunnerException if an error occurs during the benchmark
     */
    @SuppressWarnings("unused")
    static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(ToStringBenchmark.class.getSimpleName())
                .build();
        new Runner(options).run();
    }
}
