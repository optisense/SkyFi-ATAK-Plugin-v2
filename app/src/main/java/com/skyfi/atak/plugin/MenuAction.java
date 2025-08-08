package com.skyfi.atak.plugin;

import android.content.Intent;

import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.menu.MenuResourceFactory;
import com.atakmap.coremap.log.Log;

/**
 * Menu action handler for SkyFi plugin menu items
 * This class handles menu actions from the toolbar menu
 * Compatible with ATAK 5.3.0 without PluginMenuFactory dependency
 */
public class MenuAction {
    
    private static final String TAG = "SkyFi.MenuAction";
    
    public MenuAction() {
        // Constructor for compatibility
    }
    
    public boolean create(MapView mapView, android.view.MenuItem item) {
        String action = item.getIntent().getAction();
        
        if (action == null) {
            Log.w(TAG, "Menu action is null");
            return false;
        }
        
        Log.d(TAG, "Menu action triggered: " + action);
        
        // Broadcast the action to be handled by appropriate receivers
        Intent intent = new Intent(action);
        AtakBroadcast.getInstance().sendBroadcast(intent);
        
        // Handle specific actions that need immediate plugin access
        switch (action) {
            case "com.skyfi.atak.SHOW_DASHBOARD":
                // Dashboard will be shown by DashboardDropDownReceiver
                break;
                
            case "com.skyfi.atak.NEW_ORDER":
                SkyFiPlugin.getInstance().showNewOrderOptions();
                break;
                
            case "com.skyfi.atak.AOI_MANAGEMENT":
                SkyFiPlugin.getInstance().showAOIManagementDialog();
                break;
                
            case "com.skyfi.atak.SHOW_SETTINGS":
                SkyFiPlugin.getInstance().showSettingsMenu();
                break;
                
            default:
                // Let other receivers handle it
                break;
        }
        
        return true;
    }
}