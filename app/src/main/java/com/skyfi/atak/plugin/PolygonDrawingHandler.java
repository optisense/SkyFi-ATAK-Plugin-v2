package com.skyfi.atak.plugin;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.android.drawing.mapItems.DrawingShape;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles real-time feedback during polygon drawing
 */
public class PolygonDrawingHandler implements MapEventDispatcher.MapEventDispatchListener {
    private static final String LOGTAG = "PolygonDrawingHandler";
    
    private final Context context;
    private final MapView mapView;
    private LinearLayout feedbackView;
    private TextView areaText;
    private TextView warningText;
    private TextView providerCountText;
    private boolean isActive = false;
    private String currentShapeId = null;
    private Handler updateHandler = new Handler();
    private Runnable updateRunnable;
    private ValueAnimator colorAnimator;
    private int currentBackgroundColor = 0xCC2196F3;
    
    public PolygonDrawingHandler(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
    }
    
    /**
     * Start monitoring for polygon drawing
     */
    public void startMonitoring() {
        if (isActive) return;
        
        isActive = true;
        
        // Register for map events
        mapView.getMapEventDispatcher().addMapEventListener(MapEvent.ITEM_ADDED, this);
        mapView.getMapEventDispatcher().addMapEventListener(MapEvent.ITEM_REFRESH, this);
        mapView.getMapEventDispatcher().addMapEventListener(MapEvent.ITEM_REMOVED, this);
        
        // Show feedback view
        showFeedbackView();
        
        // Start periodic updates
        startPeriodicUpdates();
        
        Log.d(LOGTAG, "Started polygon drawing monitoring");
    }
    
    /**
     * Stop monitoring
     */
    public void stopMonitoring() {
        if (!isActive) return;
        
        isActive = false;
        currentShapeId = null;
        
        // Unregister from map events
        mapView.getMapEventDispatcher().removeMapEventListener(MapEvent.ITEM_ADDED, this);
        mapView.getMapEventDispatcher().removeMapEventListener(MapEvent.ITEM_REFRESH, this);
        mapView.getMapEventDispatcher().removeMapEventListener(MapEvent.ITEM_REMOVED, this);
        
        // Hide feedback view
        hideFeedbackView();
        
        // Stop periodic updates
        stopPeriodicUpdates();
        
        Log.d(LOGTAG, "Stopped polygon drawing monitoring");
    }
    
    @Override
    public void onMapEvent(MapEvent event) {
        if (!isActive) return;
        
        MapItem item = event.getItem();
        if (item instanceof DrawingShape) {
            DrawingShape shape = (DrawingShape) item;
            
            if (event.getType().equals(MapEvent.ITEM_ADDED)) {
                // New shape being drawn
                if (shape.getNumPoints() >= 2) {
                    currentShapeId = shape.getUID();
                    updateFeedback(shape);
                }
            } else if (event.getType().equals(MapEvent.ITEM_REFRESH)) {
                // Shape being updated
                if (shape.getUID().equals(currentShapeId)) {
                    updateFeedback(shape);
                }
            } else if (event.getType().equals(MapEvent.ITEM_REMOVED)) {
                // Shape removed
                if (shape.getUID().equals(currentShapeId)) {
                    currentShapeId = null;
                    updateFeedbackEmpty();
                }
            }
        }
    }
    
