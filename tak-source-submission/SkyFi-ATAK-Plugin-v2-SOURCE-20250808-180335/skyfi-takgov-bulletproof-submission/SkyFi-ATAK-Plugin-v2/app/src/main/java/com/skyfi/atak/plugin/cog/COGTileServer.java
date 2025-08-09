package com.skyfi.atak.plugin.cog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import com.atakmap.coremap.log.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Using simplified HTTP server
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Lightweight HTTP server that serves tiles from Cloud Optimized GeoTIFFs (COGs)
 * Runs on the device and provides XYZ tile access for ATAK
 */
public class COGTileServer {
    
    private static final String TAG = "SkyFi.COGTileServer";
    private static final int DEFAULT_PORT = 8282;
    private static final int TILE_SIZE = 256;
    private static final int CACHE_SIZE = 50 * 1024 * 1024; // 50MB tile cache
    
    private final Map<String, COGReader> cogReaders;
    private final LruCache<String, byte[]> tileCache;
    private final ExecutorService executor;
    private ServerSocket serverSocket;
    private Thread serverThread;
    private boolean isRunning = false;
    private final int port;
    
    public COGTileServer() {
        this(DEFAULT_PORT);
    }
    
    public COGTileServer(int port) {
        this.port = port;
        this.cogReaders = new HashMap<>();
        this.executor = Executors.newFixedThreadPool(4);
        
        // Initialize tile cache
        this.tileCache = new LruCache<String, byte[]>(CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, byte[] value) {
                return value.length;
            }
        };
        
