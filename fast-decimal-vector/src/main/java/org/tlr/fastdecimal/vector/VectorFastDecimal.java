package org.tlr.fastdecimal.vector;

import jdk.incubator.vector.LongVector;
import jdk.incubator.vector.VectorSpecies;
import org.tlr.fastdecimal.core.FastDecimal;

/**
 * Vector implementation of FastDecimal operations using the Java Vector API.
 * This class provides methods for performing operations on arrays of FastDecimal values
 * using SIMD (Single Instruction, Multiple Data) instructions.
 */
public class VectorFastDecimal {

    // The species (vector size and element type) to use for long operations
    private static final VectorSpecies<Long> SPECIES = LongVector.SPECIES_PREFERRED;

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

        // Extract the scaled values from the FastDecimal objects
        long[] aValues = extractScaledValues(a);
        long[] bValues = extractScaledValues(b);

        // Perform the vector addition
        long[] resultValues = vectorAdd(aValues, bValues);

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

        // Extract the scaled values from the FastDecimal objects
        long[] aValues = extractScaledValues(a);
        long[] bValues = extractScaledValues(b);

        // Perform the vector subtraction
        long[] resultValues = vectorSubtract(aValues, bValues);

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

        // Extract the scaled values from the FastDecimal objects
        long[] aValues = extractScaledValues(a);
        long[] bValues = extractScaledValues(b);

        // Perform the vector multiplication with scaling
        long[] resultValues = new long[a.length];

        // Process vectors in chunks
        int i = 0;
        int upperBound = a.length - (a.length % SPECIES.length());

        // Process full vectors
        for (; i < upperBound; i += SPECIES.length()) {
            LongVector va = LongVector.fromArray(SPECIES, aValues, i);
            LongVector vb = LongVector.fromArray(SPECIES, bValues, i);

            // Multiply and divide by 10_000 to maintain the correct scale
            LongVector result = va.mul(vb).div(10_000L);
            result.intoArray(resultValues, i);
        }

        // Process remaining elements
        for (; i < a.length; i++) {
            resultValues[i] = (aValues[i] * bValues[i]) / 10_000L;
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

        // Extract the scaled values from the FastDecimal objects
        long[] aValues = extractScaledValues(a);
        long[] bValues = extractScaledValues(b);

        // Check for division by zero
        for (long value : bValues) {
            if (value == 0) {
                throw new ArithmeticException("Division by zero");
            }
        }

        // Perform the vector division with scaling
        long[] resultValues = new long[a.length];

        // Process elements individually for division with HALF_UP rounding
        // This matches the behavior of FastDecimal.divide()
        for (int i = 0; i < a.length; i++) {
            // Scale up the dividend to maintain precision (same as in FastDecimal)
            long dividend = aValues[i] * 10_000L;

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

        // Convert back to FastDecimal objects
        return createFastDecimals(resultValues);
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
            // We need to access the internal scaled value
            // This assumes FastDecimal has a method to get the scaled value
            values[i] = getScaledValue(decimals[i]);
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
            // This assumes FastDecimal has a factory method that takes a scaled value
            decimals[i] = createFromScaledValue(values[i]);
        }
        return decimals;
    }

    /**
     * Gets the scaled value from a FastDecimal object.
     * This is a helper method to access the internal state of FastDecimal.
     *
     * @param decimal the FastDecimal object
     * @return the scaled value
     */
    private static long getScaledValue(FastDecimal decimal) {
        // Use the public getScaledValue method
        return decimal.getScaledValue();
    }

    /**
     * Creates a FastDecimal object from a scaled value.
     * This is a helper method to create FastDecimal objects.
     *
     * @param scaledValue the scaled value
     * @return a new FastDecimal object
     */
    private static FastDecimal createFromScaledValue(long scaledValue) {
        // Use the public fromScaledValue factory method
        return FastDecimal.fromScaledValue(scaledValue);
    }

    /**
     * Performs vector addition on two arrays of long values.
     *
     * @param a the first array
     * @param b the second array
     * @return a new array containing the element-wise sum of a and b
     */
    private static long[] vectorAdd(long[] a, long[] b) {
        long[] result = new long[a.length];

        // Process vectors in chunks
        int i = 0;
        int upperBound = a.length - (a.length % SPECIES.length());

        // Process full vectors
        for (; i < upperBound; i += SPECIES.length()) {
            LongVector va = LongVector.fromArray(SPECIES, a, i);
            LongVector vb = LongVector.fromArray(SPECIES, b, i);
            va.add(vb).intoArray(result, i);
        }

        // Process remaining elements
        for (; i < a.length; i++) {
            result[i] = a[i] + b[i];
        }

        return result;
    }

    /**
     * Performs vector subtraction on two arrays of long values.
     *
     * @param a the first array
     * @param b the second array
     * @return a new array containing the element-wise difference of a and b
     */
    private static long[] vectorSubtract(long[] a, long[] b) {
        long[] result = new long[a.length];

        // Process vectors in chunks
        int i = 0;
        int upperBound = a.length - (a.length % SPECIES.length());

        // Process full vectors
        for (; i < upperBound; i += SPECIES.length()) {
            LongVector va = LongVector.fromArray(SPECIES, a, i);
            LongVector vb = LongVector.fromArray(SPECIES, b, i);
            va.sub(vb).intoArray(result, i);
        }

        // Process remaining elements
        for (; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }

        return result;
    }
}
