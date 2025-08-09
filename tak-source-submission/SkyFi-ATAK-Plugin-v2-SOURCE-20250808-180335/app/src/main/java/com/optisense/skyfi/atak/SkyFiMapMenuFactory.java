package com.optisense.skyfi.atak;

import android.content.Context;
import android.content.Intent;

import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Shape;
import com.atakmap.android.menu.MapMenuFactory;
import com.atakmap.android.menu.MapMenuWidget;
import com.atakmap.coremap.log.Log;

/**
 * Custom MapMenuFactory for SkyFi plugin that adds SkyFi-specific actions
 * to radial menus when shapes are selected
 */
public class SkyFiMapMenuFactory implements MapMenuFactory {
    
    private static final String TAG = "SkyFi.MenuFactory";
    
    private final Context context;
    private final MapView mapView;
    private final AOIManager aoiManager;
    
    public SkyFiMapMenuFactory(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        this.aoiManager = new AOIManager(context);
    }
    
    @Override
    public MapMenuWidget create(MapItem item) {
        Log.d(TAG, "Creating menu for item: " + (item != null ? item.getType() : "null"));
        
        // Only customize menus for shapes - return null to let default factory handle
        if (item == null || !(item instanceof Shape)) {
            return null;
        }
        
        // For shapes, we'll broadcast an intent to show our custom options
        // Return null to let the default menu show, but we'll add our own handling
        broadcastSkyFiMenuRequest(item);
        
        // Return null to let default factory create the menu
        // We'll handle our actions via broadcast receivers
        return null;
    }
    
    private void broadcastSkyFiMenuRequest(MapItem item) {
        // Broadcast that a shape menu is being shown
        // This allows our receiver to add custom handling
        Intent intent = new Intent("com.skyfi.atak.SHAPE_MENU_SHOWN");
        intent.putExtra("uid", item.getUID());
        intent.putExtra("type", item.getType());
        
        if (item instanceof Shape) {
            Shape shape = (Shape) item;
            // Check if shape is closed (polygon/rectangle) vs open (polyline)
            boolean isClosed = !(shape.getType().equals("u-d-f"));  // polylines have this type
            intent.putExtra("is_closed", isClosed);
            
            // Calculate and add area if it's a closed shape
            if (isClosed && shape.getPoints() != null) {
                double area = calculateArea(shape);
                intent.putExtra("area_sqkm", area);
                
                // Store in metadata for later use
                item.setMetaDouble("skyfi_area_sqkm", area);
            }
        }
        
        AtakBroadcast.getInstance().sendBroadcast(intent);
        Log.d(TAG, "Broadcast SkyFi menu request for item: " + item.getUID());
    }
    
    private double calculateArea(Shape shape) {
        if (shape.getPoints() == null || shape.getPoints().length < 3) {
            return 0;
        }
        
        double area = 0;
        int n = shape.getPoints().length;
        
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            area += shape.getPoints()[i].getLongitude() * shape.getPoints()[j].getLatitude();
            area -= shape.getPoints()[j].getLongitude() * shape.getPoints()[i].getLatitude();
        }
        
        area = Math.abs(area) / 2.0;
        
        // Convert to square kilometers (approximate)
        double avgLat = 0;
        for (int i = 0; i < shape.getPoints().length; i++) {
            avgLat += shape.getPoints()[i].getLatitude();
        }
        avgLat /= shape.getPoints().length;
        
        double metersPerDegreeLat = 111320.0;
        double metersPerDegreeLon = 111320.0 * Math.cos(Math.toRadians(avgLat));
        
        return area * metersPerDegreeLat * metersPerDegreeLon / 1000000.0;
    }
}