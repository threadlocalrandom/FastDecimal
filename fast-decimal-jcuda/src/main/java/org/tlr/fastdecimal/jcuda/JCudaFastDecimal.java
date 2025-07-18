package org.tlr.fastdecimal.jcuda;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.*;
import jcuda.runtime.JCuda;
import jcuda.runtime.cudaDeviceProp;
import jcuda.runtime.cudaError;
import org.tlr.fastdecimal.core.FastDecimal;

import java.io.IOException;

/**
 * CUDA implementation of FastDecimal operations.
 * This class provides methods for performing operations on arrays of FastDecimal values
 * using CUDA for GPU acceleration.
 */
public class JCudaFastDecimal {

    // The scale factor used for internal representation (must match FastDecimal)
    private static final long SCALE_FACTOR = 10_000L;

    // Flag to track if CUDA has been initialized
    private static boolean initialized = false;

    // Flag to track if CUDA is available
    private static boolean cudaAvailable = false;

    // CUDA module containing the kernels
    private static CUmodule module;

    // CUDA functions (kernels)
    private static CUfunction addKernel;
    private static CUfunction subtractKernel;
    private static CUfunction multiplyKernel;
    private static CUfunction divideKernel;

    // Default block size for CUDA kernels
    private static final int DEFAULT_BLOCK_SIZE = 256;

    /**
     * Initialize CUDA.
     * This method must be called before any other method in this class.
     */
    public static synchronized void initialize() {
        if (initialized) {
            System.out.println("[DEBUG_LOG] CUDA already initialized, returning");
            return;
        }

        try {
            // Initialize CUDA
            System.out.println("[DEBUG_LOG] Starting CUDA initialization");
            JCuda.setExceptionsEnabled(true);
            int result = JCuda.cudaSetDevice(0);
            if (result != cudaError.cudaSuccess) {
                System.out.println("[DEBUG_LOG] Failed to initialize CUDA device: " + result);
                throw new RuntimeException("Failed to initialize CUDA: " + result);
            }
            System.out.println("[DEBUG_LOG] CUDA device initialized successfully");

            // Get device properties
            cudaDeviceProp deviceProp = new cudaDeviceProp();
            JCuda.cudaGetDeviceProperties(deviceProp, 0);
            System.out.println("JCudaFastDecimal: Using CUDA device: " + deviceProp.name);

            // Initialize CUDA driver API
            JCudaDriver.setExceptionsEnabled(true);
            JCudaDriver.cuInit(0);
            CUdevice device = new CUdevice();
            JCudaDriver.cuDeviceGet(device, 0);
            CUcontext context = new CUcontext();
            JCudaDriver.cuCtxCreate(context, 0, device);

            // Load the PTX code
            System.out.println("[DEBUG_LOG] Loading PTX code");
            String ptxCode = loadPtxCode();
            System.out.println("[DEBUG_LOG] PTX code loaded, length: " + ptxCode.length());
            module = new CUmodule();
            try {
                JCudaDriver.cuModuleLoadData(module, ptxCode);
                System.out.println("[DEBUG_LOG] PTX module loaded successfully");
            } catch (Exception e) {
                System.out.println("[DEBUG_LOG] Failed to load PTX module: " + e.getMessage());
                throw e;
            }

            // Get the kernel functions
            System.out.println("[DEBUG_LOG] Loading kernel functions");
            addKernel = new CUfunction();
            subtractKernel = new CUfunction();
            multiplyKernel = new CUfunction();
            divideKernel = new CUfunction();

            try {
                JCudaDriver.cuModuleGetFunction(addKernel, module, "addKernel");
                System.out.println("[DEBUG_LOG] addKernel loaded successfully");
            } catch (Exception e) {
                System.out.println("[DEBUG_LOG] Failed to load addKernel: " + e.getMessage());
                addKernel = null;
            }

            try {
                JCudaDriver.cuModuleGetFunction(subtractKernel, module, "subtractKernel");
                System.out.println("[DEBUG_LOG] subtractKernel loaded successfully");
            } catch (Exception e) {
                System.out.println("[DEBUG_LOG] Failed to load subtractKernel: " + e.getMessage());
                subtractKernel = null;
            }

            try {
                JCudaDriver.cuModuleGetFunction(multiplyKernel, module, "multiplyKernel");
                System.out.println("[DEBUG_LOG] multiplyKernel loaded successfully");
            } catch (Exception e) {
                System.out.println("[DEBUG_LOG] Failed to load multiplyKernel: " + e.getMessage());
                multiplyKernel = null;
            }

            try {
                JCudaDriver.cuModuleGetFunction(divideKernel, module, "divideKernel");
                System.out.println("[DEBUG_LOG] divideKernel loaded successfully");
            } catch (Exception e) {
                System.out.println("[DEBUG_LOG] Failed to load divideKernel: " + e.getMessage());
                divideKernel = null;
            }

            initialized = true;
            cudaAvailable = true;
            System.out.println("JCudaFastDecimal: CUDA initialized successfully");
        } catch (Exception e) {
            System.err.println("JCudaFastDecimal: Failed to initialize CUDA: " + e.getMessage());
            e.printStackTrace();

            // Reset CUDA resources
            module = null;
            addKernel = null;
            subtractKernel = null;
            multiplyKernel = null;
            divideKernel = null;
            cudaAvailable = false;

            // Fall back to CPU implementation
            System.out.println("JCudaFastDecimal: Falling back to CPU implementation");
            initialized = true;
        }
    }

