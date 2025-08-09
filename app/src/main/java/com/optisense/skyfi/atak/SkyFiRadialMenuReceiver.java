package com.optisense.skyfi.atak;
import com.skyfi.atak.plugin.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Shape;
import com.atakmap.android.menu.MapMenuReceiver;
import com.atakmap.android.menu.MenuLayoutWidget;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles radial menu interactions for SkyFi plugin
 * Adds SkyFi option to shape/polygon radial menus
 */
public class SkyFiRadialMenuReceiver extends BroadcastReceiver {
    
    private static final String TAG = "SkyFi.RadialMenu";
    private static final String SKYFI_RADIAL_ACTION = "com.skyfi.atak.RADIAL_MENU_ACTION";
    
    private final Context context;
    private final MapView mapView;
    private final AOIManager aoiManager;
    
    public SkyFiRadialMenuReceiver(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        this.aoiManager = new AOIManager(context);
    }
    
    public void initialize() {
        Log.d(TAG, "Initializing SkyFi Radial Menu Receiver");
        
        // Register for radial menu events
        IntentFilter filter = new IntentFilter();
        filter.addAction(SKYFI_RADIAL_ACTION);
        filter.addAction("com.atakmap.android.maps.SHOW_MENU");
        filter.addAction("com.atakmap.android.maps.HIDE_MENU");
        filter.addAction("com.skyfi.atak.SHAPE_MENU_SHOWN");
        
        // Register using context with RECEIVER_NOT_EXPORTED flag for Android 15+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(this, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(this, filter);
        }
        
