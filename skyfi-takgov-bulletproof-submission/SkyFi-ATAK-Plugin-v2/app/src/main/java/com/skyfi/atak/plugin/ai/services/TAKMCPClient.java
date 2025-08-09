package com.skyfi.atak.plugin.ai.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.atakmap.coremap.log.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyfi.atak.plugin.ai.models.*;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.*;
import okio.ByteString;

/**
 * TAK Server MCP (Model Context Protocol) Client for AI services integration
 * Handles WebSocket connection to TAK Server for AI request/response flow
 */
public class TAKMCPClient {
    private static final String TAG = "TAKMCPClient";
    private static final int RECONNECT_DELAY_MS = 5000;
    private static final int HEARTBEAT_INTERVAL_MS = 30000;
    
    private static TAKMCPClient instance;
    private final Context context;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final Handler mainHandler;
    
    private WebSocket webSocket;
    private String serverUrl;
    private String clientCertPath;
    private String clientKeyPath;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isReconnecting = new AtomicBoolean(false);
    
    // Request tracking
    private final Map<String, RequestCallback> pendingRequests = new ConcurrentHashMap<>();
    
    // Connection callbacks
    private ConnectionStatusListener connectionStatusListener;
    
    public interface ConnectionStatusListener {
        void onConnected();
        void onDisconnected();
        void onReconnecting();
        void onError(String error);
    }
    
    public interface RequestCallback {
        void onSuccess(AIResponse response);
        void onError(String error);
        void onTimeout();
    }
    
    private TAKMCPClient(Context context) {
        this.context = context;
        this.gson = new Gson();
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        // Create HTTP client with SSL configuration for certificate authentication
        this.httpClient = createSecureHttpClient();
    }
    
