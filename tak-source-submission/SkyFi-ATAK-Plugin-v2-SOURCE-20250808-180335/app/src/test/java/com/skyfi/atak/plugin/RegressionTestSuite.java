package com.skyfi.atak.plugin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Comprehensive regression test suite to prevent breaking changes
 * Run this suite before merging any new features or bug fixes
 */
@RunWith(Suite.class)
@SuiteClasses({
    // Core functionality tests
    APIClientTest.class,
    APIClientRegressionTest.class,
    
    // UI Component tests
    CoordinateInputDialogTest.class,
    TaskingOrderTest.class,
    
    // Data management tests
    ImageCacheManagerTest.class,
    
    // Map functionality tests
    MapOverlayTest.class,
    
    // Stability and performance tests
    StabilityTestSuite.class,
    PerformanceTest.class,
    ErrorHandlingTest.class,
    
    // Plugin lifecycle tests
    PluginLifecycleTest.class,
    SkyFiPluginTest.class
})
public class RegressionTestSuite {
    // This class remains empty, it is used only as a holder for the above annotations
}