    /**
     * Loads the PTX code for the CUDA kernels.
     *
     * @return the PTX code as a string
     * @throws IOException if the PTX code cannot be loaded
     */
    private static String loadPtxCode() throws IOException {
        // In a real implementation, this would load the PTX code from a resource file
        // For this implementation, we'll use a hardcoded PTX string

        // This is a simplified PTX code for the kernels
        System.err.println("[DEBUG_LOG] Loading PTX code from hardcoded string");
        return """
                .version 7.0
                .target sm_50
                .address_size 64
                
                // Add kernel
                .visible .entry addKernel(
                    .param .u64 a,
                    .param .u64 b,
                    .param .u64 result,
                    .param .u32 n
                )
                {
                    .reg .u64 %rd<5>;
                    .reg .u32 %r<3>;
                
                    ld.param.u64 %rd1, [a];
                    ld.param.u64 %rd2, [b];
                    ld.param.u64 %rd3, [result];
                    ld.param.u32 %r1, [n];
                
                    // Calculate global thread ID
                    .reg .u32 %tid;
                    mov.u32 %tid, %ctaid.x;
                    mul.lo.u32 %tid, %tid, %ntid.x;
                    add.u32 %tid, %tid, %tid.x;
                
                    // Check if thread ID is within bounds
                    setp.ge.u32 %p1, %tid, %r1;
                    @%p1 bra $L1;
                
                    // Calculate memory addresses
                    mul.lo.u32 %r2, %tid, 8;
                    add.u64 %rd1, %rd1, %r2;
                    add.u64 %rd2, %rd2, %r2;
                    add.u64 %rd3, %rd3, %r2;
                
                    // Load values
                    ld.global.u64 %rd4, [%rd1];
                    ld.global.u64 %rd5, [%rd2];
                
                    // Add values
                    add.s64 %rd4, %rd4, %rd5;
                
                    // Store result
                    st.global.u64 [%rd3], %rd4;
                
                $L1:
                    ret;
                }
                
                // Subtract kernel
                .visible .entry subtractKernel(
                    .param .u64 a,
                    .param .u64 b,
                    .param .u64 result,
                    .param .u32 n
                )
                {
                    .reg .u64 %rd<5>;
                    .reg .u32 %r<3>;
                
                    ld.param.u64 %rd1, [a];
                    ld.param.u64 %rd2, [b];
                    ld.param.u64 %rd3, [result];
                    ld.param.u32 %r1, [n];
                
                    // Calculate global thread ID
                    .reg .u32 %tid;
                    mov.u32 %tid, %ctaid.x;
                    mul.lo.u32 %tid, %tid, %ntid.x;
                    add.u32 %tid, %tid, %tid.x;
                
                    // Check if thread ID is within bounds
                    setp.ge.u32 %p1, %tid, %r1;
                    @%p1 bra $L1;
                
                    // Calculate memory addresses
                    mul.lo.u32 %r2, %tid, 8;
                    add.u64 %rd1, %rd1, %r2;
                    add.u64 %rd2, %rd2, %r2;
                    add.u64 %rd3, %rd3, %r2;
                
                    // Load values
                    ld.global.u64 %rd4, [%rd1];
                    ld.global.u64 %rd5, [%rd2];
                
                    // Subtract values
                    sub.s64 %rd4, %rd4, %rd5;
                
                    // Store result
                    st.global.u64 [%rd3], %rd4;
                
                $L1:
                    ret;
                }
                
                // Multiply kernel
                .visible .entry multiplyKernel(
                    .param .u64 a,
                    .param .u64 b,
                    .param .u64 result,
                    .param .u32 n,
                    .param .u64 scale_factor
                )
                {
                    .reg .u64 %rd<6>;
                    .reg .u32 %r<3>;
                
                    ld.param.u64 %rd1, [a];
                    ld.param.u64 %rd2, [b];
                    ld.param.u64 %rd3, [result];
                    ld.param.u32 %r1, [n];
                    ld.param.u64 %rd6, [scale_factor];
                
                    // Calculate global thread ID
                    .reg .u32 %tid;
                    mov.u32 %tid, %ctaid.x;
                    mul.lo.u32 %tid, %tid, %ntid.x;
                    add.u32 %tid, %tid, %tid.x;
                
                    // Check if thread ID is within bounds
                    setp.ge.u32 %p1, %tid, %r1;
                    @%p1 bra $L1;
                
                    // Calculate memory addresses
                    mul.lo.u32 %r2, %tid, 8;
                    add.u64 %rd1, %rd1, %r2;
                    add.u64 %rd2, %rd2, %r2;
                    add.u64 %rd3, %rd3, %r2;
                
                    // Load values
                    ld.global.u64 %rd4, [%rd1];
                    ld.global.u64 %rd5, [%rd2];
                
                    // Multiply values and divide by scale factor
                    mul.lo.s64 %rd4, %rd4, %rd5;
                    div.s64 %rd4, %rd4, %rd6;
                
                    // Store result
                    st.global.u64 [%rd3], %rd4;
                
                $L1:
                    ret;
                }
                
                // Divide kernel
                .visible .entry divideKernel(
                    .param .u64 a,
                    .param .u64 b,
                    .param .u64 result,
                    .param .u32 n,
                    .param .u64 scale_factor
                )
                {
                    .reg .u64 %rd<10>;
                    .reg .u32 %r<3>;
                    .reg .pred %p<5>;
                
                    ld.param.u64 %rd1, [a];
                    ld.param.u64 %rd2, [b];
                    ld.param.u64 %rd3, [result];
                    ld.param.u32 %r1, [n];
                    ld.param.u64 %rd6, [scale_factor];
                
                    // Calculate global thread ID
                    .reg .u32 %tid;
                    mov.u32 %tid, %ctaid.x;
                    mul.lo.u32 %tid, %tid, %ntid.x;
                    add.u32 %tid, %tid, %tid.x;
                
                    // Check if thread ID is within bounds
                    setp.ge.u32 %p1, %tid, %r1;
                    @%p1 bra $L1;
                
                    // Calculate memory addresses
                    mul.lo.u32 %r2, %tid, 8;
                    add.u64 %rd1, %rd1, %r2;
                    add.u64 %rd2, %rd2, %r2;
                    add.u64 %rd3, %rd3, %r2;
                
                    // Load values
                    ld.global.u64 %rd4, [%rd1];
                    ld.global.u64 %rd5, [%rd2];
                
                    // Check for division by zero
                    setp.eq.s64 %p2, %rd5, 0;
                    @%p2 bra $L1;
                
                    // Scale up the dividend
                    mul.lo.s64 %rd7, %rd4, %rd6;
                
                    // Divide
                    div.s64 %rd8, %rd7, %rd5;
                    rem.s64 %rd9, %rd7, %rd5;
                
                    // Apply HALF_UP rounding
                    abs.s64 %rd10, %rd5;
                    shr.u64 %rd10, %rd10, 1;
                    abs.s64 %rd9, %rd9;
                    setp.ge.u64 %p3, %rd9, %rd10;
                    @!%p3 bra $L2;
                
                    // Add or subtract 1 based on sign
                    setp.ge.s64 %p4, %rd4, 0;
                    @%p4 add.s64 %rd8, %rd8, 1;
                    @!%p4 sub.s64 %rd8, %rd8, 1;
                
                $L2:
                    // Store result
                    st.global.u64 [%rd3], %rd8;
                
                $L1:
                    ret;
                }
                """;
    }

