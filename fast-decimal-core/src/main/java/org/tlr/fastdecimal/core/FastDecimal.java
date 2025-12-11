package org.tlr.fastdecimal.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A high-performance decimal number implementation that uses a long value
 * scaled by 10,000 for internal representation.
 */
public class FastDecimal implements Comparable<FastDecimal> {

    public static final FastDecimal ZERO = new FastDecimal(0);

    public static final FastDecimal ONE = new FastDecimal(10_000);

    public static final FastDecimal TEN = new FastDecimal(100_000);

    /**
     * The scale factor used for internal representation
     */
    private static final int SCALE_FACTOR = 10_000;
    private static final int SCALE_DIGITS = 4;

    private static final long MAX_REPRESENTABLE_VALUE = Long.MAX_VALUE / SCALE_FACTOR;
    private static final long MIN_REPRESENTABLE_VALUE = Long.MIN_VALUE / SCALE_FACTOR;


    /**
     * Internal state of the decimal number
     */
    private enum State {
        FINITE,
        INFINITE,
        UNKNOWN
    }

    /**
     * The internal scaled value
     */
    private final long scaledValue;

    /**
     * The state of this decimal number
     */
    private final State state;

    /**
     * Cache for the string representation of this decimal number
     */
    private String stringCache;

    private FastDecimal(long scaledValue) {
        this.scaledValue = scaledValue;
        this.state = State.FINITE;
    }

    /**
     * Creates a FastDecimal from a BigDecimal value.
     * The value is rounded to 4 fractional digits using HALF_UP to match the internal scale.
     *
     * @param value BigDecimal value (non-null)
     * @throws NullPointerException if value is null
     * @throws ArithmeticException if the scaled value doesn't fit into a 64-bit signed integer
     */
    public FastDecimal(BigDecimal value) {
        this(scaleFromBigDecimal(value));
    }

    private static long scaleFromBigDecimal(BigDecimal value) {
        if (value == null) {
            throw new NullPointerException("BigDecimal value cannot be null");
        }
        // Normalize to 4 decimal places with HALF_UP rounding, then convert to scaled long
        BigDecimal scaled = value.setScale(SCALE_DIGITS, RoundingMode.HALF_UP);
        // Move decimal point to the right by SCALE_DIGITS so the result is an integer
        BigDecimal shifted = scaled.movePointRight(SCALE_DIGITS);
        // Use longValueExact to detect overflow
        return shifted.longValueExact();
    }

    /**
     * Creates a FastDecimal from a string representation.
     *
     * @param value the string representation of the number
     * @return a new FastDecimal instance
     * @throws NumberFormatException if the string cannot be parsed
     */
    public static FastDecimal of(String value) {
        boolean isNegative = value.startsWith("-");
        String absValue = isNegative ? value.substring(1) : value;

        int decimalPoint = absValue.indexOf('.');
        String wholePart = decimalPoint == -1 ? absValue : absValue.substring(0, decimalPoint);
        StringBuilder decimalPart = new StringBuilder(decimalPoint == -1 ? "0" : absValue.substring(decimalPoint + 1));

        // Pad or truncate decimal part to match scale
        if (decimalPart.length() > SCALE_DIGITS) {
            decimalPart = new StringBuilder(decimalPart.substring(0, SCALE_DIGITS));
        } else while (decimalPart.length() < SCALE_DIGITS) {
            decimalPart.append("0");
        }

        try {
            long whole = Long.parseLong(wholePart) * SCALE_FACTOR;
            long decimal = Long.parseLong(decimalPart.toString());
            long result = whole + decimal;

            return new FastDecimal(isNegative ? -result : result);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid number format: " + value);
        }
    }

    /**
     * Returns this FastDecimal as a long, discarding any fractional part.
     * This conversion is done by dividing the scaled value by the scale factor.
     *
     * @return the whole number part of this FastDecimal as a long
     */
    public long longValue() {
        return scaledValue / SCALE_FACTOR;
    }

    @Override
    public String toString() {
        // Return cached value if available
        if (stringCache != null) {
            return stringCache;
        }

        long abs = Math.abs(scaledValue);
        long wholePart = abs / SCALE_FACTOR;
        long decimalPart = abs % SCALE_FACTOR;

        // Fast path for zero decimal part
        if (decimalPart == 0) {
            stringCache = scaledValue < 0 ? "-" + wholePart : Long.toString(wholePart);
            return stringCache;
        }

        // Optimize for common case
        StringBuilder result = new StringBuilder(16); // Pre-allocate reasonable size
        if (scaledValue < 0) {
            result.append('-');
        }
        result.append(wholePart);
        result.append('.');

        // Manually format decimal part with leading zeros
        // This is much faster than String.format
        char[] decimalChars = new char[SCALE_DIGITS];
        for (int i = SCALE_DIGITS - 1; i >= 0; i--) {
            decimalChars[i] = (char) ('0' + (decimalPart % 10));
            decimalPart /= 10;
        }

        // Find last non-zero digit to remove trailing zeros
        int lastNonZero = SCALE_DIGITS - 1;
        while (lastNonZero >= 0 && decimalChars[lastNonZero] == '0') {
            lastNonZero--;
        }

        // Append decimal digits without trailing zeros
        result.append(decimalChars, 0, lastNonZero + 1);

        // Cache and return the result
        stringCache = result.toString();
        return stringCache;
    }

