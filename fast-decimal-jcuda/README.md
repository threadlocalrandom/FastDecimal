# FastDecimal JCuda Benchmarks

This module provides a CUDA-accelerated implementation of the FastDecimal interface using NVIDIA CUDA technology. It
also includes JMH benchmarks to compare the performance of the regular FastDecimal implementation with the JCuda
implementation.

## Benchmark Results

The benchmarks compare the performance of regular FastDecimal operations with JCuda-accelerated operations for different
array sizes (100, 1000, 10000) and different operations (add, subtract, multiply, divide).

### Summary of Results

| Operation | Array Size | Regular FastDecimal (μs) | JCuda FastDecimal (μs) |
|-----------|------------|--------------------------|------------------------|
| Add       | 100        | 0.414 ± 0.026            | 0.590 ± 0.124          |
| Add       | 1000       | 4.273 ± 1.187            | 6.880 ± 1.205          |
| Add       | 10000      | 42.976 ± 31.505          | 68.920 ± 13.384        |
| Subtract  | 100        | 0.422 ± 0.062            | 0.588 ± 0.018          |
| Subtract  | 1000       | 4.276 ± 1.177            | 6.926 ± 4.319          |
| Subtract  | 10000      | 43.549 ± 2.249           | 71.498 ± 7.375         |
| Multiply  | 100        | 0.380 ± 0.106            | 0.592 ± 0.042          |
| Multiply  | 1000       | 3.880 ± 0.786            | 7.142 ± 0.886          |
| Multiply  | 10000      | 42.290 ± 50.693          | 72.570 ± 19.620        |
| Divide    | 100        | 0.524 ± 0.088            | 0.852 ± 0.140          |
| Divide    | 1000       | 9.564 ± 1.543            | 10.885 ± 0.479         |
| Divide    | 10000      | 118.360 ± 16.135         | 140.594 ± 20.838       |

### Analysis

The benchmark results show that the regular FastDecimal implementation is consistently faster than the JCuda
implementation for all operations and array sizes. This is likely due to:

1. The JCuda implementation has overhead for initializing CUDA, transferring data between CPU and GPU, and cleaning up
   resources
2. The current JCuda implementation is a placeholder that simulates CUDA operations in Java (as noted in the code
   comments)
3. For small to medium-sized arrays, the overhead of using CUDA outweighs any potential performance benefits

It's important to note that a real CUDA implementation (not a simulation) might show different results, especially for
very large arrays where GPU parallelism could be more beneficial.

## Running the Benchmarks

To run the benchmarks, use the following command from the fast-decimal-jcuda directory:

```bash
mvn test -P benchmark -Dbenchmark.includes="JCudaFastDecimalBenchmark"
```

This will run all the benchmarks defined in the JCudaFastDecimalBenchmark class. You can also run specific benchmarks by
specifying a more precise pattern:

```bash
# Run only the add benchmarks
mvn test -P benchmark -Dbenchmark.includes=".*Add.*"

# Run only benchmarks with array size 10000
mvn test -P benchmark -Dbenchmark.includes=".*10000.*"
```

## Implementation Notes

The current JCuda implementation is a placeholder that simulates CUDA operations in Java. The actual CUDA operations are
commented out in the code. To implement a real CUDA-accelerated version, you would need to:

1. Compile the CUDA kernels
2. Allocate device memory
3. Copy data from host to device
4. Launch the appropriate CUDA kernel
5. Copy results back from device to host
6. Free device memory

This would require a CUDA-capable GPU and the CUDA toolkit to be installed on the system.

## CUDA Driver Installation

To use the JCuda implementation, you need to install the appropriate CUDA drivers and JCuda libraries. Follow these
steps:

### Prerequisites

1. **NVIDIA GPU**: You need an NVIDIA GPU that supports CUDA with compute capability 3.0 (Kepler) or newer. Check
   the [CUDA-Enabled GPU list](https://developer.nvidia.com/cuda-gpus) to verify your GPU is supported and has the
   required compute capability.
2. **Operating System**: Supported operating systems include Windows, Linux, and macOS (limited support).

### Installation Steps

1. **Install NVIDIA CUDA Toolkit**:
    - Visit [NVIDIA CUDA Downloads](https://developer.nvidia.com/cuda-downloads)
    - Select your operating system and follow the installation instructions
    - This project uses JCuda 11.8.0, which is compatible with CUDA Toolkit 11.x

2. **Verify CUDA Installation**:
    - Open a command prompt/terminal
    - Run `nvcc --version` to verify the CUDA compiler is installed
    - Run `nvidia-smi` to check GPU information and driver version

3. **Download JCuda Libraries**:
    - Visit [JCuda.org](http://javagl.de/jcuda.org/)
    - Navigate to the "Downloads" section
    - Download the JCuda libraries version 11.8.0 for your platform
    - Alternatively, the Maven dependencies will automatically download the required JCuda libraries

4. **Set Up Environment Variables** (if not using Maven):
    - Add the JCuda library directory to your Java library path
    - For Windows: Add the CUDA bin directory to your PATH environment variable
    - For Linux/macOS: Add the CUDA library directory to LD_LIBRARY_PATH/DYLD_LIBRARY_PATH

### Troubleshooting

- **No CUDA devices found**: Ensure your GPU is CUDA-capable and the drivers are properly installed
- **UnsatisfiedLinkError**: Verify that the JCuda libraries match your CUDA version and are in the Java library path
- **Version mismatch**: Ensure you're using JCuda 11.8.0 with a compatible CUDA Toolkit version (11.x)
- **CUDA_ERROR_INVALID_PTX**: This error occurs when the PTX code is incompatible with your GPU architecture. The
  implementation automatically detects your GPU's compute capability and generates appropriate PTX code (minimum 3.0
  Kepler). If you encounter this error, it may be due to an issue with the PTX code generation, CUDA driver version, or
  GPU compatibility.

For more detailed information and troubleshooting, visit
the [JCuda Documentation](http://javagl.de/jcuda.org/documentation/documentation.html).
