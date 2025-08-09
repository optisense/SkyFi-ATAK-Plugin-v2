package com.optisense.skyfi.atak;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.atakmap.android.drawing.DrawingToolsMapComponent;
import com.atakmap.android.drawing.mapItems.DrawingShape;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Shape;
import com.atakmap.android.toolbar.ToolManagerBroadcastReceiver;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles integration with ATAK's built-in drawing tools
 * This class triggers ATAK's native drawing functionality and listens for completed shapes
 */
public class SkyFiDrawingToolsHandler {
    private static final String TAG = "SkyFi.DrawingTools";
    
    // ATAK drawing tool constants
    private static final String TOOL_IDENTIFIER = "com.atakmap.android.drawing.tools.DrawingToolsMapComponent.SHAPE_TOOL";
    private static final String DRAWING_COMPLETE_ACTION = "com.atakmap.android.drawing.DRAWING_COMPLETE";
    private static final String SKYFI_SHAPE_ACTION = "com.skyfi.SHAPE_ACTION";
    
    private final Context context;
    private final MapView mapView;
    private final AOIManager aoiManager;
    private AOIVisualizationManager aoiVisualizationManager;
    private ShapeCompleteListener listener;
    private BroadcastReceiver drawingCompleteReceiver;
    private BroadcastReceiver shapeActionReceiver;
    
    public interface ShapeCompleteListener {
        void onShapeComplete(String shapeUid, List<GeoPoint> points, double areaSqKm);
        void onShapeCancelled();
    }
    
    public SkyFiDrawingToolsHandler(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        this.aoiManager = new AOIManager(context);
        registerReceivers();
    }
    
    public void setAOIVisualizationManager(AOIVisualizationManager manager) {
        this.aoiVisualizationManager = manager;
    }
    
    /**
     * Start ATAK's built-in polygon drawing tool
     */
    public void startPolygonDrawing(ShapeCompleteListener listener) {
        this.listener = listener;
        
        // Request ATAK's polygon drawing tool
        Intent intent = new Intent(ToolManagerBroadcastReceiver.BEGIN_TOOL);
        intent.putExtra("tool", TOOL_IDENTIFIER);
        intent.putExtra("shape_type", "polygon");
        AtakBroadcast.getInstance().sendBroadcast(intent);
        
        Toast.makeText(context, "Use ATAK drawing tools to create AOI", Toast.LENGTH_LONG).show();
        
        Log.d(TAG, "Started ATAK polygon drawing tool");
    }
    
    /**
     * Stop the drawing tool
     */
    public void stopDrawing() {
        Intent intent = new Intent(ToolManagerBroadcastReceiver.END_TOOL);
        intent.putExtra("tool", TOOL_IDENTIFIER);
        AtakBroadcast.getInstance().sendBroadcast(intent);
    }
    
