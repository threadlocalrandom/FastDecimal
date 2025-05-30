package org.tlr.fastdecimal.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FastDecimalTest {

    @Nested
    @DisplayName("Construction tests")
    class ConstructionTests {
        @Test
        @DisplayName("Create from valid strings")
        void testCreateFromValidStrings() {
            assertEquals("1.2345", FastDecimal.of("1.23456678").toString());
            assertEquals("1.23", FastDecimal.of("1.23").toString());
            assertEquals("0", FastDecimal.of("0").toString());
            assertEquals("-1.23", FastDecimal.of("-1.23").toString());
            assertEquals("1", FastDecimal.of("1.0").toString());
            assertEquals("1000", FastDecimal.of("1000").toString());
        }

        @Test
        @DisplayName("Create from valid doubles")
        void testCreateFromValidDoubles() {
            assertEquals("1.23", FastDecimal.of(1.23).toString());
            assertEquals("0", FastDecimal.of(0.0).toString());
            assertEquals("-1.23", FastDecimal.of(-1.23).toString());
            assertEquals("1000", FastDecimal.of(1000.0).toString());
        }

        @Test
        @DisplayName("Invalid string construction")
        void testInvalidStringConstruction() {
            assertThrows(NumberFormatException.class, () -> FastDecimal.of("abc"));
            assertThrows(NumberFormatException.class, () -> FastDecimal.of("1.2.3"));
            assertThrows(NumberFormatException.class, () -> FastDecimal.of(""));
            assertThrows(NumberFormatException.class, () -> FastDecimal.of("."));
        }

        @Test
        @DisplayName("Invalid double construction")
        void testInvalidDoubleConstruction() {
            assertThrows(ArithmeticException.class, () -> FastDecimal.of(Double.NaN));
            assertThrows(ArithmeticException.class, () -> FastDecimal.of(Double.POSITIVE_INFINITY));
            assertThrows(ArithmeticException.class, () -> FastDecimal.of(Double.NEGATIVE_INFINITY));
        }
    }

    @Nested
    @DisplayName("Arithmetic operation tests")
    class ArithmeticTests {
        @Test
        @DisplayName("Addition")
        void testAddition() {
            FastDecimal a = FastDecimal.of("1.23");
            FastDecimal b = FastDecimal.of("4.56");
            assertEquals("5.79", a.add(b).toString());
            assertEquals("0", a.add(a.negate()).toString());
        }

        @Test
        @DisplayName("Subtraction")
        void testSubtraction() {
            FastDecimal a = FastDecimal.of("4.56");
            FastDecimal b = FastDecimal.of("1.23");
            assertEquals("3.33", a.subtract(b).toString());
            assertEquals("0", a.subtract(a).toString());
        }

        @Test
        @DisplayName("Multiplication")
        void testMultiplication() {
            FastDecimal a = FastDecimal.of("2.5");
            FastDecimal b = FastDecimal.of("3");
            assertEquals("7.5", a.multiply(b).toString());
            assertEquals("0", a.multiply(FastDecimal.ZERO).toString());
        }

        @Test
        @DisplayName("Division")
        void testDivision() {
            FastDecimal a = FastDecimal.of("7.50003");
            FastDecimal b = FastDecimal.of("2.5");
            FastDecimal c = FastDecimal.of("3.0000");
            FastDecimal d = FastDecimal.of("0.123456789");
            BigDecimal bc = new BigDecimal("3.0000").setScale(4, RoundingMode.HALF_UP);
            BigDecimal bd = BigDecimal.valueOf(0.123456789);
            var bs = bc.divide(bd, RoundingMode.HALF_UP);
            assertEquals(bs.toString(), c.divide(d).toString());
            assertEquals("3", a.divide(b).toString());
            assertThrows(ArithmeticException.class, () -> a.divide(FastDecimal.ZERO));
        }
    }

    @Nested
    @DisplayName("Comparison tests")
    class ComparisonTests {
        @Test
        @DisplayName("Compare to")
        void testCompareTo() {
            FastDecimal a = FastDecimal.of("1.23");
            FastDecimal b = FastDecimal.of("4.56");
            FastDecimal c = FastDecimal.of("1.23");

            assertTrue(a.compareTo(b) < 0);
            assertTrue(b.compareTo(a) > 0);
            assertEquals(0, a.compareTo(c));
        }

        @Test
        @DisplayName("Equality")
        void testEquality() {
            FastDecimal a = FastDecimal.of("1.23");
            FastDecimal b = FastDecimal.of("1.23");
            FastDecimal c = FastDecimal.of("4.56");

            assertEquals(a, b);
            assertNotEquals(a, c);
            assertNotEquals(null, a);
        }
    }

    @Nested
    @DisplayName("Sign tests")
    class SignTests {
        @ParameterizedTest
        @CsvSource({
                "1.23, true",
                "0.01, true",
                "0, false",
                "-0.01, false",
                "-1.23, false"
        })
        @DisplayName("Test isPositive")
        void testIsPositive(String value, boolean expected) {
            assertEquals(expected, FastDecimal.of(value).isPositive());
        }

        @Test
        @DisplayName("Test sign operations")
        void testSignOperations() {
            FastDecimal positive = FastDecimal.of("1.23");
            FastDecimal negative = FastDecimal.of("-1.23");
            FastDecimal zero = FastDecimal.of("0");

            assertTrue(positive.isPositive());
            assertFalse(positive.isNegative());
            assertTrue(negative.isNegative());
            assertFalse(negative.isPositive());
            assertTrue(zero.isZero());
            assertFalse(zero.isPositive());
            assertFalse(zero.isNegative());
        }
    }


    @Nested
    @DisplayName("Scale tests")
    class ScaleTests {
        @ParameterizedTest
        @CsvSource({
                "1.23, 2",
                "1.0, 0",
                "1, 0",
                "1.230, 2",
                "0.001, 3"
        })
        @DisplayName("Test scale")
        void testScale(String value, int expectedScale) {
            assertEquals(expectedScale, FastDecimal.of(value).scale());
        }
    }

    @Nested
    @DisplayName("Conversion tests")
    class ConversionTests {
        @Test
        @DisplayName("Test conversion to primitive types")
        void testConversionToPrimitives() {
            FastDecimal value = FastDecimal.of("1.23");
            assertEquals(1L, value.longValue());
            assertEquals(1.23d, value.doubleValue(), 0.000001);
            assertEquals("1.23", value.toString());
            assertEquals(12300L, value.getScaledValue());
        }
    }

    @Nested
    @DisplayName("Constants tests")
    class ConstantsTests {
        @Test
        @DisplayName("Test predefined constants")
        void testConstants() {
            assertEquals("0", FastDecimal.ZERO.toString());
            assertEquals("1", FastDecimal.ONE.toString());
            assertEquals("10", FastDecimal.TEN.toString());
        }
    }

    static Stream<Arguments> edgeCases() {
        return Stream.of(
                Arguments.of("0.0001", "Smallest positive"),
                Arguments.of("-0.0001", "Largest negative"),
                Arguments.of("9999999.9999", "Large positive"),
                Arguments.of("-9999999.9999", "Large negative")
        );
    }

    @ParameterizedTest
    @MethodSource("edgeCases")
    @DisplayName("Test edge cases")
    void testEdgeCases(String value, String description) {
        FastDecimal decimal = FastDecimal.of(value);
        assertEquals(value, decimal.toString(), description);
    }
}
