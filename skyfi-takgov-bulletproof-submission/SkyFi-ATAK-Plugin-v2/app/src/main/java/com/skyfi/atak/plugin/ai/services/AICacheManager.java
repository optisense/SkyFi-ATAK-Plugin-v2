package com.skyfi.atak.plugin.ai.services;

import android.content.Context;
import android.content.SharedPreferences;
import com.atakmap.coremap.log.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.skyfi.atak.plugin.ai.models.AIResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AI Cache Manager for offline capabilities and performance optimization
 * Handles both memory and disk caching of AI results
 */
public class AICacheManager {
    private static final String TAG = "AICacheManager";
    private static final String CACHE_DIR = "ai_cache";
    private static final String CACHE_PREFS = "ai_cache_prefs";
    private static final long DEFAULT_CACHE_SIZE_MB = 50; // 50MB default cache size
    private static final long CLEANUP_INTERVAL_MINUTES = 30;
    
    private static AICacheManager instance;
    private final Context context;
    private final Gson gson;
    private final File cacheDirectory;
    private final SharedPreferences cachePrefs;
    private final ScheduledExecutorService scheduledExecutor;
    
    // Memory cache for frequently accessed items
    private final ConcurrentHashMap<String, CacheEntry> memoryCache;
    private final long maxCacheSizeBytes;
    private volatile long currentCacheSize = 0;
    
    private static class CacheEntry {
        final String data;
        final long timestamp;
        final long expirationTime;
        final int accessCount;
        final String type;
        
        CacheEntry(String data, long timestamp, long ttlSeconds, String type) {
            this.data = data;
            this.timestamp = timestamp;
            this.expirationTime = timestamp + (ttlSeconds * 1000);
            this.accessCount = 1;
            this.type = type;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
        
        CacheEntry incrementAccess() {
            return new CacheEntry(data, timestamp, (expirationTime - timestamp) / 1000, type) {
                @Override
                boolean isExpired() {
                    return System.currentTimeMillis() > expirationTime;
                }
            };
        }
    }
    
    private AICacheManager(Context context) {
        this.context = context;
        this.gson = new Gson();
        this.memoryCache = new ConcurrentHashMap<>();
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        this.cachePrefs = context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE);
        
        // Initialize cache directory
        this.cacheDirectory = new File(context.getCacheDir(), CACHE_DIR);
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs();
        }
        
        // Get cache size limit from preferences
        this.maxCacheSizeBytes = cachePrefs.getLong("max_cache_size_mb", DEFAULT_CACHE_SIZE_MB) * 1024 * 1024;
        
        // Calculate current cache size
        calculateCurrentCacheSize();
        
        // Start cleanup scheduler
        startCleanupScheduler();
        
