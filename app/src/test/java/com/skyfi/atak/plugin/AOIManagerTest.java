package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AOIManager - Tests CRUD operations for Areas of Interest
 */
@RunWith(RobolectricTestRunner.class)
public class AOIManagerTest {
    
    @Mock
    private Context mockContext;
    
    @Mock
    private SharedPreferences mockPrefs;
    
    @Mock
    private SharedPreferences.Editor mockEditor;
    
    private AOIManager aoiManager;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock SharedPreferences behavior
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockPrefs.getString(eq("saved_aois"), anyString())).thenReturn("[]");
        
        aoiManager = new AOIManager(mockContext);
    }
    
    @Test
    public void testCreateAndSaveAOI() {
        // Test creating and saving a new AOI
        String id = aoiManager.generateAOIId();
        String name = "Test AOI";
        String wkt = "POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))";
        
        AOIManager.AOI aoi = new AOIManager.AOI(id, name, wkt);
        aoi.areaKm2 = 100.0;
        aoi.sensorType = "siwei";
        
        aoiManager.saveAOI(aoi);
        
        // Verify the AOI was saved
        AOIManager.AOI retrievedAOI = aoiManager.getAOI(id);
        assertNotNull("AOI should be retrievable after saving", retrievedAOI);
        assertEquals("Name should match", name, retrievedAOI.name);
        assertEquals("WKT should match", wkt, retrievedAOI.wkt);
        assertEquals("Area should match", 100.0, retrievedAOI.areaKm2, 0.01);
        assertEquals("Sensor type should match", "siwei", retrievedAOI.sensorType);
        
        // Verify persistence was called
        verify(mockEditor, atLeastOnce()).putString(eq("saved_aois"), anyString());
        verify(mockEditor, atLeastOnce()).apply();
    }
    
    @Test
    public void testUpdateExistingAOI() {
        // Create and save initial AOI
        String id = "test_aoi_123";
        AOIManager.AOI aoi = new AOIManager.AOI(id, "Original Name", "POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))");
        aoiManager.saveAOI(aoi);
        
        // Update the AOI
        AOIManager.AOI updatedAOI = new AOIManager.AOI(id, "Updated Name", "POLYGON((0 0, 0 2, 2 2, 2 0, 0 0))");
        updatedAOI.areaKm2 = 400.0;
        aoiManager.saveAOI(updatedAOI);
        
        // Verify the update
        AOIManager.AOI retrievedAOI = aoiManager.getAOI(id);
        assertEquals("Updated Name", retrievedAOI.name);
        assertEquals("POLYGON((0 0, 0 2, 2 2, 2 0, 0 0))", retrievedAOI.wkt);
        assertEquals(400.0, retrievedAOI.areaKm2, 0.01);
    }
    
    @Test
    public void testDeleteAOI() {
        // Create and save AOI
        String id = "test_aoi_delete";
        AOIManager.AOI aoi = new AOIManager.AOI(id, "To Delete", "POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))");
        aoiManager.saveAOI(aoi);
        
        // Verify it exists
        assertNotNull(aoiManager.getAOI(id));
        
        // Delete the AOI
        aoiManager.deleteAOI(id);
        
        // Verify it's deleted
        assertNull("AOI should be null after deletion", aoiManager.getAOI(id));
        
        // Verify persistence was called
        verify(mockEditor, atLeastOnce()).putString(eq("saved_aois"), anyString());
        verify(mockEditor, atLeastOnce()).apply();
    }
    
    @Test
    public void testRenameAOI() {
        // Create and save AOI
        String id = "test_aoi_rename";
        String originalName = "Original Name";
        String newName = "New Name";
        AOIManager.AOI aoi = new AOIManager.AOI(id, originalName, "POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))");
        aoiManager.saveAOI(aoi);
        
        // Rename the AOI
        aoiManager.renameAOI(id, newName);
        
        // Verify the rename
        AOIManager.AOI retrievedAOI = aoiManager.getAOI(id);
        assertEquals("Name should be updated", newName, retrievedAOI.name);
        
        // Verify persistence was called
        verify(mockEditor, atLeastOnce()).putString(eq("saved_aois"), anyString());
        verify(mockEditor, atLeastOnce()).apply();
    }
    
    @Test
    public void testRenameNonExistentAOI() {
        // Try to rename a non-existent AOI
        aoiManager.renameAOI("non_existent_id", "New Name");
        
        // Should not throw exception and persistence should not be called for rename
        // (Only called during initial load)
        verify(mockEditor, times(0)).putString(eq("saved_aois"), anyString());
    }
    
    @Test
    public void testGetAllAOIs() {
        // Save multiple AOIs
        AOIManager.AOI aoi1 = new AOIManager.AOI("id1", "AOI 1", "POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))");
        AOIManager.AOI aoi2 = new AOIManager.AOI("id2", "AOI 2", "POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))");
        AOIManager.AOI aoi3 = new AOIManager.AOI("id3", "AOI 3", "POLYGON((2 2, 2 3, 3 3, 3 2, 2 2))");
        
        aoiManager.saveAOI(aoi1);
        aoiManager.saveAOI(aoi2);
        aoiManager.saveAOI(aoi3);
        
        // Get all AOIs
        ArrayList<AOIManager.AOI> allAOIs = aoiManager.getAllAOIs();
        
        assertEquals("Should have 3 AOIs", 3, allAOIs.size());
        
        // Verify all AOIs are present
        boolean foundAOI1 = false, foundAOI2 = false, foundAOI3 = false;
        for (AOIManager.AOI aoi : allAOIs) {
            if (aoi.id.equals("id1")) foundAOI1 = true;
            if (aoi.id.equals("id2")) foundAOI2 = true;
            if (aoi.id.equals("id3")) foundAOI3 = true;
        }
        
        assertTrue("AOI 1 should be present", foundAOI1);
        assertTrue("AOI 2 should be present", foundAOI2);
        assertTrue("AOI 3 should be present", foundAOI3);
    }
    
    @Test
    public void testGenerateAOIId() {
        String id1 = aoiManager.generateAOIId();
        String id2 = aoiManager.generateAOIId();
        
        assertNotNull("Generated ID should not be null", id1);
        assertNotNull("Generated ID should not be null", id2);
        assertNotEquals("Generated IDs should be unique", id1, id2);
        assertTrue("ID should start with 'aoi_'", id1.startsWith("aoi_"));
        assertTrue("ID should start with 'aoi_'", id2.startsWith("aoi_"));
    }
    
    @Test
    public void testGetMinimumAOISize() {
        assertEquals("Siwei minimum size", 25.0, AOIManager.getMinimumAOISize("siwei"), 0.01);
        assertEquals("Satellogic minimum size", 100.0, AOIManager.getMinimumAOISize("satellogic"), 0.01);
        assertEquals("Umbra minimum size", 16.0, AOIManager.getMinimumAOISize("umbra"), 0.01);
        assertEquals("Geosat minimum size", 25.0, AOIManager.getMinimumAOISize("geosat"), 0.01);
        assertEquals("Planet minimum size", 100.0, AOIManager.getMinimumAOISize("planet"), 0.01);
        assertEquals("Impro minimum size", 64.0, AOIManager.getMinimumAOISize("impro"), 0.01);
        assertEquals("Default minimum size", 25.0, AOIManager.getMinimumAOISize("unknown"), 0.01);
        
        // Test case insensitivity
        assertEquals("Case insensitive check", 25.0, AOIManager.getMinimumAOISize("SIWEI"), 0.01);
    }
    
    @Test
    public void testGetSensorRequirements() {
        String requirements = AOIManager.getSensorRequirements("siwei");
        assertTrue("Should contain minimum AOI text", requirements.contains("Minimum AOI:"));
        assertTrue("Should contain size in km", requirements.contains("5.0 km x 5.0 km"));
        assertTrue("Should contain total area", requirements.contains("25 km²"));
        
        requirements = AOIManager.getSensorRequirements("planet");
        assertTrue("Should contain correct size for Planet", requirements.contains("10.0 km x 10.0 km"));
        assertTrue("Should contain correct area for Planet", requirements.contains("100 km²"));
    }
    
    @Test
    public void testAOIJsonSerialization() throws Exception {
        // Test AOI to JSON conversion
        AOIManager.AOI aoi = new AOIManager.AOI("test_id", "Test AOI", "POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))");
        aoi.areaKm2 = 50.0;
        aoi.sensorType = "geosat";
        aoi.timestamp = 1234567890L;
        
        JSONObject json = aoi.toJSON();
        
        assertEquals("test_id", json.getString("id"));
        assertEquals("Test AOI", json.getString("name"));
        assertEquals("POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))", json.getString("wkt"));
        assertEquals(1234567890L, json.getLong("timestamp"));
        assertEquals(50.0, json.getDouble("areaKm2"), 0.01);
        assertEquals("geosat", json.getString("sensorType"));
    }
    
    @Test
    public void testAOIJsonDeserialization() throws Exception {
        // Test JSON to AOI conversion
        JSONObject json = new JSONObject();
        json.put("id", "test_id");
        json.put("name", "Test AOI");
        json.put("wkt", "POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))");
        json.put("timestamp", 1234567890L);
        json.put("areaKm2", 75.0);
        json.put("sensorType", "umbra");
        
        AOIManager.AOI aoi = AOIManager.AOI.fromJSON(json);
        
        assertEquals("test_id", aoi.id);
        assertEquals("Test AOI", aoi.name);
        assertEquals("POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))", aoi.wkt);
        assertEquals(1234567890L, aoi.timestamp);
        assertEquals(75.0, aoi.areaKm2, 0.01);
        assertEquals("umbra", aoi.sensorType);
    }
    
    @Test
    public void testLoadAOIsFromStorage() throws Exception {
        // Mock persisted AOIs
        JSONArray persistedAOIs = new JSONArray();
        
        JSONObject aoi1 = new JSONObject();
        aoi1.put("id", "stored_aoi_1");
        aoi1.put("name", "Stored AOI 1");
        aoi1.put("wkt", "POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))");
        aoi1.put("timestamp", System.currentTimeMillis());
        aoi1.put("areaKm2", 25.0);
        aoi1.put("sensorType", "siwei");
        persistedAOIs.put(aoi1);
        
        JSONObject aoi2 = new JSONObject();
        aoi2.put("id", "stored_aoi_2");
        aoi2.put("name", "Stored AOI 2");
        aoi2.put("wkt", "POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))");
        aoi2.put("timestamp", System.currentTimeMillis());
        aoi2.put("areaKm2", 100.0);
        aoi2.put("sensorType", "planet");
        persistedAOIs.put(aoi2);
        
        when(mockPrefs.getString(eq("saved_aois"), anyString())).thenReturn(persistedAOIs.toString());
        
        // Create new AOIManager to trigger loading
        AOIManager loadedManager = new AOIManager(mockContext);
        
        // Verify loaded AOIs
        ArrayList<AOIManager.AOI> allAOIs = loadedManager.getAllAOIs();
        assertEquals("Should load 2 AOIs", 2, allAOIs.size());
        
        AOIManager.AOI loadedAOI1 = loadedManager.getAOI("stored_aoi_1");
        assertNotNull("Should load first AOI", loadedAOI1);
        assertEquals("Stored AOI 1", loadedAOI1.name);
        assertEquals(25.0, loadedAOI1.areaKm2, 0.01);
        
        AOIManager.AOI loadedAOI2 = loadedManager.getAOI("stored_aoi_2");
        assertNotNull("Should load second AOI", loadedAOI2);
        assertEquals("Stored AOI 2", loadedAOI2.name);
        assertEquals(100.0, loadedAOI2.areaKm2, 0.01);
    }
}