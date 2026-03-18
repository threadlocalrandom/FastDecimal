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
import org.tlr.fastdecimal.core.FastDecimal;

import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark to compare the performance of scalar FastDecimal and ValueFastDecimal.
 */
@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"--enable-preview"})
@State(Scope.Benchmark)
public class FastDecimalVsValueBenchmark {

    private final String stringValue1 = "123.4567";
    private final String stringValue2 = "98.7654";

    private FastDecimal fastDecimal1;
    private FastDecimal fastDecimal2;
    private ValueFastDecimal valueFastDecimal1;
    private ValueFastDecimal valueFastDecimal2;

    @Setup
    public void setup() {
        fastDecimal1 = FastDecimal.of(stringValue1);
        fastDecimal2 = FastDecimal.of(stringValue2);
        valueFastDecimal1 = ValueFastDecimal.of(stringValue1);
        valueFastDecimal2 = ValueFastDecimal.of(stringValue2);
    }

    @Benchmark
    public void addFastDecimal(Blackhole blackhole) {
        blackhole.consume(fastDecimal1.add(fastDecimal2));
    }

    @Benchmark
    public void addValueFastDecimal(Blackhole blackhole) {
        blackhole.consume(valueFastDecimal1.add(valueFastDecimal2));
    }

    @Benchmark
    public void subtractFastDecimal(Blackhole blackhole) {
        blackhole.consume(fastDecimal1.subtract(fastDecimal2));
    }

    @Benchmark
    public void subtractValueFastDecimal(Blackhole blackhole) {
        blackhole.consume(valueFastDecimal1.subtract(valueFastDecimal2));
    }

    @Benchmark
    public void multiplyFastDecimal(Blackhole blackhole) {
        blackhole.consume(fastDecimal1.multiply(fastDecimal2));
    }

    @Benchmark
    public void multiplyValueFastDecimal(Blackhole blackhole) {
        blackhole.consume(valueFastDecimal1.multiply(valueFastDecimal2));
    }

    @Benchmark
    public void divideFastDecimal(Blackhole blackhole) {
        blackhole.consume(fastDecimal1.divide(fastDecimal2));
    }

    @Benchmark
    public void divideValueFastDecimal(Blackhole blackhole) {
        blackhole.consume(valueFastDecimal1.divide(valueFastDecimal2));
    }

    /**
     * Main method to run the benchmark from IDE.
     */
    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(FastDecimalVsValueBenchmark.class.getSimpleName())
                .build();
        new Runner(options).run();
    }
}
