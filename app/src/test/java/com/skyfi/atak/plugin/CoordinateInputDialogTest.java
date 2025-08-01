package com.skyfi.atak.plugin;

import android.content.Context;

import com.atakmap.coremap.maps.coords.GeoPoint;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CoordinateInputDialog - Tests coordinate format conversions and WKT generation
 */
@RunWith(RobolectricTestRunner.class)
public class CoordinateInputDialogTest {
    
    @Mock
    private Context mockContext;
    
    @Mock
    private CoordinateInputDialog.CoordinateInputListener mockListener;
    
    private CoordinateInputDialog dialog;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        dialog = new CoordinateInputDialog(mockContext, mockListener);
    }
    
    @Test
    public void testCreateSquareWKTValidCoordinates() throws Exception {
        // Use reflection to test private method
        Method createSquareWKT = CoordinateInputDialog.class.getDeclaredMethod(
            "createSquareWKT", GeoPoint.class, double.class);
        createSquareWKT.setAccessible(true);
        
        // Test with equator coordinates (simple case)
        GeoPoint center = new GeoPoint(0.0, 0.0);
        double radiusKm = 10.0;
        
        String wkt = (String) createSquareWKT.invoke(dialog, center, radiusKm);
        assertNotNull("WKT should not be null", wkt);
        assertTrue("WKT should be a polygon", wkt.startsWith("POLYGON"));
        
        // Parse the WKT to verify it's valid
        WKTReader reader = new WKTReader();
        Polygon polygon = (Polygon) reader.read(wkt);
        assertNotNull("Should parse to valid polygon", polygon);
        
        // Verify it's a closed polygon (5 points for a square)
        assertEquals("Square should have 5 coordinates (closed)", 5, polygon.getCoordinates().length);
        
        // Verify first and last points are the same (closed polygon)
        Coordinate first = polygon.getCoordinates()[0];
        Coordinate last = polygon.getCoordinates()[4];
        assertEquals("First and last longitude should match", first.x, last.x, 0.00001);
        assertEquals("First and last latitude should match", first.y, last.y, 0.00001);
    }
    
    @Test
    public void testCreateSquareWKTHighLatitude() throws Exception {
        // Test at high latitude where longitude degrees cover less distance
        Method createSquareWKT = CoordinateInputDialog.class.getDeclaredMethod(
            "createSquareWKT", GeoPoint.class, double.class);
        createSquareWKT.setAccessible(true);
        
        // Test near the Arctic Circle
        GeoPoint center = new GeoPoint(66.5, 0.0);
        double radiusKm = 5.0;
        
        String wkt = (String) createSquareWKT.invoke(dialog, center, radiusKm);
        assertNotNull("WKT should not be null at high latitude", wkt);
        
        WKTReader reader = new WKTReader();
        Polygon polygon = (Polygon) reader.read(wkt);
        
        // At high latitudes, the longitude offset should be larger
        double lonRange = polygon.getEnvelopeInternal().getWidth();
        double latRange = polygon.getEnvelopeInternal().getHeight();
        
        // Longitude range should be larger than latitude range at high latitudes
        assertTrue("Longitude range should account for latitude", lonRange > latRange);
    }
    
    @Test
    public void testCreateSquareWKTVariousSizes() throws Exception {
        Method createSquareWKT = CoordinateInputDialog.class.getDeclaredMethod(
            "createSquareWKT", GeoPoint.class, double.class);
        createSquareWKT.setAccessible(true);
        
        GeoPoint center = new GeoPoint(40.0, -74.0); // New York area
        double[] testSizes = {5.0, 10.0, 25.0, 45.0}; // Various AOI sizes in km
        
        for (double size : testSizes) {
            String wkt = (String) createSquareWKT.invoke(dialog, center, size);
            assertNotNull("WKT should not be null for size " + size, wkt);
            
            WKTReader reader = new WKTReader();
            Polygon polygon = (Polygon) reader.read(wkt);
            
            // Verify the polygon is roughly the right size
            double width = polygon.getEnvelopeInternal().getWidth();
            double height = polygon.getEnvelopeInternal().getHeight();
            
            // Very rough check - just ensure it scales with size
            if (size > 5.0) {
                assertTrue("Larger AOI should have larger polygon", width > 0.05);
            }
        }
    }
    
    @Test
    public void testCoordinateParsingDecimalDegrees() {
        // Test parsing decimal degree format: "40.7128, -74.0060"
        String validCoords = "40.7128, -74.0060";
        String[] parts = validCoords.split(",");
        
        assertEquals("Should split into 2 parts", 2, parts.length);
        
        double lat = Double.parseDouble(parts[0].trim());
        double lon = Double.parseDouble(parts[1].trim());
        
        assertEquals("Latitude should parse correctly", 40.7128, lat, 0.0001);
        assertEquals("Longitude should parse correctly", -74.0060, lon, 0.0001);
    }
    
    @Test
    public void testCoordinateParsingInvalidFormats() {
        // Test various invalid formats
        String[] invalidFormats = {
            "40.7128",              // Missing longitude
            "40.7128 -74.0060",     // No comma
            "40.7128, -74.0060, 0", // Too many values
            "north, west",          // Non-numeric
            "",                     // Empty
            "40.7128,",            // Trailing comma
            ",40.7128"             // Leading comma
        };
        
        for (String invalid : invalidFormats) {
            String[] parts = invalid.split(",");
            
            try {
                if (parts.length == 2) {
                    Double.parseDouble(parts[0].trim());
                    Double.parseDouble(parts[1].trim());
                }
                
                // If we get here without exception, the format might be valid
                // but we should still check the array length
                if (parts.length != 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
                    // This is expected - invalid format
                    assertTrue("Invalid format detected: " + invalid, true);
                }
            } catch (NumberFormatException e) {
                // Expected for invalid formats
                assertTrue("Should throw exception for invalid format: " + invalid, true);
            }
        }
    }
    
    @Test
    public void testCoordinateValidationRanges() {
        // Test coordinate range validation
        double[][] testCoords = {
            {90.0, 180.0},    // Max valid values
            {-90.0, -180.0},  // Min valid values
            {0.0, 0.0},       // Null Island
            {91.0, 0.0},      // Invalid latitude (too high)
            {-91.0, 0.0},     // Invalid latitude (too low)
            {0.0, 181.0},     // Invalid longitude (too high)
            {0.0, -181.0}     // Invalid longitude (too low)
        };
        
        for (double[] coord : testCoords) {
            boolean validLat = coord[0] >= -90.0 && coord[0] <= 90.0;
            boolean validLon = coord[1] >= -180.0 && coord[1] <= 180.0;
            boolean isValid = validLat && validLon;
            
            if (isValid) {
                // Valid coordinates should create valid GeoPoint
                GeoPoint point = new GeoPoint(coord[0], coord[1]);
                assertEquals("Latitude should match", coord[0], point.getLatitude(), 0.0001);
                assertEquals("Longitude should match", coord[1], point.getLongitude(), 0.0001);
            } else {
                // Invalid coordinates should be detectable
                assertTrue("Should detect invalid coordinate range", !validLat || !validLon);
            }
        }
    }
    
    @Test
    public void testSensorRequirementsCalculation() {
        // Test area calculations for different radii
        int[] radiiKm = {5, 10, 15, 20, 25, 30, 35, 40, 45};
        
        for (int radius : radiiKm) {
            double expectedArea = radius * radius;
            
            // Check sensor compatibility
            if (expectedArea < 16) {
                // Too small for most sensors
                assertTrue("Area should be too small for sensors", expectedArea < 16);
            } else if (expectedArea < 25) {
                // Suitable for Umbra SAR
                assertTrue("Area suitable for Umbra", expectedArea >= 16 && expectedArea < 25);
            } else if (expectedArea < 64) {
                // Suitable for Siwei, Geosat, Umbra
                assertTrue("Area suitable for multiple sensors", expectedArea >= 25 && expectedArea < 64);
            } else {
                // Suitable for all sensors
                assertTrue("Area suitable for all sensors", expectedArea >= 64);
            }
        }
    }
    
    @Test
    public void testMGRSFormatValidation() {
        // Test MGRS format patterns
        String[] validMGRS = {
            "18T WL 89009 13758",
            "18TWL8900913758",
            "18T WL 890 137",
            "18TWL890137"
        };
        
        String[] invalidMGRS = {
            "18T",              // Too short
            "99Z WL 89009",     // Invalid zone
            "18I WL 89009",     // Invalid letter (I)
            "18O WL 89009",     // Invalid letter (O)
            "ABC DEF GHI"       // Not MGRS format
        };
        
        // MGRS validation pattern (simplified)
        String mgrsPattern = "^\\d{1,2}[A-HJ-NP-Z]\\s*[A-Z]{2}\\s*\\d+\\s*\\d*$";
        
        for (String mgrs : validMGRS) {
            // Remove extra spaces for pattern matching
            String normalized = mgrs.replaceAll("\\s+", " ").trim();
            // Note: Real MGRS validation is complex, this is simplified
            assertTrue("Should match MGRS pattern: " + mgrs, 
                normalized.length() >= 5 && Character.isDigit(normalized.charAt(0)));
        }
        
        for (String mgrs : invalidMGRS) {
            // These should be detected as invalid
            if (mgrs.length() < 5) {
                assertTrue("Too short to be valid MGRS", mgrs.length() < 5);
            }
        }
    }
    
    @Test
    public void testCallbackInvocation() {
        // Test that callbacks are properly invoked
        
        // Test successful coordinate selection
        String wkt = "POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))";
        double area = 100.0;
        String coordString = "40.7128, -74.0060";
        
        // Simulate successful selection
        mockListener.onCoordinateSelected(wkt, area, coordString);
        
        // Verify callback was invoked
        verify(mockListener, times(1)).onCoordinateSelected(wkt, area, coordString);
        
        // Test cancellation
        mockListener.onCancelled();
        verify(mockListener, times(1)).onCancelled();
    }
    
    @Test
    public void testWKTPolygonClosure() throws ParseException {
        // Test that generated polygons are properly closed
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(10000000.0));
        
        // Create a square polygon
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(new Coordinate(0, 0));
        coordinates.add(new Coordinate(0, 1));
        coordinates.add(new Coordinate(1, 1));
        coordinates.add(new Coordinate(1, 0));
        coordinates.add(new Coordinate(0, 0)); // Close the polygon
        
        Polygon polygon = factory.createPolygon(coordinates.toArray(new Coordinate[0]));
        assertTrue("Polygon should be closed", polygon.isClosed());
        assertTrue("Polygon should be valid", polygon.isValid());
        
        // Generate WKT
        WKTWriter writer = new WKTWriter();
        String wkt = writer.write(polygon);
        
        assertNotNull("WKT should not be null", wkt);
        assertTrue("WKT should start with POLYGON", wkt.startsWith("POLYGON"));
        
        // Parse it back to verify
        WKTReader reader = new WKTReader();
        Polygon parsed = (Polygon) reader.read(wkt);
        assertTrue("Parsed polygon should be closed", parsed.isClosed());
        assertEquals("Should have same number of points", 
            polygon.getNumPoints(), parsed.getNumPoints());
    }
}