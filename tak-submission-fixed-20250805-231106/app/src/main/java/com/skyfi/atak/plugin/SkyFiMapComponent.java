package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.Intent;

import com.atakmap.android.dropdown.DropDownMapComponent;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;

/**
 * SkyFi Map Component to properly register with ATAK's menu system
 */
public class SkyFiMapComponent extends DropDownMapComponent {
    
    private static final String TAG = "SkyFi.MapComponent";
    
    @Override
    public void onCreate(Context context, Intent intent, MapView view) {
        super.onCreate(context, intent, view);
        
        Log.d(TAG, "SkyFi MapComponent created");
        
        // The menu.xml in assets should be automatically loaded by ATAK
        // This component ensures proper integration with ATAK's menu system
    }
    
    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        super.onDestroyImpl(context, view);
        Log.d(TAG, "SkyFi MapComponent destroyed");
    }
}