    /**
     * Adds two arrays of FastDecimal values element-wise and stores the result in a new array.
     *
     * @param a the first array of FastDecimal values
     * @param b the second array of FastDecimal values
     * @return a new array containing the element-wise sum of a and b
     * @throws IllegalArgumentException if the arrays have different lengths
     */
    public static FastDecimal[] add(FastDecimal[] a, FastDecimal[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }

        if (!initialized) {
            initialize();
        }

        // Extract the scaled values from the FastDecimal objects
        long[] aValues = extractScaledValues(a);
        long[] bValues = extractScaledValues(b);
        long[] resultValues = new long[a.length];

        // Check if CUDA is available and addKernel is not null
        if (cudaAvailable && addKernel != null) {
            System.out.println("[DEBUG_LOG] Using CUDA for addition");
            try {
                // Allocate device memory
                CUdeviceptr dA = new CUdeviceptr();
                CUdeviceptr dB = new CUdeviceptr();
                CUdeviceptr dResult = new CUdeviceptr();

                JCudaDriver.cuMemAlloc(dA, a.length * Sizeof.LONG);
                JCudaDriver.cuMemAlloc(dB, a.length * Sizeof.LONG);
                JCudaDriver.cuMemAlloc(dResult, a.length * Sizeof.LONG);

                // Copy host memory to device
                JCudaDriver.cuMemcpyHtoD(dA, Pointer.to(aValues), a.length * Sizeof.LONG);
                JCudaDriver.cuMemcpyHtoD(dB, Pointer.to(bValues), a.length * Sizeof.LONG);

                // Set up kernel parameters
                Pointer kernelParams = Pointer.to(
                        Pointer.to(dA),
                        Pointer.to(dB),
                        Pointer.to(dResult),
                        Pointer.to(new int[]{a.length})
                );

                // Calculate grid and block dimensions
                int blockSize = DEFAULT_BLOCK_SIZE;
                int gridSize = (a.length + blockSize - 1) / blockSize;

                // Launch the kernel
                JCudaDriver.cuLaunchKernel(addKernel,
                        gridSize, 1, 1,      // Grid dimension
                        blockSize, 1, 1,     // Block dimension
                        0, null,             // Shared memory size and stream
                        kernelParams, null   // Kernel parameters and extra parameters
                );

                // Copy result from device to host
                JCudaDriver.cuMemcpyDtoH(Pointer.to(resultValues), dResult, a.length * Sizeof.LONG);

                // Free device memory
                JCudaDriver.cuMemFree(dA);
                JCudaDriver.cuMemFree(dB);
                JCudaDriver.cuMemFree(dResult);
            } catch (Exception e) {
                // Fall back to CPU implementation if CUDA fails
                System.err.println("JCudaFastDecimal: CUDA addition failed, falling back to CPU: " + e.getMessage());
                for (int i = 0; i < a.length; i++) {
                    resultValues[i] = aValues[i] + bValues[i];
                }
            }
        } else {
            // Use CPU implementation if CUDA is not available or addKernel is null
            if (!cudaAvailable) {
                System.out.println("JCudaFastDecimal: CUDA not available, using CPU implementation for addition");
            } else if (addKernel == null) {
                System.err.println("JCudaFastDecimal: CUDA addition kernel is null, falling back to CPU");
            }
            System.out.println("[DEBUG_LOG] Falling back to CPU for addition");
            for (int i = 0; i < a.length; i++) {
                resultValues[i] = aValues[i] + bValues[i];
            }
        }

        // Convert back to FastDecimal objects
        return createFastDecimals(resultValues);
    }