    public static synchronized TAKMCPClient getInstance(Context context) {
        if (instance == null) {
            instance = new TAKMCPClient(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Initialize connection parameters
     */
    public void initialize(String serverUrl, String clientCertPath, String clientKeyPath) {
        this.serverUrl = serverUrl;
        this.clientCertPath = clientCertPath;
        this.clientKeyPath = clientKeyPath;
        Log.d(TAG, "TAK MCP Client initialized with server: " + serverUrl);
    }
    
    /**
     * Connect to TAK Server MCP
     */
    public void connect() {
        if (serverUrl == null) {
            Log.e(TAG, "Server URL not configured. Call initialize() first.");
            return;
        }
        
        if (isConnected.get()) {
            Log.d(TAG, "Already connected to TAK MCP Server");
            return;
        }
        
        Log.d(TAG, "Connecting to TAK MCP Server: " + serverUrl);
        
        Request request = new Request.Builder()
                .url(serverUrl + "/ai/mcp")
                .addHeader("X-Client-Type", "ATAK-Plugin")
                .addHeader("X-Client-Version", "2.0")
                .build();
        
        webSocket = httpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "Connected to TAK MCP Server");
                isConnected.set(true);
                isReconnecting.set(false);
                
                // Send authentication message
                sendAuthenticationMessage();
                
                // Start heartbeat
                startHeartbeat();
                
                if (connectionStatusListener != null) {
                    mainHandler.post(() -> connectionStatusListener.onConnected());
                }
            }
            
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                handleMessage(text);
            }
            
            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                handleMessage(bytes.utf8());
            }
            
            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "TAK MCP connection closing: " + reason);
            }
            
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "TAK MCP connection closed: " + reason);
                isConnected.set(false);
                
                if (connectionStatusListener != null) {
                    mainHandler.post(() -> connectionStatusListener.onDisconnected());
                }
                
                // Attempt reconnection
                scheduleReconnect();
            }
            
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "TAK MCP connection failed", t);
                isConnected.set(false);
                
                String error = t.getMessage();
                if (response != null) {
                    error = "HTTP " + response.code() + ": " + response.message();
                }
                
                if (connectionStatusListener != null) {
                    final String finalError = error;
                    mainHandler.post(() -> connectionStatusListener.onError(finalError));
                }
                
                // Attempt reconnection
                scheduleReconnect();
            }
        });
    }
    
    /**
     * Disconnect from TAK Server MCP
     */
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Client disconnect");
            webSocket = null;
        }
        isConnected.set(false);
        isReconnecting.set(false);
    }
    
    /**
     * Send AI request to TAK Server MCP
     */
    public void sendAIRequest(AIRequest request, RequestCallback callback) {
        if (!isConnected.get()) {
            if (callback != null) {
                callback.onError("Not connected to TAK MCP Server");
            }
            return;
        }
        
        try {
            // Store callback for response handling
            pendingRequests.put(request.getRequestId(), callback);
            
            // Create MCP message
            JsonObject mcpMessage = new JsonObject();
            mcpMessage.addProperty("type", "ai_request");
            mcpMessage.addProperty("request_id", request.getRequestId());
            mcpMessage.addProperty("request_type", request.getClass().getSimpleName());
            mcpMessage.add("payload", gson.toJsonTree(request));
            
            // Send message
            String message = gson.toJson(mcpMessage);
            boolean sent = webSocket.send(message);
            
            if (!sent) {
                pendingRequests.remove(request.getRequestId());
                if (callback != null) {
                    callback.onError("Failed to send message to TAK MCP Server");
                }
            } else {
                Log.d(TAG, "Sent AI request: " + request.getRequestId());
                
                // Set timeout for request
                mainHandler.postDelayed(() -> {
                    RequestCallback timeoutCallback = pendingRequests.remove(request.getRequestId());
                    if (timeoutCallback != null) {
                        timeoutCallback.onTimeout();
                    }
                }, 30000); // 30 second timeout
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error sending AI request", e);
            pendingRequests.remove(request.getRequestId());
            if (callback != null) {
                callback.onError("Error sending request: " + e.getMessage());
            }
        }
    }
    
    /**
     * Set connection status listener
     */
    public void setConnectionStatusListener(ConnectionStatusListener listener) {
        this.connectionStatusListener = listener;
    }
    
    /**
     * Check if connected to TAK MCP Server
     */
    public boolean isConnected() {
        return isConnected.get();
    }
    
    private OkHttpClient createSecureHttpClient() {
        try {
            // Create trust manager that accepts all certificates
            // In production, implement proper certificate validation
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        // Accept all client certificates
                    }
                    
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        // Accept all server certificates
                        // TODO: Implement proper certificate validation for production
                    }
                    
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
            };
            
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
                    
        } catch (Exception e) {
            Log.e(TAG, "Error creating secure HTTP client", e);
            return new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
    }
    
    private void sendAuthenticationMessage() {
        JsonObject authMessage = new JsonObject();
        authMessage.addProperty("type", "auth");
        authMessage.addProperty("client_type", "ATAK_PLUGIN");
        authMessage.addProperty("client_version", "2.0");
        authMessage.addProperty("capabilities", "object_detection,natural_language,predictions");
        
        String message = gson.toJson(authMessage);
        webSocket.send(message);
        Log.d(TAG, "Sent authentication message");
    }
    
    private void startHeartbeat() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isConnected.get() && webSocket != null) {
                    JsonObject heartbeat = new JsonObject();
                    heartbeat.addProperty("type", "heartbeat");
                    heartbeat.addProperty("timestamp", System.currentTimeMillis());
                    
                    webSocket.send(gson.toJson(heartbeat));
                    
                    // Schedule next heartbeat
                    mainHandler.postDelayed(this, HEARTBEAT_INTERVAL_MS);
                }
            }
        }, HEARTBEAT_INTERVAL_MS);
    }
    
    private void handleMessage(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get("type").getAsString();
            
            switch (type) {
                case "ai_response":
                    handleAIResponse(json);
                    break;
                case "heartbeat_ack":
                    Log.v(TAG, "Received heartbeat acknowledgment");
                    break;
                case "auth_ack":
                    Log.d(TAG, "Authentication acknowledged");
                    break;
                case "error":
                    handleError(json);
                    break;
                default:
                    Log.w(TAG, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling message: " + message, e);
        }
    }
    
    private void handleAIResponse(JsonObject json) {
        try {
            String requestId = json.get("request_id").getAsString();
            String responseType = json.get("response_type").getAsString();
            JsonObject payload = json.getAsJsonObject("payload");
            
            RequestCallback callback = pendingRequests.remove(requestId);
            if (callback != null) {
                // Deserialize response based on type
                AIResponse response = null;
                switch (responseType) {
                    case "ObjectDetectionResponse":
                        response = gson.fromJson(payload, ObjectDetectionResponse.class);
                        break;
                    case "NaturalLanguageResponse":
                        response = gson.fromJson(payload, NaturalLanguageResponse.class);
                        break;
                    case "PredictionResponse":
                        response = gson.fromJson(payload, PredictionResponse.class);
                        break;
                }
                
                if (response != null) {
                    Log.d(TAG, "Received AI response for request: " + requestId);
                    callback.onSuccess(response);
                } else {
                    callback.onError("Unknown response type: " + responseType);
                }
            } else {
                Log.w(TAG, "No callback found for request: " + requestId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling AI response", e);
        }
    }
    
    private void handleError(JsonObject json) {
        String errorMessage = json.has("message") ? json.get("message").getAsString() : "Unknown error";
        String requestId = json.has("request_id") ? json.get("request_id").getAsString() : null;
        
        Log.e(TAG, "Received error from TAK MCP Server: " + errorMessage);
        
        if (requestId != null) {
            RequestCallback callback = pendingRequests.remove(requestId);
            if (callback != null) {
                callback.onError(errorMessage);
            }
        }
        
        if (connectionStatusListener != null) {
            mainHandler.post(() -> connectionStatusListener.onError(errorMessage));
        }
    }
    
    private void scheduleReconnect() {
        if (isReconnecting.get()) {
            return;
        }
        
        isReconnecting.set(true);
        if (connectionStatusListener != null) {
            mainHandler.post(() -> connectionStatusListener.onReconnecting());
        }
        
        mainHandler.postDelayed(() -> {
            if (!isConnected.get()) {
                Log.d(TAG, "Attempting to reconnect to TAK MCP Server");
                connect();
            }
        }, RECONNECT_DELAY_MS);
    }
}