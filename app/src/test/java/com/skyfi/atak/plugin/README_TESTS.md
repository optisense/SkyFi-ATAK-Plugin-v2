# SkyFi ATAK Plugin Unit Tests

## Overview
This test suite provides comprehensive unit testing for the critical functionality of the SkyFi ATAK plugin, specifically designed for the Space Force demonstration.

## Test Coverage

### 1. AOIManager Tests (AOIManagerTest.java)
- **Purpose**: Tests Area of Interest management functionality
- **Coverage**:
  - Create, Read, Update, Delete (CRUD) operations
  - JSON serialization/deserialization
  - Sensor size requirements validation
  - Concurrent access handling
  - Large dataset performance
- **Test Count**: 11 tests

### 2. ImageCacheManager Tests (ImageCacheManagerTest.java)
- **Purpose**: Tests satellite imagery caching system
- **Coverage**:
  - Image caching and retrieval
  - Cache size management and cleanup
  - Callback mechanisms (progress, success, error)
  - Disk space handling
  - Memory leak prevention
- **Test Count**: 14 tests

### 3. CoordinateInputDialog Tests (CoordinateInputDialogTest.java)
- **Purpose**: Tests coordinate input and conversion functionality
- **Coverage**:
  - Decimal degree parsing
  - MGRS format validation
  - WKT polygon generation
  - High latitude calculations
  - Coordinate range validation
  - Square AOI creation
- **Test Count**: 10 tests

### 4. OrderUtility Tests (OrderUtilityTest.java)
- **Purpose**: Tests polygon area calculations and WKT conversions
- **Coverage**:
  - Area calculations for various shapes (square, rectangle, triangle, pentagon)
  - High latitude area adjustments
  - WKT generation and validation
  - Polygon closure verification
  - Precision handling
  - Edge cases (date line, poles)
- **Test Count**: 12 tests

### 5. Edge Case Tests (EdgeCaseTests.java)
- **Purpose**: Tests edge cases and stress scenarios
- **Coverage**:
  - Concurrent access to AOIManager
  - Large dataset handling (1000+ AOIs)
  - Extreme coordinate values (poles, date line)
  - Complex polygon shapes
  - Malformed JSON handling
  - Error state handling
- **Test Count**: 9 tests

## Running the Tests

### Command Line
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests com.skyfi.atak.plugin.AOIManagerTest

# Run with detailed output
./gradlew test --info

# Run test suite
./gradlew test --tests com.skyfi.atak.plugin.SkyFiTestSuite
```

### Android Studio
1. Right-click on the test package or specific test class
2. Select "Run Tests"
3. View results in the Test Results window

## Test Dependencies
The following test dependencies are configured in build.gradle:
- JUnit 4.13.2 - Core testing framework
- Mockito 5.1.1 - Mocking framework
- Robolectric 4.11.1 - Android unit testing framework
- OkHttp MockWebServer 4.12.0 - HTTP mocking

## Important Notes

### Space Force Demo Requirements
These tests specifically validate:
1. **AOI Management**: Critical for defining areas of interest for satellite tasking
2. **Image Caching**: Ensures offline capability for cached satellite imagery
3. **Coordinate Conversions**: Validates accurate coordinate handling for precise targeting
4. **Area Calculations**: Ensures AOIs meet minimum sensor requirements
5. **Reliability**: Edge case testing ensures robust performance under stress

### Test Assumptions
- Tests use mocked Android components (Context, SharedPreferences)
- File system operations use temporary directories
- Network operations are mocked or stubbed
- GPS/Location services return mock data

### Coverage Goals
- Minimum 80% code coverage for critical components
- 100% coverage for coordinate conversions and area calculations
- All sensor types and requirements tested
- Thread safety validated for shared resources

## Continuous Integration
These tests should be run:
- Before every commit
- As part of PR validation
- Before releases
- During nightly builds

## Troubleshooting

### Common Issues
1. **MockWebServer timeout**: Increase timeout in test configuration
2. **Robolectric errors**: Ensure proper SDK path configuration
3. **Memory issues**: Increase JVM heap size in gradle.properties
4. **File permission errors**: Run with appropriate permissions

### Debug Output
Enable debug logging:
```java
@Before
public void setUp() {
    // Enable debug logging
    System.setProperty("robolectric.logging", "stdout");
}
```

## Future Enhancements
- Integration tests with real ATAK APIs
- Performance benchmarking
- UI testing with Espresso
- End-to-end satellite tasking simulation
- Load testing with multiple concurrent users