    /**
     * Subtracts the second array of FastDecimal values from the first element-wise
     * and stores the result in a new array.
     *
     * @param a the first array of FastDecimal values
     * @param b the second array of FastDecimal values
     * @return a new array containing the element-wise difference of a and b
     * @throws IllegalArgumentException if the arrays have different lengths
     */
    public static FastDecimal[] subtract(FastDecimal[] a, FastDecimal[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }

        if (!initialized) {
            initialize();
        }

        // Extract the scaled values from the FastDecimal objects
        long[] aValues = extractScaledValues(a);
        long[] bValues = extractScaledValues(b);
        long[] resultValues = new long[a.length];

        // Check if CUDA is available and subtractKernel is not null
        if (cudaAvailable && subtractKernel != null) {
            System.out.println("[DEBUG_LOG] Using CUDA for subtraction");
            try {
                // Allocate device memory
                CUdeviceptr dA = new CUdeviceptr();
                CUdeviceptr dB = new CUdeviceptr();
                CUdeviceptr dResult = new CUdeviceptr();

                JCudaDriver.cuMemAlloc(dA, a.length * Sizeof.LONG);
                JCudaDriver.cuMemAlloc(dB, a.length * Sizeof.LONG);
                JCudaDriver.cuMemAlloc(dResult, a.length * Sizeof.LONG);

                // Copy host memory to device
                JCudaDriver.cuMemcpyHtoD(dA, Pointer.to(aValues), a.length * Sizeof.LONG);
                JCudaDriver.cuMemcpyHtoD(dB, Pointer.to(bValues), a.length * Sizeof.LONG);

                // Set up kernel parameters
                Pointer kernelParams = Pointer.to(
                        Pointer.to(dA),
                        Pointer.to(dB),
                        Pointer.to(dResult),
                        Pointer.to(new int[]{a.length})
                );

                // Calculate grid and block dimensions
                int blockSize = DEFAULT_BLOCK_SIZE;
                int gridSize = (a.length + blockSize - 1) / blockSize;

                // Launch the kernel
                JCudaDriver.cuLaunchKernel(subtractKernel,
                        gridSize, 1, 1,      // Grid dimension
                        blockSize, 1, 1,     // Block dimension
                        0, null,             // Shared memory size and stream
                        kernelParams, null   // Kernel parameters and extra parameters
                );

                // Copy result from device to host
                JCudaDriver.cuMemcpyDtoH(Pointer.to(resultValues), dResult, a.length * Sizeof.LONG);

                // Free device memory
                JCudaDriver.cuMemFree(dA);
                JCudaDriver.cuMemFree(dB);
                JCudaDriver.cuMemFree(dResult);
            } catch (Exception e) {
                // Fall back to CPU implementation if CUDA fails
                System.err.println("JCudaFastDecimal: CUDA subtraction failed, falling back to CPU: " + e.getMessage());
                for (int i = 0; i < a.length; i++) {
                    resultValues[i] = aValues[i] - bValues[i];
                }
            }
        } else {
            // Use CPU implementation if CUDA is not available or subtractKernel is null
            if (!cudaAvailable) {
                System.out.println("JCudaFastDecimal: CUDA not available, using CPU implementation for subtraction");
            } else if (subtractKernel == null) {
                System.err.println("JCudaFastDecimal: CUDA subtraction kernel is null, falling back to CPU");
            }
            System.out.println("[DEBUG_LOG] Falling back to CPU for subtraction");
            for (int i = 0; i < a.length; i++) {
                resultValues[i] = aValues[i] - bValues[i];
            }
        }

        // Convert back to FastDecimal objects
        return createFastDecimals(resultValues);
    }

