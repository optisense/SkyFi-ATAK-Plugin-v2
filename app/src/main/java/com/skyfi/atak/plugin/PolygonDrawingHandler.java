package com.skyfi.atak.plugin;

import android.graphics.Color;
import android.widget.Toast;

import com.atakmap.android.drawing.DrawingToolsMapComponent;
import com.atakmap.android.drawing.mapItems.DrawingShape;
import com.atakmap.android.editableShapes.EditablePolyline;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Shape;
import com.atakmap.android.toolbar.ToolManagerBroadcastReceiver;
import com.atakmap.app.R;
import com.atakmap.coremap.conversions.CoordinateFormat;
import com.atakmap.coremap.conversions.CoordinateFormatUtilities;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PolygonDrawingHandler implements MapEventDispatcher.MapEventDispatchListener {
    private static final String TAG = "SkyFi.PolygonDrawing";
    
    private final Context context;
    private final MapView mapView;
    private MapGroup drawingGroup;
    private DrawingShape currentPolygon;
    private List<GeoPoint> currentPoints;
    private PolygonCompleteListener listener;
    private boolean isDrawing = false;
    
    public interface PolygonCompleteListener {
        void onPolygonComplete(List<GeoPoint> points, double areaSqKm);
        void onPolygonCancelled();
    }
    
    public PolygonDrawingHandler(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        this.currentPoints = new ArrayList<>();
        
        // Get or create drawing group
        MapGroup rootGroup = mapView.getRootGroup();
        drawingGroup = rootGroup.findMapGroup("SkyFi_AOI_Drawing");
        if (drawingGroup == null) {
            drawingGroup = rootGroup.addGroup("SkyFi_AOI_Drawing");
        }
    }
    
    public void startPolygonDrawing(PolygonCompleteListener listener) {
        this.listener = listener;
        this.isDrawing = true;
        currentPoints.clear();
        
        // Register for map clicks
        mapView.getMapEventDispatcher().addMapEventListener(MapEvent.MAP_CLICK, this);
        mapView.getMapEventDispatcher().addMapEventListener(MapEvent.MAP_LONG_PRESS, this);
        
        Toast.makeText(context, "Tap to add points. Long press to complete.", Toast.LENGTH_LONG).show();
        
        // Request drawing tool
        Intent intent = new Intent(ToolManagerBroadcastReceiver.BEGIN_TOOL);
        intent.putExtra("tool", "skyfi_polygon_tool");
        AtakBroadcast.getInstance().sendBroadcast(intent);
    }
    
    public void stopPolygonDrawing() {
        isDrawing = false;
        mapView.getMapEventDispatcher().removeMapEventListener(MapEvent.MAP_CLICK, this);
        mapView.getMapEventDispatcher().removeMapEventListener(MapEvent.MAP_LONG_PRESS, this);
        
        // Clear any temporary drawing
        if (currentPolygon != null) {
            drawingGroup.removeItem(currentPolygon);
            currentPolygon = null;
        }
        
        // End tool
        Intent intent = new Intent(ToolManagerBroadcastReceiver.END_TOOL);
        intent.putExtra("tool", "skyfi_polygon_tool");
        AtakBroadcast.getInstance().sendBroadcast(intent);
    }
    
    @Override
    public void onMapEvent(MapEvent event) {
        if (!isDrawing) return;
        
        GeoPoint point = mapView.inverse(event.getPoint().x, event.getPoint().y).get();
        
        if (event.getType().equals(MapEvent.MAP_CLICK)) {
            // Add point to polygon
            currentPoints.add(point);
            updatePolygonDisplay();
            
            if (currentPoints.size() == 1) {
                Toast.makeText(context, "First point added. Continue tapping to define AOI.", 
                    Toast.LENGTH_SHORT).show();
            }
            
        } else if (event.getType().equals(MapEvent.MAP_LONG_PRESS)) {
            // Complete polygon
            if (currentPoints.size() >= 3) {
                completePolygon();
            } else {
                Toast.makeText(context, "Need at least 3 points to create an AOI", 
                    Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void updatePolygonDisplay() {
        // Remove old polygon
        if (currentPolygon != null) {
            drawingGroup.removeItem(currentPolygon);
        }
        
        if (currentPoints.size() < 2) return;
        
        // Create new polygon shape
        currentPolygon = new DrawingShape(mapView, drawingGroup, UUID.randomUUID().toString());
        currentPolygon.setTitle("SkyFi AOI (Drawing)");
        currentPolygon.setStrokeColor(Color.parseColor("#0080FF")); // SkyFi blue
        currentPolygon.setFillColor(Color.parseColor("#400080FF")); // Semi-transparent blue
        currentPolygon.setStrokeWeight(3.0);
        currentPolygon.setFilled(true);
        
        // Add points
        for (GeoPoint gp : currentPoints) {
            currentPolygon.addPoint(GeoPointMetaData.wrap(gp));
        }
        
        // Close the polygon for display
        if (currentPoints.size() > 2) {
            currentPolygon.setClosed(true);
        }
        
        drawingGroup.addItem(currentPolygon);
    }
    
    private void completePolygon() {
        if (currentPoints.size() < 3) return;
        
        // Calculate area
        double areaSqKm = calculatePolygonArea(currentPoints);
        
        // Check minimum area requirement (example: 0.25 sq km minimum)
        double minAreaSqKm = 0.25;
        if (areaSqKm < minAreaSqKm) {
            Toast.makeText(context, 
                String.format("AOI too small. Minimum area is %.2f sq km. Current: %.2f sq km", 
                    minAreaSqKm, areaSqKm), 
                Toast.LENGTH_LONG).show();
            return;
        }
        
        // Finalize the polygon
        if (currentPolygon != null) {
            currentPolygon.setTitle("SkyFi AOI");
            currentPolygon.setMetaString("skyfi_aoi", "true");
            currentPolygon.setMetaDouble("area_sq_km", areaSqKm);
        }
        
        stopPolygonDrawing();
        
        if (listener != null) {
            listener.onPolygonComplete(new ArrayList<>(currentPoints), areaSqKm);
        }
        
        Toast.makeText(context, 
            String.format("AOI created: %.2f sq km", areaSqKm), 
            Toast.LENGTH_SHORT).show();
    }
    
    private double calculatePolygonArea(List<GeoPoint> points) {
        if (points.size() < 3) return 0;
        
        // Simple area calculation using shoelace formula
        // This is approximate but sufficient for our needs
        double area = 0;
        int n = points.size();
        
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            double lat1 = Math.toRadians(points.get(i).getLatitude());
            double lon1 = Math.toRadians(points.get(i).getLongitude());
            double lat2 = Math.toRadians(points.get(j).getLatitude());
            double lon2 = Math.toRadians(points.get(j).getLongitude());
            
            area += (lon2 - lon1) * (2 + Math.sin(lat1) + Math.sin(lat2));
        }
        
        area = Math.abs(area) * 6371 * 6371 / 2.0; // Earth radius in km
        return area;
    }
    
    public void clearDrawing() {
        currentPoints.clear();
        if (currentPolygon != null) {
            drawingGroup.removeItem(currentPolygon);
            currentPolygon = null;
        }
    }
}