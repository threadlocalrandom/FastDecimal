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
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

/**
 * A more comprehensive benchmark comparing FastDecimal and BigDecimal
 * with different scales and magnitudes of numbers.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class ComprehensiveBenchmark {

    // Pre-created instances
    private FastDecimal smallFast1;
    private FastDecimal smallFast2;
    private BigDecimal smallBig1;
    private BigDecimal smallBig2;

    private FastDecimal mediumFast1;
    private FastDecimal mediumFast2;
    private BigDecimal mediumBig1;
    private BigDecimal mediumBig2;

    private FastDecimal largeFast1;
    private FastDecimal largeFast2;
    private BigDecimal largeBig1;
    private BigDecimal largeBig2;

    private FastDecimal mixedFast1;
    private FastDecimal mixedFast2;
    private BigDecimal mixedBig1;
    private BigDecimal mixedBig2;

    @Setup
    public void setup() {
        // Initialize small numbers
        // Small numbers (1-2 digits)
        String smallValue1 = "5.7";
        smallFast1 = FastDecimal.of(smallValue1);
        String smallValue2 = "3.2";
        smallFast2 = FastDecimal.of(smallValue2);
        smallBig1 = new BigDecimal(smallValue1);
        smallBig2 = new BigDecimal(smallValue2);

        // Initialize medium numbers
        // Medium numbers (3-5 digits)
        String mediumValue1 = "123.45";
        mediumFast1 = FastDecimal.of(mediumValue1);
        String mediumValue2 = "67.89";
        mediumFast2 = FastDecimal.of(mediumValue2);
        mediumBig1 = new BigDecimal(mediumValue1);
        mediumBig2 = new BigDecimal(mediumValue2);

        // Initialize large numbers
        // Large numbers (close to the limit of FastDecimal)
        String largeValue1 = "9999999.9999";
        largeFast1 = FastDecimal.of(largeValue1);
        String largeValue2 = "8888888.8888";
        largeFast2 = FastDecimal.of(largeValue2);
        largeBig1 = new BigDecimal(largeValue1);
        largeBig2 = new BigDecimal(largeValue2);

        // Initialize mixed scale numbers
        // Numbers with different scales
        String mixedScale1 = "123.4567";
        mixedFast1 = FastDecimal.of(mixedScale1);
        String mixedScale2 = "98.76";
        mixedFast2 = FastDecimal.of(mixedScale2);
        mixedBig1 = new BigDecimal(mixedScale1);
        mixedBig2 = new BigDecimal(mixedScale2);
    }

    // Small number benchmarks

    @Benchmark
    public void addSmallFastDecimal(Blackhole blackhole) {
        blackhole.consume(smallFast1.add(smallFast2));
    }

    @Benchmark
    public void addSmallBigDecimal(Blackhole blackhole) {
        blackhole.consume(smallBig1.add(smallBig2));
    }

    @Benchmark
    public void multiplySmallFastDecimal(Blackhole blackhole) {
        blackhole.consume(smallFast1.multiply(smallFast2));
    }

    @Benchmark
    public void multiplySmallBigDecimal(Blackhole blackhole) {
        blackhole.consume(smallBig1.multiply(smallBig2));
    }

    // Medium number benchmarks

    @Benchmark
    public void addMediumFastDecimal(Blackhole blackhole) {
        blackhole.consume(mediumFast1.add(mediumFast2));
    }

    @Benchmark
    public void addMediumBigDecimal(Blackhole blackhole) {
        blackhole.consume(mediumBig1.add(mediumBig2));
    }

    @Benchmark
    public void multiplyMediumFastDecimal(Blackhole blackhole) {
        blackhole.consume(mediumFast1.multiply(mediumFast2));
    }

    @Benchmark
    public void multiplyMediumBigDecimal(Blackhole blackhole) {
        blackhole.consume(mediumBig1.multiply(mediumBig2));
    }

    // Large number benchmarks

    @Benchmark
    public void addLargeFastDecimal(Blackhole blackhole) {
        blackhole.consume(largeFast1.add(largeFast2));
    }

    @Benchmark
    public void addLargeBigDecimal(Blackhole blackhole) {
        blackhole.consume(largeBig1.add(largeBig2));
    }

    @Benchmark
    public void multiplyLargeFastDecimal(Blackhole blackhole) {
        blackhole.consume(largeFast1.multiply(largeFast2));
    }

    @Benchmark
    public void multiplyLargeBigDecimal(Blackhole blackhole) {
        blackhole.consume(largeBig1.multiply(largeBig2));
    }

    // Mixed scale benchmarks

    @Benchmark
    public void addMixedFastDecimal(Blackhole blackhole) {
        blackhole.consume(mixedFast1.add(mixedFast2));
    }

    @Benchmark
    public void addMixedBigDecimal(Blackhole blackhole) {
        blackhole.consume(mixedBig1.add(mixedBig2));
    }

    @Benchmark
    public void divideMixedFastDecimal(Blackhole blackhole) {
        blackhole.consume(mixedFast1.divide(mixedFast2));
    }

    @Benchmark
    public void divideMixedBigDecimal(Blackhole blackhole) {
        blackhole.consume(mixedBig1.divide(mixedBig2, RoundingMode.HALF_UP));
    }

    // Chained operations benchmark

    @Benchmark
    public void chainedOperationsFastDecimal(Blackhole blackhole) {
        // (a + b) * c - d
        FastDecimal result = smallFast1.add(smallFast2)
                .multiply(mediumFast1)
                .subtract(mediumFast2);
        blackhole.consume(result);
    }

    @Benchmark
    public void chainedOperationsBigDecimal(Blackhole blackhole) {
        // (a + b) * c - d
        BigDecimal result = smallBig1.add(smallBig2)
                .multiply(mediumBig1)
                .subtract(mediumBig2);
        blackhole.consume(result);
    }

    /**
     * Main method to run the benchmark from IDE.
     */
    @SuppressWarnings("unused")
    static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(ComprehensiveBenchmark.class.getSimpleName())
                .build();
        new Runner(options).run();
    }
}