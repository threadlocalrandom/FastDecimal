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
 * JMH benchmark to compare the performance of BigDecimal and FastDecimal.
 * <p>
 * To run the benchmark:
 * 1. Build the project with: mvn clean package
 * 2. Run the benchmark with: java -jar target/benchmarks.jar
 */
@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 20, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 20, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"--add-modules=jdk.incubator.vector", "--enable-preview"})
@State(Scope.Benchmark)
public class ValueFastDecimalBenchmark {

    // Values for benchmarking
    private final String stringValue1 = "123.4567";
    private final double doubleValue1 = 123.4567;

    // Pre-created instances for operation benchmarks
    private ValueFastDecimal valueFastDecimal1;
    private ValueFastDecimal valueFastDecimal2;
    private BigDecimal bigDecimal1;
    private BigDecimal bigDecimal2;

    @Setup
    public void setup() {
        // Initialize the pre-created instances
        valueFastDecimal1 = ValueFastDecimal.of(stringValue1);
        String stringValue2 = "98.7654";
        valueFastDecimal2 = ValueFastDecimal.of(stringValue2);
        bigDecimal1 = new BigDecimal(stringValue1);
        bigDecimal2 = new BigDecimal(stringValue2);
    }

    // Creation benchmarks

    @Benchmark
    public void createFastDecimalFromString(Blackhole blackhole) {
        blackhole.consume(ValueFastDecimal.of(stringValue1));
    }

    @Benchmark
    public void createBigDecimalFromString(Blackhole blackhole) {
        blackhole.consume(new BigDecimal(stringValue1));
    }

    @Benchmark
    public void createFastDecimalFromDouble(Blackhole blackhole) {
        blackhole.consume(ValueFastDecimal.of(doubleValue1));
    }

    @Benchmark
    public void createBigDecimalFromDouble(Blackhole blackhole) {
        blackhole.consume(BigDecimal.valueOf(doubleValue1));
    }

    // Addition benchmarks

    @Benchmark
    public void addFastDecimal(Blackhole blackhole) {
        blackhole.consume(valueFastDecimal1.add(valueFastDecimal2));
    }

    @Benchmark
    public void addBigDecimal(Blackhole blackhole) {
        blackhole.consume(bigDecimal1.add(bigDecimal2));
    }

    // Subtraction benchmarks

    @Benchmark
    public void subtractFastDecimal(Blackhole blackhole) {
        blackhole.consume(valueFastDecimal1.subtract(valueFastDecimal2));
    }

    @Benchmark
    public void subtractBigDecimal(Blackhole blackhole) {
        blackhole.consume(bigDecimal1.subtract(bigDecimal2));
    }

    // Multiplication benchmarks

    @Benchmark
    public void multiplyFastDecimal(Blackhole blackhole) {
        blackhole.consume(valueFastDecimal1.multiply(valueFastDecimal2));
    }

    @Benchmark
    public void multiplyBigDecimal(Blackhole blackhole) {
        blackhole.consume(bigDecimal1.multiply(bigDecimal2));
    }

    // Division benchmarks

    @Benchmark
    public void divideFastDecimal(Blackhole blackhole) {
        blackhole.consume(valueFastDecimal1.divide(valueFastDecimal2));
    }

    @Benchmark
    public void divideBigDecimal(Blackhole blackhole) {
        blackhole.consume(bigDecimal1.divide(bigDecimal2, RoundingMode.HALF_UP));
    }

    // Comparison benchmarks

    @Benchmark
    public void compareFastDecimal(Blackhole blackhole) {
        blackhole.consume(valueFastDecimal1.compareTo(valueFastDecimal2));
    }

    @Benchmark
    public void compareBigDecimal(Blackhole blackhole) {
        blackhole.consume(bigDecimal1.compareTo(bigDecimal2));
    }

    // String conversion benchmarks

    @Benchmark
    public void toStringFastDecimal(Blackhole blackhole) {
        blackhole.consume(valueFastDecimal1.toString());
    }

    @Benchmark
    public void toStringBigDecimal(Blackhole blackhole) {
        blackhole.consume(bigDecimal1.toString());
    }

    /**
     * Main method to run the benchmark from IDE.
     */
    @SuppressWarnings("unused")
    static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(ValueFastDecimalBenchmark.class.getSimpleName())
                .build();
        new Runner(options).run();
    }
}
