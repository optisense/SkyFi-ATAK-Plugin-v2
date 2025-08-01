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

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Call;
import retrofit2.Response;

@RunWith(RobolectricTestRunner.class)
public class APIClientTest {
    
    private APIClient apiClient;
    private MockWebServer mockWebServer;
    
    @Mock
    private Preferences mockPreferences;
    
    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        // Mock preferences to return test API key
        when(mockPreferences.getApiKey()).thenReturn("test-api-key");
        
        // Initialize APIClient (would need to modify constructor to accept base URL for testing)
        apiClient = new APIClient();
    }
    
    @Test
    public void testHealthCheck() throws IOException {
        // Arrange
        String responseJson = "{\"message\":\"pong\"}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));
        
        // Act
        SkyFiAPI api = apiClient.getApiClient();
        Call<Pong> call = api.healthCheck(new HealthCheck());
        Response<Pong> response = call.execute();
        
        // Assert
        assertTrue("Response should be successful", response.isSuccessful());
        assertNotNull("Response body should not be null", response.body());
        assertEquals("Response message should match", "pong", response.body().getMessage());
    }
    
    @Test
    public void testGetProfile() throws IOException {
        // Arrange
        String profileJson = "{" +
                "\"user\": {" +
                "\"email\": \"test@example.com\"," +
                "\"name\": \"Test User\"," +
                "\"username\": \"testuser\"" +
                "}," +
                "\"api_key\": \"test-api-key\"" +
                "}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(profileJson));
        
        // Act
        SkyFiAPI api = apiClient.getApiClient();
        Call<MyProfile> call = api.myProfile();
        Response<MyProfile> response = call.execute();
        
        // Assert
        assertTrue("Response should be successful", response.isSuccessful());
        assertNotNull("Response body should not be null", response.body());
        assertNotNull("User should not be null", response.body().getUser());
        assertEquals("Email should match", "test@example.com", response.body().getUser().getEmail());
        assertEquals("Name should match", "Test User", response.body().getUser().getName());
        assertEquals("Username should match", "testuser", response.body().getUser().getUsername());
    }
    
    @Test
    public void testApiKeyHeader() throws IOException {
        // Arrange
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        
        // Act
        SkyFiAPI api = apiClient.getApiClient();
        api.healthCheck(new HealthCheck()).execute();
        
        // Assert
        String authHeader = mockWebServer.takeRequest().getHeader("X-Skyfi-Api-Key");
        assertEquals("API key header should be set", "test-api-key", authHeader);
    }
    
    @Test
    public void testUserAgentHeader() throws IOException {
        // Arrange
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        
        // Act
        SkyFiAPI api = apiClient.getApiClient();
        api.healthCheck(new HealthCheck()).execute();
        
        // Assert
        String userAgent = mockWebServer.takeRequest().getHeader("User-Agent");
        assertNotNull("User-Agent header should be set", userAgent);
        assertTrue("User-Agent should contain ATAK", userAgent.contains("ATAK"));
    }
    
    @Test
    public void testErrorResponse() throws IOException {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{\"error\": \"Unauthorized\"}"));
        
        // Act
        SkyFiAPI api = apiClient.getApiClient();
        Call<MyProfile> call = api.myProfile();
        Response<MyProfile> response = call.execute();
        
        // Assert
        assertFalse("Response should not be successful", response.isSuccessful());
        assertEquals("Response code should be 401", 401, response.code());
    }
}