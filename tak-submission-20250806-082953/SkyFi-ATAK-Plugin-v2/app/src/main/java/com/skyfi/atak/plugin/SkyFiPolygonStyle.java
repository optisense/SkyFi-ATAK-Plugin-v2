package com.skyfi.atak.plugin;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.atakmap.android.drawing.mapItems.DrawingShape;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.Shape;
import com.atakmap.coremap.log.Log;

/**
 * Manages custom polygon styling for SkyFi with animations and visual effects
 */
public class SkyFiPolygonStyle {
    private static final String TAG = "SkyFiPolygonStyle";
    
    // SkyFi color scheme
    public static final int SKYFI_PRIMARY = 0xFF0066FF;
    public static final int SKYFI_SECONDARY = 0xFF00D4FF;
    public static final int SKYFI_ACCENT = 0xFFFF6B35;
    public static final int SKYFI_SUCCESS = 0xFF4CAF50;
    public static final int SKYFI_WARNING = 0xFFFF9800;
    public static final int SKYFI_ERROR = 0xFFF44336;
    
    // Style constants
    private static final float DEFAULT_STROKE_WIDTH = 3.0f;
    private static final float SELECTED_STROKE_WIDTH = 5.0f;
    private static final float ANIMATED_STROKE_WIDTH = 7.0f;
    
    private static final int DEFAULT_FILL_ALPHA = 30;
    private static final int SELECTED_FILL_ALPHA = 50;
    private static final int TASKED_FILL_ALPHA = 40;
    
    // User-configurable opacity (0-255)
    private static int userOpacity = DEFAULT_FILL_ALPHA;
    
    /**
     * Apply default SkyFi styling to a shape
     */
    public static void applyDefaultStyle(Shape shape) {
        if (shape == null) return;
        
        shape.setStrokeColor(SKYFI_PRIMARY);
        shape.setFillColor(adjustAlpha(SKYFI_PRIMARY, DEFAULT_FILL_ALPHA));
        shape.setStrokeWeight(DEFAULT_STROKE_WIDTH);
        shape.setClickable(true);
        // shape.setTouchable(true); // Method may not be available in all Shape implementations
        
        // Add metadata for SkyFi
        shape.setMetaString("skyfi_styled", "true");
        shape.setMetaString("skyfi_style_type", "default");
    }
    
    /**
     * Apply selected/active styling with animation
     */
    public static void applySelectedStyle(Shape shape, boolean animate) {
        if (shape == null) return;
        
        if (animate) {
            animateToSelectedStyle(shape);
        } else {
            shape.setStrokeColor(SKYFI_SECONDARY);
            shape.setFillColor(adjustAlpha(SKYFI_SECONDARY, SELECTED_FILL_ALPHA));
            shape.setStrokeWeight(SELECTED_STROKE_WIDTH);
        }
        
        shape.setMetaString("skyfi_style_type", "selected");
    }
    
    /**
     * Apply tasked area styling
     */
    public static void applyTaskedStyle(Shape shape, String status) {
        if (shape == null) return;
        
        int strokeColor;
        int fillColor;
        
        switch (status.toLowerCase()) {
            case "pending":
                strokeColor = SKYFI_WARNING;
                fillColor = adjustAlpha(SKYFI_WARNING, TASKED_FILL_ALPHA);
                break;
            case "completed":
                strokeColor = SKYFI_SUCCESS;
                fillColor = adjustAlpha(SKYFI_SUCCESS, TASKED_FILL_ALPHA);
                break;
            case "failed":
                strokeColor = SKYFI_ERROR;
                fillColor = adjustAlpha(SKYFI_ERROR, TASKED_FILL_ALPHA);
                break;
            default:
                strokeColor = SKYFI_ACCENT;
                fillColor = adjustAlpha(SKYFI_ACCENT, TASKED_FILL_ALPHA);
                break;
        }
        
        shape.setStrokeColor(strokeColor);
        shape.setFillColor(fillColor);
        shape.setStrokeWeight(DEFAULT_STROKE_WIDTH);
        
        shape.setMetaString("skyfi_style_type", "tasked");
        shape.setMetaString("skyfi_task_status", status);
    }
    