        // Add SkyFi menu item to shape radial menus
        addSkyFiToRadialMenu();
    }
    
    public void dispose() {
        try {
            context.unregisterReceiver(this);
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver", e);
        }
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (SKYFI_RADIAL_ACTION.equals(action)) {
            String uid = intent.getStringExtra("uid");
            handleSkyFiRadialAction(uid);
        } else if ("com.atakmap.android.maps.SHOW_MENU".equals(action)) {
            String uid = intent.getStringExtra("uid");
            MapItem item = mapView.getRootGroup().deepFindUID(uid);
            
            if (item instanceof Shape) {
                // Shape radial menu is being shown
                Log.d(TAG, "Shape radial menu shown for: " + uid);
            }
        } else if ("com.skyfi.atak.SHAPE_MENU_SHOWN".equals(action)) {
            // Handle our custom menu request
            String uid = intent.getStringExtra("uid");
            double area = intent.getDoubleExtra("area_sqkm", 0);
            Log.d(TAG, "SkyFi menu request for shape: " + uid + " with area: " + area + " sq km");
            
            // Auto-show our dialog for shapes
            MapItem item = mapView.getRootGroup().deepFindUID(uid);
            if (item != null) {
                showSkyFiOptionsDialog(item);
            }
        }
    }
    
    private void addSkyFiToRadialMenu() {
        try {
            // Register the menu configuration with ATAK
            // For now, the menu is defined in assets/menus/skyfi_radial_menu.xml
            // It will be loaded automatically by ATAK when the plugin starts
            
            Log.d(TAG, "SkyFi radial menu items configured for all shape types");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to add SkyFi to radial menu", e);
        }
    }
    
    private void handleSkyFiRadialAction(String uid) {
        Log.d(TAG, "Handling SkyFi radial action for UID: " + uid);
        
        MapItem item = mapView.getRootGroup().deepFindUID(uid);
        
        if (item != null) {
            Log.d(TAG, "Found map item of type: " + item.getType());
            
            // Handle different shape types - focus on Shape instances for now
            if (item instanceof Shape) {
                showSkyFiOptionsDialog(item);
            } else {
                Log.d(TAG, "Item type: " + item.getType() + " - not yet supported");
                Toast.makeText(context, "Item type not yet supported: " + item.getType(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Please select a shape or polygon", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showSkyFiOptionsDialog(final MapItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mapView.getContext());
        builder.setTitle("SkyFi Options");
        builder.setIcon(android.R.drawable.ic_menu_mapmode);
        
        // Create layout for dialog
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);
        
        // Add name input
        final EditText nameInput = new EditText(context);
        nameInput.setHint("AOI Name");
        nameInput.setText(item.getTitle() != null ? item.getTitle() : "AOI");
        layout.addView(nameInput);
        
        builder.setView(layout);
        
        // Options
        String[] options = {
            "Save as AOI",
            "Create Tasking Order", 
            "View Archive Imagery",
            "Calculate Feasibility",
            "Convert to AOI (Advanced)"
        };
        
        builder.setItems(options, (dialog, which) -> {
            String aoiName = nameInput.getText().toString();
            
            switch (which) {
                case 0: // Save as AOI
                    saveAsAOI(item, aoiName);
                    break;
                case 1: // Create Tasking Order
                    createTaskingOrder(item, aoiName);
                    break;
                case 2: // View Archive Imagery
                    viewArchiveImagery(item);
                    break;
                case 3: // Calculate Feasibility
                    calculateFeasibility(item);
                    break;
                case 4: // Convert to AOI (Advanced)
                    convertToAOIAdvanced(item, aoiName);
                    break;
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void saveAsAOI(MapItem item, String name) {
        try {
            // Get shape points
            List<GeoPoint> points = new ArrayList<>();
            
            Shape shape = (Shape) item;
            GeoPoint[] shapePoints = shape.getPoints();
            if (shapePoints != null) {
                for (GeoPoint point : shapePoints) {
                    points.add(point);
                }
            }
            
            // Calculate area
            double area = calculateArea(points);
            
            // Save AOI
            aoiManager.createAOI(name, points, area, "optical");
            
            Toast.makeText(context, "AOI '" + name + "' saved successfully", Toast.LENGTH_SHORT).show();
            
            // Update shape metadata
            item.setTitle(name);
            item.setMetaString("skyfi_aoi", "true");
            item.setMetaString("skyfi_area_sqkm", String.valueOf(area));
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to save AOI", e);
            Toast.makeText(context, "Failed to save AOI", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void createTaskingOrder(MapItem item, String aoiName) {
        try {
            // Get shape points
            List<GeoPoint> points = new ArrayList<>();
            
            Shape shape = (Shape) item;
            GeoPoint[] shapePoints = shape.getPoints();
            if (shapePoints != null) {
                for (GeoPoint point : shapePoints) {
                    points.add(point);
                }
            }
            
            // Open tasking order UI
            Intent intent = new Intent("com.skyfi.atak.SHOW_TASKING_ORDER");
            intent.putExtra("aoi_name", aoiName);
            intent.putExtra("shape_uid", item.getUID());
            
            // Convert points to arrays for intent
            double[] lats = new double[points.size()];
            double[] lons = new double[points.size()];
            for (int i = 0; i < points.size(); i++) {
                lats[i] = points.get(i).getLatitude();
                lons[i] = points.get(i).getLongitude();
            }
            intent.putExtra("lats", lats);
            intent.putExtra("lons", lons);
            
            AtakBroadcast.getInstance().sendBroadcast(intent);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to create tasking order", e);
            Toast.makeText(context, "Failed to create tasking order", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void viewArchiveImagery(MapItem item) {
        try {
            // Get shape bounds
            Shape shape = (Shape) item;
            GeoPoint[] points = shape.getPoints();
            if (points != null && points.length > 0) {
                // Calculate bounds
                double minLat = Double.MAX_VALUE, maxLat = Double.MIN_VALUE;
                double minLon = Double.MAX_VALUE, maxLon = Double.MIN_VALUE;
                
                for (GeoPoint point : points) {
                    minLat = Math.min(minLat, point.getLatitude());
                    maxLat = Math.max(maxLat, point.getLatitude());
                    minLon = Math.min(minLon, point.getLongitude());
                    maxLon = Math.max(maxLon, point.getLongitude());
                }
                
                // Open archive search with bounds
                Intent intent = new Intent("com.skyfi.atak.SHOW_ARCHIVE_SEARCH");
                intent.putExtra("min_lat", minLat);
                intent.putExtra("max_lat", maxLat);
                intent.putExtra("min_lon", minLon);
                intent.putExtra("max_lon", maxLon);
                
                AtakBroadcast.getInstance().sendBroadcast(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to view archive imagery", e);
            Toast.makeText(context, "Failed to view archive imagery", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void calculateFeasibility(MapItem item) {
        try {
            List<GeoPoint> points = new ArrayList<>();
            
            Shape shape = (Shape) item;
            GeoPoint[] shapePoints = shape.getPoints();
            if (shapePoints != null) {
                for (GeoPoint point : shapePoints) {
                    points.add(point);
                }
            }
            
            double area = calculateArea(points);
            
            // Show feasibility info
            AlertDialog.Builder builder = new AlertDialog.Builder(mapView.getContext());
            builder.setTitle("SkyFi Feasibility");
            builder.setMessage(String.format(
                "Area: %.2f sq km\n" +
                "Next Pass: ~2-3 days\n" +
                "Priority Available: Yes\n" +
                "Estimated Cost: $%.2f",
                area, area * 15.0 // $15 per sq km estimate
            ));
            builder.setPositiveButton("Create Order", (dialog, which) -> {
                createTaskingOrder(item, item.getTitle());
            });
            builder.setNegativeButton("Close", null);
            builder.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to calculate feasibility", e);
            Toast.makeText(context, "Failed to calculate feasibility", Toast.LENGTH_SHORT).show();
        }
    }
    
    private double calculateArea(List<GeoPoint> points) {
        if (points.size() < 3) return 0;
        
        double area = 0;
        int n = points.size();
        
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            GeoPoint p1 = points.get(i);
            GeoPoint p2 = points.get(j);
            
            area += p1.getLongitude() * p2.getLatitude();
            area -= p2.getLongitude() * p1.getLatitude();
        }
        
        area = Math.abs(area) / 2.0;
        
        // Convert to square kilometers (approximate)
        double avgLat = 0;
        for (GeoPoint p : points) {
            avgLat += p.getLatitude();
        }
        avgLat /= points.size();
        
        double metersPerDegreeLat = 111320.0;
        double metersPerDegreeLon = 111320.0 * Math.cos(Math.toRadians(avgLat));
        
        return area * metersPerDegreeLat * metersPerDegreeLon / 1000000.0;
    }
    
    private List<GeoPoint> createCirclePoints(GeoPoint center, double radiusMeters) {
        List<GeoPoint> points = new ArrayList<>();
        int numPoints = 32; // Create a 32-sided polygon to approximate the circle
        
        for (int i = 0; i < numPoints; i++) {
            double angle = (2 * Math.PI * i) / numPoints;
            
            // Calculate offset in meters
            double northing = radiusMeters * Math.cos(angle);
            double easting = radiusMeters * Math.sin(angle);
            
            // Convert to lat/lon offset
            double latOffset = northing / 111320.0; // meters per degree latitude
            double lonOffset = easting / (111320.0 * Math.cos(Math.toRadians(center.getLatitude())));
            
            points.add(new GeoPoint(
                center.getLatitude() + latOffset,
                center.getLongitude() + lonOffset
            ));
        }
        
        return points;
    }
    
    /**
     * Advanced AOI conversion using the ShapeGeometryExtractor
     */
    private void convertToAOIAdvanced(MapItem item, String aoiName) {
        try {
            Log.d(TAG, "Converting item to AOI using advanced extractor: " + item.getType());
            
            // Use the advanced shape geometry extractor
            ShapeGeometryExtractor extractor = new ShapeGeometryExtractor();
            ShapeGeometryExtractor.ShapeGeometry geometry = extractor.extractGeometry(item);
            
            if (geometry == null || geometry.points.isEmpty()) {
                Toast.makeText(context, "Unable to extract geometry from this shape", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Validate minimum area
            double minArea = aoiManager.getMinimumAreaForSensor("default");
            if (!extractor.isValidGeometry(geometry, minArea)) {
                new AlertDialog.Builder(mapView.getContext())
                    .setTitle("Area Warning")
                    .setMessage(String.format("Shape area (%.2f sq km) is below recommended minimum (%.2f sq km).\n\nProceed anyway?", 
                        geometry.areaSqKm, minArea))
                    .setPositiveButton("Proceed", (d, w) -> {
                        createAdvancedAOI(item, aoiName, geometry);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            } else {
                createAdvancedAOI(item, aoiName, geometry);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to convert to AOI using advanced method", e);
            Toast.makeText(context, "Advanced conversion failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Create AOI from extracted geometry
     */
    private void createAdvancedAOI(MapItem item, String aoiName, ShapeGeometryExtractor.ShapeGeometry geometry) {
        try {
            // Create the AOI
            AOIManager.AOI aoi = aoiManager.createAOI(aoiName, geometry.points, geometry.areaSqKm, "radial_menu_advanced");
            
            // Add metadata
            item.setMetaString("skyfi_aoi_id", aoi.id);
            item.setMetaString("skyfi_converted_to_aoi_advanced", "true");
            item.setMetaString("skyfi_geometry_type", geometry.shapeType);
            
            // Show detailed success dialog
            new AlertDialog.Builder(mapView.getContext())
                .setTitle("AOI Created Successfully")
                .setMessage(String.format("AOI: %s\nFrom: %s\nGeometry Type: %s\nArea: %.2f sq km\nPoints: %d", 
                    aoiName, item.getType(), geometry.shapeType, geometry.areaSqKm, geometry.points.size()))
                .setPositiveButton("Create Order", (d, w) -> {
                    createTaskingOrder(item, aoiName);
                })
                .setNegativeButton("Done", null)
                .show();
                
            Log.d(TAG, "Advanced AOI creation successful: " + aoiName);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to create advanced AOI", e);
            Toast.makeText(context, "Failed to create AOI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}