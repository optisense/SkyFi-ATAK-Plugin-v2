package com.skyfi.atak.plugin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.skyfi.atak.plugin.skyfiapi.OrderRequest;
import com.skyfi.atak.plugin.skyfiapi.OrderResponse;
import com.skyfi.atak.plugin.skyfiapi.PricingQuery;
import com.skyfi.atak.plugin.skyfiapi.PricingResponse;
import com.skyfi.atak.plugin.skyfiapi.TaskingOrder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class TaskingOrderTest {
    
    @Mock
    private APIClient mockApiClient;
    
    private TaskingOrderFragment taskingOrderFragment;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        taskingOrderFragment = new TaskingOrderFragment();
    }
    
    @Test
    public void testTaskingOrderCreation() {
        // Test basic tasking order creation
        TaskingOrder order = new TaskingOrder();
        order.setAoi("{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,0],[1,1],[0,1],[0,0]]]}");
        order.setPriorityItem(true);
        order.setProductType("optical");
        order.setResolution("30cm");
        order.setWindowStart("2024-01-01");
        order.setWindowEnd("2024-01-31");
        
        assertEquals("AOI should match", "{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,0],[1,1],[0,1],[0,0]]]}", order.getAoi());
        assertTrue("Should be priority item", order.isPriorityItem());
        assertEquals("Product type should match", "optical", order.getProductType());
        assertEquals("Resolution should match", "30cm", order.getResolution());
    }
    
    @Test
    public void testPindropTasking() {
        // Test tasking via pindrop converted to AOI
        double lat = 35.6762;
        double lon = 139.6503; // Tokyo coordinates
        String aoiFromPindrop = String.format("{\"type\":\"Point\",\"coordinates\":[%f,%f]}", lon, lat);
        
        TaskingOrder order = new TaskingOrder();
        order.setAoi(aoiFromPindrop);
        order.setProductType("optical");
        order.setResolution("50cm");
        
        assertNotNull("Order should not be null", order);
        assertTrue("AOI should contain coordinates", order.getAoi().contains(String.valueOf(lat)));
        assertTrue("AOI should contain coordinates", order.getAoi().contains(String.valueOf(lon)));
    }
    
    @Test
    public void testMGRSTasking() {
        // Test tasking via MGRS coordinates
        String mgrsCoord = "33UXP04";
        // In real implementation, this would convert to lat/lon
        
        assertNotNull("MGRS coordinate should not be null", mgrsCoord);
        assertFalse("MGRS coordinate should not be empty", mgrsCoord.isEmpty());
        assertTrue("MGRS should start with grid zone", Character.isDigit(mgrsCoord.charAt(0)));
    }
    
    @Test
    public void testCurrentLocationTasking() {
        // Test tasking via current location
        double currentLat = 37.7749;
        double currentLon = -122.4194; // San Francisco
        String aoiFromLocation = String.format("{\"type\":\"Point\",\"coordinates\":[%f,%f]}", currentLon, currentLat);
        
        TaskingOrder order = new TaskingOrder();
        order.setAoi(aoiFromLocation);
        order.setProductType("sar");
        order.setResolution("1m");
        
        assertTrue("AOI should contain current coordinates", order.getAoi().contains(String.valueOf(currentLat)));
        assertTrue("AOI should contain current coordinates", order.getAoi().contains(String.valueOf(currentLon)));
    }
    
    @Test
    public void testPriorityTasking() {
        // Test priority tasking option (similar to assured)
        TaskingOrder order = new TaskingOrder();
        order.setPriorityItem(true);
        order.setProductType("optical");
        
        assertTrue("Priority tasking should be enabled", order.isPriorityItem());
    }
    
    @Test
    public void testCloudCoverageConstraints() {
        // Test cloud coverage constraints
        TaskingOrder order = new TaskingOrder();
        order.setMaxCloudCoveragePercent(20.0f);
        order.setMaxOffNadirAngle(30.0f);
        
        assertEquals("Max cloud coverage should match", 20.0f, order.getMaxCloudCoveragePercent(), 0.1);
        assertEquals("Max off nadir angle should match", 30.0f, order.getMaxOffNadirAngle(), 0.1);
    }
    
    @Test
    public void testDeliveryConfiguration() {
        // Test delivery configuration
        TaskingOrder order = new TaskingOrder();
        order.setDeliveryDriver("s3");
        
        DeliveryParams params = new DeliveryParams();
        order.setDeliveryParams(params);
        
        assertEquals("Delivery driver should match", "s3", order.getDeliveryDriver());
        assertNotNull("Delivery params should not be null", order.getDeliveryParams());
    }
    
    @Test
    public void testOrderValidation() {
        // Test order validation with required fields
        TaskingOrder order = new TaskingOrder();
        
        // Test missing required fields
        assertNull("AOI should be null initially", order.getAoi());
        assertNull("Product type should be null initially", order.getProductType());
        
        // Set required fields
        order.setAoi("{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,0],[1,1],[0,1],[0,0]]]}");
        order.setProductType("optical");
        order.setResolution("30cm");
        order.setWindowStart("2024-01-01");
        order.setWindowEnd("2024-01-31");
        
        // Verify all required fields are set
        assertNotNull("AOI should be set", order.getAoi());
        assertNotNull("Product type should be set", order.getProductType());
        assertNotNull("Resolution should be set", order.getResolution());
        assertNotNull("Window start should be set", order.getWindowStart());
        assertNotNull("Window end should be set", order.getWindowEnd());
    }
    
    @Test
    public void testProviderSelection() {
        // Test provider selection
        TaskingOrder order = new TaskingOrder();
        order.setRequiredProvider("Maxar");
        order.setProductType("optical");
        
        assertEquals("Provider should match", "Maxar", order.getRequiredProvider());
        // Note: requiredProvider is ironically not required according to the comment
    }
    
    @Test
    public void testWindowTimeframe() {
        // Test window timeframe for tasking
        TaskingOrder order = new TaskingOrder();
        order.setWindowStart("2024-01-01T00:00:00Z");
        order.setWindowEnd("2024-01-31T23:59:59Z");
        
        assertEquals("Window start should match", "2024-01-01T00:00:00Z", order.getWindowStart());
        assertEquals("Window end should match", "2024-01-31T23:59:59Z", order.getWindowEnd());
    }
    
    @Test
    public void testCoordinateInputValidation() {
        // Test coordinate input validation
        
        // Valid coordinates
        assertTrue("Valid latitude", isValidLatitude(40.7128));
        assertTrue("Valid longitude", isValidLongitude(-74.0060));
        
        // Invalid coordinates
        assertFalse("Invalid latitude", isValidLatitude(91.0));
        assertFalse("Invalid longitude", isValidLongitude(181.0));
    }
    
    private boolean isValidLatitude(double lat) {
        return lat >= -90.0 && lat <= 90.0;
    }
    
    private boolean isValidLongitude(double lon) {
        return lon >= -180.0 && lon <= 180.0;
    }
}