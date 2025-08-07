# SkyFi ATAK Plugin v2 - Testing Guide

This guide provides comprehensive information about the testing framework and stability measures implemented for the SkyFi ATAK Plugin v2.

## 🎯 Overview

The testing framework is designed to:
- **Prevent regressions** through comprehensive test coverage
- **Ensure stability** across different environments and use cases
- **Maintain code quality** through automated checks
- **Enable confident deployments** with thorough validation

## 🧪 Test Structure

### Test Categories

#### 1. Unit Tests (`app/src/test/java/`)
- **SkyFiPluginTest**: Core plugin functionality and lifecycle
- **APIClientTest**: API client functionality and configuration
- **APIClientRegressionTest**: API compatibility and regression prevention
- **PluginLifecycleTest**: Plugin initialization and cleanup
- **ErrorHandlingTest**: Error scenarios and edge cases
- **PerformanceTest**: Performance benchmarks and resource usage

#### 2. Integration Tests (`app/src/androidTest/java/`)
- **PluginIntegrationTest**: Real Android environment integration
- Tests plugin behavior with actual Android components
- Validates resource access and system integration

#### 3. Test Suites
- **StabilityTestSuite**: Comprehensive stability validation
- Combines all critical tests for regression prevention

## 🚀 Running Tests

### Local Development

#### Quick Test Run
```bash
# Run all unit tests
./gradlew testCivDebugUnitTest

# Run specific test class
./gradlew testCivDebugUnitTest --tests "com.skyfi.atak.plugin.SkyFiPluginTest"

# Run stability test suite
./gradlew testCivDebugUnitTest --tests "com.skyfi.atak.plugin.StabilityTestSuite"
```

#### Comprehensive Test Runner
```bash
# Run all tests with detailed reporting
./test-runner.sh

# Run specific test categories
./test-runner.sh unit          # Unit tests only
./test-runner.sh regression    # Regression tests only
./test-runner.sh stability     # Stability tests only
./test-runner.sh integration   # Integration tests only
./test-runner.sh build         # Build verification only
```

#### With Code Coverage
```bash
# Generate coverage report
./gradlew testWithCoverage

# View coverage report
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

### Integration Tests
```bash
# Requires Android emulator or device
./gradlew connectedCivDebugAndroidTest
```

## 📊 Code Coverage

### Coverage Requirements
- **Minimum overall coverage**: 60%
- **Minimum class coverage**: 50%
- **Critical components**: 80%+ (SkyFiPlugin, APIClient)

### Coverage Reports
- **HTML Report**: `app/build/reports/jacoco/jacocoTestReport/html/index.html`
- **XML Report**: `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`

### Excluded from Coverage
- Generated code (R.class, BuildConfig)
- Test files
- Android framework code
- Data binding classes

## 🔄 Automated Testing (CI/CD)

### GitHub Actions Workflows

#### 1. Stability Tests (`.github/workflows/stability-tests.yml`)
**Triggers**: Push to main/develop, PRs, nightly schedule

**Jobs**:
- **Unit Tests**: Core functionality validation
- **Integration Tests**: Android environment testing
- **Regression Tests**: Compatibility verification
- **Build Verification**: APK building and lint checks
- **Security Scan**: Dependency vulnerability analysis

#### 2. Pre-commit Checks (`.github/workflows/pre-commit-checks.yml`)
**Triggers**: Pull requests

**Validations**:
- Quick unit tests
- Critical regression tests
- Code compilation
- Lint checks
- Security scans
- Performance validation

### Branch Protection
- **Main branch**: Requires all status checks to pass
- **Develop branch**: Requires core tests and build verification
- **Feature branches**: Must pass pre-commit checks

## 🛡️ Stability Measures

### 1. Regression Prevention
- **API Regression Tests**: Ensure API compatibility
- **Lifecycle Tests**: Validate plugin initialization/cleanup
- **Error Handling Tests**: Verify graceful error handling

### 2. Performance Monitoring
- **Startup Time**: Plugin initialization < 5 seconds
- **Operation Time**: Menu actions < 1 second
- **Memory Usage**: Stable memory consumption
- **APK Size**: Monitor and alert on size increases

### 3. Error Resilience
- **Null Input Handling**: Graceful handling of null/invalid inputs
- **Network Failures**: Proper error handling for API failures
- **Resource Cleanup**: Prevent memory leaks and resource leaks
- **Concurrent Access**: Thread-safe operations

### 4. Quality Gates
- **Code Coverage**: Minimum thresholds enforced
- **Lint Checks**: Zero critical lint issues
- **Security Scans**: No high-severity vulnerabilities
- **Build Verification**: All build variants must succeed

## 🔧 Test Configuration

### Gradle Configuration
```gradle
// Test dependencies
testImplementation 'junit:junit:4.13.2'
testImplementation 'org.mockito:mockito-core:5.11.0'
testImplementation 'org.robolectric:robolectric:4.11.1'
testImplementation 'com.squareup.okhttp3:mockwebserver:3.14.9'

