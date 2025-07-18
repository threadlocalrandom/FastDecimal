/**
 * CUDA kernels for FastDecimal operations.
 * These kernels implement basic arithmetic operations for arrays of scaled decimal values.
 */

/**
 * Adds two arrays of scaled decimal values element-wise.
 *
 * @param a The first input array
 * @param b The second input array
 * @param result The output array to store the results
 * @param n The length of the arrays
 */
extern "C" __global__ void add_kernel(long* a, long* b, long* result, int n) {
    int i = blockIdx.x * blockDim.x + threadIdx.x;
    if (i < n) {
        result[i] = a[i] + b[i];
    }
}

/**
 * Subtracts one array of scaled decimal values from another element-wise.
 *
 * @param a The first input array (minuend)
 * @param b The second input array (subtrahend)
 * @param result The output array to store the results
 * @param n The length of the arrays
 */
extern "C" __global__ void subtract_kernel(long* a, long* b, long* result, int n) {
    int i = blockIdx.x * blockDim.x + threadIdx.x;
    if (i < n) {
        result[i] = a[i] - b[i];
    }
}

/**
 * Multiplies two arrays of scaled decimal values element-wise.
 * The result is scaled by dividing by the scale factor to maintain the correct scale.
 *
 * @param a The first input array
 * @param b The second input array
 * @param result The output array to store the results
 * @param n The length of the arrays
 * @param scale_factor The scale factor used for internal representation
 */
extern "C" __global__ void multiply_kernel(long* a, long* b, long* result, int n, long scale_factor) {
    int i = blockIdx.x * blockDim.x + threadIdx.x;
    if (i < n) {
        result[i] = (a[i] * b[i]) / scale_factor;
    }
}

/**
 * Divides one array of scaled decimal values by another element-wise.
 * The dividend is scaled up by multiplying by the scale factor to maintain precision.
 * HALF_UP rounding is applied to match the behavior of FastDecimal.divide().
 *
 * @param a The first input array (dividend)
 * @param b The second input array (divisor)
 * @param result The output array to store the results
 * @param n The length of the arrays
 * @param scale_factor The scale factor used for internal representation
 */
extern "C" __global__ void divide_kernel(long* a, long* b, long* result, int n, long scale_factor) {
    int i = blockIdx.x * blockDim.x + threadIdx.x;
    if (i < n) {
        if (b[i] == 0) {
            // Set a special value to indicate division by zero
            // This will be checked later in the Java code
            result[i] = 9223372036854775807L; // Long.MAX_VALUE
        } else {
            // Scale up the dividend to maintain precision
            long dividend = a[i] * scale_factor;
            
            // Calculate quotient and remainder
            long quotient = dividend / b[i];
            long remainder = dividend % b[i];
            
            // Apply HALF_UP rounding
            long halfDivisor = (b[i] < 0 ? -b[i] : b[i]) / 2;
            if ((remainder < 0 ? -remainder : remainder) >= halfDivisor) {
                result[i] = quotient + (a[i] >= 0 ? 1 : -1);
            } else {
                result[i] = quotient;
            }
        }
    }
}