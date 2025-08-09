package com.skyfi.atak.plugin;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Shape;
import com.atakmap.coremap.maps.coords.GeoPoint;

/**
 * Provides visual feedback and user guidance during shape selection mode
 * Shows instruction overlays, highlights selected shapes, and provides status updates
 */
public class ShapeSelectionFeedback {
    private static final String TAG = "SkyFi.ShapeFeedback";
    
    private final Context context;
    private final MapView mapView;
    private TextView instructionOverlay;
    private Toast currentToast;
    private MapItem highlightedShape;
    
    public ShapeSelectionFeedback(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
    }
    
    /**
     * Show initial instruction overlay when entering shape selection mode
     */
    public void showSelectionModeInstructions() {
        Log.d(TAG, "Showing selection mode instructions");
        
        showPersistentToast("Shape Selection Mode Active\n" +
            "• Tap any shape to convert to AOI\n" +
            "• Tap empty area to cancel\n" +
            "• Supported: Polygons, Circles, Rectangles", Toast.LENGTH_LONG);
    }
    
    /**
     * Show feedback when hovering over or near a valid shape
     */
    public void showShapeHoverFeedback(MapItem shape) {
        if (shape == null) return;
        
        Log.d(TAG, "Showing hover feedback for shape: " + shape.getType());
        
        String shapeInfo = getShapeInfo(shape);
        showQuickToast("Shape: " + shapeInfo + "\nTap to select", Toast.LENGTH_SHORT);
        
        highlightShape(shape);
    }
    
    /**
     * Show feedback when a shape is successfully selected
     */
    public void showShapeSelectedFeedback(MapItem shape, double areaSqKm, int pointCount) {
        Log.d(TAG, "Showing selection feedback for: " + shape.getType());
        
        String message = String.format("Selected: %s\nArea: %.2f sq km\nPoints: %d", 
            getShapeInfo(shape), areaSqKm, pointCount);
            
        showQuickToast(message, Toast.LENGTH_SHORT);
        
        // Flash the shape briefly to indicate selection
        flashShape(shape);
    }
    
    /**
     * Show feedback when shape selection is cancelled
     */
    public void showSelectionCancelledFeedback() {
        Log.d(TAG, "Showing cancellation feedback");
        showQuickToast("Shape selection cancelled", Toast.LENGTH_SHORT);
        clearHighlight();
        hideInstructions();
    }
    
    /**
     * Show feedback for invalid or unsupported shapes
     */
    public void showInvalidShapeFeedback(MapItem shape, String reason) {
        Log.d(TAG, "Showing invalid shape feedback: " + reason);
        
        String message = String.format("Cannot select %s:\n%s", 
            getShapeInfo(shape), reason);
            
        showQuickToast(message, Toast.LENGTH_LONG);
    }
    
    /**
     * Show feedback when area is too small
     */
    public void showAreaTooSmallFeedback(double areaSqKm, double minAreaSqKm) {
        Log.d(TAG, "Showing area too small feedback");
        
        String message = String.format("Area Warning:\nSelected: %.2f sq km\nMinimum: %.2f sq km", 
            areaSqKm, minAreaSqKm);
            
        showQuickToast(message, Toast.LENGTH_LONG);
    }
    
    /**
     * Show processing feedback
     */
    public void showProcessingFeedback(String message) {
        Log.d(TAG, "Showing processing feedback: " + message);
        showQuickToast("Processing: " + message, Toast.LENGTH_SHORT);
    }
    
    /**
     * Get readable information about a shape
     */
    private String getShapeInfo(MapItem shape) {
        if (shape == null) return "Unknown";
        
        String type = shape.getType();
        String title = shape.getTitle();
        
        if (title != null && !title.isEmpty()) {
            return title + " (" + type + ")";
        }
        
        // Make type more readable
        String readableType = type;
        if (type.contains("drawing")) {
            if (type.contains("circle")) {
                readableType = "Circle";
            } else if (type.contains("rectangle")) {
                readableType = "Rectangle";
            } else if (type.contains("polygon")) {
                readableType = "Polygon";
            } else {
                readableType = "Drawing";
            }
        } else if (type.contains("shape")) {
            readableType = "Shape";
        }
        
        return readableType;
    }
    