    /**
     * Start periodic updates to catch shape changes
     */
    private void startPeriodicUpdates() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isActive && currentShapeId != null) {
                    // Find and update current shape
                    mapView.getRootGroup().deepForEachItem(item -> {
                        if (item instanceof DrawingShape && item.getUID().equals(currentShapeId)) {
                            updateFeedback((DrawingShape) item);
                        }
                        return false; // Continue iteration
                    });
                }
                
                if (isActive) {
                    updateHandler.postDelayed(this, 500); // Update every 500ms
                }
            }
        };
        updateHandler.post(updateRunnable);
    }
    
    /**
     * Stop periodic updates
     */
    private void stopPeriodicUpdates() {
        if (updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
            updateRunnable = null;
        }
    }
    
    /**
     * Update feedback based on current shape
     */
    private void updateFeedback(DrawingShape shape) {
        if (shape.getNumPoints() < 3) {
            updateFeedbackEmpty();
            return;
        }
        
        try {
            // Calculate current area
            GeoPoint[] points = shape.getPoints();
            double areaKm2 = calculatePolygonArea(points);
            
            // Get validation results
            AOISizeValidator.ValidationResult validation = AOISizeValidator.validateAOISize(areaKm2);
            
            // Update area text with more detailed information
            areaText.setText(String.format("Area: %s (%d points)", 
                AOISizeValidator.formatArea(areaKm2), points.length));
            
            // Update warning with enhanced messaging
            if (areaKm2 > 2000) {
                warningText.setText("⚠️ Area exceeds 2000 km² limit");
                warningText.setTextColor(0xFFFF0000);
            } else if (validation.warningLevel == 2) {
                warningText.setText("⚠️ Area too small for most providers");
                warningText.setTextColor(0xFFFF5722);
            } else if (validation.warningLevel == 1) {
                warningText.setText("⚠️ Limited provider compatibility");
                warningText.setTextColor(0xFFFF9800);
            } else {
                warningText.setText("✓ Good size for satellite tasking");
                warningText.setTextColor(0xFF4CAF50);
            }
            
            // Update provider count with more detail
            long compatibleCount = validation.providerResults.stream()
                .filter(p -> p.isCompatible)
                .count();
            providerCountText.setText(String.format("%d of %d providers compatible • Tap to complete", 
                compatibleCount, validation.providerResults.size()));
            
            // Update background color based on status with animation
            int targetColor;
            if (areaKm2 > 2000) {
                targetColor = 0xCCFF0000; // Red for too large
            } else if (validation.warningLevel == 2) {
                targetColor = 0xCCFF5722; // Deep orange for too small
            } else if (validation.warningLevel == 1) {
                targetColor = 0xCCFF9800; // Orange for warning
            } else {
                targetColor = 0xCC4CAF50; // Green for good
            }
            
            animateBackgroundColor(targetColor);
            
            // Check if polygon is effectively complete (closed)
            if (isEffectivelyComplete(points)) {
                // Add pulse animation to indicate completion readiness
                addCompletionIndicator();
            }
            
        } catch (Exception e) {
            Log.e(LOGTAG, "Error updating feedback", e);
            updateFeedbackEmpty();
        }
    }
    
    /**
     * Update feedback when no valid polygon
     */
    private void updateFeedbackEmpty() {
        areaText.setText("Draw at least 3 points");
        warningText.setText("Tap on map to add points");
        providerCountText.setText("");
        feedbackView.setBackgroundColor(0xCC2196F3); // Blue for info
    }
    
    /**
     * Show the feedback view
     */
    private void showFeedbackView() {
        mapView.post(() -> {
            try {
                if (feedbackView == null) {
                    // Create feedback view
                    feedbackView = new LinearLayout(mapView.getContext());
                    feedbackView.setOrientation(LinearLayout.VERTICAL);
                    feedbackView.setBackgroundColor(0xCC2196F3);
                    feedbackView.setPadding(30, 20, 30, 20);
                    
                    // Area text
                    areaText = new TextView(mapView.getContext());
                    areaText.setTextColor(Color.WHITE);
                    areaText.setTextSize(18);
                    areaText.setText("Draw at least 3 points");
                    feedbackView.addView(areaText);
                    
                    // Warning text
                    warningText = new TextView(mapView.getContext());
                    warningText.setTextColor(Color.WHITE);
                    warningText.setTextSize(14);
                    warningText.setText("Tap on map to add points");
                    feedbackView.addView(warningText);
                    
                    // Provider count
                    providerCountText = new TextView(mapView.getContext());
                    providerCountText.setTextColor(Color.WHITE);
                    providerCountText.setTextSize(12);
                    feedbackView.addView(providerCountText);
                }
                
                // Add to map view
                if (feedbackView.getParent() == null) {
                    android.widget.FrameLayout.LayoutParams params = 
                        new android.widget.FrameLayout.LayoutParams(
                            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
                        );
                    params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    params.bottomMargin = 150;
                    
                    if (mapView instanceof android.view.ViewGroup) {
                        ((android.view.ViewGroup) mapView).addView(feedbackView, params);
                    }
                }
            } catch (Exception e) {
                Log.e(LOGTAG, "Error showing feedback view", e);
            }
        });
    }
    
    /**
     * Hide the feedback view
     */
    private void hideFeedbackView() {
        if (feedbackView != null && feedbackView.getParent() != null) {
            mapView.post(() -> {
                try {
                    ((android.view.ViewGroup) feedbackView.getParent()).removeView(feedbackView);
                } catch (Exception e) {
                    Log.e(LOGTAG, "Error hiding feedback view", e);
                }
            });
        }
    }
    
    /**
     * Calculate polygon area in km²
     */
    private double calculatePolygonArea(GeoPoint[] points) {
        if (points == null || points.length < 3) {
            return 0.0;
        }
        
        // Use shoelace formula
        double area = 0.0;
        int n = points.length;
        
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            area += points[i].getLongitude() * points[j].getLatitude();
            area -= points[j].getLongitude() * points[i].getLatitude();
        }
        
        area = Math.abs(area) / 2.0;
        
        // Convert from square degrees to square kilometers
        double avgLat = 0;
        for (GeoPoint p : points) {
            avgLat += p.getLatitude();
        }
        avgLat /= points.length;
        
        double metersPerDegreeLat = 111320.0;
        double metersPerDegreeLon = 111320.0 * Math.cos(Math.toRadians(avgLat));
        
        return area * (metersPerDegreeLat * metersPerDegreeLon) / 1000000.0;
    }
    
    /**
     * Animate background color change
     */
    private void animateBackgroundColor(int targetColor) {
        if (colorAnimator != null && colorAnimator.isRunning()) {
            colorAnimator.cancel();
        }
        
        colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), currentBackgroundColor, targetColor);
        colorAnimator.setDuration(300);
        colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        colorAnimator.addUpdateListener(animation -> {
            int animatedColor = (int) animation.getAnimatedValue();
            if (feedbackView != null) {
                feedbackView.setBackgroundColor(animatedColor);
            }
        });
        
        colorAnimator.start();
        currentBackgroundColor = targetColor;
    }
    
    /**
     * Check if polygon is effectively complete (closed or near-closed)
     */
    private boolean isEffectivelyComplete(GeoPoint[] points) {
        if (points == null || points.length < 3) return false;
        
        GeoPoint first = points[0];
        GeoPoint last = points[points.length - 1];
        
        // Consider closed if points are very close (within 50 meters)
        double distance = first.distanceTo(last);
        return distance < 50.0;
    }
    
    /**
     * Add visual completion indicator
     */
    private void addCompletionIndicator() {
        if (feedbackView != null) {
            // Add subtle pulse animation to the feedback view
            feedbackView.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(200)
                .withEndAction(() -> {
                    feedbackView.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(200);
                });
        }
    }
    
    /**
     * Get current drawing session info
     */
    public boolean isActive() {
        return isActive;
    }
    
    public String getCurrentShapeId() {
        return currentShapeId;
    }
}