    /**
     * Multiplies two arrays of FastDecimal values element-wise and stores the result in a new array.
     * Note: This implementation handles the scaling factor for multiplication.
     *
     * @param a the first array of FastDecimal values
     * @param b the second array of FastDecimal values
     * @return a new array containing the element-wise product of a and b
     * @throws IllegalArgumentException if the arrays have different lengths
     */
    public static FastDecimal[] multiply(FastDecimal[] a, FastDecimal[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }

        if (!initialized) {
            initialize();
        }

        // Extract the scaled values from the FastDecimal objects
        long[] aValues = extractScaledValues(a);
        long[] bValues = extractScaledValues(b);
        long[] resultValues = new long[a.length];

        // Check if CUDA is available and multiplyKernel is not null
        if (cudaAvailable && multiplyKernel != null) {
            System.out.println("[DEBUG_LOG] Using CUDA for multiplication");
            try {
                // Allocate device memory
                CUdeviceptr dA = new CUdeviceptr();
                CUdeviceptr dB = new CUdeviceptr();
                CUdeviceptr dResult = new CUdeviceptr();

                JCudaDriver.cuMemAlloc(dA, a.length * Sizeof.LONG);
                JCudaDriver.cuMemAlloc(dB, a.length * Sizeof.LONG);
                JCudaDriver.cuMemAlloc(dResult, a.length * Sizeof.LONG);

                // Copy host memory to device
                JCudaDriver.cuMemcpyHtoD(dA, Pointer.to(aValues), a.length * Sizeof.LONG);
                JCudaDriver.cuMemcpyHtoD(dB, Pointer.to(bValues), a.length * Sizeof.LONG);

                // Set up kernel parameters
                Pointer kernelParams = Pointer.to(
                        Pointer.to(dA),
                        Pointer.to(dB),
                        Pointer.to(dResult),
                        Pointer.to(new int[]{a.length}),
                        Pointer.to(new long[]{SCALE_FACTOR})
                );

                // Calculate grid and block dimensions
                int blockSize = DEFAULT_BLOCK_SIZE;
                int gridSize = (a.length + blockSize - 1) / blockSize;

                // Launch the kernel
                JCudaDriver.cuLaunchKernel(multiplyKernel,
                        gridSize, 1, 1,      // Grid dimension
                        blockSize, 1, 1,     // Block dimension
                        0, null,             // Shared memory size and stream
                        kernelParams, null   // Kernel parameters and extra parameters
                );

                // Copy result from device to host
                JCudaDriver.cuMemcpyDtoH(Pointer.to(resultValues), dResult, a.length * Sizeof.LONG);

                // Free device memory
                JCudaDriver.cuMemFree(dA);
                JCudaDriver.cuMemFree(dB);
                JCudaDriver.cuMemFree(dResult);
            } catch (Exception e) {
                // Fall back to CPU implementation if CUDA fails
                System.err.println("JCudaFastDecimal: CUDA multiplication failed, falling back to CPU: " + e.getMessage());
                for (int i = 0; i < a.length; i++) {
                    resultValues[i] = (aValues[i] * bValues[i]) / SCALE_FACTOR;
                }
            }
        } else {
            // Use CPU implementation if CUDA is not available or multiplyKernel is null
            if (!cudaAvailable) {
                System.out.println("JCudaFastDecimal: CUDA not available, using CPU implementation for multiplication");
            } else if (multiplyKernel == null) {
                System.err.println("JCudaFastDecimal: CUDA multiplication kernel is null, falling back to CPU");
            }
            System.out.println("[DEBUG_LOG] Falling back to CPU for multiplication");
            for (int i = 0; i < a.length; i++) {
                resultValues[i] = (aValues[i] * bValues[i]) / SCALE_FACTOR;
            }
        }

        // Convert back to FastDecimal objects
        return createFastDecimals(resultValues);
    }