        Log.d(TAG, "COG Tile Server initialized on port " + port);
    }
    
    /**
     * Start the tile server
     */
    public void startServer() {
        if (isRunning) {
            return;
        }
        
        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                isRunning = true;
                Log.d(TAG, "COG Tile Server started on port " + port);
                
                while (isRunning) {
                    Socket client = serverSocket.accept();
                    executor.execute(() -> handleClient(client));
                }
            } catch (IOException e) {
                if (isRunning) {
                    Log.e(TAG, "Server error", e);
                }
            }
        });
        serverThread.start();
    }
    
    /**
     * Stop the tile server
     */
    public void stopServer() {
        isRunning = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing server socket", e);
            }
        }
        if (serverThread != null) {
            try {
                serverThread.join(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error waiting for server thread", e);
            }
        }
        executor.shutdown();
        Log.d(TAG, "COG Tile Server stopped");
    }
    
    /**
     * Handle HTTP client request
     */
    private void handleClient(Socket client) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            OutputStream output = client.getOutputStream();
            
            // Read request line
            String requestLine = reader.readLine();
            if (requestLine == null || !requestLine.startsWith("GET ")) {
                sendError(output, 400, "Bad Request");
                return;
            }
            
            // Parse URI from request
            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                sendError(output, 400, "Bad Request");
                return;
            }
            
            String uri = parts[1];
            Log.d(TAG, "Serving request: " + uri);
            
            // Serve the tile
            serveTile(uri, output);
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling client", e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    /**
     * Serve a tile based on the URI
     */
    private void serveTile(String uri, OutputStream output) throws IOException {
        try {
            // Parse the URI: /layerId/z/x/y.png
            String[] parts = uri.split("/");
            if (parts.length < 5) {
                sendError(output, 400, "Invalid tile request format");
                return;
            }
            
            String layerId = parts[1];
            int z = Integer.parseInt(parts[2]);
            int x = Integer.parseInt(parts[3]);
            int y = Integer.parseInt(parts[4].replace(".png", ""));
            
            // Check cache first
            String cacheKey = getCacheKey(layerId, z, x, y);
            byte[] cachedTile = tileCache.get(cacheKey);
            if (cachedTile != null) {
                Log.d(TAG, "Serving cached tile: " + cacheKey);
                sendTile(output, cachedTile);
                return;
            }
            
            // Get the COG reader
            COGReader reader = cogReaders.get(layerId);
            if (reader == null) {
                sendError(output, 404, "Layer not found: " + layerId);
                return;
            }
            
            // Fetch the tile
            byte[] tileData = reader.getTile(z, x, y);
            if (tileData == null) {
                // Return transparent tile for out-of-bounds requests
                tileData = createTransparentTile();
            }
            
            // Cache the tile
            tileCache.put(cacheKey, tileData);
            
            sendTile(output, tileData);
            
        } catch (Exception e) {
            Log.e(TAG, "Error serving tile", e);
            sendError(output, 500, "Internal Server Error");
        }
    }
    
    /**
     * Send tile data as HTTP response
     */
    private void sendTile(OutputStream output, byte[] tileData) throws IOException {
        String headers = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: image/png\r\n" +
                        "Content-Length: " + tileData.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        output.write(headers.getBytes());
        output.write(tileData);
        output.flush();
    }
    
    /**
     * Send HTTP error response
     */
    private void sendError(OutputStream output, int code, String message) throws IOException {
        String response = "HTTP/1.1 " + code + " " + message + "\r\n" +
                         "Content-Type: text/plain\r\n" +
                         "Content-Length: " + message.length() + "\r\n" +
                         "Connection: close\r\n" +
                         "\r\n" +
                         message;
        output.write(response.getBytes());
        output.flush();
    }
    
    /**
     * Register a COG for serving
     * @param layerId Unique identifier for this COG layer
     * @param cogUrl URL to the COG file
     * @return true if successfully registered
     */
    public boolean registerCOG(String layerId, String cogUrl) {
        try {
            COGReader reader = new COGReader(cogUrl);
            reader.initialize();
            cogReaders.put(layerId, reader);
            Log.d(TAG, "Registered COG layer: " + layerId + " -> " + cogUrl);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to register COG: " + layerId, e);
            return false;
        }
    }
    
    /**
     * Unregister a COG layer
     */
    public void unregisterCOG(String layerId) {
        COGReader reader = cogReaders.remove(layerId);
        if (reader != null) {
            reader.close();
            // Clear cached tiles for this layer
            clearLayerCache(layerId);
            Log.d(TAG, "Unregistered COG layer: " + layerId);
        }
    }
    
    /**
     * Get the URL pattern for accessing tiles from this server
     * @param layerId The layer identifier
     * @return URL pattern like "http://localhost:8282/layerId/{z}/{x}/{y}.png"
     */
    public String getTileUrlPattern(String layerId) {
        return String.format("http://localhost:%d/%s/{z}/{x}/{y}.png", port, layerId);
    }
    
    /**
     * Get the port the server is listening on
     */
    public int getListeningPort() {
        return port;
    }
    
    private String getCacheKey(String layerId, int z, int x, int y) {
        return String.format("%s_%d_%d_%d", layerId, z, x, y);
    }
    
    private void clearLayerCache(String layerId) {
        // Note: LruCache doesn't provide iteration, so we'd need to track keys
        // For now, just clear all cache when a layer is removed
        tileCache.evictAll();
    }
    
    private byte[] createTransparentTile() {
        try {
            Bitmap bitmap = Bitmap.createBitmap(TILE_SIZE, TILE_SIZE, Bitmap.Config.ARGB_8888);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            bitmap.recycle();
            return stream.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "Failed to create transparent tile", e);
            return new byte[0];
        }
    }
    
    /**
     * Inner class to handle COG reading
     */
    private static class COGReader {
        private final String cogUrl;
        private COGMetadata metadata;
        
        public COGReader(String cogUrl) {
            this.cogUrl = cogUrl;
        }
        
        public void initialize() throws IOException {
            // Read COG headers to get metadata
            metadata = COGMetadata.readFromUrl(cogUrl);
            Log.d(TAG, "Initialized COG reader: " + metadata.toString());
        }
        
        public byte[] getTile(int z, int x, int y) {
            try {
                // Calculate which overview to use based on zoom level
                int overviewLevel = metadata.getOverviewForZoom(z);
                
                // Calculate byte range for this tile
                long[] byteRange = metadata.getTileByteRange(overviewLevel, x, y);
                if (byteRange == null) {
                    return null; // Tile out of bounds
                }
                
                // Fetch tile data using HTTP range request
                return fetchTileData(byteRange[0], byteRange[1]);
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to get tile " + z + "/" + x + "/" + y, e);
                return null;
            }
        }
        
        private byte[] fetchTileData(long start, long end) throws IOException {
            URL url = new URL(cogUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Set range header
            String rangeHeader = String.format("bytes=%d-%d", start, end);
            connection.setRequestProperty("Range", rangeHeader);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            
            try {
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_PARTIAL && 
                    responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }
                
                // Read the tile data
                try (InputStream is = connection.getInputStream();
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    
                    // Decode TIFF tile to PNG
                    return decodeTiffToPng(baos.toByteArray());
                }
                
            } finally {
                connection.disconnect();
            }
        }
        
        private byte[] decodeTiffToPng(byte[] tiffData) {
            try {
                // Try Android's native decoder (works for JPEG-compressed TIFFs)
                Bitmap bitmap = BitmapFactory.decodeByteArray(tiffData, 0, tiffData.length);
                if (bitmap != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    bitmap.recycle();
                    return stream.toByteArray();
                }
                
                // If Android can't decode it, we'd need a TIFF library
                // For now, return null which will show a transparent tile
                Log.w(TAG, "Could not decode TIFF tile (may need JPEG compression)");
                return null;
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to decode TIFF to PNG", e);
                return null;
            }
        }
        
        public void close() {
            // Cleanup resources if needed
        }
    }
}