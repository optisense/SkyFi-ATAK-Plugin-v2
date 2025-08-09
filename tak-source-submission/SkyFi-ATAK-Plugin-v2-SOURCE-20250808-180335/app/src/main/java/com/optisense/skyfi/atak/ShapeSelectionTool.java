package com.optisense.skyfi.atak;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Shape;
import com.atakmap.android.toolbar.ToolManagerBroadcastReceiver;
import android.os.Bundle;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.util.List;

/**
 * Tool for selecting existing shapes on the map to convert to AOIs
 * This tool enters a special selection mode where users can tap on existing shapes
 */
public class ShapeSelectionTool implements MapEventDispatcher.MapEventDispatchListener {
    private static final String TAG = "SkyFi.ShapeSelection";
    private static final String TOOL_IDENTIFIER = "com.optisense.skyfi.atak.SHAPE_SELECTION_TOOL";
    
    public interface ShapeSelectionListener {
        void onShapeSelected(MapItem shape, List<GeoPoint> points, double areaSqKm);
        void onSelectionCancelled();
    }
    
    private final Context context;
    private final MapView mapView;
    private final ShapeGeometryExtractor geometryExtractor;
    private final ShapeSelectionFeedback feedback;
    private ShapeSelectionListener listener;
    private boolean isActive = false;
    
    public ShapeSelectionTool(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        this.geometryExtractor = new ShapeGeometryExtractor();
        this.feedback = new ShapeSelectionFeedback(context, mapView);
    }
    
    /**
     * Start shape selection mode
     */
    public void startShapeSelection(ShapeSelectionListener listener) {
        this.listener = listener;
        
        Log.d(TAG, "Starting shape selection mode");
        
        // Register this tool with ATAK
        Intent startToolIntent = new Intent(ToolManagerBroadcastReceiver.BEGIN_TOOL);
        startToolIntent.putExtra("tool", TOOL_IDENTIFIER);
        AtakBroadcast.getInstance().sendBroadcast(startToolIntent);
        
        // Add map event listener for clicks
        mapView.getMapEventDispatcher().addMapEventListener(MapEvent.MAP_CLICK, this);
        mapView.getMapEventDispatcher().addMapEventListener(MapEvent.ITEM_CLICK, this);
        
        isActive = true;
        
        // Show visual feedback and instructions
        feedback.showSelectionModeInstructions();
        
        Log.d(TAG, "Shape selection mode activated");
    }
    
    /**
     * Stop shape selection mode
     */
    public void stopShapeSelection() {
        if (!isActive) return;
        
        Log.d(TAG, "Stopping shape selection mode");
        
        // Unregister tool
        Intent endToolIntent = new Intent(ToolManagerBroadcastReceiver.END_TOOL);
        endToolIntent.putExtra("tool", TOOL_IDENTIFIER);
        AtakBroadcast.getInstance().sendBroadcast(endToolIntent);
        
        // Remove map event listeners
        mapView.getMapEventDispatcher().removeMapEventListener(MapEvent.MAP_CLICK, this);
        mapView.getMapEventDispatcher().removeMapEventListener(MapEvent.ITEM_CLICK, this);
        
        isActive = false;
        
        // Cleanup visual feedback
        feedback.cleanup();
        
        Log.d(TAG, "Shape selection mode deactivated");
    }
    
    @Override
    public void onMapEvent(MapEvent event) {
        if (!isActive) return;
        
        String eventType = event.getType();
        Log.d(TAG, "Map event: " + eventType);
        
        if (MapEvent.ITEM_CLICK.equals(eventType)) {
            // Handle item click - this is what we want for shape selection
            MapItem clickedItem = event.getItem();
            handleItemClick(clickedItem);
            // Event consumed (implicit in ATAK event handling)
            
        } else if (MapEvent.MAP_CLICK.equals(eventType)) {
            // Handle empty map click - cancel selection
            Log.d(TAG, "Empty map clicked, cancelling selection");
            feedback.showSelectionCancelledFeedback();
            stopShapeSelection();
            if (listener != null) {
                listener.onSelectionCancelled();
            }
            // Event consumed (implicit in ATAK event handling)
        }
    }
    
    private void handleItemClick(MapItem item) {
        if (item == null) {
            Log.d(TAG, "Clicked item is null");
            return;
        }
        
        // Safely get item type and UID with defensive error handling
        String itemType = safeGetItemType(item);
        String itemUID = safeGetItemUID(item);
        
        Log.d(TAG, "Item clicked: " + itemType + " UID: " + itemUID);
        
        // Check if this is a shape we can process
        if (isValidShape(item)) {
            feedback.showShapeHoverFeedback(item);
            processShape(item);
        } else {
            Log.d(TAG, "Item is not a valid shape type: " + itemType);
            feedback.showInvalidShapeFeedback(item, "Unsupported shape type. Please select a polygon, circle, or rectangle.");
        }
    }
    
    private boolean isValidShape(MapItem item) {
        try {
            // Check if item is a supported shape type
            if (item instanceof Shape) {
                return true;
            }
            
            // Check by type string for other shape types
            String itemType = safeGetItemType(item);
            return itemType != null && (
                itemType.contains("polygon") ||
                itemType.contains("circle") ||
                itemType.contains("rectangle") ||
                itemType.contains("ellipse") ||
                itemType.contains("drawing") ||
                itemType.contains("freehand")
            );
        } catch (Exception e) {
            Log.w(TAG, "Error validating shape type", e);
            return false;
        }
    }
    
