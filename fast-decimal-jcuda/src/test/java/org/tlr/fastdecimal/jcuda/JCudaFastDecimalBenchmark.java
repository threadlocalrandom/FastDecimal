package org.tlr.fastdecimal.jcuda;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.tlr.fastdecimal.core.FastDecimal;
import org.tlr.fastdecimal.vector.VectorFastDecimal;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark to compare the performance of JCudaFastDecimal with regular FastDecimal and VectorFastDecimal.
 * <p>
 * To run this benchmark:
 * 1. Build the project with `mvn clean package`
 * 2. Run the benchmark with `java -jar target/benchmarks.jar`
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class JCudaFastDecimalBenchmark {

    // Array sizes to benchmark
    @Param({"100", "1000", "10000", "100000"})
    private int size;

    // Test arrays
    private FastDecimal[] a;
    private FastDecimal[] b;
    private FastDecimal[] result;

    @Setup
    public void setup() {
        // Initialize CUDA
        JCudaFastDecimal.initialize();

        // Create test arrays
        a = createTestArray(size);
        b = createTestArray(size);
        result = new FastDecimal[size];
    }

    /**
     * Benchmark for regular FastDecimal addition.
     */
    @Benchmark
    public void regularAddition(Blackhole blackhole) {
        for (int i = 0; i < size; i++) {
            result[i] = a[i].add(b[i]);
        }
        blackhole.consume(result);
    }

    /**
     * Benchmark for VectorFastDecimal addition.
     */
    @Benchmark
    public void vectorAddition(Blackhole blackhole) {
        FastDecimal[] vectorResult = VectorFastDecimal.add(a, b);
        blackhole.consume(vectorResult);
    }

    /**
     * Benchmark for JCudaFastDecimal addition.
     */
    @Benchmark
    public void cudaAddition(Blackhole blackhole) {
        FastDecimal[] cudaResult = JCudaFastDecimal.add(a, b);
        blackhole.consume(cudaResult);
    }

    /**
     * Benchmark for regular FastDecimal multiplication.
     */
    @Benchmark
    public void regularMultiplication(Blackhole blackhole) {
        for (int i = 0; i < size; i++) {
            result[i] = a[i].multiply(b[i]);
        }
        blackhole.consume(result);
    }

    /**
     * Benchmark for VectorFastDecimal multiplication.
     */
    @Benchmark
    public void vectorMultiplication(Blackhole blackhole) {
        FastDecimal[] vectorResult = VectorFastDecimal.multiply(a, b);
        blackhole.consume(vectorResult);
    }

    /**
     * Benchmark for JCudaFastDecimal multiplication.
     */
    @Benchmark
    public void cudaMultiplication(Blackhole blackhole) {
        FastDecimal[] cudaResult = JCudaFastDecimal.multiply(a, b);
        blackhole.consume(cudaResult);
    }

    /**
     * Benchmark for regular FastDecimal division.
     */
    @Benchmark
    public void regularDivision(Blackhole blackhole) {
        for (int i = 0; i < size; i++) {
            result[i] = a[i].divide(b[i]);
        }
        blackhole.consume(result);
    }

    /**
     * Benchmark for VectorFastDecimal division.
     */
    @Benchmark
    public void vectorDivision(Blackhole blackhole) {
        FastDecimal[] vectorResult = VectorFastDecimal.divide(a, b);
        blackhole.consume(vectorResult);
    }

    /**
     * Benchmark for JCudaFastDecimal division.
     */
    @Benchmark
    public void cudaDivision(Blackhole blackhole) {
        FastDecimal[] cudaResult = JCudaFastDecimal.divide(a, b);
        blackhole.consume(cudaResult);
    }

    /**
     * Creates an array of FastDecimal objects with test values.
     *
     * @param size the size of the array
     * @return an array of FastDecimal objects
     */
    private FastDecimal[] createTestArray(int size) {
        FastDecimal[] array = new FastDecimal[size];
        for (int i = 0; i < size; i++) {
            // Use a deterministic pattern for test values
            double value = ((i % 19) - 9) + 0.1; // Values from -8.9 to 9.1, avoiding 0
            array[i] = FastDecimal.of(value);
        }
        return array;
    }

    /**
     * Main method to run the benchmark.
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JCudaFastDecimalBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}