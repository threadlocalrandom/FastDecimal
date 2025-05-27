package org.tlr.fastdecimal.vector;

import org.junit.jupiter.api.Test;
import org.tlr.fastdecimal.core.FastDecimal;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the VectorFastDecimal class.
 * These tests verify that the vector operations produce the same results as the regular operations.
 */
public class VectorFastDecimalTest {

    @Test
    public void testAddition() {
        // Create test arrays
        FastDecimal[] a = createTestArray(100);
        FastDecimal[] b = createTestArray(100);

        // Compute expected results using regular operations
        FastDecimal[] expected = new FastDecimal[a.length];
        for (int i = 0; i < a.length; i++) {
            expected[i] = a[i].add(b[i]);
        }

        // Compute actual results using vector operations
        FastDecimal[] actual = VectorFastDecimal.add(a, b);

        // Verify results
        assertArrayEquals(expected, actual, "Vector addition should produce the same results as regular addition");
    }

    @Test
    public void testSubtraction() {
        // Create test arrays
        FastDecimal[] a = createTestArray(100);
        FastDecimal[] b = createTestArray(100);

        // Compute expected results using regular operations
        FastDecimal[] expected = new FastDecimal[a.length];
        for (int i = 0; i < a.length; i++) {
            expected[i] = a[i].subtract(b[i]);
        }

        // Compute actual results using vector operations
        FastDecimal[] actual = VectorFastDecimal.subtract(a, b);

        // Verify results
        assertArrayEquals(expected, actual, "Vector subtraction should produce the same results as regular subtraction");
    }

    @Test
    public void testMultiplication() {
        // Create test arrays
        FastDecimal[] a = createTestArray(100);
        FastDecimal[] b = createTestArray(100);

        // Compute expected results using regular operations
        FastDecimal[] expected = new FastDecimal[a.length];
        for (int i = 0; i < a.length; i++) {
            expected[i] = a[i].multiply(b[i]);
        }

        // Compute actual results using vector operations
        FastDecimal[] actual = VectorFastDecimal.multiply(a, b);

        // Verify results
        assertArrayEquals(expected, actual, "Vector multiplication should produce the same results as regular multiplication");
    }

    @Test
    public void testDivision() {
        // Create test arrays
        FastDecimal[] a = createTestArray(100);
        FastDecimal[] b = createNonZeroTestArray(100);

        // Compute expected results using regular operations
        FastDecimal[] expected = new FastDecimal[a.length];
        for (int i = 0; i < a.length; i++) {
            expected[i] = a[i].divide(b[i]);
        }

        // Compute actual results using vector operations
        FastDecimal[] actual = VectorFastDecimal.divide(a, b);

        // Verify results
        assertArrayEquals(expected, actual, "Vector division should produce the same results as regular division");
    }

    @Test
    public void testDivisionByZero() {
        // Create test arrays
        FastDecimal[] a = createTestArray(10);
        FastDecimal[] b = new FastDecimal[10];
        Arrays.fill(b, FastDecimal.ZERO);

        // Verify that division by zero throws an exception
        assertThrows(ArithmeticException.class, () -> VectorFastDecimal.divide(a, b),
                "Division by zero should throw an ArithmeticException");
    }

    @Test
    public void testArraysOfDifferentLengths() {
        // Create test arrays of different lengths
        FastDecimal[] a = createTestArray(10);
        FastDecimal[] b = createTestArray(5);

        // Verify that operations with arrays of different lengths throw an exception
        assertThrows(IllegalArgumentException.class, () -> VectorFastDecimal.add(a, b),
                "Addition with arrays of different lengths should throw an IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> VectorFastDecimal.subtract(a, b),
                "Subtraction with arrays of different lengths should throw an IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> VectorFastDecimal.multiply(a, b),
                "Multiplication with arrays of different lengths should throw an IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> VectorFastDecimal.divide(a, b),
                "Division with arrays of different lengths should throw an IllegalArgumentException");
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
            double value = (i % 20) - 10; // Values from -10 to 9
            array[i] = FastDecimal.of(value);
        }
        return array;
    }

    /**
     * Creates an array of non-zero FastDecimal objects with test values.
     *
     * @param size the size of the array
     * @return an array of non-zero FastDecimal objects
     */
    private FastDecimal[] createNonZeroTestArray(int size) {
        FastDecimal[] array = new FastDecimal[size];
        for (int i = 0; i < size; i++) {
            // Use a deterministic pattern for test values, ensuring no zeros
            double value = ((i % 19) - 9) + 0.1; // Values from -8.9 to 9.1, avoiding 0
            array[i] = FastDecimal.of(value);
        }
        return array;
    }
}