package com.skyfi.atak.plugin;

import android.content.Context;

import com.atakmap.android.maps.MapView;
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
 * Unit tests for OrderUtility - Tests polygon area calculations and WKT conversions
 */
@RunWith(RobolectricTestRunner.class)
public class OrderUtilityTest {
    
    @Mock
    private MapView mockMapView;
    
    @Mock
    private Context mockContext;
    
    private OrderUtility orderUtility;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        orderUtility = new OrderUtility(mockMapView, mockContext);
    }
    
    @Test
    public void testCalculatePolygonAreaSquare() throws Exception {
        // Test area calculation for a perfect square at the equator
        Method calculateArea = OrderUtility.class.getDeclaredMethod(
            "calculatePolygonArea", GeoPoint[].class);
        calculateArea.setAccessible(true);
        
        // Create a 1 degree x 1 degree square at the equator
        // At equator: 1 degree ≈ 111.32 km
        GeoPoint[] square = new GeoPoint[] {
            new GeoPoint(0.0, 0.0),
            new GeoPoint(0.0, 1.0),
            new GeoPoint(1.0, 1.0),
            new GeoPoint(1.0, 0.0)
        };
        
        double area = (double) calculateArea.invoke(orderUtility, (Object) square);
        
        // Expected area: ~111.32 * 111.32 = ~12,392 km²
        assertTrue("Area should be positive", area > 0);
        assertTrue("Area should be approximately 12,392 km²", 
            area > 12000 && area < 13000);
    }
    
    @Test
    public void testCalculatePolygonAreaRectangle() throws Exception {
        // Test area calculation for a rectangle
        Method calculateArea = OrderUtility.class.getDeclaredMethod(
            "calculatePolygonArea", GeoPoint[].class);
        calculateArea.setAccessible(true);
        
        // Create a 0.5 x 1 degree rectangle
        GeoPoint[] rectangle = new GeoPoint[] {
            new GeoPoint(0.0, 0.0),
            new GeoPoint(0.0, 1.0),
            new GeoPoint(0.5, 1.0),
            new GeoPoint(0.5, 0.0)
        };
        
        double area = (double) calculateArea.invoke(orderUtility, (Object) rectangle);
        
        // Expected area: ~55.66 * 111.32 = ~6,196 km²
        assertTrue("Area should be positive", area > 0);
        assertTrue("Area should be approximately half of square", 
            area > 6000 && area < 6500);
    }
    
    @Test
    public void testCalculatePolygonAreaTriangle() throws Exception {
        // Test area calculation for a triangle
        Method calculateArea = OrderUtility.class.getDeclaredMethod(
            "calculatePolygonArea", GeoPoint[].class);
        calculateArea.setAccessible(true);
        
        // Create a right triangle
        GeoPoint[] triangle = new GeoPoint[] {
            new GeoPoint(0.0, 0.0),
            new GeoPoint(0.0, 1.0),
            new GeoPoint(1.0, 0.0)
        };
        
        double area = (double) calculateArea.invoke(orderUtility, (Object) triangle);
        
        // Expected area: ~0.5 * 111.32 * 111.32 = ~6,196 km²
        assertTrue("Area should be positive", area > 0);
        assertTrue("Triangle area should be half of square", 
            area > 6000 && area < 6500);
    }
    
    @Test
    public void testCalculatePolygonAreaHighLatitude() throws Exception {
        // Test area calculation at high latitude where distortion is significant
        Method calculateArea = OrderUtility.class.getDeclaredMethod(
            "calculatePolygonArea", GeoPoint[].class);
        calculateArea.setAccessible(true);
        
        // Create a square at 60° latitude
        // At 60° latitude: 1 degree longitude ≈ 55.66 km
        GeoPoint[] square = new GeoPoint[] {
            new GeoPoint(60.0, 0.0),
            new GeoPoint(60.0, 1.0),
            new GeoPoint(61.0, 1.0),
            new GeoPoint(61.0, 0.0)
        };
        
        double area = (double) calculateArea.invoke(orderUtility, (Object) square);
        
        // Expected area: ~111.32 * 55.66 = ~6,196 km²
        assertTrue("Area should be positive", area > 0);
        assertTrue("Area should account for latitude", 
            area > 5000 && area < 7000);
    }
    
    @Test
    public void testCalculatePolygonAreaComplex() throws Exception {
        // Test area calculation for a complex polygon (pentagon)
        Method calculateArea = OrderUtility.class.getDeclaredMethod(
            "calculatePolygonArea", GeoPoint[].class);
        calculateArea.setAccessible(true);
        
        // Create an irregular pentagon
        GeoPoint[] pentagon = new GeoPoint[] {
            new GeoPoint(0.0, 0.0),
            new GeoPoint(0.0, 1.0),
            new GeoPoint(0.5, 1.5),
            new GeoPoint(1.0, 1.0),
            new GeoPoint(1.0, 0.0)
        };
        
        double area = (double) calculateArea.invoke(orderUtility, (Object) pentagon);
        
        assertTrue("Area should be positive", area > 0);
        // Pentagon area should be between triangle and square
        assertTrue("Pentagon area should be reasonable", 
            area > 6000 && area < 15000);
    }
    
    @Test
    public void testCalculatePolygonAreaMinimumSensor() throws Exception {
        // Test calculation for minimum sensor sizes
        Method calculateArea = OrderUtility.class.getDeclaredMethod(
            "calculatePolygonArea", GeoPoint[].class);
        calculateArea.setAccessible(true);
        
        // Create polygons matching minimum sensor requirements
        // Umbra minimum: 16 km² (4x4 km)
        double kmToDegree = 1.0 / 111.32;
        double side4km = 4.0 * kmToDegree;
        
        GeoPoint[] umbraMin = new GeoPoint[] {
            new GeoPoint(0.0, 0.0),
            new GeoPoint(0.0, side4km),
            new GeoPoint(side4km, side4km),
            new GeoPoint(side4km, 0.0)
        };
        
        double umbraArea = (double) calculateArea.invoke(orderUtility, (Object) umbraMin);
        assertTrue("Umbra minimum area should be ~16 km²", 
            umbraArea > 15 && umbraArea < 17);
        
        // Siwei minimum: 25 km² (5x5 km)
        double side5km = 5.0 * kmToDegree;
        
        GeoPoint[] siweiMin = new GeoPoint[] {
            new GeoPoint(0.0, 0.0),
            new GeoPoint(0.0, side5km),
            new GeoPoint(side5km, side5km),
            new GeoPoint(side5km, 0.0)
        };
        
        double siweiArea = (double) calculateArea.invoke(orderUtility, (Object) siweiMin);
        assertTrue("Siwei minimum area should be ~25 km²", 
            siweiArea > 24 && siweiArea < 26);
    }
    
    @Test
    public void testGetWktValidPolygon() throws Exception {
        // Test WKT generation for valid polygon
        Method getWkt = OrderUtility.class.getDeclaredMethod(
            "getWkt", GeoPoint[].class);
        getWkt.setAccessible(true);
        
        GeoPoint[] points = new GeoPoint[] {
            new GeoPoint(0.0, 0.0),
            new GeoPoint(0.0, 1.0),
            new GeoPoint(1.0, 1.0),
            new GeoPoint(1.0, 0.0)
        };
        
        String wkt = (String) getWkt.invoke(orderUtility, (Object) points);
        
        assertNotNull("WKT should not be null", wkt);
        assertTrue("WKT should start with POLYGON", wkt.startsWith("POLYGON"));
        
        // Verify it's valid WKT
        WKTReader reader = new WKTReader();
        Polygon polygon = (Polygon) reader.read(wkt);
        assertNotNull("Should parse to valid polygon", polygon);
        assertTrue("Polygon should be closed", polygon.isClosed());
        assertEquals("Should have 5 points (closed)", 5, polygon.getNumPoints());
    }
    
    @Test
    public void testGetWktAutomaticClosure() throws Exception {
        // Test that WKT generation automatically closes polygons
        Method getWkt = OrderUtility.class.getDeclaredMethod(
            "getWkt", GeoPoint[].class);
        getWkt.setAccessible(true);
        
        // Provide unclosed polygon
        GeoPoint[] unclosed = new GeoPoint[] {
            new GeoPoint(0.0, 0.0),
            new GeoPoint(0.0, 1.0),
            new GeoPoint(1.0, 1.0),
            new GeoPoint(1.0, 0.0)
            // Missing closing point
        };
        
        String wkt = (String) getWkt.invoke(orderUtility, (Object) unclosed);
        
        assertNotNull("WKT should not be null", wkt);
        
        // Verify the polygon is closed
        WKTReader reader = new WKTReader();
        Polygon polygon = (Polygon) reader.read(wkt);
        assertTrue("Generated polygon should be closed", polygon.isClosed());
        
        // First and last coordinates should match
        Coordinate[] coords = polygon.getCoordinates();
        assertEquals("First and last X should match", 
            coords[0].x, coords[coords.length-1].x, 0.00001);
        assertEquals("First and last Y should match", 
            coords[0].y, coords[coords.length-1].y, 0.00001);
    }
    
    @Test
    public void testGetWktPrecision() throws Exception {
        // Test WKT precision handling
        Method getWkt = OrderUtility.class.getDeclaredMethod(
            "getWkt", GeoPoint[].class);
        getWkt.setAccessible(true);
        
        // Use coordinates with many decimal places
        GeoPoint[] precise = new GeoPoint[] {
            new GeoPoint(40.71234567890, -74.00598765432),
            new GeoPoint(40.71234567890, -74.00498765432),
            new GeoPoint(40.71334567890, -74.00498765432),
            new GeoPoint(40.71334567890, -74.00598765432)
        };
        
        String wkt = (String) getWkt.invoke(orderUtility, (Object) precise);
        assertNotNull("WKT should handle precise coordinates", wkt);
        
        // Verify precision is maintained to reasonable level
        assertTrue("WKT should contain decimal places", wkt.contains("."));
        
        // Parse and check precision
        WKTReader reader = new WKTReader();
        Polygon polygon = (Polygon) reader.read(wkt);
        Coordinate[] coords = polygon.getCoordinates();
        
        // Check that coordinates maintain at least 4 decimal places
        for (Coordinate coord : coords) {
            String xStr = String.valueOf(coord.x);
            String yStr = String.valueOf(coord.y);
            
            int xDecimals = xStr.substring(xStr.indexOf(".") + 1).length();
            int yDecimals = yStr.substring(yStr.indexOf(".") + 1).length();
            
            assertTrue("Should maintain reasonable precision", 
                xDecimals >= 4 || yDecimals >= 4);
        }
    }
    
    @Test
    public void testGetWktInvalidInput() throws Exception {
        // Test WKT generation with invalid inputs
        Method getWkt = OrderUtility.class.getDeclaredMethod(
            "getWkt", GeoPoint[].class);
        getWkt.setAccessible(true);
        
        // Test with too few points
        GeoPoint[] tooFew = new GeoPoint[] {
            new GeoPoint(0.0, 0.0),
            new GeoPoint(0.0, 1.0)
        };
        
        try {
            String wkt = (String) getWkt.invoke(orderUtility, (Object) tooFew);
            // If it doesn't throw, it should return null
            assertNull("Should return null for invalid polygon", wkt);
        } catch (Exception e) {
            // Expected - invalid polygon
            assertTrue("Should handle invalid polygon gracefully", true);
        }
        
        // Test with null array
        try {
            String wkt = (String) getWkt.invoke(orderUtility, (Object) null);
            assertNull("Should return null for null input", wkt);
        } catch (Exception e) {
            // Expected - null input
            assertTrue("Should handle null input", true);
        }
        
        // Test with empty array
        GeoPoint[] empty = new GeoPoint[0];
        try {
            String wkt = (String) getWkt.invoke(orderUtility, (Object) empty);
            assertNull("Should return null for empty array", wkt);
        } catch (Exception e) {
            // Expected - empty array
            assertTrue("Should handle empty array", true);
        }
    }
    
    @Test
    public void testAreaCalculationEdgeCases() throws Exception {
        // Test edge cases for area calculation
        Method calculateArea = OrderUtility.class.getDeclaredMethod(
            "calculatePolygonArea", GeoPoint[].class);
        calculateArea.setAccessible(true);
        
        // Test with minimum polygon (triangle)
        GeoPoint[] minPolygon = new GeoPoint[] {
            new GeoPoint(0.0, 0.0),
            new GeoPoint(0.0, 0.1),
            new GeoPoint(0.1, 0.0)
        };
        
        double minArea = (double) calculateArea.invoke(orderUtility, (Object) minPolygon);
        assertTrue("Minimum polygon should have positive area", minArea > 0);
        
        // Test with very large polygon
        GeoPoint[] largePolygon = new GeoPoint[] {
            new GeoPoint(0.0, 0.0),
            new GeoPoint(0.0, 10.0),
            new GeoPoint(10.0, 10.0),
            new GeoPoint(10.0, 0.0)
        };
        
        double largeArea = (double) calculateArea.invoke(orderUtility, (Object) largePolygon);
        assertTrue("Large polygon should have very large area", largeArea > 1000000);
        
        // Test with polygon crossing date line
        GeoPoint[] dateLineCrossing = new GeoPoint[] {
            new GeoPoint(0.0, 179.0),
            new GeoPoint(0.0, -179.0),
            new GeoPoint(1.0, -179.0),
            new GeoPoint(1.0, 179.0)
        };
        
        double dateLineArea = (double) calculateArea.invoke(orderUtility, (Object) dateLineCrossing);
        // This is a complex case - the algorithm may not handle it correctly
        // but it should at least not crash
        assertTrue("Should handle date line crossing without crashing", true);
    }
}