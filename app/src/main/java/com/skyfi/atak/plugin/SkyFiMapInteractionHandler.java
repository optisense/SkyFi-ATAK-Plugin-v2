package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.Toast;

import com.atakmap.android.drawing.DrawingToolsMapComponent;
import com.atakmap.android.drawing.DrawingToolsToolbar;
import com.atakmap.android.drawing.mapItems.DrawingShape;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Shape;
import com.atakmap.android.toolbar.ToolManagerBroadcastReceiver;
import com.atakmap.android.util.ATAKUtilities;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;

import android.animation.ValueAnimator;
import java.util.ArrayList;
import java.util.UUID;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTWriter;
import android.app.AlertDialog;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.LinearLayout;

/**
 * Enhanced map interaction handler for SkyFi plugin
 * Integrates with ATAK's drawing tools and provides custom interactions
 */
public class SkyFiMapInteractionHandler implements MapEventDispatcher.MapEventDispatchListener {
    private static final String TAG = "SkyFiMapInteractionHandler";
    
    private final Context context;
    private final MapView mapView;
    private final SkyFiMapOverlay overlay;
    private final PolygonDrawingHandler drawingHandler;
    
    private boolean isDrawingMode = false;
    private DrawingShape currentShape = null;
    private Handler animationHandler = new Handler();
    private long lastInteractionTime = 0;
    
    // Callback interfaces
    public interface PolygonCompleteListener {
        void onPolygonComplete(com.atakmap.coremap.maps.coords.GeoPoint[] points, double areaKm2, String wkt);
        void onPolygonCancelled();
    }
    
    public interface MapItemInteractionListener {
        void onMapItemSelected(MapItem item);
        void onMapItemLongPress(MapItem item);
    }
    
    private PolygonCompleteListener polygonListener;
    private MapItemInteractionListener itemListener;
    private boolean isSelectionMode = false;
    
    public SkyFiMapInteractionHandler(Context context, MapView mapView, SkyFiMapOverlay overlay) {
        this.context = context;
        this.mapView = mapView;
        this.overlay = overlay;
        this.drawingHandler = new PolygonDrawingHandler(context, mapView);
        
        // Register for map events
        registerMapEventListeners();
    }
    
    private void registerMapEventListeners() {
        MapEventDispatcher dispatcher = mapView.getMapEventDispatcher();
        
        // Listen for item clicks
        dispatcher.addMapEventListener(MapEvent.ITEM_CLICK, this);
        dispatcher.addMapEventListener(MapEvent.ITEM_LONG_PRESS, this);
        
        // Listen for drawing events
        dispatcher.addMapEventListener(MapEvent.ITEM_ADDED, this);
        dispatcher.addMapEventListener(MapEvent.ITEM_REFRESH, this);
        dispatcher.addMapEventListener(MapEvent.ITEM_REMOVED, this);
        
        // Listen for map clicks
        dispatcher.addMapEventListener(MapEvent.MAP_CLICK, this);
        dispatcher.addMapEventListener(MapEvent.MAP_LONG_PRESS, this);
    }
    
    /**
     * Start polygon drawing mode with SkyFi enhancements
     */
    public void startPolygonDrawing(PolygonCompleteListener listener) {
        this.polygonListener = listener;
        this.isDrawingMode = true;
        
        // Activate ATAK's drawing tool
        Intent drawingIntent = new Intent("com.atakmap.android.toolbar.ACTIVATE_TOOL");
        drawingIntent.putExtra("tool", "com.atakmap.android.drawing.DrawingToolsToolbar");
        drawingIntent.putExtra("subTool", "polygon");
        AtakBroadcast.getInstance().sendBroadcast(drawingIntent);
        
        // Start monitoring for drawing feedback
        // Temporarily disabled to debug blue screen
        // drawingHandler.startMonitoring();
        // overlay.setDrawingMode(true);  // Disabled to prevent blue screen
        
        // Show instructions
        showDrawingInstructions();
        
        Log.d(TAG, "Started SkyFi polygon drawing mode");
    }
    
