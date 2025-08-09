package com.optisense.skyfi.atak.compat;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.atakmap.coremap.log.Log;

/**
 * Compatibility helper for handling differences between ATAK versions
 */
public class CompatibilityHelper {
    
    private static final String TAG = "SkyFi.Compat";
    
    /**
     * Check if IServiceController is available (ATAK 5.4.0.17+)
     */
    public static boolean hasServiceController() {
        try {
            Class.forName("gov.tak.api.plugin.IServiceController");
            return true;
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "IServiceController not available - using legacy initialization");
            return false;
        }
    }
    
    /**
     * Check if Pane API is available (ATAK 5.4.0.17+)
     */
    public static boolean hasPaneAPI() {
        try {
            Class.forName("gov.tak.api.ui.Pane");
            Class.forName("gov.tak.api.ui.PaneBuilder");
            return true;
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "Pane API not available - using dropdown receivers");
            return false;
        }
    }
    
    /**
     * Get the ATAK version string
     */
    public static String getATAKVersion(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            // Try different package names for different ATAK versions
            String[] packageNames = {
                "com.atakmap.app.civ",  // Play Store version
                "com.atakmap.app",      // SDK version
                context.getPackageName() // Fallback
            };
            
            for (String packageName : packageNames) {
                try {
                    PackageInfo info = pm.getPackageInfo(packageName, 0);
                    return info.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    // Try next package name
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get ATAK version", e);
        }
        return "Unknown";
    }
    
    /**
     * Get the ATAK version code
     */
    public static int getATAKVersionCode(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            String[] packageNames = {
                "com.atakmap.app.civ",
                "com.atakmap.app",
                context.getPackageName()
            };
            
            for (String packageName : packageNames) {
                try {
                    PackageInfo info = pm.getPackageInfo(packageName, 0);
                    return info.versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    // Try next package name
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get ATAK version code", e);
        }
        return 0;
    }
    
    /**
     * Parse version string to compare versions
     */
    public static int[] parseVersion(String version) {
        if (version == null || version.isEmpty()) {
            return new int[]{0, 0, 0, 0};
        }
        
        // Remove any non-numeric suffixes like "-CIV"
        version = version.replaceAll("[^0-9.]", "");
        
        String[] parts = version.split("\\.");
        int[] result = new int[4];
        
        for (int i = 0; i < Math.min(parts.length, 4); i++) {
            try {
                result[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        
        return result;
    }
    
    /**
     * Compare two version arrays
     * Returns: -1 if v1 < v2, 0 if v1 == v2, 1 if v1 > v2
     */
    public static int compareVersions(int[] v1, int[] v2) {
        for (int i = 0; i < 4; i++) {
            if (v1[i] < v2[i]) return -1;
            if (v1[i] > v2[i]) return 1;
        }
        return 0;
    }
    
    /**
     * Check if current ATAK version is at least the specified version
     */
    public static boolean isAtLeastVersion(Context context, String minVersion) {
        String currentVersion = getATAKVersion(context);
        int[] current = parseVersion(currentVersion);
        int[] min = parseVersion(minVersion);
        return compareVersions(current, min) >= 0;
    }
    
    /**
     * Check if this is a Play Store version of ATAK
     */
    public static boolean isPlayStoreVersion(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo("com.atakmap.app.civ", 0);
            // Check if it's installed and not a debug build
            return info != null && (info.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) == 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Log compatibility information
     */
    public static void logCompatibilityInfo(Context context) {
        Log.i(TAG, "=== SkyFi Plugin Compatibility Info ===");
        Log.i(TAG, "ATAK Version: " + getATAKVersion(context));
        Log.i(TAG, "ATAK Version Code: " + getATAKVersionCode(context));
        Log.i(TAG, "Is Play Store Version: " + isPlayStoreVersion(context));
        Log.i(TAG, "Has IServiceController: " + hasServiceController());
        Log.i(TAG, "Has Pane API: " + hasPaneAPI());
        Log.i(TAG, "=====================================");
    }
}