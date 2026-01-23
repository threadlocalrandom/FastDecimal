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
 * JMH benchmark that compares Vector API operations executed sequentially
 * (single-threaded, vectorized) versus their parallel counterparts that
 * split the work across cores while still using the Vector API per chunk.
 * <p>
 * It focuses only on VectorFastDecimal.* methods to offer a fair comparison
 * between non-parallel and parallel implementations.
 */
@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1, jvmArgs = {"--add-modules=jdk.incubator.vector", "--enable-preview"})
@State(Scope.Thread)
public class VectorParallelVsSequentialBenchmark {

    @Param({"256", "2048", "16384"})
    private int size;

    private FastDecimal[] a;
    private FastDecimal[] b;
    private FastDecimal[] bSafe; // b but without zeros for division

    @Setup
    public void setup() {
        Random rnd = new Random(42);
        a = new FastDecimal[size];
        b = new FastDecimal[size];
        bSafe = new FastDecimal[size];
        for (int i = 0; i < size; i++) {
            double v1 = (rnd.nextDouble() * 2000.0) - 1000.0; // [-1000, 1000)
            double v2 = (rnd.nextDouble() * 2000.0) - 1000.0; // [-1000, 1000)
            a[i] = FastDecimal.of(v1);
            FastDecimal fb = FastDecimal.of(v2);
            b[i] = fb;
            bSafe[i] = fb.isZero() ? FastDecimal.ONE : fb;
        }
    }

    // =========================
    // Addition
    // =========================

    @Benchmark
    public void vector_add_sequential(Blackhole bh) {
        FastDecimal[] out = VectorFastDecimal.add(a, b);
        bh.consume(out);
    }

    @Benchmark
    public void vector_add_parallel(Blackhole bh) {
        FastDecimal[] out = VectorFastDecimal.addParallel(a, b);
        bh.consume(out);
    }

    // =========================
    // Subtraction
    // =========================

    @Benchmark
    public void vector_subtract_sequential(Blackhole bh) {
        FastDecimal[] out = VectorFastDecimal.subtract(a, b);
        bh.consume(out);
    }

    @Benchmark
    public void vector_subtract_parallel(Blackhole bh) {
        FastDecimal[] out = VectorFastDecimal.subtractParallel(a, b);
        bh.consume(out);
    }

    // =========================
    // Multiplication
    // =========================

    @Benchmark
    public void vector_multiply_sequential(Blackhole bh) {
        FastDecimal[] out = VectorFastDecimal.multiply(a, b);
        bh.consume(out);
    }

    @Benchmark
    public void vector_multiply_parallel(Blackhole bh) {
        FastDecimal[] out = VectorFastDecimal.multiplyParallel(a, b);
        bh.consume(out);
    }

    // =========================
    // Division (safe denominators)
    // =========================

    @Benchmark
    public void vector_divide_sequential(Blackhole bh) {
        FastDecimal[] out = VectorFastDecimal.divide(a, bSafe);
        bh.consume(out);
    }

    @Benchmark
    public void vector_divide_parallel(Blackhole bh) {
        FastDecimal[] out = VectorFastDecimal.divideParallel(a, bSafe);
        bh.consume(out);
    }

    /**
     * Convenience main to run this benchmark from IDE.
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(VectorParallelVsSequentialBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