    /**
     * Stop polygon drawing mode
     */
    public void stopPolygonDrawing() {
        this.isDrawingMode = false;
        this.polygonListener = null;
        
        // Deactivate drawing tool
        Intent intent = new Intent("com.atakmap.android.toolbar.UNSET_TOOL");
        intent.putExtra("tool", "com.atakmap.android.drawing.DrawingToolsToolbar");
        AtakBroadcast.getInstance().sendBroadcast(intent);
        
        // Stop monitoring
        // Temporarily disabled to debug blue screen
        // drawingHandler.stopMonitoring();
        // overlay.setDrawingMode(false);  // Disabled to prevent blue screen
        
        // Clean up current shape if any
        if (currentShape != null) {
            currentShape.removeFromGroup();
            currentShape = null;
        }
        
        Log.d(TAG, "Stopped SkyFi polygon drawing mode");
    }
    
    @Override
    public void onMapEvent(MapEvent event) {
        String type = event.getType();
        MapItem item = event.getItem();
        
        switch (type) {
            case MapEvent.ITEM_ADDED:
                handleItemAdded(item);
                break;
                
            case MapEvent.ITEM_REFRESH:
                handleItemRefresh(item);
                break;
                
            case MapEvent.ITEM_REMOVED:
                handleItemRemoved(item);
                break;
                
            case MapEvent.ITEM_CLICK:
                handleItemClick(item);
                break;
                
            case MapEvent.ITEM_LONG_PRESS:
                handleItemLongPress(item);
                break;
                
            case MapEvent.MAP_CLICK:
                handleMapClick(event);
                break;
                
            case MapEvent.MAP_LONG_PRESS:
                handleMapLongPress(event);
                break;
        }
    }
    
    private void handleItemAdded(MapItem item) {
        if (!isDrawingMode) return;
        
        if (item instanceof DrawingShape) {
            DrawingShape shape = (DrawingShape) item;
            
            // Check if this is our polygon
            if (shape.getType().equals("u-d-f") || shape.getType().contains("polygon")) {
                currentShape = shape;
                
                // Apply SkyFi drawing style
                SkyFiPolygonStyle.applyDrawingStyle(shape);
                
                // Add visual feedback
                animateShapeCreation(shape);
                
                Log.d(TAG, "New polygon shape added: " + shape.getUID());
            }
        }
    }
    
    private void handleItemRefresh(MapItem item) {
        if (!isDrawingMode || currentShape == null) return;
        
        if (item.getUID().equals(currentShape.getUID())) {
            DrawingShape shape = (DrawingShape) item;
            
            // Update overlay with current points
            // Disabled to prevent blue screen
            // com.atakmap.coremap.maps.coords.GeoPoint[] points = shape.getPoints();
            // overlay.updateCurrentPolygon(points);
            
            // Check if polygon is complete (closed)
            if (shape.getNumPoints() >= 3 && isPolygonClosed(shape)) {
                handlePolygonComplete(shape);
            }
        }
    }
    
    private void handleItemRemoved(MapItem item) {
        if (currentShape != null && item.getUID().equals(currentShape.getUID())) {
            currentShape = null;
            // overlay.updateCurrentPolygon(null);  // Disabled to prevent blue screen
        }
    }
    
    private void handleItemClick(MapItem item) {
        // Prevent rapid clicks
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastInteractionTime < 300) return;
        lastInteractionTime = currentTime;
        
        // Handle polygon selection mode
        if (isSelectionMode && isSelectablePolygon(item)) {
            handlePolygonSelection(item);
            return;
        }
        
        // Check if it's a SkyFi-styled item or any polygon shape
        if (item instanceof Shape && !isDrawingMode) {
            Shape shape = (Shape) item;
            
            // Show opacity control dialog for this shape
            showShapeOpacityDialog(shape);
            
            // Apply selection animation
            if (SkyFiPolygonStyle.isSkyFiStyled(item)) {
                SkyFiPolygonStyle.applySelectedStyle(shape, true);
                
                // Deselect after delay
                animationHandler.postDelayed(() -> {
                    SkyFiPolygonStyle.resetStyle(shape);
                }, 3000);
            }
        }
        