// Integration test dependencies
androidTestImplementation 'androidx.test.ext:junit:1.1.5'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'

// Code coverage
apply plugin: 'jacoco'
android.buildTypes.debug.testCoverageEnabled = true
```

### Test Environment Setup
```bash
# Required environment variables
export ANDROID_HOME=/path/to/android/sdk
export JAVA_HOME=/path/to/java/11

# Optional for enhanced testing
export SKYFI_TEST_API_KEY=test_key_for_integration_tests
```

## 📝 Writing Tests

### Test Naming Convention
```java
// Unit tests
public class ComponentNameTest {
    @Test
    public void testMethodName_condition_expectedResult() {
        // Test implementation
    }
}

// Integration tests
public class ComponentNameIntegrationTest {
    @Test
    public void testIntegrationScenario_condition_expectedResult() {
        // Integration test implementation
    }
}
```

### Test Structure
```java
@Before
public void setUp() {
    // Initialize test dependencies
}

@Test
public void testFeature_normalCondition_success() {
    // Arrange
    // Act
    // Assert
}

@Test
public void testFeature_errorCondition_gracefulHandling() {
    // Test error scenarios
}

@After
public void tearDown() {
    // Cleanup resources
}
```

### Best Practices
1. **Test Independence**: Each test should be independent
2. **Clear Assertions**: Use descriptive assertion messages
3. **Error Testing**: Always test error conditions
4. **Resource Cleanup**: Properly clean up resources in tests
5. **Mock External Dependencies**: Use mocks for external services

## 🚨 Troubleshooting

### Common Issues

#### Test Failures
```bash
# Clean and retry
./gradlew clean testCivDebugUnitTest

# Run with debug info
./gradlew testCivDebugUnitTest --debug --stacktrace
```

#### Coverage Issues
```bash
# Verify coverage configuration
./gradlew jacocoTestReport --info

# Check excluded files
./gradlew jacocoTestCoverageVerification --info
```

#### Integration Test Issues
```bash
# Check emulator status
adb devices

# Clear app data
adb shell pm clear com.skyfi.atak.plugin

# Restart ADB
adb kill-server && adb start-server
```

### Debug Information
- **Test Reports**: `app/build/reports/tests/`
- **Coverage Reports**: `app/build/reports/jacoco/`
- **Lint Reports**: `app/build/reports/lint-results-civDebug.html`
- **Build Logs**: Check GitHub Actions logs for CI failures

## 📈 Metrics and Monitoring

### Key Metrics
- **Test Success Rate**: Target 100% for critical tests
- **Code Coverage**: Maintain >60% overall coverage
- **Build Time**: Monitor and optimize build performance
- **Test Execution Time**: Keep test suite under 10 minutes

### Quality Trends
- Track coverage trends over time
- Monitor test failure patterns
- Analyze performance regression trends
- Review security vulnerability trends

## 🔄 Continuous Improvement

### Regular Reviews
- **Weekly**: Review test failures and flaky tests
- **Monthly**: Analyze coverage trends and gaps
- **Quarterly**: Review and update test strategy
- **Annually**: Major framework updates and improvements

### Enhancement Opportunities
- Add more integration test scenarios
- Implement visual regression testing
- Add performance benchmarking
- Enhance security testing coverage
- Implement mutation testing for test quality

## 📚 Resources

### Documentation
- [Android Testing Guide](https://developer.android.com/training/testing)
- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Robolectric Documentation](http://robolectric.org/)

### Tools
- **JaCoCo**: Code coverage analysis
- **Android Lint**: Static code analysis
- **Dependency Check**: Security vulnerability scanning
- **GitHub Actions**: CI/CD automation

---

For questions or issues with the testing framework, please refer to the project documentation or create an issue in the repository.