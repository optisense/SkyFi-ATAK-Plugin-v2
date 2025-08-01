package com.skyfi.atak.plugin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for all SkyFi ATAK plugin unit tests
 * This suite ensures all critical functionality is tested for the Space Force demo
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    AOIManagerTest.class,
    ImageCacheManagerTest.class,
    CoordinateInputDialogTest.class,
    OrderUtilityTest.class,
    EdgeCaseTests.class
})
public class SkyFiTestSuite {
    // This class remains empty, it is used only as a holder for the above annotations
    
    /**
     * Test Coverage Summary:
     * 
     * 1. AOIManagerTest (11 tests):
     *    - CRUD operations for Areas of Interest
     *    - JSON serialization/deserialization
     *    - Sensor size requirements
     *    - Concurrent access handling
     *    
     * 2. ImageCacheManagerTest (14 tests):
     *    - Image caching and retrieval
     *    - Cache cleanup and size management
     *    - Callback mechanisms
     *    - Error handling
     *    
     * 3. CoordinateInputDialogTest (10 tests):
     *    - Coordinate format parsing (Decimal, MGRS)
     *    - WKT polygon generation
     *    - Coordinate validation
     *    - High latitude calculations
     *    
     * 4. OrderUtilityTest (12 tests):
     *    - Polygon area calculations
     *    - WKT conversions
     *    - Various polygon shapes (square, rectangle, triangle)
     *    - Edge cases and precision
     *    
     * 5. EdgeCaseTests (9 tests):
     *    - Concurrent access scenarios
     *    - Large data sets
     *    - Extreme coordinate values
     *    - Complex polygons
     *    - Error conditions
     *    
     * Total: 56+ comprehensive unit tests covering all critical functionality
     */
}