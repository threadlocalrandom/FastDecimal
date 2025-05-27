package org.tlr.fastdecimal.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify the string caching behavior of FastDecimal.
 */
public class StringCacheTest {

    @Test
    public void testStringCaching() {
        // Create a FastDecimal instance
        FastDecimal decimal = FastDecimal.of("123.4567");

        // First call to toString() should compute and cache the result
        String firstResult = decimal.toString();
        assertEquals("123.4567", firstResult, "First toString() call should return correct value");

        // Get the string representation multiple times
        for (int i = 0; i < 5; i++) {
            String result = decimal.toString();
            assertEquals(firstResult, result, "Subsequent toString() calls should return the same value");

            // Verify that the same object is returned (identity check)
            // This will only pass if the string is cached and the same instance is returned
            assertSame(firstResult, result, "Subsequent toString() calls should return the cached string instance");
        }

        // Test with a different decimal
        FastDecimal anotherDecimal = FastDecimal.of("987.6543");
        String anotherResult = anotherDecimal.toString();
        assertEquals("987.6543", anotherResult, "toString() for different instance should return correct value");

        // Ensure different instances have different cache values
        assertNotEquals(firstResult, anotherResult, "Different instances should have different string representations");
    }

    @Test
    public void testPerformanceImprovement() {
        // Create a FastDecimal instance with a complex representation
        FastDecimal decimal = FastDecimal.of("9876543210.12345");

        // Warm-up call
        decimal.toString();

        // Measure time for first call after warm-up (should use cache)
        long startTime = System.nanoTime();
        String result = decimal.toString();
        long firstCallTime = System.nanoTime() - startTime;

        // Measure time for second call (should be faster due to caching)
        startTime = System.nanoTime();
        String secondResult = decimal.toString();
        long secondCallTime = System.nanoTime() - startTime;

        // Log the times
        System.out.println("[DEBUG_LOG] First call time: " + firstCallTime + " ns");
        System.out.println("[DEBUG_LOG] Second call time: " + secondCallTime + " ns");

        // The second call should be significantly faster
        // Note: This is a simple test and might be affected by JVM optimizations
        // We're not making a strict assertion here, just logging the difference

        // Verify the results are the same
        assertEquals(result, secondResult, "Both calls should return the same value");

        // Verify the same object is returned (identity check)
        assertSame(result, secondResult, "Second call should return the cached string instance");
    }
}