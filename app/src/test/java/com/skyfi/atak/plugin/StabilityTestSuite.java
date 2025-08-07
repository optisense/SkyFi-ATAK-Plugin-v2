package com.skyfi.atak.plugin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Comprehensive test suite for stability and regression testing
 * This suite runs all critical tests to ensure plugin stability
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SkyFiPluginTest.class,
    APIClientTest.class,
    APIClientRegressionTest.class,
    PluginLifecycleTest.class,
    ErrorHandlingTest.class,
    PerformanceTest.class
})
public class StabilityTestSuite {
    // This class remains empty, it is used only as a holder for the above annotations
}