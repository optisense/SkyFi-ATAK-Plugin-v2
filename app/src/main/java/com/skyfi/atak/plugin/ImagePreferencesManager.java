package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class ImagePreferencesManager {
    private static final String TAG = "SkyFi.ImagePrefs";
    private static final String PREF_KEY_ARCHIVED_IMAGES = "skyfi_archived_images";
    private static final String PREF_KEY_FAVORITE_IMAGES = "skyfi_favorite_images";
    
    private static ImagePreferencesManager instance;
    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson;
    private Set<String> archivedImages;
    private Set<String> favoriteImages;
    
    private ImagePreferencesManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.gson = new Gson();
        loadPreferences();
    }
    
    public static synchronized ImagePreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new ImagePreferencesManager(context);
        }
        return instance;
    }
    
    private void loadPreferences() {
        // Load archived images
        String archivedJson = prefs.getString(PREF_KEY_ARCHIVED_IMAGES, "[]");
        Type setType = new TypeToken<Set<String>>(){}.getType();
        archivedImages = gson.fromJson(archivedJson, setType);
        if (archivedImages == null) {
            archivedImages = new HashSet<>();
        }
        
        // Load favorite images
        String favoriteJson = prefs.getString(PREF_KEY_FAVORITE_IMAGES, "[]");
        favoriteImages = gson.fromJson(favoriteJson, setType);
        if (favoriteImages == null) {
            favoriteImages = new HashSet<>();
        }
    }
    
    private void saveArchivedImages() {
        String json = gson.toJson(archivedImages);
        prefs.edit().putString(PREF_KEY_ARCHIVED_IMAGES, json).apply();
    }
    
    private void saveFavoriteImages() {
        String json = gson.toJson(favoriteImages);
        prefs.edit().putString(PREF_KEY_FAVORITE_IMAGES, json).apply();
    }
    
    public boolean isArchived(String archiveId) {
        return archivedImages.contains(archiveId);
    }
    
    public boolean isFavorite(String archiveId) {
        return favoriteImages.contains(archiveId);
    }
    
    public void toggleArchived(String archiveId) {
        if (archivedImages.contains(archiveId)) {
            archivedImages.remove(archiveId);
            Log.d(TAG, "Removed from archived: " + archiveId);
        } else {
            archivedImages.add(archiveId);
            Log.d(TAG, "Added to archived: " + archiveId);
        }
        saveArchivedImages();
    }
    
    public void toggleFavorite(String archiveId) {
        if (favoriteImages.contains(archiveId)) {
            favoriteImages.remove(archiveId);
            Log.d(TAG, "Removed from favorites: " + archiveId);
        } else {
            favoriteImages.add(archiveId);
            Log.d(TAG, "Added to favorites: " + archiveId);
        }
        saveFavoriteImages();
    }
    
    public Set<String> getArchivedImages() {
        return new HashSet<>(archivedImages);
    }
    
    public Set<String> getFavoriteImages() {
        return new HashSet<>(favoriteImages);
    }
    
    public void clearArchived() {
        archivedImages.clear();
        saveArchivedImages();
        Log.d(TAG, "Cleared all archived images");
    }
    
    public void clearFavorites() {
        favoriteImages.clear();
        saveFavoriteImages();
        Log.d(TAG, "Cleared all favorite images");
    }
}