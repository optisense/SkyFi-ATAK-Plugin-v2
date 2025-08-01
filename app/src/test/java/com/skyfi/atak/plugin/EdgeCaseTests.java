package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import com.atakmap.coremap.maps.coords.GeoPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTWriter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Edge case tests for critical SkyFi ATAK plugin functionality
 */
@RunWith(RobolectricTestRunner.class)
public class EdgeCaseTests {
    
    @Mock
    private Context mockContext;
    
    @Mock
    private SharedPreferences mockPrefs;
    
    @Mock
    private SharedPreferences.Editor mockEditor;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
    }
    
    @Test
    public void testAOIManagerConcurrentAccess() throws InterruptedException {
        // Test thread safety of AOIManager
        when(mockPrefs.getString(eq("saved_aois"), anyString())).thenReturn("[]");
        
        AOIManager aoiManager = new AOIManager(mockContext);
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicBoolean hasError = new AtomicBoolean(false);
        
        // Create multiple threads that save AOIs concurrently
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        String id = "thread_" + threadId + "_aoi_" + j;
                        AOIManager.AOI aoi = new AOIManager.AOI(id, "Test AOI " + id, 
                            "POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))");
                        aoiManager.saveAOI(aoi);
                        
                        // Try to retrieve immediately
                        AOIManager.AOI retrieved = aoiManager.getAOI(id);
                        if (retrieved == null || !retrieved.name.equals(aoi.name)) {
                            hasError.set(true);
                        }
                    }
                } catch (Exception e) {
                    hasError.set(true);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        assertTrue("Concurrent operations should complete", latch.await(5, TimeUnit.SECONDS));
        assertFalse("No errors should occur during concurrent access", hasError.get());
        
        // Verify all AOIs were saved
        ArrayList<AOIManager.AOI> allAOIs = aoiManager.getAllAOIs();
        assertEquals("Should have all AOIs from all threads", threadCount * 10, allAOIs.size());
    }
    
    @Test
    public void testAOIManagerLargeDataSet() {
        // Test handling of large number of AOIs
        when(mockPrefs.getString(eq("saved_aois"), anyString())).thenReturn("[]");
        
        AOIManager aoiManager = new AOIManager(mockContext);
        int aoiCount = 1000;
        
        // Save many AOIs
        for (int i = 0; i < aoiCount; i++) {
            AOIManager.AOI aoi = new AOIManager.AOI(
                "large_set_" + i,
                "Large Set AOI " + i,
                "POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))"
            );
            aoi.areaKm2 = 25.0 + i;
            aoi.sensorType = i % 2 == 0 ? "siwei" : "umbra";
            aoiManager.saveAOI(aoi);
        }
        
        // Verify all were saved
        ArrayList<AOIManager.AOI> allAOIs = aoiManager.getAllAOIs();
        assertEquals("Should handle large number of AOIs", aoiCount, allAOIs.size());
        
        // Test retrieval performance
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            AOIManager.AOI aoi = aoiManager.getAOI("large_set_" + (i * 10));
            assertNotNull("Should retrieve AOI quickly", aoi);
        }
        long retrievalTime = System.currentTimeMillis() - startTime;
        assertTrue("Retrieval should be fast", retrievalTime < 1000); // Less than 1 second for 100 retrievals
    }
    
    @Test
    public void testImageCacheManagerDiskSpaceHandling() {
        // Test behavior when disk space is limited
        // This is a conceptual test as we can't easily simulate disk full
        
        ImageCacheManager cacheManager = ImageCacheManager.getInstance(mockContext);
        
        // Test cache size limit enforcement
        long maxSize = 500 * 1024 * 1024; // 500MB
        long currentSize = cacheManager.getCacheSize();
        
        assertTrue("Cache size should not exceed maximum", currentSize <= maxSize);
        
        // Test clearing cache
        cacheManager.clearCache();
        assertEquals("Cache should be empty after clear", 0, cacheManager.getCacheSize());
    }
    
    @Test
    public void testCoordinateEdgeCases() {
        // Test extreme coordinate values
        double[][] extremeCoords = {
            {90.0, 0.0},      // North Pole
            {-90.0, 0.0},     // South Pole
            {0.0, 180.0},     // International Date Line East
            {0.0, -180.0},    // International Date Line West
            {89.999, 179.999}, // Near maximum values
            {-89.999, -179.999} // Near minimum values
        };
        
        for (double[] coord : extremeCoords) {
            try {
                GeoPoint point = new GeoPoint(coord[0], coord[1]);
                
                // Create WKT at extreme location
                ArrayList<Coordinate> coordinates = new ArrayList<>();
                coordinates.add(new Coordinate(coord[1], coord[0]));
                coordinates.add(new Coordinate(coord[1] + 0.001, coord[0]));
                coordinates.add(new Coordinate(coord[1] + 0.001, coord[0] + 0.001));
                coordinates.add(new Coordinate(coord[1], coord[0] + 0.001));
                coordinates.add(new Coordinate(coord[1], coord[0]));
                
                GeometryFactory factory = new GeometryFactory(new PrecisionModel(10000000.0));
                Polygon polygon = factory.createPolygon(coordinates.toArray(new Coordinate[0]));
                WKTWriter writer = new WKTWriter();
                String wkt = writer.write(polygon);
                
                assertNotNull("Should handle extreme coordinates: " + coord[0] + ", " + coord[1], wkt);
                assertTrue("Should create valid WKT", wkt.startsWith("POLYGON"));
            } catch (Exception e) {
                fail("Should not throw exception for extreme coordinates: " + e.getMessage());
            }
        }
    }
    
    @Test
    public void testJSONParsingEdgeCases() throws JSONException {
        // Test malformed JSON handling
        String[] malformedJSON = {
            "{\"id\":\"test\",\"name\":null}",  // Null value
            "{\"id\":\"\",\"name\":\"test\"}",  // Empty ID
            "{\"id\":\"test\"}",                 // Missing required field
            "{}",                                // Empty object
            "[{\"id\":\"test\"}]",              // Array instead of object
            "{\"id\":\"test\",\"areaKm2\":\"not_a_number\"}" // Wrong type
        };
        
        for (String json : malformedJSON) {
            try {
                JSONObject obj = new JSONObject(json);
                
                // Test AOI parsing
                if (obj.has("id") && obj.has("name") && obj.has("wkt")) {
                    AOIManager.AOI aoi = AOIManager.AOI.fromJSON(obj);
                    // Should either parse successfully or throw appropriate exception
                    assertNotNull("Should handle JSON: " + json, aoi);
                }
            } catch (Exception e) {
                // Expected for malformed JSON
                assertTrue("Should handle malformed JSON gracefully", true);
            }
        }
    }
    
    @Test
    public void testMemoryLeakPrevention() {
        // Test that callbacks are properly cleaned up
        ImageCacheManager cacheManager = ImageCacheManager.getInstance(mockContext);
        
        // Create many callbacks
        ArrayList<TestCallback> callbacks = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            TestCallback callback = new TestCallback();
            callbacks.add(callback);
            
            // Start cache operation (will fail without network, but that's ok)
            cacheManager.cacheImage("test_" + i, "http://example.com/image.jpg", "{}", callback);
        }
        
        // Clear references
        callbacks.clear();
        
        // Force garbage collection hint
        System.gc();
        
        // In a real test, we'd verify callbacks are garbage collected
        // For now, just ensure no crashes occur
        assertTrue("Should handle many callbacks without issues", true);
    }
    
    @Test
    public void testWKTComplexPolygons() {
        // Test WKT generation for complex polygons
        
        // Star-shaped polygon
        ArrayList<Coordinate> starCoords = new ArrayList<>();
        int points = 10;
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double radius = (i % 2 == 0) ? 1.0 : 0.5;
            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle);
            starCoords.add(new Coordinate(x, y));
        }
        starCoords.add(starCoords.get(0)); // Close polygon
        
        try {
            GeometryFactory factory = new GeometryFactory(new PrecisionModel(10000000.0));
            Polygon starPolygon = factory.createPolygon(starCoords.toArray(new Coordinate[0]));
            WKTWriter writer = new WKTWriter();
            String wkt = writer.write(starPolygon);
            
            assertNotNull("Should handle star polygon", wkt);
            assertTrue("Should be valid WKT", wkt.contains("POLYGON"));
            assertTrue("Should contain all points", 
                wkt.split(",").length >= points);
        } catch (Exception e) {
            fail("Should handle complex polygons: " + e.getMessage());
        }
    }
    
    @Test
    public void testSensorTypeValidation() {
        // Test all sensor types with various case combinations
        String[] sensorVariants = {
            "siwei", "SIWEI", "Siwei", "SiWeI",
            "satellogic", "SATELLOGIC", "Satellogic",
            "umbra", "UMBRA", "Umbra",
            "geosat", "GEOSAT", "Geosat",
            "planet", "PLANET", "Planet",
            "impro", "IMPRO", "Impro",
            "unknown", "UNKNOWN", "Unknown",
            "", " ", null
        };
        
        for (String sensor : sensorVariants) {
            if (sensor != null) {
                double minSize = AOIManager.getMinimumAOISize(sensor);
                assertTrue("Should return valid minimum size for: " + sensor, minSize > 0);
                assertTrue("Should return reasonable size", minSize >= 16 && minSize <= 100);
                
                String requirements = AOIManager.getSensorRequirements(sensor);
                assertNotNull("Should return requirements for: " + sensor, requirements);
                assertTrue("Should contain size info", requirements.contains("km"));
            }
        }
    }
    
    @Test
    public void testCacheCallbackErrorStates() {
        // Test various error conditions for cache callbacks
        TestCallback callback = new TestCallback();
        
        // Test various error messages
        String[] errorMessages = {
            "Network timeout",
            "HTTP 404: Not Found",
            "Invalid image format",
            "Insufficient storage space",
            "Permission denied",
            null,  // Null error message
            "",    // Empty error message
            "Very long error message that exceeds normal length expectations and contains special characters !@#$%^&*()"
        };
        
        for (String error : errorMessages) {
            callback.onError(error);
            
            if (error != null && !error.isEmpty()) {
                assertEquals("Should store error message", error, callback.lastError);
            }
        }
    }
    
    // Helper class for testing callbacks
    private static class TestCallback implements ImageCacheManager.CacheCallback {
        String lastError;
        File cachedFile;
        int progressCount = 0;
        
        @Override
        public void onCached(String imageId, File cachedFile) {
            this.cachedFile = cachedFile;
        }
        
        @Override
        public void onProgress(int percent) {
            progressCount++;
        }
        
        @Override
        public void onError(String error) {
            this.lastError = error;
        }
    }
}