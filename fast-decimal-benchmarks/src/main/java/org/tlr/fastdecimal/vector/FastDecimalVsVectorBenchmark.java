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

package org.tlr.fastdecimal.vector;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.tlr.fastdecimal.core.FastDecimal;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark comparing scalar FastDecimal operations with vectorized
 * VectorFastDecimal operations over arrays of values. This provides
 * a direct apples-to-apples comparison on identical inputs.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1, jvmArgs = {"--add-modules=jdk.incubator.vector", "--enable-preview"})
@State(Scope.Thread)
public class FastDecimalVsVectorBenchmark {

    @Param({"256", "2048", "16384"})
    private int size;

    private FastDecimal[] a;
    private FastDecimal[] b;

    @Setup
    public void setup() {
        Random rnd = new Random(42);
        a = new FastDecimal[size];
        b = new FastDecimal[size];
        for (int i = 0; i < size; i++) {
            // Range chosen to keep values reasonable for fixed 4-dec scale, avoid overflow
            double v1 = (rnd.nextDouble() * 2000.0) - 1000.0; // [-1000, 1000)
            double v2 = (rnd.nextDouble() * 2000.0) - 1000.0; // [-1000, 1000)
            a[i] = FastDecimal.of(v1);
            b[i] = FastDecimal.of(v2 == 0.0 ? 1.0 : v2); // avoid zeros for division
        }
    }

    // Addition

    @Benchmark
    public void scalar_add(Blackhole bh) {
        FastDecimal[] out = new FastDecimal[size];
        for (int i = 0; i < size; i++) {
            out[i] = a[i].add(b[i]);
        }
        bh.consume(out);
    }

    @Benchmark
    public void vector_add(Blackhole bh) {
        FastDecimal[] out = VectorFastDecimal.add(a, b);
        bh.consume(out);
    }

    // Subtraction

    @Benchmark
    public void scalar_subtract(Blackhole bh) {
        FastDecimal[] out = new FastDecimal[size];
        for (int i = 0; i < size; i++) {
            out[i] = a[i].subtract(b[i]);
        }
        bh.consume(out);
    }

    @Benchmark
    public void vector_subtract(Blackhole bh) {
        FastDecimal[] out = VectorFastDecimal.subtract(a, b);
        bh.consume(out);
    }

    // Multiplication

    @Benchmark
    public void scalar_multiply(Blackhole bh) {
        FastDecimal[] out = new FastDecimal[size];
        for (int i = 0; i < size; i++) {
            out[i] = a[i].multiply(b[i]);
        }
        bh.consume(out);
    }

    @Benchmark
    public void vector_multiply(Blackhole bh) {
        FastDecimal[] out = VectorFastDecimal.multiply(a, b);
        bh.consume(out);
    }

    // Division

    @Benchmark
    public void scalar_divide(Blackhole bh) {
        FastDecimal[] out = new FastDecimal[size];
        for (int i = 0; i < size; i++) {
            out[i] = a[i].divide(b[i]);
        }
        bh.consume(out);
    }

    @Benchmark
    public void vector_divide(Blackhole bh) {
        FastDecimal[] out = VectorFastDecimal.divide(a, b);
        bh.consume(out);
    }

    /**
     * Convenience main to run this benchmark from IDE.
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FastDecimalVsVectorBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
