package com.skyfi.atak.plugin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.skyfi.atak.plugin.skyfiapi.HealthCheck;
import com.skyfi.atak.plugin.skyfiapi.MyProfile;
import com.skyfi.atak.plugin.skyfiapi.Pong;
import com.skyfi.atak.plugin.skyfiapi.SkyFiAPI;
import com.skyfi.atak.plugin.skyfiapi.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

@RunWith(RobolectricTestRunner.class)
public class APIClientTest {
    
    private APIClient apiClient;
    
    @Mock
    private SkyFiAPI mockSkyFiAPI;
    
    @Mock
    private Call<Pong> mockPongCall;
    
    @Mock
    private Call<MyProfile> mockProfileCall;
    
    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        
        // Initialize APIClient
        apiClient = new APIClient();
    }
    
    @Test
    public void testApiClientCreation() {
        // Test that API client is properly created
        SkyFiAPI api = apiClient.getApiClient();
        
        assertNotNull("API client should not be null", api);
    }
    
    @Test
    public void testPreferencesIntegration() {
        // Test that preferences are used for API key
        Preferences prefs = new Preferences();
        
        // In real app, API key would be set by user
        // For test, we just verify the preferences exist
        assertNotNull("Preferences should not be null", prefs);
    }
    
    @Test
    public void testHealthCheckObject() {
        // Test HealthCheck object creation
        HealthCheck healthCheck = new HealthCheck();
        
        assertNotNull("HealthCheck should not be null", healthCheck);
    }
    
    @Test
    public void testPongResponse() {
        // Test Pong response object
        Pong pong = new Pong();
        pong.setMessage("pong");
        
        assertEquals("Message should match", "pong", pong.getMessage());
    }
    
    @Test
    public void testUserProfile() {
        // Test User profile object
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setUsername("testuser");
        
        assertEquals("Email should match", "test@example.com", user.getEmail());
        assertEquals("Name should match", "Test User", user.getName());
        assertEquals("Username should match", "testuser", user.getUsername());
    }
}