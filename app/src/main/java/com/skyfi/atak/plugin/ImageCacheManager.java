package com.skyfi.atak.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.log.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages offline caching of SkyFi imagery
 */
public class ImageCacheManager {
    private static final String LOGTAG = "ImageCacheManager";
    private static final String CACHE_DIR = "skyfi_image_cache";
    private static final long MAX_CACHE_SIZE = 500 * 1024 * 1024; // 500MB
    private static final long MAX_CACHE_AGE = 30 * 24 * 60 * 60 * 1000; // 30 days
    
    private static ImageCacheManager instance;
    private final Context context;
    private final File cacheDir;
    private final Map<String, CacheEntry> cacheIndex;
    
    public interface CacheCallback {
        void onCached(String imageId, File cachedFile);
        void onProgress(int percent);
        void onError(String error);
    }
    
    private static class CacheEntry {
        String imageId;
        String fileName;
        long size;
        long timestamp;
        String metadata; // JSON metadata about the image
    }
    
    private ImageCacheManager(Context context) {
        this.context = context;
        this.cacheDir = new File(FileSystemUtils.getItem("cache"), CACHE_DIR);
        this.cacheIndex = new HashMap<>();
        
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        
        loadCacheIndex();
        cleanupOldCache();
    }
    
    public static synchronized ImageCacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new ImageCacheManager(context);
        }
        return instance;
    }
    
    /**
     * Cache an image from URL
     */
    public void cacheImage(String imageId, String imageUrl, String metadata, CacheCallback callback) {
        new CacheImageTask(imageId, imageUrl, metadata, callback).execute();
    }
    
    /**
     * Get cached image file if exists
     */
    public File getCachedImage(String imageId) {
        CacheEntry entry = cacheIndex.get(imageId);
        if (entry != null) {
            File file = new File(cacheDir, entry.fileName);
            if (file.exists()) {
                // Update timestamp for LRU
                entry.timestamp = System.currentTimeMillis();
                saveCacheIndex();
                return file;
            } else {
                // File was deleted, remove from index
                cacheIndex.remove(imageId);
                saveCacheIndex();
            }
        }
        return null;
    }
    
    /**
     * Check if image is cached
     */
    public boolean isCached(String imageId) {
        return getCachedImage(imageId) != null;
    }
    
    /**
     * Get cache status for an image
     */
    public CacheStatus getCacheStatus(String imageId) {
        CacheEntry entry = cacheIndex.get(imageId);
        if (entry != null && getCachedImage(imageId) != null) {
            return new CacheStatus(true, entry.size, entry.timestamp);
        }
        return new CacheStatus(false, 0, 0);
    }
    
    /**
     * Clear all cached images
     */
    public void clearCache() {
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        cacheIndex.clear();
        saveCacheIndex();
    }
    
    /**
     * Get total cache size
     */
    public long getCacheSize() {
        long totalSize = 0;
        for (CacheEntry entry : cacheIndex.values()) {
            totalSize += entry.size;
        }
        return totalSize;
    }
    
    /**
     * Remove cached image
     */
    public void removeFromCache(String imageId) {
        CacheEntry entry = cacheIndex.get(imageId);
        if (entry != null) {
            File file = new File(cacheDir, entry.fileName);
            if (file.exists()) {
                file.delete();
            }
            cacheIndex.remove(imageId);
            saveCacheIndex();
        }
    }
    
    private class CacheImageTask extends AsyncTask<Void, Integer, File> {
        private final String imageId;
        private final String imageUrl;
        private final String metadata;
        private final CacheCallback callback;
        private String errorMessage;
        
        CacheImageTask(String imageId, String imageUrl, String metadata, CacheCallback callback) {
            this.imageId = imageId;
            this.imageUrl = imageUrl;
            this.metadata = metadata;
            this.callback = callback;
        }
        
        @Override
        protected File doInBackground(Void... params) {
            try {
                // Check if already cached
                File cachedFile = getCachedImage(imageId);
                if (cachedFile != null) {
                    return cachedFile;
                }
                
                // Download image
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", "Bearer " + new Preferences().getApiKey());
                connection.connect();
                
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    errorMessage = "HTTP error: " + responseCode;
                    return null;
                }
                
                int fileLength = connection.getContentLength();
                
                // Create cache file
                String fileName = "img_" + imageId + "_" + System.currentTimeMillis() + ".jpg";
                File cacheFile = new File(cacheDir, fileName);
                
                // Download and save
                InputStream input = connection.getInputStream();
                FileOutputStream output = new FileOutputStream(cacheFile);
                
                byte[] buffer = new byte[4096];
                long total = 0;
                int count;
                
                while ((count = input.read(buffer)) != -1) {
                    total += count;
                    if (fileLength > 0) {
                        publishProgress((int) (total * 100 / fileLength));
                    }
                    output.write(buffer, 0, count);
                }
                
                output.flush();
                output.close();
                input.close();
                
                // Add to cache index
                CacheEntry entry = new CacheEntry();
                entry.imageId = imageId;
                entry.fileName = fileName;
                entry.size = cacheFile.length();
                entry.timestamp = System.currentTimeMillis();
                entry.metadata = metadata;
                
                cacheIndex.put(imageId, entry);
                saveCacheIndex();
                
                // Check cache size and cleanup if needed
                ensureCacheSize();
                
                return cacheFile;
                
            } catch (Exception e) {
                Log.e(LOGTAG, "Failed to cache image", e);
                errorMessage = e.getMessage();
                return null;
            }
        }
        
        @Override
        protected void onProgressUpdate(Integer... progress) {
            if (callback != null) {
                callback.onProgress(progress[0]);
            }
        }
        
        @Override
        protected void onPostExecute(File result) {
            if (callback != null) {
                if (result != null) {
                    callback.onCached(imageId, result);
                } else {
                    callback.onError(errorMessage);
                }
            }
        }
    }
    
    private void loadCacheIndex() {
        // TODO: Load cache index from persistent storage
        // For now, scan cache directory
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith("img_")) {
                    // Extract imageId from filename
                    String name = file.getName();
                    String[] parts = name.split("_");
                    if (parts.length >= 3) {
                        CacheEntry entry = new CacheEntry();
                        entry.imageId = parts[1];
                        entry.fileName = name;
                        entry.size = file.length();
                        entry.timestamp = file.lastModified();
                        cacheIndex.put(entry.imageId, entry);
                    }
                }
            }
        }
    }
    
    private void saveCacheIndex() {
        // TODO: Save cache index to persistent storage
    }
    
    private void cleanupOldCache() {
        long now = System.currentTimeMillis();
        for (CacheEntry entry : new HashMap<>(cacheIndex).values()) {
            if (now - entry.timestamp > MAX_CACHE_AGE) {
                removeFromCache(entry.imageId);
            }
        }
    }
    
    private void ensureCacheSize() {
        while (getCacheSize() > MAX_CACHE_SIZE && !cacheIndex.isEmpty()) {
            // Remove oldest entry
            CacheEntry oldest = null;
            for (CacheEntry entry : cacheIndex.values()) {
                if (oldest == null || entry.timestamp < oldest.timestamp) {
                    oldest = entry;
                }
            }
            if (oldest != null) {
                removeFromCache(oldest.imageId);
            }
        }
    }
    
    public static class CacheStatus {
        public final boolean isCached;
        public final long size;
        public final long timestamp;
        
        CacheStatus(boolean isCached, long size, long timestamp) {
            this.isCached = isCached;
            this.size = size;
            this.timestamp = timestamp;
        }
    }
}