    /**
     * Divides the first array of FastDecimal values by the second element-wise
     * and stores the result in a new array.
     * Note: This implementation handles the scaling factor for division and uses HALF_UP rounding.
     *
     * @param a the first array of FastDecimal values (dividend)
     * @param b the second array of FastDecimal values (divisor)
     * @return a new array containing the element-wise quotient of a and b
     * @throws IllegalArgumentException if the arrays have different lengths
     * @throws ArithmeticException      if any element in b is zero
     */
    public static FastDecimal[] divide(FastDecimal[] a, FastDecimal[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }

        if (!initialized) {
            initialize();
        }

        // Extract the scaled values from the FastDecimal objects
        long[] aValues = extractScaledValues(a);
        long[] bValues = extractScaledValues(b);

        // Check for division by zero
        for (long value : bValues) {
            if (value == 0) {
                throw new ArithmeticException("Division by zero");
            }
        }

        long[] resultValues = new long[a.length];

        // Check if CUDA is available and divideKernel is not null
        if (cudaAvailable && divideKernel != null) {
            System.out.println("[DEBUG_LOG] Using CUDA for division");
            try {
                // Allocate device memory
                CUdeviceptr dA = new CUdeviceptr();
                CUdeviceptr dB = new CUdeviceptr();
                CUdeviceptr dResult = new CUdeviceptr();

                JCudaDriver.cuMemAlloc(dA, a.length * Sizeof.LONG);
                JCudaDriver.cuMemAlloc(dB, a.length * Sizeof.LONG);
                JCudaDriver.cuMemAlloc(dResult, a.length * Sizeof.LONG);

                // Copy host memory to device
                JCudaDriver.cuMemcpyHtoD(dA, Pointer.to(aValues), a.length * Sizeof.LONG);
                JCudaDriver.cuMemcpyHtoD(dB, Pointer.to(bValues), a.length * Sizeof.LONG);

                // Set up kernel parameters
                Pointer kernelParams = Pointer.to(
                        Pointer.to(dA),
                        Pointer.to(dB),
                        Pointer.to(dResult),
                        Pointer.to(new int[]{a.length}),
                        Pointer.to(new long[]{SCALE_FACTOR})
                );

                // Calculate grid and block dimensions
                int blockSize = DEFAULT_BLOCK_SIZE;
                int gridSize = (a.length + blockSize - 1) / blockSize;

                // Launch the kernel
                JCudaDriver.cuLaunchKernel(divideKernel,
                        gridSize, 1, 1,      // Grid dimension
                        blockSize, 1, 1,     // Block dimension
                        0, null,             // Shared memory size and stream
                        kernelParams, null   // Kernel parameters and extra parameters
                );

                // Copy result from device to host
                JCudaDriver.cuMemcpyDtoH(Pointer.to(resultValues), dResult, a.length * Sizeof.LONG);

                // Free device memory
                JCudaDriver.cuMemFree(dA);
                JCudaDriver.cuMemFree(dB);
                JCudaDriver.cuMemFree(dResult);
            } catch (Exception e) {
                // Fall back to CPU implementation if CUDA fails
                System.err.println("JCudaFastDecimal: CUDA division failed, falling back to CPU: " + e.getMessage());
                performCpuDivision(aValues, bValues, resultValues);
            }
        } else {
            // Use CPU implementation if CUDA is not available or divideKernel is null
            if (!cudaAvailable) {
                System.out.println("JCudaFastDecimal: CUDA not available, using CPU implementation for division");
            } else if (divideKernel == null) {
                System.err.println("JCudaFastDecimal: CUDA division kernel is null, falling back to CPU");
            }
            System.out.println("[DEBUG_LOG] Falling back to CPU for division");
            performCpuDivision(aValues, bValues, resultValues);
        }

        // Convert back to FastDecimal objects
        return createFastDecimals(resultValues);
    }

