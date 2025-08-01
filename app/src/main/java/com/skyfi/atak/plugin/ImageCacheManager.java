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
    
    public interface CacheCallback {
        void onCached(boolean success);
    }
    
    private ImageCacheManager(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newFixedThreadPool(2);
        
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
}