        // Notify listener
        if (itemListener != null) {
            itemListener.onMapItemSelected(item);
        }
    }
    
    private void handleItemLongPress(MapItem item) {
        // Check if it's a polygon shape
        if (item instanceof DrawingShape || item instanceof Shape) {
            // Apply pulse animation for feedback
            if (item instanceof Shape) {
                SkyFiPolygonStyle.applyPulseAnimation((Shape) item);
            }
            
            // Show SkyFi menu if it's not already shown
            if (!isDrawingMode && itemListener != null) {
                itemListener.onMapItemLongPress(item);
            }
        }
    }
    
    private void handleMapClick(MapEvent event) {
        // Handle map clicks during drawing mode
        if (isDrawingMode && currentShape != null) {
            // Map click handling is delegated to ATAK's drawing tools
            // Custom feedback could be added here if needed
        }
    }
    
    private void handleMapLongPress(MapEvent event) {
        // Quick action to complete polygon
        if (isDrawingMode && currentShape != null && currentShape.getNumPoints() >= 3) {
            handlePolygonComplete(currentShape);
        }
    }
    
    private void handlePolygonComplete(DrawingShape shape) {
        if (polygonListener == null) return;
        
        try {
            GeoPoint[] points = shape.getPoints();
            String wkt = getWkt(points);
            double areaKm2 = calculatePolygonArea(points);
            
            // Apply final style
            SkyFiPolygonStyle.applyAreaBasedStyle(shape, areaKm2);
            
            // Stop drawing mode
            stopPolygonDrawing();
            
            // Notify listener
            polygonListener.onPolygonComplete(points, areaKm2, wkt);
            
            // Show completion animation
            animateShapeCompletion(shape);
            
        } catch (Exception e) {
            Log.e(TAG, "Error completing polygon", e);
            Toast.makeText(context, "Error completing polygon", Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean isPolygonClosed(DrawingShape shape) {
        // Check if shape is closed by comparing first and last points
        com.atakmap.coremap.maps.coords.GeoPoint[] points = shape.getPoints();
        if (points.length < 3) return false;
        
        com.atakmap.coremap.maps.coords.GeoPoint first = points[0];
        com.atakmap.coremap.maps.coords.GeoPoint last = points[points.length - 1];
        
        // Check if points are very close (within 1 meter)
        double distance = first.distanceTo(last);
        return distance < 1.0;
    }
    
    private com.atakmap.coremap.maps.coords.GeoPoint applyGridSnapping(com.atakmap.coremap.maps.coords.GeoPoint point) {
        // Grid snapping implementation (optional)
        // Snap to nearest 0.0001 degree (approximately 11 meters)
        double snapInterval = 0.0001;
        
        double lat = Math.round(point.getLatitude() / snapInterval) * snapInterval;
        double lon = Math.round(point.getLongitude() / snapInterval) * snapInterval;
        
        return new com.atakmap.coremap.maps.coords.GeoPoint(lat, lon);
    }
    
    private void showPointAddedFeedback(com.atakmap.coremap.maps.coords.GeoPoint point) {
        // Visual feedback when a point is added
        // This could be a temporary marker or animation
        mapView.post(() -> {
            // Create temporary visual feedback at point location
            // Implementation depends on specific UI requirements
        });
    }
    
    private void animateShapeCreation(Shape shape) {
        // Animate shape creation with fade-in effect
        // Note: setAlpha might not be available in all Shape implementations
        try {
            // Instead of alpha animation, use visibility toggle
            shape.setVisible(false);
            animationHandler.postDelayed(() -> {
                shape.setVisible(true);
            }, 100);
        } catch (Exception e) {
            // Fallback - just make sure shape is visible
            shape.setVisible(true);
        }
    }
    
    private void animateShapeCompletion(Shape shape) {
        // Completion animation - flash effect
        SkyFiPolygonStyle.applyPulseAnimation(shape);
        
        // Show success message
        Toast.makeText(context, "Polygon completed!", Toast.LENGTH_SHORT).show();
    }
    
    private void showDrawingInstructions() {
        Toast.makeText(context, 
            "Tap to add points. Long press map to complete polygon.", 
            Toast.LENGTH_LONG).show();
    }
    
    private double calculatePolygonArea(com.atakmap.coremap.maps.coords.GeoPoint[] points) {
        if (points == null || points.length < 3) return 0.0;
        
        // Shoelace formula
        double area = 0.0;
        int n = points.length;
        
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            area += points[i].getLongitude() * points[j].getLatitude();
            area -= points[j].getLongitude() * points[i].getLatitude();
        }
        
        area = Math.abs(area) / 2.0;
        
        // Convert to km²
        double avgLat = 0;
        for (com.atakmap.coremap.maps.coords.GeoPoint p : points) {
            avgLat += p.getLatitude();
        }
        avgLat /= points.length;
        
        double metersPerDegreeLat = 111320.0;
        double metersPerDegreeLon = 111320.0 * Math.cos(Math.toRadians(avgLat));
        
        return area * (metersPerDegreeLat * metersPerDegreeLon) / 1000000.0;
    }
    
    private com.atakmap.coremap.maps.coords.GeoPoint findPoint(MapEvent event) {
        // Extract point from event - limited by available API
        // For now, return null and handle elsewhere
        return null;
    }
    
    private String getWkt(com.atakmap.coremap.maps.coords.GeoPoint[] points) {
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        Coordinate firstCoord = null;
        for (com.atakmap.coremap.maps.coords.GeoPoint point : points) {
            Coordinate coordinate = new Coordinate(point.getLongitude(), point.getLatitude());
            coordinates.add(coordinate);
            if (firstCoord == null)
                firstCoord = coordinate;
        }
        
        // Make sure the polygon is closed
        coordinates.add(firstCoord);
        
        try {
            GeometryFactory factory = new GeometryFactory(new PrecisionModel(10000.0));
            Polygon polygon = factory.createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));
            WKTWriter wktWriter = new WKTWriter();
            return wktWriter.write(polygon);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed to convert to WKT", e);
        }
        
        return null;
    }
    
    public void setPolygonCompleteListener(PolygonCompleteListener listener) {
        this.polygonListener = listener;
    }
    
    public void setMapItemInteractionListener(MapItemInteractionListener listener) {
        this.itemListener = listener;
    }
    
    /**
     * Start polygon selection mode to select existing shapes
     */
    public void startPolygonSelection(PolygonCompleteListener listener) {
        this.polygonListener = listener;
        this.isSelectionMode = true;
        
        // overlay.setDrawingMode(false); // Not drawing, just selecting - Disabled to prevent blue screen
        
        // Show selection instructions
        showSelectionInstructions();
        
        Log.d(TAG, "Started polygon selection mode");
    }
    
    /**
     * Stop polygon selection mode
     */
    public void stopPolygonSelection() {
        this.isSelectionMode = false;
        this.polygonListener = null;
        
        // overlay.setDrawingMode(false);  // Disabled to prevent blue screen
        
        Log.d(TAG, "Stopped polygon selection mode");
    }
    
    /**
     * Find and highlight all selectable polygons on the map
     */
    public void highlightSelectablePolygons() {
        mapView.getRootGroup().deepForEachItem(item -> {
            if (isSelectablePolygon(item)) {
                // Apply selection highlight
                if (item instanceof Shape) {
                    SkyFiPolygonStyle.applyHoverStyle((Shape) item);
                }
            }
            return false; // Continue iteration
        });
    }
    
    /**
     * Check if a map item is a selectable polygon
     */
    private boolean isSelectablePolygon(MapItem item) {
        if (!(item instanceof Shape) && !(item instanceof DrawingShape)) {
            return false;
        }
        
        // Check if it's a polygon-like shape
        if (item instanceof DrawingShape) {
            DrawingShape shape = (DrawingShape) item;
            return shape.getNumPoints() >= 3 && 
                   (shape.getType().contains("polygon") || shape.getType().equals("u-d-f"));
        }
        
        // For other shapes, check if they have enough points
        if (item instanceof Shape) {
            // Shape interface may not have getNumPoints method
            // Return true and let the selection handler validate
            return true;
        }
        
        return false;
    }
    
    /**
     * Handle selection of an existing polygon
     */
    private void handlePolygonSelection(MapItem item) {
        if (!isSelectionMode || polygonListener == null) return;
        
        try {
            com.atakmap.coremap.maps.coords.GeoPoint[] points = null;
            
            if (item instanceof DrawingShape) {
                points = ((DrawingShape) item).getPoints();
            } else if (item instanceof Shape) {
                // Shape interface may not have getPoints method, try casting or alternative approach
                try {
                    points = ((Shape) item).getPoints();
                } catch (Exception e) {
                    Log.w(TAG, "Unable to get points from Shape: " + e.getMessage());
                    return; // Can't process this shape
                }
            }
            
            if (points != null && points.length >= 3) {
                // Calculate area and convert to WKT
                double areaKm2 = calculatePolygonArea(points);
                String wkt = getWkt(points);
                
                if (wkt != null) {
                    // Apply selection styling
                    if (item instanceof Shape) {
                        SkyFiPolygonStyle.applySelectedStyle((Shape) item, true);
                    }
                    
                    // Stop selection mode
                    stopPolygonSelection();
                    
                    // Notify listener
                    polygonListener.onPolygonComplete(points, areaKm2, wkt);
                    
                    Log.d(TAG, "Selected existing polygon with area: " + areaKm2 + " km²");
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling polygon selection", e);
        }
    }
    
    private void showSelectionInstructions() {
        android.widget.Toast.makeText(context, 
            "Tap on an existing polygon to select it for AOI creation", 
            android.widget.Toast.LENGTH_LONG).show();
    }
    
    public void cleanup() {
        // Unregister all listeners
        MapEventDispatcher dispatcher = mapView.getMapEventDispatcher();
        dispatcher.removeMapEventListener(this);
        
        // Clean up handlers
        animationHandler.removeCallbacksAndMessages(null);
        
        // Stop any ongoing operations
        if (isDrawingMode) {
            stopPolygonDrawing();
        }
        
        if (isSelectionMode) {
            stopPolygonSelection();
        }
    }
    
    /**
     * Show opacity control dialog for a specific shape
     */
    private void showShapeOpacityDialog(Shape shape) {
        try {
            // Get current opacity from shape's fill color
            int currentFillColor = shape.getFillColor();
            int currentOpacity = (currentFillColor >> 24) & 0xFF;
            
            // Create layout for dialog
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(20, 20, 20, 20);
            
            // Create TextView for current value display
            TextView opacityValueText = new TextView(context);
            int opacityPercent = (int) ((currentOpacity / 255.0) * 100);
            opacityValueText.setText("Opacity: " + opacityPercent + "%");
            opacityValueText.setTextSize(16);
            opacityValueText.setPadding(0, 0, 0, 10);
            layout.addView(opacityValueText);
            
            // Create SeekBar
            SeekBar seekBar = new SeekBar(context);
            seekBar.setMax(100);
            seekBar.setProgress(opacityPercent);
            layout.addView(seekBar);
            
            // Update text as SeekBar changes
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    opacityValueText.setText("Opacity: " + progress + "%");
                    
                    // Apply opacity change in real-time
                    int alpha = (int) ((progress / 100.0) * 255);
                    int baseColor = currentFillColor & 0x00FFFFFF;
                    int newFillColor = (alpha << 24) | baseColor;
                    shape.setFillColor(newFillColor);
                    
                    // Also update stroke opacity to match
                    int currentStrokeColor = shape.getStrokeColor();
                    int baseStrokeColor = currentStrokeColor & 0x00FFFFFF;
                    int newStrokeColor = (alpha << 24) | baseStrokeColor;
                    shape.setStrokeColor(newStrokeColor);
                    
                    shape.refresh(mapView.getMapEventDispatcher(), null, this.getClass());
                }
                
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            
            // Create and show dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Shape Opacity");
            builder.setView(layout);
            builder.setPositiveButton("OK", (dialog, which) -> {
                // Opacity is already applied in real-time, just close
                dialog.dismiss();
                
                // Save the opacity preference if this is a SkyFi-styled shape
                if (SkyFiPolygonStyle.isSkyFiStyled(shape)) {
                    int finalOpacity = (shape.getFillColor() >> 24) & 0xFF;
                    SkyFiPolygonStyle.setUserOpacity(finalOpacity);
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                // Restore original opacity
                shape.setFillColor(currentFillColor);
                shape.setStrokeColor(shape.getStrokeColor());
                shape.refresh(mapView.getMapEventDispatcher(), null, this.getClass());
                dialog.dismiss();
            });
            
            AlertDialog dialog = builder.create();
            dialog.show();
            
            Log.d(TAG, "Showing opacity dialog for shape: " + shape.getUID());
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing shape opacity dialog", e);
            Toast.makeText(context, "Unable to adjust opacity for this shape", Toast.LENGTH_SHORT).show();
        }
    }
}