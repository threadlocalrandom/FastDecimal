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
 * JMH benchmark for the parallel operations provided by VectorFastDecimal.
 * This specifically measures the addParallel, subtractParallel, multiplyParallel,
 * and divideParallel methods on arrays of FastDecimal values.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1, jvmArgs = {"--add-modules=jdk.incubator.vector", "--enable-preview"})
@State(Scope.Thread)
public class VectorFastDecimalParallelBenchmark {

    @Param({"256", "2048", "16384"})
    private int size;

    private FastDecimal[] a;
    private FastDecimal[] b; // ensure non-zero divisors for divide benchmarks

    @Setup
    public void setup() {
        Random rnd = new Random(42);
        a = new FastDecimal[size];
        b = new FastDecimal[size];
        for (int i = 0; i < size; i++) {
            double v1 = (rnd.nextDouble() * 2000.0) - 1000.0; // [-1000, 1000)
            double v2 = (rnd.nextDouble() * 2000.0) - 1000.0; // [-1000, 1000)
            if (v2 == 0.0) v2 = 1.0; // avoid zero for division
            a[i] = FastDecimal.of(v1);
            b[i] = FastDecimal.of(v2);
        }
    }

    // Parallel Addition
    @Benchmark
    public void parallel_add(Blackhole bh) {
        FastDecimal[] out = VectorFastDecimal.addParallel(a, b);
        bh.consume(out);
    }

    // Parallel Subtraction
    @Benchmark
    public void parallel_subtract(Blackhole bh) {
        FastDecimal[] out = VectorFastDecimal.subtractParallel(a, b);
        bh.consume(out);
    }

    // Parallel Multiplication
    @Benchmark
    public void parallel_multiply(Blackhole bh) {
        FastDecimal[] out = VectorFastDecimal.multiplyParallel(a, b);
        bh.consume(out);
    }

    // Parallel Division
    @Benchmark
    public void parallel_divide(Blackhole bh) {
        FastDecimal[] out = VectorFastDecimal.divideParallel(a, b);
        bh.consume(out);
    }

    /**
     * Convenience main to run this benchmark from IDE.
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(VectorFastDecimalParallelBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
