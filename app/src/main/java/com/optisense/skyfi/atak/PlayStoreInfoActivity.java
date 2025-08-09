package com.optisense.skyfi.atak;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.graphics.Color;
import android.text.method.ScrollingMovementMethod;

/**
 * Information activity for Play Store ATAK Plugin
 * This activity can be launched standalone to verify plugin installation
 */
public class PlayStoreInfoActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create a simple layout programmatically
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(Color.parseColor("#1a1a1a"));
        
        // Title
        TextView title = new TextView(this);
        title.setText("SkyFi ATAK Plugin");
        title.setTextSize(24);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);
        
        // Version info
        TextView version = new TextView(this);
        version.setText("Version: 2.0.0 (Play Store Edition)");
        version.setTextSize(16);
        version.setTextColor(Color.parseColor("#00ff00"));
        version.setGravity(Gravity.CENTER);
        version.setPadding(0, 0, 0, 30);
        layout.addView(version);
        
        // Info text
        TextView info = new TextView(this);
        info.setText(getInfoText());
        info.setTextSize(14);
        info.setTextColor(Color.parseColor("#cccccc"));
        info.setPadding(0, 0, 0, 20);
        info.setMovementMethod(new ScrollingMovementMethod());
        info.setVerticalScrollBarEnabled(true);
        info.setMaxHeight(400);
        layout.addView(info);
        
        // Status
        TextView status = new TextView(this);
        status.setText(getPluginStatus());
        status.setTextSize(14);
        status.setTextColor(Color.parseColor("#00ff00"));
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 20, 0, 20);
        layout.addView(status);
        
        // Close button
        Button closeButton = new Button(this);
        closeButton.setText("Close");
        closeButton.setOnClickListener(v -> finish());
        layout.addView(closeButton);
        
        setContentView(layout);
    }
    
    private String getInfoText() {
        return "SkyFi ATAK Plugin - Play Store Edition\n\n" +
               "This plugin enables satellite tasking directly from ATAK.\n\n" +
               "Features:\n" +
               "• Task satellites with custom AOIs\n" +
               "• View and manage tasking orders\n" +
               "• Search archive imagery\n" +
               "• Draw AOIs on the map\n" +
               "• Coordinate input support\n" +
               "• Preview satellite imagery\n\n" +
               "Installation:\n" +
               "1. Install this plugin from Google Play Store\n" +
               "2. Open ATAK\n" +
               "3. The plugin will auto-load\n" +
               "4. Look for 'SkyFi' in the toolbar\n\n" +
               "Note: This version is specifically designed for\n" +
               "Google Play Store ATAK and will not work with\n" +
               "SDK or TAK.gov versions of ATAK.\n\n" +
               "© 2024 OptiSense";
    }
    
    private String getPluginStatus() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return "✓ Plugin Installed - Version " + pInfo.versionName + " (Build " + pInfo.versionCode + ")";
        } catch (Exception e) {
            return "⚠ Status Unknown";
        }
    }
}