package org.tlr.fastdecimal.jcuda;

import org.junit.jupiter.api.Test;
import org.tlr.fastdecimal.core.FastDecimal;

/**
 * A simple test class to debug CUDA functionality.
 */
public class CudaDebugTest {

    @Test
    public void testCudaInitialization() {
        System.err.println("[DEBUG_LOG] Starting CUDA debug test");

        // Initialize CUDA
        JCudaFastDecimal.initialize();

        // Create test arrays
        FastDecimal[] a = new FastDecimal[10];
        FastDecimal[] b = new FastDecimal[10];

        for (int i = 0; i < 10; i++) {
            a[i] = FastDecimal.of(i);
            b[i] = FastDecimal.of(i + 1);
        }

        System.err.println("[DEBUG_LOG] Test arrays created");

        // Test addition
        System.err.println("[DEBUG_LOG] Testing addition");
        FastDecimal[] result = JCudaFastDecimal.add(a, b);

        // Print results
        System.err.println("[DEBUG_LOG] Addition results:");
        for (int i = 0; i < result.length; i++) {
            System.err.println("[DEBUG_LOG] " + a[i] + " + " + b[i] + " = " + result[i]);
        }

        // Test other operations
        System.err.println("[DEBUG_LOG] Testing subtraction");
        result = JCudaFastDecimal.subtract(a, b);

        System.err.println("[DEBUG_LOG] Testing multiplication");
        result = JCudaFastDecimal.multiply(a, b);

        System.err.println("[DEBUG_LOG] Testing division");
        result = JCudaFastDecimal.divide(a, b);

        System.err.println("[DEBUG_LOG] CUDA debug test completed");
    }
}