    /**
     * Highlight a shape on the map
     */
    private void highlightShape(MapItem shape) {
        // Clear previous highlight
        clearHighlight();
        
        if (shape instanceof Shape) {
            Shape s = (Shape) shape;
            
            // Store original stroke color to restore later
            int originalStroke = s.getStrokeColor();
            shape.setMetaInteger("skyfi_original_stroke", originalStroke);
            
            // Set highlight color (bright cyan)
            s.setStrokeColor(Color.CYAN);
            s.setStrokeWeight(s.getStrokeWeight() + 2); // Make thicker
            
            highlightedShape = shape;
            
            Log.d(TAG, "Shape highlighted: " + shape.getUID());
        }
    }
    
    /**
     * Flash a shape briefly to indicate selection
     */
    private void flashShape(MapItem shape) {
        if (!(shape instanceof Shape)) return;
        
        Shape s = (Shape) shape;
        int originalColor = s.getFillColor();
        
        // Flash with semi-transparent green
        s.setFillColor(Color.argb(100, 0, 255, 0));
        
        // Restore original color after delay
        mapView.postDelayed(() -> {
            s.setFillColor(originalColor);
        }, 500);
    }
    
    /**
     * Clear any shape highlighting
     */
    private void clearHighlight() {
        if (highlightedShape != null && highlightedShape instanceof Shape) {
            Shape s = (Shape) highlightedShape;
            
            // Restore original stroke color if we saved it
            if (highlightedShape.hasMetaValue("skyfi_original_stroke")) {
                int originalStroke = highlightedShape.getMetaInteger("skyfi_original_stroke", Color.WHITE);
                s.setStrokeColor(originalStroke);
                s.setStrokeWeight(Math.max(1, s.getStrokeWeight() - 2)); // Restore thickness
                highlightedShape.removeMetaData("skyfi_original_stroke");
            }
            
            Log.d(TAG, "Shape highlight cleared: " + highlightedShape.getUID());
            highlightedShape = null;
        }
    }
    
    /**
     * Show a persistent toast message
     */
    private void showPersistentToast(String message, int duration) {
        hidePersistentToast(); // Hide any existing toast
        
        currentToast = Toast.makeText(context, message, duration);
        currentToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        currentToast.show();
    }
    
    /**
     * Show a quick toast message
     */
    private void showQuickToast(String message, int duration) {
        Toast toast = Toast.makeText(context, message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    
    /**
     * Hide persistent toast
     */
    private void hidePersistentToast() {
        if (currentToast != null) {
            currentToast.cancel();
            currentToast = null;
        }
    }
    
    /**
     * Hide instruction overlay
     */
    private void hideInstructions() {
        hidePersistentToast();
        
        if (instructionOverlay != null && instructionOverlay.getParent() != null) {
            ((LinearLayout) instructionOverlay.getParent()).removeView(instructionOverlay);
            instructionOverlay = null;
        }
    }
    
    /**
     * Show count of valid shapes in current view
     */
    public void showValidShapeCount(int count) {
        String message;
        if (count == 0) {
            message = "No compatible shapes found in current view.\nDraw shapes using ATAK tools first.";
        } else {
            message = String.format("Found %d compatible shape%s in view", count, count == 1 ? "" : "s");
        }
        
        showQuickToast(message, Toast.LENGTH_LONG);
    }
    
    /**
     * Cleanup all visual feedback elements
     */
    public void cleanup() {
        Log.d(TAG, "Cleaning up visual feedback");
        
        clearHighlight();
        hideInstructions();
        hidePersistentToast();
    }
    
    /**
     * Check if feedback is currently active
     */
    public boolean isActive() {
        return currentToast != null || instructionOverlay != null || highlightedShape != null;
    }
}