    /**
     * Performs division using CPU implementation with HALF_UP rounding.
     *
     * @param aValues      the dividend values
     * @param bValues      the divisor values
     * @param resultValues the array to store the results
     */
    private static void performCpuDivision(long[] aValues, long[] bValues, long[] resultValues) {
        for (int i = 0; i < aValues.length; i++) {
            // Scale up the dividend to maintain precision
            long dividend = aValues[i] * SCALE_FACTOR;

            // Calculate quotient and remainder
            long quotient = dividend / bValues[i];
            long remainder = dividend % bValues[i];

            // Apply HALF_UP rounding
            long halfDivisor = Math.abs(bValues[i]) / 2;
            if (Math.abs(remainder) >= halfDivisor) {
                resultValues[i] = quotient + (aValues[i] >= 0 ? 1 : -1);
            } else {
                resultValues[i] = quotient;
            }
        }
    }

    /**
     * Extracts the scaled values from an array of FastDecimal objects.
     *
     * @param decimals the array of FastDecimal objects
     * @return an array of the scaled values
     */
    private static long[] extractScaledValues(FastDecimal[] decimals) {
        long[] values = new long[decimals.length];
        for (int i = 0; i < decimals.length; i++) {
            values[i] = decimals[i].getScaledValue();
        }
        return values;
    }

    /**
     * Creates an array of FastDecimal objects from an array of scaled values.
     *
     * @param values the array of scaled values
     * @return an array of FastDecimal objects
     */
    private static FastDecimal[] createFastDecimals(long[] values) {
        FastDecimal[] decimals = new FastDecimal[values.length];
        for (int i = 0; i < values.length; i++) {
            decimals[i] = FastDecimal.fromScaledValue(values[i]);
        }
        return decimals;
    }
}