    /**
     * Apply drawing mode styling (while user is creating polygon)
     */
    public static void applyDrawingStyle(Shape shape) {
        if (shape == null) return;
        
        shape.setStrokeColor(SKYFI_SECONDARY);
        shape.setFillColor(adjustAlpha(SKYFI_PRIMARY, userOpacity));
        shape.setStrokeWeight(DEFAULT_STROKE_WIDTH);
        shape.setStyle(Shape.STYLE_STROKE_MASK | Shape.STYLE_FILLED_MASK);
        
        shape.setMetaString("skyfi_style_type", "drawing");
        shape.setMetaInteger("skyfi_user_opacity", userOpacity);
    }
    
    /**
     * Apply area-based styling (changes color based on area size)
     */
    public static void applyAreaBasedStyle(Shape shape, double areaKm2) {
        if (shape == null) return;
        
        int strokeColor;
        int fillColor;
        
        if (areaKm2 > 2000) {
            // Too large
            strokeColor = SKYFI_ERROR;
            fillColor = adjustAlpha(SKYFI_ERROR, 30);
        } else if (areaKm2 < 1) {
            // Too small
            strokeColor = SKYFI_WARNING;
            fillColor = adjustAlpha(SKYFI_WARNING, 30);
        } else if (areaKm2 < 10) {
            // Small but valid
            strokeColor = SKYFI_SECONDARY;
            fillColor = adjustAlpha(SKYFI_SECONDARY, DEFAULT_FILL_ALPHA);
        } else {
            // Normal size
            strokeColor = SKYFI_PRIMARY;
            fillColor = adjustAlpha(SKYFI_PRIMARY, DEFAULT_FILL_ALPHA);
        }
        
        shape.setStrokeColor(strokeColor);
        shape.setFillColor(fillColor);
        shape.setStrokeWeight(DEFAULT_STROKE_WIDTH);
        
        shape.setMetaString("skyfi_style_type", "area_based");
        shape.setMetaDouble("skyfi_area_km2", areaKm2);
    }
    
    /**
     * Animate selection effect
     */
    private static void animateToSelectedStyle(final Shape shape) {
        ValueAnimator colorAnimator = ValueAnimator.ofObject(
            new ArgbEvaluator(), 
            SKYFI_PRIMARY, 
            SKYFI_SECONDARY
        );
        
        colorAnimator.setDuration(500);
        colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        colorAnimator.addUpdateListener(animation -> {
            int animatedColor = (int) animation.getAnimatedValue();
            shape.setStrokeColor(animatedColor);
            shape.setFillColor(adjustAlpha(animatedColor, SELECTED_FILL_ALPHA));
        });
        
        // Animate stroke width
        ValueAnimator strokeAnimator = ValueAnimator.ofFloat(
            DEFAULT_STROKE_WIDTH, 
            ANIMATED_STROKE_WIDTH, 
            SELECTED_STROKE_WIDTH
        );
        
        strokeAnimator.setDuration(500);
        strokeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        strokeAnimator.addUpdateListener(animation -> {
            float animatedWidth = (float) animation.getAnimatedValue();
            shape.setStrokeWeight(animatedWidth);
        });
        
        colorAnimator.start();
        strokeAnimator.start();
    }
    