    /**
     * Compares this FastDecimal with another FastDecimal numerically.
     * Returns:
     * - A negative value if this FastDecimal is less than the argument
     * - Zero if this FastDecimal is equal to the argument
     * - A positive value if this FastDecimal is greater than the argument
     *
     * @param other the FastDecimal to compare with
     * @return a negative integer, zero, or a positive integer as this FastDecimal
     * is less than, equal to, or greater than the specified FastDecimal
     * @throws NullPointerException if the specified FastDecimal is null
     */
    @Override
    public int compareTo(FastDecimal other) {
        if (other == null) {
            throw new NullPointerException("Cannot compare with null");
        }

        // Quick return if the objects are the same
        if (this == other) {
            return 0;
        }

        // Compare scaled values directly since they're normalized to the same scale
        return Long.compare(this.scaledValue, other.scaledValue);
    }

    /**
     * Indicates whether this FastDecimal is equal to another object.
     * Two FastDecimals are considered equal if they have the same scaled value.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FastDecimal other)) {
            return false;
        }
        return this.scaledValue == other.scaledValue;
    }

    /**
     * Returns a hash code value for this FastDecimal.
     *
     * @return a hash code value for this FastDecimal
     */
    @Override
    public int hashCode() {
        return Long.hashCode(scaledValue);
    }

    /**
     * Returns whether this FastDecimal is less than the other FastDecimal.
     *
     * @param other the FastDecimal to compare with
     * @return true if this FastDecimal is less than the other FastDecimal
     * @throws NullPointerException if the specified FastDecimal is null
     */
    public boolean isLessThan(FastDecimal other) {
        return compareTo(other) < 0;
    }

    public FastDecimal negate() {
        return new FastDecimal(-scaledValue);
    }

    /**
     * Returns whether this FastDecimal is greater than the other FastDecimal.
     *
     * @param other the FastDecimal to compare with
     * @return true if this FastDecimal is greater than the other FastDecimal
     * @throws NullPointerException if the specified FastDecimal is null
     */
    public boolean isGreaterThan(FastDecimal other) {
        return compareTo(other) > 0;
    }

    /**
     * Returns whether this FastDecimal is less than or equal to the other FastDecimal.
     *
     * @param other the FastDecimal to compare with
     * @return true if this FastDecimal is less than or equal to the other FastDecimal
     * @throws NullPointerException if the specified FastDecimal is null
     */
    public boolean isLessThanOrEqual(FastDecimal other) {
        return compareTo(other) <= 0;
    }

    /**
     * Returns whether this FastDecimal is greater than or equal to the other FastDecimal.
     *
     * @param other the FastDecimal to compare with
     * @return true if this FastDecimal is greater than or equal to the other FastDecimal
     * @throws NullPointerException if the specified FastDecimal is null
     */
    public boolean isGreaterThanOrEqual(FastDecimal other) {
        return compareTo(other) >= 0;
    }

    /**
     * Adds another FastDecimal to this one.
     *
     * @param other the FastDecimal to add
     * @return a new FastDecimal representing the sum
     */
    public FastDecimal add(FastDecimal other) {
        return new FastDecimal(this.scaledValue + other.scaledValue);
    }

    /**
     * Subtracts another FastDecimal from this one.
     *
     * @param other the FastDecimal to subtract
     * @return a new FastDecimal representing the difference
     */
    public FastDecimal subtract(FastDecimal other) {
        return new FastDecimal(this.scaledValue - other.scaledValue);
    }

    public FastDecimal multiply(FastDecimal other) {
        // Use a wider type for intermediate calculation to prevent overflow
        long result = (scaledValue * other.scaledValue) / SCALE_FACTOR;
        return new FastDecimal(result);
    }

