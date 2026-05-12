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

package org.tlr.fastdecimal.core;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark                            Mode  Cnt  Score   Error  Units
 * FastDecimalBenchmark.addFastDecimal  avgt  100  3.263 ± 0.089  ns/op
 * <p>
 * Benchmark                           Mode  Cnt  Score   Error  Units
 * FastDecimalBenchmark.addBigDecimal  avgt  100  5.230 ± 0.124  ns/op
 * <p>
 * Benchmark                            Mode  Cnt  Score   Error  Units
 * FastDecimalBenchmark.addBigDecimal   avgt  100  5.347 ± 0.133  ns/op
 * FastDecimalBenchmark.addFastDecimal  avgt  100  3.395 ± 0.093  ns/op
 * <p>
 * Benchmark                                 Mode  Cnt  Score   Error  Units
 * FastDecimalBenchmark.subtractBigDecimal   avgt  100  5.873 ± 0.138  ns/op
 * FastDecimalBenchmark.subtractFastDecimal  avgt  100  3.327 ± 0.086  ns/op
 * <p>
 * Benchmark                                 Mode  Cnt  Score   Error  Units
 * FastDecimalBenchmark.multiplyBigDecimal   avgt  100  3.252 ± 0.008  ns/op
 * FastDecimalBenchmark.multiplyFastDecimal  avgt  100  2.612 ± 0.015  ns/op
 * <p>
 * Benchmark                               Mode  Cnt  Score   Error  Units
 * FastDecimalBenchmark.divideBigDecimal   avgt  100  6.328 ± 0.036  ns/op
 * FastDecimalBenchmark.divideFastDecimal  avgt  100  3.061 ± 0.056  ns/op
 */


/**
 * JMH benchmark to compare the performance of BigDecimal and FastDecimal.
 * <p>
 * To run the benchmark:
 * 1. Build the project with: mvn clean package
 * 2. Run the benchmark with: java -jar target/benchmarks.jar
 */
@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 100, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 1, jvmArgs = {"--add-modules=jdk.incubator.vector", "--enable-preview"})
@State(Scope.Benchmark)
public class FastDecimalBenchmark {

    // Values for benchmarking
    private final String stringValue1 = "123.4567";
    private final double doubleValue1 = 123.4567;

    // Pre-created instances for operation benchmarks
    private FastDecimal fastDecimal1;
    private FastDecimal fastDecimal2;
    private BigDecimal bigDecimal1;
    private BigDecimal bigDecimal2;

    @Setup
    public void setup() {
        // Initialize the pre-created instances
        fastDecimal1 = FastDecimal.of(stringValue1);
        String stringValue2 = "98.7654";
        fastDecimal2 = FastDecimal.of(stringValue2);
        bigDecimal1 = new BigDecimal(stringValue1);
        bigDecimal2 = new BigDecimal(stringValue2);
    }

    // Creation benchmarks

    // @Benchmark
    public void createFastDecimalFromString(Blackhole blackhole) {
        blackhole.consume(FastDecimal.of(stringValue1));
    }

    // @Benchmark
    public void createBigDecimalFromString(Blackhole blackhole) {
        blackhole.consume(new BigDecimal(stringValue1));
    }

    // @Benchmark
    public void createFastDecimalFromDouble(Blackhole blackhole) {
        blackhole.consume(FastDecimal.of(doubleValue1));
    }

    // @Benchmark
    public void createBigDecimalFromDouble(Blackhole blackhole) {
        blackhole.consume(BigDecimal.valueOf(doubleValue1));
    }

    // Addition benchmarks
//    @Benchmark
    public void addFastDecimal(Blackhole blackhole) {
        blackhole.consume(fastDecimal1.add(fastDecimal2));
    }

    //    @Benchmark
    public void addBigDecimal(Blackhole blackhole) {
        blackhole.consume(bigDecimal1.add(bigDecimal2));
    }

    // Subtraction benchmarks

    //    @Benchmark
    public void subtractFastDecimal(Blackhole blackhole) {
        blackhole.consume(fastDecimal1.subtract(fastDecimal2));
    }

    //    @Benchmark
    public void subtractBigDecimal(Blackhole blackhole) {
        blackhole.consume(bigDecimal1.subtract(bigDecimal2));
    }

    // Multiplication benchmarks

    @Benchmark
    public void multiplyFastDecimal(Blackhole blackhole) {
        blackhole.consume(fastDecimal1.multiply(fastDecimal2));
    }

    @Benchmark
    public void multiplyBigDecimal(Blackhole blackhole) {
        blackhole.consume(bigDecimal1.multiply(bigDecimal2));
    }

    // Division benchmarks

    //    @Benchmark
    public void divideFastDecimal(Blackhole blackhole) {
        blackhole.consume(fastDecimal1.divide(fastDecimal2));
    }

    //    @Benchmark
    public void divideBigDecimal(Blackhole blackhole) {
        blackhole.consume(bigDecimal1.divide(bigDecimal2, RoundingMode.HALF_UP));
    }

    // Comparison benchmarks

    // @Benchmark
    public void compareFastDecimal(Blackhole blackhole) {
        blackhole.consume(fastDecimal1.compareTo(fastDecimal2));
    }

    // @Benchmark
    public void compareBigDecimal(Blackhole blackhole) {
        blackhole.consume(bigDecimal1.compareTo(bigDecimal2));
    }

    // String conversion benchmarks

    // @Benchmark
    public void toStringFastDecimal(Blackhole blackhole) {
        blackhole.consume(fastDecimal1.toString());
    }

    // @Benchmark
    public void toStringBigDecimal(Blackhole blackhole) {
        blackhole.consume(bigDecimal1.toString());
    }

    /**
     * Main method to run the benchmark from IDE.
     */
//    @SuppressWarnings("unused")
//    static void main(String[] args) throws RunnerException {
//        Options options = new OptionsBuilder()
//                .include(FastDecimalBenchmark.class.getSimpleName())
//                .build();
//        new Runner(options).run();
//    }
}