    private void processShape(MapItem item) {
        try {
            String itemType = safeGetItemType(item);
            String itemUID = safeGetItemUID(item);
            Log.d(TAG, "Processing shape: " + itemType + " (UID: " + itemUID + ")");
            feedback.showProcessingFeedback("Extracting geometry from shape...");
            
            // Process geometry extraction on background thread
            new Thread(() -> {
                try {
                    // Extract geometry from the shape on background thread
                    ShapeGeometryExtractor.ShapeGeometry geometry = geometryExtractor.extractGeometry(item);
                    
                    // Return to UI thread for callback
                    mapView.post(() -> {
                        handleGeometryResult(item, geometry);
                    });
                    
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "Out of memory processing shape " + itemUID, e);
                    mapView.post(() -> {
                        feedback.showInvalidShapeFeedback(item, "Shape too complex - insufficient memory");
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error processing selected shape " + itemUID, e);
                    mapView.post(() -> {
                        feedback.showInvalidShapeFeedback(item, "Error processing shape: " + e.getMessage());
                    });
                }
            }).start();
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting shape processing", e);
            feedback.showInvalidShapeFeedback(item, "Error starting shape processing: " + e.getMessage());
        }
    }
    
    /**
     * Handle geometry extraction result on UI thread
     */
    private void handleGeometryResult(MapItem item, ShapeGeometryExtractor.ShapeGeometry geometry) {
        if (geometry == null) {
            Log.w(TAG, "Geometry extraction returned null");
            feedback.showInvalidShapeFeedback(item, "Unable to process this shape type.");
            return;
        }
        
        if (!geometry.isValid) {
            Log.w(TAG, "Geometry extraction failed: " + geometry.validationError);
            feedback.showInvalidShapeFeedback(item, geometry.validationError != null ? 
                geometry.validationError : "Shape geometry is invalid");
            return;
        }
        
        if (geometry.points == null || geometry.points.isEmpty()) {
            Log.w(TAG, "No valid points extracted from shape");
            feedback.showInvalidShapeFeedback(item, "Unable to extract coordinates from this shape.");
            return;
        }
        
        Log.d(TAG, "Extracted " + geometry.points.size() + " points, area: " + geometry.areaSqKm + " sq km");
        
        // Additional validation for extremely small areas
        if (geometry.areaSqKm < 0.000001) { // Less than 1 sq meter
            Log.w(TAG, "Shape area too small: " + geometry.areaSqKm + " sq km");
            feedback.showInvalidShapeFeedback(item, 
                String.format("Shape area too small: %.6f sq km", geometry.areaSqKm));
            return;
        }
        
        // Check for problematic geometry
        if (geometry.metrics != null) {
            if (geometry.metrics.hasSelfIntersections) {
                Log.w(TAG, "Shape has self-intersections");
                feedback.showInvalidShapeFeedback(item, 
                    "Shape has self-intersections which may cause issues.");
                // Don't return - let user decide if they want to proceed
            }
            
            if (geometry.metrics.crossesDateLine) {
                Log.w(TAG, "Shape crosses international date line");
                // This is just a warning, proceed with processing
            }
            
            if (geometry.metrics.nearPolarRegion) {
                Log.w(TAG, "Shape is near polar region - area calculations may be approximate");
            }
        }
        
        // Show selection feedback
        feedback.showShapeSelectedFeedback(item, geometry.areaSqKm, geometry.points.size());
        
        // Stop selection mode
        stopShapeSelection();
        
        // Notify listener
        if (listener != null) {
            listener.onShapeSelected(item, geometry.points, geometry.areaSqKm);
        }
    }
    
    /**
     * Safely get MapItem type with error handling
     */
    private String safeGetItemType(MapItem item) {
        try {
            String type = item.getType();
            return type != null ? type : "unknown";
        } catch (Exception e) {
            Log.w(TAG, "Failed to get item type", e);
            return "unknown";
        }
    }
    
    /**
     * Safely get MapItem UID with error handling
     */
    private String safeGetItemUID(MapItem item) {
        try {
            String uid = item.getUID();
            return uid != null ? uid : "unknown";
        } catch (Exception e) {
            Log.w(TAG, "Failed to get item UID", e);
            return "unknown";
        }
    }
    
    
    // Tool lifecycle methods
    public boolean onToolBegin(Bundle extras) {
        Log.d(TAG, "Tool begin");
        return true;
    }
    
    public void onToolEnd() {
        Log.d(TAG, "Tool end");
        feedback.showSelectionCancelledFeedback();
        stopShapeSelection();
        if (listener != null) {
            listener.onSelectionCancelled();
        }
    }
    
    public String getIdentifier() {
        return TOOL_IDENTIFIER;
    }
    
    /**
     * Check if shape selection mode is currently active
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Cancel the current selection operation
     */
    public void cancel() {
        feedback.showSelectionCancelledFeedback();
        stopShapeSelection();
        if (listener != null) {
            listener.onSelectionCancelled();
        }
    }
}