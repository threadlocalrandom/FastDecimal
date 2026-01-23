# FastDecimal

FastDecimal is a high-performance decimal number implementation for Java that uses a fixed-point representation. It is
designed to be a faster alternative to `java.math.BigDecimal` for applications that require high-throughput decimal
arithmetic within a specific range and precision.

## Implementation Overview

Unlike `BigDecimal`, which can represent numbers of arbitrary precision and scale using an unscaled `BigInteger` and an
`int` scale, `FastDecimal` uses a more efficient fixed-point internal representation:

- **Internal Storage**: A single 64-bit `long` value.
- **Fixed Scale**: A constant scale factor of 10,000 (4 decimal places).
- **Range**: Approximately ±922,337,203,685,477.5807.
- **Precision**: Fixed at 4 decimal places. Operations are rounded using `HALF_UP`.

### FastDecimal vs. BigDecimal

| Feature          | FastDecimal                                       | BigDecimal                                      |
|------------------|---------------------------------------------------|-------------------------------------------------|
| **Performance**  | Extremely high (uses primitive `long` arithmetic) | Lower (object overhead and complex algorithms)  |
| **Memory**       | Very low (minimal object footprint)               | Higher (contains `BigInteger` and other fields) |
| **Precision**    | Fixed (4 decimal places)                          | Arbitrary                                       |
| **Range**        | Limited by `long` (±2^63 / 10,000)                | Practically unlimited                           |
| **Immutability** | Immutable                                         | Immutable                                       |

`FastDecimal` is ideal for financial applications, trading systems, and other performance-critical domains where 4
decimal places of precision are sufficient and the values stay within the 64-bit range.

## Project Structure

- `fast-decimal-core`: The core implementation of `FastDecimal`. This is compiled to release 21.
- `fast-decimal-vector`: SIMD-accelerated and parallel operations for `FastDecimal` arrays. This is compiled to release
    25.
- `fast-decimal-benchmarks`: JMH benchmarks comparing `FastDecimal`, `BigDecimal`, and `VectorFastDecimal`. This is
  compiled to release 25.

## Vectorized Operations

The `fast-decimal-vector` module provides `VectorFastDecimal`, which leverages the **Java Vector API** (Incubator) to
perform SIMD (Single Instruction, Multiple Data) operations on arrays of `FastDecimal` values.

### Key Features

- **SIMD Acceleration**: Uses hardware-level vector instructions to process multiple decimal values simultaneously.
- **Parallel Processing**: Supports multi-threaded execution for large arrays using a thread pool.
- **Efficient Internal Handling**: Optimized for bulk operations by reducing object creation and using primitive `long`
  arrays internally.
- **Seamless Integration**: Operates directly on `FastDecimal[]` arrays.

### Supported Operations

- **Vectorized (SIMD)**: `add`, `subtract`, `multiply`, `divide`
- **Parallel**: `addParallel`, `subtractParallel`, `multiplyParallel`, `divideParallel`

## Benchmarks

This project contains JMH (Java Microbenchmark Harness) benchmarks to compare the performance of `FastDecimal` against
Java's standard `BigDecimal`, as well as evaluating the benefits of vectorized operations.

### Benchmark Overview

Several benchmark classes are provided:

1. **FastDecimalBenchmark**: Basic benchmarks comparing common operations between `FastDecimal` and `BigDecimal`.
2. **ComprehensiveBenchmark**: More detailed benchmarks with different magnitudes of numbers and chained operations.
3. **VectorFastDecimalBenchmark**: Compares scalar `FastDecimal` array operations against SIMD-accelerated vectorized
   operations.
4. **VectorFastDecimalParallelBenchmark**: Evaluates the performance of parallel vectorized operations.

### Operations Benchmarked

- Creation from `String` and `double`
- Basic arithmetic: `add`, `subtract`, `multiply`, `divide`
- Comparison: `compareTo`
- String conversion: `toString`
- Chained operations: (e.g., `(a + b) * c / d`)

### Running the Benchmarks

#### Prerequisites

- Java/JDK 25 or higher. I use the Azul Zulu build of OpenJDK.
- Maven 3.9.9

#### Method 1: Running via Maven

You can run the benchmarks directly using the `benchmark` profile:

```bash
# To build the project and run all test, including benchmarks
mvn clean verify -P benchmark
# Run all benchmarks
mvn test -P benchmark -pl fast-decimal-benchmarks

# Run a specific benchmark class
mvn test -P benchmark -pl fast-decimal-benchmarks -Dbenchmark.includes=FastDecimalBenchmark

# Run benchmarks matching a pattern (e.g., all addition tests)
mvn test -P benchmark -pl fast-decimal-benchmarks -Dbenchmark.includes=".*add.*"
```

#### Method 2: Running via Packaged JAR

For more stable results, it's recommended to run the benchmarks from a standalone JAR. Note that vector benchmarks
require enabling the Vector API:

```bash
# Build the project
mvn clean package -DskipTests

# Run all benchmarks
java --add-modules=jdk.incubator.vector --enable-preview -jar fast-decimal-benchmarks/target/benchmarks.jar

# Run specific benchmarks
java --add-modules=jdk.incubator.vector --enable-preview -jar fast-decimal-benchmarks/target/benchmarks.jar FastDecimalBenchmark
java --add-modules=jdk.incubator.vector --enable-preview -jar fast-decimal-benchmarks/target/benchmarks.jar VectorFastDecimalBenchmark
```

### Interpreting Results

The benchmarks measure the average time per operation in nanoseconds (`avgt`). Lower values indicate better performance.

```
Benchmark                                  Mode  Cnt    Score    Error  Units
FastDecimalBenchmark.addBigDecimal         avgt    5   12.345 ±  0.500  ns/op
FastDecimalBenchmark.addFastDecimal        avgt    5    2.123 ±  0.100  ns/op
```

In most scenarios, `FastDecimal` is expected to be significantly faster (often 5-10x) than `BigDecimal` due to its
avoidance of object allocations and use of primitive arithmetic.
