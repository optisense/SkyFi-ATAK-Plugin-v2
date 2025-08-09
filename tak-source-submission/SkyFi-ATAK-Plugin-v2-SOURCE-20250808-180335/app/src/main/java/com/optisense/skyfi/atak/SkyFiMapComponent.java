package com.optisense.skyfi.atak;

import android.content.Context;
import android.content.Intent;

import com.atakmap.android.cot.detail.CotDetailManager;
import com.atakmap.android.dropdown.DropDownMapComponent;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.menu.MapMenuReceiver;
import com.atakmap.coremap.log.Log;
import com.optisense.skyfi.atak.cog.COGLayerManager;
import com.optisense.skyfi.atak.compat.CompatibilityHelper;

/**
 * SkyFi Map Component - Main entry point for the plugin
 * This component handles plugin initialization for all ATAK versions
 */
public class SkyFiMapComponent extends DropDownMapComponent {
    
    private static final String TAG = "SkyFi.MapComponent";
    private SkyFiPlugin plugin;
    private SkyFiRadialMenuReceiver radialMenuReceiver;
    private SkyFiMapMenuFactory menuFactory;
    private SkyFiDetailHandler skyfiDetailHandler;
    private COGLayerManager cogLayerManager;
    private DashboardDropDownReceiver dashboardReceiver;
    
    @Override
    public void onCreate(Context context, Intent intent, MapView view) {
        super.onCreate(context, intent, view);
        
        Log.d(TAG, "SkyFi MapComponent created");
        
        // Log compatibility information
        CompatibilityHelper.logCompatibilityInfo(context);
        
        // Initialize the main plugin
        plugin = SkyFiPlugin.getInstance();
        plugin.initialize(context, view);
        Log.d(TAG, "SkyFi Plugin initialized");
        
        // Register dashboard dropdown receiver for compatibility
        dashboardReceiver = new DashboardDropDownReceiver(view, context);
        AtakBroadcast.DocumentedIntentFilter filter = new AtakBroadcast.DocumentedIntentFilter();
        filter.addAction("com.skyfi.atak.SHOW_DASHBOARD");
        AtakBroadcast.getInstance().registerReceiver(dashboardReceiver, filter);
        
        // Initialize and register custom menu factory
        menuFactory = new SkyFiMapMenuFactory(context, view);
        MapMenuReceiver.getInstance().registerMapMenuFactory(menuFactory);
        Log.d(TAG, "SkyFi custom menu factory registered");
        
        // Initialize radial menu integration
        radialMenuReceiver = new SkyFiRadialMenuReceiver(context, view);
        radialMenuReceiver.initialize();
        
        // Register custom CoT detail handler for SkyFi-specific metadata
        registerSkyFiDetailHandler();
        
        // Initialize COG layer manager
        cogLayerManager = COGLayerManager.getInstance(context, view);
        Log.d(TAG, "COG Layer Manager initialized");
        
        // The menu.xml in assets should be automatically loaded by ATAK
        // This component ensures proper integration with ATAK's menu system
    }
    
    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        super.onDestroyImpl(context, view);
        
        // Stop the plugin
        if (plugin != null) {
            plugin.onStop();
            plugin = null;
        }
        
        // Unregister dashboard receiver
        if (dashboardReceiver != null) {
            AtakBroadcast.getInstance().unregisterReceiver(dashboardReceiver);
            dashboardReceiver = null;
        }
        
        // Unregister menu factory
        if (menuFactory != null) {
            MapMenuReceiver.getInstance().unregisterMapMenuFactory(menuFactory);
            menuFactory = null;
        }
        
        // Clean up radial menu receiver
        if (radialMenuReceiver != null) {
            radialMenuReceiver.dispose();
            radialMenuReceiver = null;
        }
        
        // Unregister CoT detail handler
        unregisterSkyFiDetailHandler();
        
        // Clean up COG layer manager
        if (cogLayerManager != null) {
            cogLayerManager.dispose();
            cogLayerManager = null;
        }
        
        Log.d(TAG, "SkyFi MapComponent destroyed");
    }
    
    private void registerSkyFiDetailHandler() {
        // Register handler for SkyFi-specific CoT details
        skyfiDetailHandler = new SkyFiDetailHandler();
        CotDetailManager.getInstance().registerHandler(
            "__skyfi",
            skyfiDetailHandler
        );
        Log.d(TAG, "SkyFi CoT detail handler registered");
    }
    
    private void unregisterSkyFiDetailHandler() {
        if (skyfiDetailHandler != null) {
            CotDetailManager.getInstance().unregisterHandler(skyfiDetailHandler);
            skyfiDetailHandler = null;
        }
    }
}