        Log.d(TAG, "AI Cache Manager initialized with " + (maxCacheSizeBytes / 1024 / 1024) + "MB limit");
    }
    
    public static synchronized AICacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new AICacheManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Store AI response in cache
     */
    public <T extends AIResponse> void put(String key, T response, long ttlSeconds) {
        if (key == null || response == null) {
            return;
        }
        
        try {
            String json = gson.toJson(response);
            String type = response.getClass().getSimpleName();
            
            // Store in memory cache
            CacheEntry entry = new CacheEntry(json, System.currentTimeMillis(), ttlSeconds, type);
            memoryCache.put(key, entry);
            
            // Store in disk cache for persistence
            saveToDisk(key, json, entry.expirationTime, type);
            
            Log.d(TAG, "Cached AI response: " + key + " (type: " + type + ", TTL: " + ttlSeconds + "s)");
            
        } catch (Exception e) {
            Log.e(TAG, "Error caching AI response: " + key, e);
        }
    }
    
    /**
     * Retrieve AI response from cache
     */
    public <T extends AIResponse> T get(String key, Class<T> responseClass) {
        if (key == null || responseClass == null) {
            return null;
        }
        
        try {
            // Check memory cache first
            CacheEntry entry = memoryCache.get(key);
            if (entry != null) {
                if (!entry.isExpired()) {
                    // Update access count
                    memoryCache.put(key, entry.incrementAccess());
                    Log.v(TAG, "Cache hit (memory): " + key);
                    return gson.fromJson(entry.data, responseClass);
                } else {
                    // Remove expired entry
                    memoryCache.remove(key);
                    deleteDiskEntry(key);
                }
            }
            
            // Check disk cache
            String diskData = loadFromDisk(key);
            if (diskData != null) {
                T response = gson.fromJson(diskData, responseClass);
                // Also store in memory cache for faster access
                put(key, response, 3600); // Default 1 hour TTL for disk->memory promotion
                Log.v(TAG, "Cache hit (disk): " + key);
                return response;
            }
            
            Log.v(TAG, "Cache miss: " + key);
            return null;
            
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Error deserializing cached response: " + key, e);
            // Remove corrupted entry
            memoryCache.remove(key);
            deleteDiskEntry(key);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving cached response: " + key, e);
            return null;
        }
    }
    
    /**
     * Check if cache contains valid entry for key
     */
    public boolean contains(String key) {
        if (key == null) return false;
        
        CacheEntry entry = memoryCache.get(key);
        if (entry != null && !entry.isExpired()) {
            return true;
        }
        
        return diskContains(key);
    }
    
    /**
     * Remove specific entry from cache
     */
    public void remove(String key) {
        if (key == null) return;
        
        memoryCache.remove(key);
        deleteDiskEntry(key);
        Log.d(TAG, "Removed from cache: " + key);
    }
    
    /**
     * Clear all cache entries
     */
    public void clear() {
        memoryCache.clear();
        clearDiskCache();
        currentCacheSize = 0;
        Log.d(TAG, "Cleared all cache entries");
    }
    
    /**
     * Get cache statistics
     */
    public CacheStats getStats() {
        int memoryEntries = memoryCache.size();
        int diskEntries = getDiskEntryCount();
        long diskSize = getCurrentDiskCacheSize();
        
        return new CacheStats(memoryEntries, diskEntries, diskSize, maxCacheSizeBytes);
    }
    
    /**
     * Set maximum cache size
     */
    public void setMaxCacheSize(long maxSizeMB) {
        long newMaxBytes = maxSizeMB * 1024 * 1024;
        cachePrefs.edit().putLong("max_cache_size_mb", maxSizeMB).apply();
        
        if (newMaxBytes < currentCacheSize) {
            // Trigger cleanup to respect new limit
            performCleanup();
        }
        
        Log.d(TAG, "Cache size limit updated to " + maxSizeMB + "MB");
    }
    
    /**
     * Enable/disable offline mode caching
     */
    public void setOfflineMode(boolean enabled) {
        cachePrefs.edit().putBoolean("offline_mode", enabled).apply();
        Log.d(TAG, "Offline mode " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Check if offline mode is enabled
     */
    public boolean isOfflineModeEnabled() {
        return cachePrefs.getBoolean("offline_mode", true);
    }
    
    private void saveToDisk(String key, String data, long expirationTime, String type) {
        if (!isOfflineModeEnabled()) return;
        
        try {
            File cacheFile = new File(cacheDirectory, key + ".cache");
            File metaFile = new File(cacheDirectory, key + ".meta");
            
            // Write data
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                fos.write(data.getBytes("UTF-8"));
            }
            
            // Write metadata
            CacheMetadata metadata = new CacheMetadata(expirationTime, type, data.length());
            try (FileOutputStream fos = new FileOutputStream(metaFile)) {
                fos.write(gson.toJson(metadata).getBytes("UTF-8"));
            }
            
            currentCacheSize += data.length();
            
            // Check if we need to cleanup
            if (currentCacheSize > maxCacheSizeBytes) {
                performCleanup();
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Error saving to disk cache: " + key, e);
        }
    }
    
    private String loadFromDisk(String key) {
        if (!isOfflineModeEnabled()) return null;
        
        try {
            File cacheFile = new File(cacheDirectory, key + ".cache");
            File metaFile = new File(cacheDirectory, key + ".meta");
            
            if (!cacheFile.exists() || !metaFile.exists()) {
                return null;
            }
            
            // Check metadata for expiration
            try (FileInputStream fis = new FileInputStream(metaFile)) {
                byte[] metaBytes = fis.readAllBytes();
                String metaJson = new String(metaBytes, "UTF-8");
                CacheMetadata metadata = gson.fromJson(metaJson, CacheMetadata.class);
                
                if (System.currentTimeMillis() > metadata.expirationTime) {
                    // Expired, delete files
                    cacheFile.delete();
                    metaFile.delete();
                    return null;
                }
            }
            
            // Read cache data
            try (FileInputStream fis = new FileInputStream(cacheFile)) {
                byte[] dataBytes = fis.readAllBytes();
                return new String(dataBytes, "UTF-8");
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Error loading from disk cache: " + key, e);
            return null;
        }
    }
    
    private boolean diskContains(String key) {
        File cacheFile = new File(cacheDirectory, key + ".cache");
        File metaFile = new File(cacheDirectory, key + ".meta");
        
        if (!cacheFile.exists() || !metaFile.exists()) {
            return false;
        }
        
        try (FileInputStream fis = new FileInputStream(metaFile)) {
            byte[] metaBytes = fis.readAllBytes();
            String metaJson = new String(metaBytes, "UTF-8");
            CacheMetadata metadata = gson.fromJson(metaJson, CacheMetadata.class);
            
            return System.currentTimeMillis() <= metadata.expirationTime;
        } catch (IOException e) {
            return false;
        }
    }
    
    private void deleteDiskEntry(String key) {
        File cacheFile = new File(cacheDirectory, key + ".cache");
        File metaFile = new File(cacheDirectory, key + ".meta");
        
        if (cacheFile.exists()) {
            long size = cacheFile.length();
            cacheFile.delete();
            currentCacheSize -= size;
        }
        
        if (metaFile.exists()) {
            metaFile.delete();
        }
    }
    
    private void clearDiskCache() {
        File[] files = cacheDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        currentCacheSize = 0;
    }
    
    private int getDiskEntryCount() {
        File[] cacheFiles = cacheDirectory.listFiles((dir, name) -> name.endsWith(".cache"));
        return cacheFiles != null ? cacheFiles.length : 0;
    }
    
    private long getCurrentDiskCacheSize() {
        return currentCacheSize;
    }
    
    private void calculateCurrentCacheSize() {
        currentCacheSize = 0;
        File[] files = cacheDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                currentCacheSize += file.length();
            }
        }
    }
    
    private void startCleanupScheduler() {
        scheduledExecutor.scheduleAtFixedRate(
            this::performCleanup,
            CLEANUP_INTERVAL_MINUTES,
            CLEANUP_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        );
    }
    
    private void performCleanup() {
        Log.d(TAG, "Performing cache cleanup");
        
        // Clean memory cache of expired entries
        memoryCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        
        // Clean disk cache
        File[] cacheFiles = cacheDirectory.listFiles((dir, name) -> name.endsWith(".cache"));
        if (cacheFiles != null) {
            for (File cacheFile : cacheFiles) {
                String key = cacheFile.getName().replace(".cache", "");
                if (!diskContains(key)) {
                    deleteDiskEntry(key);
                }
            }
        }
        
        // If still over limit, remove oldest entries
        if (currentCacheSize > maxCacheSizeBytes) {
            removeOldestEntries();
        }
        
        Log.d(TAG, "Cache cleanup completed. Size: " + (currentCacheSize / 1024 / 1024) + "MB");
    }
    
    private void removeOldestEntries() {
        // Implementation to remove oldest entries based on last access time
        // This is a simplified version - could be enhanced with LRU algorithm
        File[] metaFiles = cacheDirectory.listFiles((dir, name) -> name.endsWith(".meta"));
        if (metaFiles == null) return;
        
        java.util.Arrays.sort(metaFiles, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
        
        for (File metaFile : metaFiles) {
            if (currentCacheSize <= maxCacheSizeBytes * 0.8) { // Clean to 80% of limit
                break;
            }
            
            String key = metaFile.getName().replace(".meta", "");
            deleteDiskEntry(key);
        }
    }
    
    private static class CacheMetadata {
        final long expirationTime;
        final String type;
        final long size;
        
        CacheMetadata(long expirationTime, String type, long size) {
            this.expirationTime = expirationTime;
            this.type = type;
            this.size = size;
        }
    }
    
    public static class CacheStats {
        public final int memoryEntries;
        public final int diskEntries;
        public final long diskSizeBytes;
        public final long maxSizeBytes;
        
        CacheStats(int memoryEntries, int diskEntries, long diskSizeBytes, long maxSizeBytes) {
            this.memoryEntries = memoryEntries;
            this.diskEntries = diskEntries;
            this.diskSizeBytes = diskSizeBytes;
            this.maxSizeBytes = maxSizeBytes;
        }
        
        public double getDiskUsagePercentage() {
            return maxSizeBytes > 0 ? (double) diskSizeBytes / maxSizeBytes * 100 : 0;
        }
    }
    
    public void shutdown() {
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
    }
}