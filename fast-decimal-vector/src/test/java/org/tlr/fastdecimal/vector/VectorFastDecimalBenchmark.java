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
 * Benchmark to compare the performance of regular FastDecimal operations
 * with vectorized operations using the Vector API.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class VectorFastDecimalBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    private FastDecimal[] array1;
    private FastDecimal[] array2;
    private Random random;

    @Setup
    public void setup() {
        random = new Random(42); // Fixed seed for reproducibility
        array1 = new FastDecimal[size];
        array2 = new FastDecimal[size];

        // Initialize arrays with random values
        for (int i = 0; i < size; i++) {
            // Generate random values between -1000 and 1000
            double value1 = (random.nextDouble() * 2000) - 1000;
            double value2 = (random.nextDouble() * 2000) - 1000;
            array1[i] = FastDecimal.of(value1);
            array2[i] = FastDecimal.of(value2);
        }
    }

    /**
     * Benchmark for regular (scalar) addition of FastDecimal arrays.
     */
    @Benchmark
    public void regularAdd(Blackhole blackhole) {
        FastDecimal[] result = new FastDecimal[size];
        for (int i = 0; i < size; i++) {
            result[i] = array1[i].add(array2[i]);
        }
        blackhole.consume(result);
    }

    /**
     * Benchmark for vectorized addition of FastDecimal arrays.
     */
    @Benchmark
    public void vectorAdd(Blackhole blackhole) {
        FastDecimal[] result = VectorFastDecimal.add(array1, array2);
        blackhole.consume(result);
    }

    /**
     * Benchmark for regular (scalar) subtraction of FastDecimal arrays.
     */
    @Benchmark
    public void regularSubtract(Blackhole blackhole) {
        FastDecimal[] result = new FastDecimal[size];
        for (int i = 0; i < size; i++) {
            result[i] = array1[i].subtract(array2[i]);
        }
        blackhole.consume(result);
    }

    /**
     * Benchmark for vectorized subtraction of FastDecimal arrays.
     */
    @Benchmark
    public void vectorSubtract(Blackhole blackhole) {
        FastDecimal[] result = VectorFastDecimal.subtract(array1, array2);
        blackhole.consume(result);
    }

    /**
     * Benchmark for regular (scalar) multiplication of FastDecimal arrays.
     */
    @Benchmark
    public void regularMultiply(Blackhole blackhole) {
        FastDecimal[] result = new FastDecimal[size];
        for (int i = 0; i < size; i++) {
            result[i] = array1[i].multiply(array2[i]);
        }
        blackhole.consume(result);
    }

    /**
     * Benchmark for vectorized multiplication of FastDecimal arrays.
     */
    @Benchmark
    public void vectorMultiply(Blackhole blackhole) {
        FastDecimal[] result = VectorFastDecimal.multiply(array1, array2);
        blackhole.consume(result);
    }

    /**
     * Benchmark for regular (scalar) division of FastDecimal arrays.
     */
    @Benchmark
    public void regularDivide(Blackhole blackhole) {
        FastDecimal[] result = new FastDecimal[size];
        for (int i = 0; i < size; i++) {
            // Skip division by zero
            if (!array2[i].isZero()) {
                result[i] = array1[i].divide(array2[i]);
            } else {
                result[i] = FastDecimal.ZERO;
            }
        }
        blackhole.consume(result);
    }

    /**
     * Benchmark for vectorized division of FastDecimal arrays.
     * Note: This benchmark ensures no division by zero occurs.
     */
    @Benchmark
    public void vectorDivide(Blackhole blackhole) {
        // Create a copy of array2 to ensure no zeros
        FastDecimal[] safeArray2 = new FastDecimal[size];
        for (int i = 0; i < size; i++) {
            if (array2[i].isZero()) {
                safeArray2[i] = FastDecimal.ONE; // Replace zeros with ONE
            } else {
                safeArray2[i] = array2[i];
            }
        }

        FastDecimal[] result = VectorFastDecimal.divide(array1, safeArray2);
        blackhole.consume(result);
    }

    /**
     * Main method to run the benchmark from IDE.
     */
    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(VectorFastDecimalBenchmark.class.getSimpleName())
                .build();
        new Runner(options).run();
    }
}
