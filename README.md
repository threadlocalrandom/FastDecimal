# FastDecimal Benchmarks

This project contains JMH (Java Microbenchmark Harness) benchmarks to compare the performance of `FastDecimal` against Java's standard `BigDecimal`.

## Benchmark Overview

Two benchmark classes are provided:

1. **FastDecimalBenchmark**: Basic benchmarks comparing common operations between FastDecimal and BigDecimal.
2. **ComprehensiveBenchmark**: More detailed benchmarks with different scales and magnitudes of numbers.

## Operations Benchmarked

The benchmarks measure the performance of:

- Creation from string and double
- Basic arithmetic operations (addition, subtraction, multiplication, division)
- Comparison operations
- String conversion
- Chained operations

## Running the Benchmarks

### Prerequisites

- Java 21 or higher
- Maven 3.8 or higher

### Running from Maven

To run the benchmarks using Maven:

```bash
# Build the project
mvn clean package

# Run the basic benchmarks
java -cp target/fast-decimal-1.0-SNAPSHOT.jar:target/test-classes org.tlr.fastdecimal.FastDecimalBenchmark

# Run the comprehensive benchmarks
java -cp target/fast-decimal-1.0-SNAPSHOT.jar:target/test-classes org.tlr.fastdecimal.ComprehensiveBenchmark
```

### Running from IDE

Both benchmark classes have a `main` method, so they can be run directly from an IDE.

## Benchmark Configuration

The benchmarks are configured with:

- Warmup: 3 iterations, 1 second each
- Measurement: 5 iterations, 1 second each
- Fork: 1
- Mode: Average time
- Time unit: Nanoseconds

## Interpreting Results

The benchmark results show the average time in nanoseconds for each operation. Lower values indicate better performance.

Example output:

```
Benchmark                                  Mode  Cnt    Score    Error  Units
FastDecimalBenchmark.addBigDecimal         avgt    5   XX.XXX ±  X.XXX  ns/op
FastDecimalBenchmark.addFastDecimal        avgt    5   XX.XXX ±  X.XXX  ns/op
```

## Expected Performance Characteristics

- **FastDecimal** should generally be faster for basic arithmetic operations due to its simpler internal representation.
- **BigDecimal** may perform better for very large numbers or operations requiring high precision.
- The performance gap may vary depending on the scale and magnitude of the numbers involved.