    /**
     * Register broadcast receivers for drawing events
     */
    private void registerReceivers() {
        // Listen for drawing completion
        drawingCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String shapeUid = intent.getStringExtra("uid");
                if (shapeUid != null) {
                    handleDrawingComplete(shapeUid);
                }
            }
        };
        
        // Listen for SkyFi shape actions from menu
        shapeActionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String shapeUid = intent.getStringExtra("targetUID");
                String action = intent.getStringExtra("action");
                
                if (shapeUid != null && action != null) {
                    handleShapeAction(shapeUid, action);
                }
            }
        };
        
        // Register receivers
        AtakBroadcast.getInstance().registerReceiver(
            drawingCompleteReceiver, 
            new AtakBroadcast.DocumentedIntentFilter(DRAWING_COMPLETE_ACTION)
        );
        
        AtakBroadcast.getInstance().registerReceiver(
            shapeActionReceiver,
            new AtakBroadcast.DocumentedIntentFilter(SKYFI_SHAPE_ACTION)
        );
    }
    
    /**
     * Handle completed drawing from ATAK
     */
    private void handleDrawingComplete(String shapeUid) {
        Log.d(TAG, "Drawing complete for shape: " + shapeUid);
        
        MapItem item = mapView.getRootGroup().deepFindUID(shapeUid);
        if (item instanceof Shape) {
            Shape shape = (Shape) item;
            List<GeoPoint> points = getShapePoints(shape);
            double areaSqKm = calculateAreaSqKm(points);
            
            if (listener != null) {
                listener.onShapeComplete(shapeUid, points, areaSqKm);
            }
        }
    }
    
    /**
     * Handle shape action from menu
     */
    public void handleShapeAction(String shapeUid, String action) {
        Log.d(TAG, "Shape action: " + action + " for UID: " + shapeUid);
        
        MapItem item = mapView.getRootGroup().deepFindUID(shapeUid);
        if (!(item instanceof Shape)) {
            Toast.makeText(context, "Selected item is not a valid shape", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Shape shape = (Shape) item;
        List<GeoPoint> points = getShapePoints(shape);
        double areaSqKm = calculateAreaSqKm(points);
        
        switch (action) {
            case "save_aoi":
                saveShapeAsAOI(shape, points, areaSqKm);
                break;
                
            case "task_satellite":
                taskSatellite(shape, points, areaSqKm);
                break;
                
            default:
                Log.w(TAG, "Unknown action: " + action);
        }
    }
    
    /**
     * Save shape as AOI
     */
    private void saveShapeAsAOI(Shape shape, List<GeoPoint> points, double areaSqKm) {
        String aoiName = "AOI_" + System.currentTimeMillis();
        
        try {
            // Area is already calculated and passed as parameter
            AOIManager.AOI aoi = aoiManager.createAOI(aoiName, points, areaSqKm, "default");
            String aoiId = aoi.id;
            Toast.makeText(context, "Saved as AOI: " + aoiName, Toast.LENGTH_SHORT).show();
            
            // Update shape metadata
            shape.setMetaString("skyfi_aoi_id", aoiId);
            shape.setMetaString("skyfi_aoi_name", aoiName);
            shape.setTitle("SkyFi AOI: " + aoiName);
            
            // Visualize the AOI on map
            if (aoiVisualizationManager != null) {
                aoiVisualizationManager.displayAOI(aoi);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to save AOI", e);
            Toast.makeText(context, "Failed to save AOI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Task satellite for shape
     */
    private void taskSatellite(Shape shape, List<GeoPoint> points, double areaSqKm) {
        // Check minimum area
        // Check minimum area based on default sensor type
        double minArea = aoiManager.getMinimumAreaForSensor("default");
        if (areaSqKm < minArea) {
            Toast.makeText(context, 
                String.format("Area too small. Minimum: %.2f sq km", minArea), 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Launch tasking order fragment
        Intent intent = new Intent("com.optisense.skyfi.atak.SHOW_PANE");
        intent.putExtra("pane", "tasking_order");
        intent.putExtra("aoi_points", pointsToString(points));
        intent.putExtra("aoi_area", areaSqKm);
        AtakBroadcast.getInstance().sendBroadcast(intent);
    }
    
    /**
     * Get points from shape
     */
    private List<GeoPoint> getShapePoints(Shape shape) {
        List<GeoPoint> points = new ArrayList<>();
        
        if (shape instanceof DrawingShape) {
            DrawingShape drawingShape = (DrawingShape) shape;
            for (int i = 0; i < drawingShape.getNumPoints(); i++) {
                points.add(drawingShape.getPoint(i).get());
            }
        } else {
            // Handle other shape types
            GeoPoint[] shapePoints = shape.getPoints();
            if (shapePoints != null) {
                for (GeoPoint pt : shapePoints) {
                    points.add(pt);
                }
            }
        }
        
        return points;
    }
    
    /**
     * Calculate area in square kilometers
     */
    private double calculateAreaSqKm(List<GeoPoint> points) {
        if (points.size() < 3) return 0;
        
        // Use shoelace formula
        double area = 0;
        int n = points.size();
        
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            area += points.get(i).getLongitude() * points.get(j).getLatitude();
            area -= points.get(j).getLongitude() * points.get(i).getLatitude();
        }
        
        area = Math.abs(area) / 2.0;
        
        // Convert to square kilometers (rough approximation)
        double avgLat = points.stream()
            .mapToDouble(GeoPoint::getLatitude)
            .average()
            .orElse(0);
        
        double metersPerDegreeLat = 111132.92 - 559.82 * Math.cos(2 * Math.toRadians(avgLat));
        double metersPerDegreeLon = 111412.84 * Math.cos(Math.toRadians(avgLat));
        
        return area * metersPerDegreeLat * metersPerDegreeLon / 1_000_000;
    }
    
    /**
     * Convert points to string for intent
     */
    private String pointsToString(List<GeoPoint> points) {
        StringBuilder sb = new StringBuilder();
        for (GeoPoint pt : points) {
            if (sb.length() > 0) sb.append(";");
            sb.append(pt.getLatitude()).append(",").append(pt.getLongitude());
        }
        return sb.toString();
    }
    
    /**
     * Cleanup
     */
    public void dispose() {
        if (drawingCompleteReceiver != null) {
            AtakBroadcast.getInstance().unregisterReceiver(drawingCompleteReceiver);
        }
        if (shapeActionReceiver != null) {
            AtakBroadcast.getInstance().unregisterReceiver(shapeActionReceiver);
        }
    }
}