    /**
     * Apply pulse animation for attention
     */
    public static void applyPulseAnimation(final Shape shape) {
        if (shape == null) return;
        
        final int originalColor = shape.getStrokeColor();
        final double originalWidth = shape.getStrokeWeight();
        
        ValueAnimator pulseAnimator = ValueAnimator.ofFloat(0, 1, 0);
        pulseAnimator.setDuration(1000);
        pulseAnimator.setRepeatCount(2);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        pulseAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            
            // Interpolate between original and accent color
            int pulseColor = interpolateColor(originalColor, SKYFI_ACCENT, fraction);
            shape.setStrokeColor(pulseColor);
            
            // Pulse stroke width
            float pulseWidth = (float)(originalWidth + (fraction * 4));
            shape.setStrokeWeight(pulseWidth);
        });
        
        pulseAnimator.start();
    }
    
    /**
     * Apply hover effect styling
     */
    public static void applyHoverStyle(Shape shape) {
        if (shape == null) return;
        
        // Brighten the current color
        int currentColor = shape.getStrokeColor();
        int hoverColor = brightenColor(currentColor, 1.3f);
        
        shape.setStrokeColor(hoverColor);
        shape.setStrokeWeight(shape.getStrokeWeight() + 1);
    }
    
    /**
     * Reset to default style
     */
    public static void resetStyle(Shape shape) {
        if (shape == null) return;
        
        String styleType = shape.getMetaString("skyfi_style_type", "default");
        
        switch (styleType) {
            case "selected":
                applySelectedStyle(shape, false);
                break;
            case "tasked":
                String status = shape.getMetaString("skyfi_task_status", "active");
                applyTaskedStyle(shape, status);
                break;
            case "area_based":
                double area = shape.getMetaDouble("skyfi_area_km2", 0);
                applyAreaBasedStyle(shape, area);
                break;
            default:
                applyDefaultStyle(shape);
                break;
        }
    }
    
    /**
     * Helper to adjust alpha of a color
     */
    private static int adjustAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
    
    /**
     * Helper to interpolate between two colors
     */
    private static int interpolateColor(int startColor, int endColor, float fraction) {
        int startA = Color.alpha(startColor);
        int startR = Color.red(startColor);
        int startG = Color.green(startColor);
        int startB = Color.blue(startColor);
        
        int endA = Color.alpha(endColor);
        int endR = Color.red(endColor);
        int endG = Color.green(endColor);
        int endB = Color.blue(endColor);
        
        return Color.argb(
            (int)(startA + (endA - startA) * fraction),
            (int)(startR + (endR - startR) * fraction),
            (int)(startG + (endG - startG) * fraction),
            (int)(startB + (endB - startB) * fraction)
        );
    }
    
    /**
     * Helper to brighten a color
     */
    private static int brightenColor(int color, float factor) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        
        return Color.argb(
            Color.alpha(color),
            Math.min(255, (int)(r * factor)),
            Math.min(255, (int)(g * factor)),
            Math.min(255, (int)(b * factor))
        );
    }
    
    /**
     * Check if a shape has SkyFi styling
     */
    public static boolean isSkyFiStyled(MapItem item) {
        return item != null && "true".equals(item.getMetaString("skyfi_styled", "false"));
    }
    
    /**
     * Get style type of a shape
     */
    public static String getStyleType(MapItem item) {
        return item != null ? item.getMetaString("skyfi_style_type", "none") : "none";
    }
    
    /**
     * Set user-defined opacity for polygon fills
     * @param opacity Value between 0-255 (0 = transparent, 255 = opaque)
     */
    public static void setUserOpacity(int opacity) {
        userOpacity = Math.max(0, Math.min(255, opacity));
        Log.d(TAG, "User opacity set to: " + userOpacity);
    }
    
    /**
     * Get current user-defined opacity
     * @return Current opacity value (0-255)
     */
    public static int getUserOpacity() {
        return userOpacity;
    }
    
    /**
     * Apply default style with user-defined opacity
     */
    public static void applyDefaultStyleWithUserOpacity(Shape shape) {
        if (shape == null) return;
        
        shape.setStrokeColor(SKYFI_PRIMARY);
        shape.setFillColor(adjustAlpha(SKYFI_PRIMARY, userOpacity));
        shape.setStrokeWeight(DEFAULT_STROKE_WIDTH);
        shape.setClickable(true);
        
        shape.setMetaString("skyfi_styled", "true");
        shape.setMetaString("skyfi_style_type", "default");
        shape.setMetaInteger("skyfi_user_opacity", userOpacity);
    }
}