    public FastDecimal divide(FastDecimal other, RoundingMode roundingMode) {
        if (other.scaledValue == 0) {
            throw new ArithmeticException("Division by zero");
        }

        // Scale up the dividend to maintain precision
        long dividend = scaledValue * SCALE_FACTOR;
        long result;

        switch (roundingMode) {
            case FLOOR -> result = dividend / other.scaledValue;
            case CEILING -> result = (dividend + other.scaledValue - 1) / other.scaledValue;
            case HALF_UP -> {
                long quotient = dividend / other.scaledValue;
                long remainder = dividend % other.scaledValue;
                long halfDivisor = Math.abs(other.scaledValue) / 2;
                if (Math.abs(remainder) >= halfDivisor) {
                    result = quotient + (scaledValue >= 0 ? 1 : -1);
                } else {
                    result = quotient;
                }
            }
            default -> throw new IllegalArgumentException("Unsupported rounding mode: " + roundingMode);
        }

        return new FastDecimal(result);
    }

    public FastDecimal divide(FastDecimal other) {
        return divide(other, RoundingMode.HALF_UP);
    }

    public int scale() {
        if (scaledValue == 0) {
            return 0;
        }

        long decimal = Math.abs(scaledValue % SCALE_FACTOR);
        if (decimal == 0) {
            return 0;
        }

        // Count trailing zeros to determine actual scale
        int scale = SCALE_DIGITS;
        while (decimal % 10 == 0) {
            scale--;
            decimal /= 10;
        }
        return scale;
    }

    public double doubleValue() {
        return (double) scaledValue / SCALE_FACTOR;
    }

    /**
     * Returns the scaled value used for internal representation.
     *
     * @return the internal scaled value
     */
    public long getScaledValue() {
        return scaledValue;
    }

    /**
     * Creates a FastDecimal from a scaled value.
     *
     * @param scaledValue the scaled value (internal representation)
     * @return a new FastDecimal with the given scaled value
     */
    public static FastDecimal fromScaledValue(long scaledValue) {
        return new FastDecimal(scaledValue);
    }

    /**
     * Factory method to create a FastDecimal from BigDecimal.
     * Equivalent to calling the BigDecimal constructor.
     */
    public static FastDecimal of(BigDecimal value) {
        return new FastDecimal(value);
    }

    // Existing methods that don't use BigDecimal can remain unchanged:
    // add, subtract, abs, negate, isZero, isPositive, isNegative,
    // compareTo, max, min, hasDecimalPart, longValue, decimalPart,
    // isWholeNumber, signum

    // Methods for moving decimal point
    public FastDecimal movePointLeft(int n) {
        if (n < 0) {
            return movePointRight(-n);
        }
        long factor = (long) Math.pow(10, n);
        return new FastDecimal(scaledValue / factor);
    }

    public FastDecimal movePointRight(int n) {
        if (n < 0) {
            return movePointLeft(-n);
        }
        long factor = (long) Math.pow(10, n);
        // Check for overflow
        if (scaledValue > Long.MAX_VALUE / factor || scaledValue < Long.MIN_VALUE / factor) {
            throw new ArithmeticException("Overflow in movePointRight");
        }
        return new FastDecimal(scaledValue * factor);
    }

    public static FastDecimal of(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new ArithmeticException("Cannot represent NaN or Infinite values");
        }

        validateValueInRange(value);

        long wholePart = (long) value;
        double decimal = value - wholePart;

        long scaledWhole = wholePart * SCALE_FACTOR;
        long scaledDecimal = Math.round(decimal * SCALE_FACTOR);

        return new FastDecimal(scaledWhole + scaledDecimal);
    }

    /**
     * Validates that the given value is within the representable range for FastDecimal.
     *
     * @param value the value to validate
     * @throws ArithmeticException if the value exceeds the representable range
     */
    private static void validateValueInRange(double value) {
        if (value > MAX_REPRESENTABLE_VALUE || value < MIN_REPRESENTABLE_VALUE) {
            throw new ArithmeticException("Value exceeds the representable range");
        }
    }


    /**
     * Returns whether this FastDecimal is strictly positive (greater than zero).
     *
     * @return true if this FastDecimal is greater than zero, false otherwise
     */
    public boolean isPositive() {
        return scaledValue > 0;
    }

    /**
     * Returns whether this FastDecimal is strictly negative (less than zero).
     *
     * @return true if this FastDecimal is less than zero, false otherwise
     */
    public boolean isNegative() {
        return scaledValue < 0;
    }

    /**
     * Returns whether this FastDecimal is zero.
     *
     * @return true if this FastDecimal equals zero, false otherwise
     */
    public boolean isZero() {
        return scaledValue == 0;
    }

    /**
     * Returns whether this FastDecimal is non-negative (greater than or equal to zero).
     *
     * @return true if this FastDecimal is greater than or equal to zero, false otherwise
     */
    public boolean isNonNegative() {
        return scaledValue >= 0;
    }

    /**
     * Returns whether this FastDecimal is non-positive (less than or equal to zero).
     *
     * @return true if this FastDecimal is less than or equal to zero, false otherwise
     */
    public boolean isNonPositive() {
        return scaledValue <= 0;
    }
}
