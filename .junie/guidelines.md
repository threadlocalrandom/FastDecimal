# FastDecimal Project Guidelines

This document provides essential information for developers working on the FastDecimal project.

## Build/Configuration Instructions

### Prerequisites
- Java 21 or higher
- Maven 3.8 or higher

### Building the Project
To build the project, run:
```bash
mvn clean install
```

### Project Structure
- `src/main/java/org/tlr/fastdecimal/` - Main source code
- `src/test/java/org/tlr/fastdecimal/` - Test code

## Testing Information

### Running Tests
To run all tests:
```bash
mvn test
```

To run a specific test class:
```bash
mvn test -Dtest=FastDecimalTest
```

To run a specific test method:
```bash
mvn test -Dtest=FastDecimalTest#testToBigDecimal
```

### Adding New Tests
1. Create a new test class in the `src/test/java/org/tlr/fastdecimal/` directory
2. Extend the test class with JUnit 5 annotations
3. Follow the existing test patterns

### Test Example
Here's a simple test example:

```java
package org.tlr.fastdecimal;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class FastDecimalTest {
    @Test
    public void testToBigDecimal() {
        FastDecimal decimal = FastDecimal.of("123");
        BigDecimal bigDecimal = decimal.toBigDecimal();
        assertEquals(new BigDecimal("12.3"), bigDecimal, 
                    "FastDecimal should convert to correct BigDecimal");
    }
}
```

## Additional Development Information

### Code Style
- Follow standard Java code style conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods and classes

### Implementation Details
- FastDecimal uses a long value scaled by 10,000 for internal representation
- The `toBigDecimal()` method converts the internal representation to a BigDecimal with proper scale
- Trailing zeros are stripped from BigDecimal representations
- The following methods are to be implemented using fixed decimal algorithms
  1. add
  2. subtract
  3. multiply
  4. divide

### Performance Considerations
- FastDecimal is designed for high-performance decimal arithmetic
- It avoids the overhead of BigDecimal for simple operations
- For complex operations or when precision is critical, convert to BigDecimal

### Extending the Library
When adding new functionality:
1. Add the method to the FastDecimal interface
2. Implement the method in FastDecimalImpl
3. Add appropriate tests
4. Update documentation

### Debugging Tips
- Use the toString() method to get a string representation of FastDecimal values
- Check the internal state using the State enum (FINITE, INFINITE, UNKNOWN)