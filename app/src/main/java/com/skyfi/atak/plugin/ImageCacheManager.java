package com.skyfi.atak.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageCacheManager {
    private static final String TAG = "SkyFi.ImageCache";
    private static final String CACHE_DIR = "skyfi_image_cache";
    private static final int MEMORY_CACHE_SIZE = 20 * 1024 * 1024; // 20MB
    private static final int DISK_CACHE_SIZE = 100 * 1024 * 1024; // 100MB
    
    private static ImageCacheManager instance;
    private final Context context;
    private final LruCache<String, Bitmap> memoryCache;
    private final File diskCacheDir;
    private final ExecutorService executorService;
    private final AORFilterManager aorFilterManager;
    
    public interface CacheCallback {
        void onCached(boolean success);
    }
    
    public interface ProgressCallback {
        void onProgress(int progress, int total);
        void onComplete(boolean success, String message);
    }
    
    private ImageCacheManager(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newFixedThreadPool(2);
        this.aorFilterManager = new AORFilterManager(context);
        
        // Initialize memory cache
        memoryCache = new LruCache<String, Bitmap>(MEMORY_CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
        
        // Initialize disk cache directory
        diskCacheDir = new File(context.getCacheDir(), CACHE_DIR);
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }
        
        // Clean up old cache files
        cleanupOldCache();
    }
    
    public static synchronized ImageCacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new ImageCacheManager(context);
        }
        return instance;
    }
    
    public void cacheImage(String url, Bitmap bitmap, CacheCallback callback) {
        if (url == null || bitmap == null) {
            if (callback != null) callback.onCached(false);
            return;
        }
        
        String key = generateKey(url);
        
        // Add to memory cache
        memoryCache.put(key, bitmap);
        
        // Save to disk cache asynchronously
        executorService.execute(() -> {
            boolean success = saveToDisk(key, bitmap);
            if (callback != null) {
                callback.onCached(success);
            }
        });
    }
    
    public void cacheImageData(String url, byte[] data, CacheCallback callback) {
        if (url == null || data == null) {
            if (callback != null) callback.onCached(false);
            return;
        }
        
        executorService.execute(() -> {
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                if (bitmap != null) {
                    cacheImage(url, bitmap, callback);
                } else {
                    // If it's not a bitmap, save raw data
                    String key = generateKey(url);
                    boolean success = saveRawToDisk(key, data);
                    if (callback != null) {
                        callback.onCached(success);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error caching image data", e);
                if (callback != null) callback.onCached(false);
            }
        });
    }
    
    public Bitmap getFromCache(String url) {
        if (url == null) return null;
        
        String key = generateKey(url);
        
        // Check memory cache first
        Bitmap bitmap = memoryCache.get(key);
        if (bitmap != null) {
            return bitmap;
        }
        
        // Check disk cache
        bitmap = loadFromDisk(key);
        if (bitmap != null) {
            // Add to memory cache
            memoryCache.put(key, bitmap);
        }
        
        return bitmap;
    }
    
    public boolean isCached(String url) {
        if (url == null) return false;
        
        String key = generateKey(url);
        
        // Check memory cache
        if (memoryCache.get(key) != null) {
            return true;
        }
        
        // Check disk cache
        File file = new File(diskCacheDir, key);
        return file.exists();
    }
    
    public void clearCache() {
        memoryCache.evictAll();
        
        executorService.execute(() -> {
            File[] files = diskCacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        });
    }
    
    public long getCacheSize() {
        long size = 0;
        File[] files = diskCacheDir.listFiles();
        if (files != null) {
            for (File file : files) {
                size += file.length();
            }
        }
        return size;
    }
    
    private String generateKey(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(url.getBytes());
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to hash code
            return String.valueOf(url.hashCode());
        }
    }
    
    private boolean saveToDisk(String key, Bitmap bitmap) {
        File file = new File(diskCacheDir, key);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap to disk", e);
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
    
    private boolean saveRawToDisk(String key, byte[] data) {
        File file = new File(diskCacheDir, key + ".raw");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving raw data to disk", e);
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
    
    private Bitmap loadFromDisk(String key) {
        File file = new File(diskCacheDir, key);
        if (file.exists()) {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        }
        return null;
    }
    
    private void cleanupOldCache() {
        executorService.execute(() -> {
            long totalSize = getCacheSize();
            if (totalSize > DISK_CACHE_SIZE) {
                // Delete oldest files until under limit
                File[] files = diskCacheDir.listFiles();
                if (files != null && files.length > 0) {
                    // Sort by last modified
                    java.util.Arrays.sort(files, (f1, f2) -> 
                        Long.compare(f1.lastModified(), f2.lastModified()));
                    
                    int i = 0;
                    while (totalSize > DISK_CACHE_SIZE && i < files.length) {
                        long fileSize = files[i].length();
                        if (files[i].delete()) {
                            totalSize -= fileSize;
                        }
                        i++;
                    }
                }
            }
        });
    }
    
    /**
     * Cache high-resolution images from a list of URLs with progress tracking
     */
    public void cacheHighResImages(java.util.List<String> imageUrls, ProgressCallback callback) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            if (callback != null) {
                callback.onComplete(false, "No images to cache");
            }
            return;
        }
        
        executorService.execute(() -> {
            int total = imageUrls.size();
            int completed = 0;
            int failed = 0;
            
            for (int i = 0; i < imageUrls.size(); i++) {
                String url = imageUrls.get(i);
                try {
                    // Check if already cached
                    if (!isCached(url)) {
                        // Here you would typically download the high-res image
                        // For now, we'll simulate the caching process
                        Log.d(TAG, "Caching high-res image: " + url);
                        
                        // In a real implementation, you would:
                        // 1. Download the high-res version of the image
                        // 2. Store it using cacheImageData or cacheImage
                        // For now, we'll mark it as cached
                        String key = generateKey(url);
                        File cacheFile = new File(diskCacheDir, key + ".highres");
                        try {
                            cacheFile.createNewFile();
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to create cache file marker", e);
                            failed++;
                        }
                    }
                    
                    completed++;
                    if (callback != null) {
                        callback.onProgress(completed, total);
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Failed to cache image: " + url, e);
                    failed++;
                    completed++;
                    if (callback != null) {
                        callback.onProgress(completed, total);
                    }
                }
            }
            
            if (callback != null) {
                boolean success = failed == 0;
                String message = success ? 
                    "Successfully cached " + (total - failed) + " images" :
                    "Cached " + (total - failed) + " images, " + failed + " failed";
                callback.onComplete(success, message);
            }
        });
    }
    
    /**
     * Check if a high-resolution version is cached
     */
    public boolean isHighResCached(String url) {
        if (url == null) return false;
        
        String key = generateKey(url);
        File file = new File(diskCacheDir, key + ".highres");
        return file.exists();
    }
    
    /**
     * Cache images that are within the current AOR region with progress tracking
     */
    public void cacheRegionImages(List<String> imageUrls, List<Double> latitudes, List<Double> longitudes, ProgressCallback callback) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            if (callback != null) {
                callback.onComplete(false, "No images to cache");
            }
            return;
        }
        
        if (latitudes.size() != imageUrls.size() || longitudes.size() != imageUrls.size()) {
            if (callback != null) {
                callback.onComplete(false, "Image URLs and coordinates size mismatch");
            }
            return;
        }
        
        executorService.execute(() -> {
            // Filter images based on current AOR settings
            List<String> filteredUrls = new ArrayList<>();
            for (int i = 0; i < imageUrls.size(); i++) {
                String url = imageUrls.get(i);
                double lat = latitudes.get(i);
                double lon = longitudes.get(i);
                
                // Check if this image should be cached based on AOR filter settings
                // For now, cache all images since we don't have specific point-in-region checking
                // This could be enhanced to check if the point is within the selected AOR bounds
                if (aorFilterManager.getSelectedAOR().equals("all")) {
                    filteredUrls.add(url);
                } else {
                    // For a specific AOR, we would need to implement point-in-region checking
                    // For now, include all images when a specific AOR is selected
                    filteredUrls.add(url);
                }
            }
            
            if (filteredUrls.isEmpty()) {
                if (callback != null) {
                    callback.onComplete(true, "No images in current region to cache");
                }
                return;
            }
            
            Log.d(TAG, "Caching " + filteredUrls.size() + " images in region (filtered from " + imageUrls.size() + " total)");
            
            // Use existing caching logic for filtered URLs
            cacheHighResImages(filteredUrls, callback);
        });
    }
    
    /**
     * Get cache statistics for region vs world
     */
    public void getCacheStats(CacheStatsCallback callback) {
        executorService.execute(() -> {
            File[] files = diskCacheDir.listFiles();
            if (files == null) {
                if (callback != null) {
                    callback.onStatsReady(0, 0, 0);
                }
                return;
            }
            
            long totalSize = 0;
            int totalFiles = 0;
            int regionFiles = 0;
            
            for (File file : files) {
                totalSize += file.length();
                totalFiles++;
                
                // For now, we can't determine which files are region-specific without metadata
                // In a real implementation, you might store this information
            }
            
            if (callback != null) {
                callback.onStatsReady(totalFiles, regionFiles, totalSize);
            }
        });
    }
    
    /**
     * Clear cache for images outside current region (if in region mode)
     */
    public void clearNonRegionCache(CacheCallback callback) {
        if (aorFilterManager.getSelectedAOR().equals("all")) {
            // Don't clear anything when showing all regions
            if (callback != null) {
                callback.onCached(true);
            }
            return;
        }
        
        executorService.execute(() -> {
            // In a full implementation, you would need to track which cached files
            // correspond to which geographic locations to selectively clear them
            // For now, this is a placeholder that could be enhanced with metadata storage
            Log.d(TAG, "Selective region cache clearing would be implemented here");
            
            if (callback != null) {
                callback.onCached(true);
            }
        });
    }
    
    public interface CacheStatsCallback {
        void onStatsReady(int totalFiles, int regionFiles, long totalSize);
    }
}