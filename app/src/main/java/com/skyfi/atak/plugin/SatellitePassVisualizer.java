package com.skyfi.atak.plugin;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.atakmap.android.maps.MapView;
import com.atakmap.android.widgets.MapWidget;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.log.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Visualizes satellite pass predictions on the map
 */
public class SatellitePassVisualizer extends MapWidget {
    private static final String TAG = "SatellitePassVisualizer";
    
    private final MapView mapView;
    private final Paint orbitPaint;
    private final Paint footprintPaint;
    private final Paint timeLabelPaint;
    private final Paint satelliteIconPaint;
    
    private List<SatellitePass> satellitePasses = new ArrayList<>();
    private boolean showPredictions = false;
    private long animationTime = 0;
    
    public static class SatellitePass {
        public final String satelliteId;
        public final String satelliteName;
        public final long startTime;
        public final long endTime;
        public final List<GeoPoint> orbitPath;
        public final double swathWidth; // km
        public final double elevation; // degrees
        
        public SatellitePass(String id, String name, long start, long end, 
                           List<GeoPoint> path, double swath, double elev) {
            this.satelliteId = id;
            this.satelliteName = name;
            this.startTime = start;
            this.endTime = end;
            this.orbitPath = path;
            this.swathWidth = swath;
            this.elevation = elev;
        }
        
        public GeoPoint getPositionAtTime(long time) {
            if (orbitPath == null || orbitPath.isEmpty()) return null;
            
            // Interpolate position based on time
            float progress = (float)(time - startTime) / (endTime - startTime);
            progress = Math.max(0, Math.min(1, progress));
            
            int index = (int)(progress * (orbitPath.size() - 1));
            return orbitPath.get(index);
        }
    }
    
    public SatellitePassVisualizer(MapView mapView) {
        this.mapView = mapView;
        
        // Initialize orbit path paint
        orbitPaint = new Paint();
        orbitPaint.setColor(SkyFiPolygonStyle.SKYFI_SECONDARY);
        orbitPaint.setStyle(Paint.Style.STROKE);
        orbitPaint.setStrokeWidth(2f);
        orbitPaint.setAntiAlias(true);
        orbitPaint.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));
        
        // Initialize footprint paint
        footprintPaint = new Paint();
        footprintPaint.setColor(SkyFiPolygonStyle.SKYFI_PRIMARY);
        footprintPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        footprintPaint.setStrokeWidth(1f);
        footprintPaint.setAlpha(30);
        footprintPaint.setAntiAlias(true);
        
        // Initialize time label paint
        timeLabelPaint = new Paint();
        timeLabelPaint.setColor(Color.WHITE);
        timeLabelPaint.setTextSize(12f);
        timeLabelPaint.setAntiAlias(true);
        timeLabelPaint.setShadowLayer(3f, 1f, 1f, Color.BLACK);
        
        // Initialize satellite icon paint
        satelliteIconPaint = new Paint();
        satelliteIconPaint.setColor(SkyFiPolygonStyle.SKYFI_ACCENT);
        satelliteIconPaint.setStyle(Paint.Style.FILL);
        satelliteIconPaint.setAntiAlias(true);
    }
    
    public void onDraw(Canvas canvas) {
        if (!showPredictions || satellitePasses.isEmpty()) return;
        
        long currentTime = System.currentTimeMillis();
        
        for (SatellitePass pass : satellitePasses) {
            // Only draw passes that are upcoming or in progress
            if (pass.endTime > currentTime) {
                drawSatellitePass(canvas, pass, currentTime);
            }
        }
    }
    
    private void drawSatellitePass(Canvas canvas, SatellitePass pass, long currentTime) {
        // Draw orbit path
        drawOrbitPath(canvas, pass);
        
        // Draw footprint/swath
        drawFootprint(canvas, pass, currentTime);
        
        // Draw satellite position
        drawSatellitePosition(canvas, pass, currentTime);
        
        // Draw time labels
        drawTimeLabels(canvas, pass);
    }
    
    private void drawOrbitPath(Canvas canvas, SatellitePass pass) {
        if (pass.orbitPath == null || pass.orbitPath.size() < 2) return;
        
        Path path = new Path();
        boolean first = true;
        
        for (GeoPoint point : pass.orbitPath) {
            android.graphics.PointF xy = mapView.forward(point);
            if (first) {
                path.moveTo(xy.x, xy.y);
                first = false;
            } else {
                path.lineTo(xy.x, xy.y);
            }
        }
        
        // Draw with different styles based on time
        long currentTime = System.currentTimeMillis();
        if (pass.startTime > currentTime) {
            // Future pass - dashed line
            orbitPaint.setAlpha(150);
            canvas.drawPath(path, orbitPaint);
        } else if (pass.endTime > currentTime) {
            // Active pass - solid line with glow
            orbitPaint.setAlpha(255);
            orbitPaint.setPathEffect(null);
            orbitPaint.setShadowLayer(5f, 0, 0, SkyFiPolygonStyle.SKYFI_SECONDARY);
            canvas.drawPath(path, orbitPaint);
            orbitPaint.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));
            orbitPaint.clearShadowLayer();
        }
    }
    
    private void drawFootprint(Canvas canvas, SatellitePass pass, long currentTime) {
        if (pass.startTime > currentTime || pass.endTime < currentTime) return;
        
        // Get current satellite position
        GeoPoint satPos = pass.getPositionAtTime(currentTime);
        if (satPos == null) return;
        
        // Calculate footprint based on swath width
        android.graphics.PointF center = mapView.forward(satPos);
        
        // Convert swath width to screen pixels (approximate)
        double metersPerPixel = mapView.getMapScale();
        float swathPixels = (float)(pass.swathWidth * 1000 / metersPerPixel);
        
        // Draw footprint as a circle/ellipse
        RectF footprintRect = new RectF(
            center.x - swathPixels/2,
            center.y - swathPixels/2,
            center.x + swathPixels/2,
            center.y + swathPixels/2
        );
        
        // Animate footprint
        float pulseScale = 1.0f + 0.1f * (float)Math.sin(animationTime / 1000.0);
        canvas.save();
        canvas.scale(pulseScale, pulseScale, center.x, center.y);
        
        // Draw gradient effect
        Paint gradientPaint = new Paint(footprintPaint);
        gradientPaint.setShader(new android.graphics.RadialGradient(
            center.x, center.y, swathPixels/2,
            new int[]{
                adjustAlpha(SkyFiPolygonStyle.SKYFI_PRIMARY, 50),
                adjustAlpha(SkyFiPolygonStyle.SKYFI_PRIMARY, 20),
                adjustAlpha(SkyFiPolygonStyle.SKYFI_PRIMARY, 0)
            },
            new float[]{0, 0.7f, 1},
            android.graphics.Shader.TileMode.CLAMP
        ));
        
        canvas.drawOval(footprintRect, gradientPaint);
        canvas.restore();
    }
    
    private void drawSatellitePosition(Canvas canvas, SatellitePass pass, long currentTime) {
        GeoPoint satPos = null;
        
        if (currentTime >= pass.startTime && currentTime <= pass.endTime) {
            // Satellite is currently passing
            satPos = pass.getPositionAtTime(currentTime);
        } else if (currentTime < pass.startTime) {
            // Show start position for future passes
            satPos = pass.orbitPath.get(0);
        }
        
        if (satPos == null) return;
        
        android.graphics.PointF xy = mapView.forward(satPos);
        
        // Draw satellite icon (triangle pointing in direction of travel)
        Path satellitePath = new Path();
        float size = 15;
        
        // Calculate direction
        float angle = 0;
        if (pass.orbitPath.size() > 1) {
            int currentIndex = (int)((currentTime - pass.startTime) / (float)(pass.endTime - pass.startTime) * pass.orbitPath.size());
            currentIndex = Math.max(0, Math.min(currentIndex, pass.orbitPath.size() - 2));
            
            GeoPoint next = pass.orbitPath.get(currentIndex + 1);
            android.graphics.PointF nextXY = mapView.forward(next);
            angle = (float)Math.atan2(nextXY.y - xy.y, nextXY.x - xy.x);
        }
        
        // Draw satellite as directional triangle
        canvas.save();
        canvas.translate(xy.x, xy.y);
        canvas.rotate((float)Math.toDegrees(angle) + 90);
        
        satellitePath.moveTo(0, -size);
        satellitePath.lineTo(-size/2, size/2);
        satellitePath.lineTo(size/2, size/2);
        satellitePath.close();
        
        // Draw with glow effect for active satellite
        if (currentTime >= pass.startTime && currentTime <= pass.endTime) {
            satelliteIconPaint.setShadowLayer(8f, 0, 0, SkyFiPolygonStyle.SKYFI_ACCENT);
            canvas.drawPath(satellitePath, satelliteIconPaint);
            satelliteIconPaint.clearShadowLayer();
        } else {
            satelliteIconPaint.setAlpha(150);
            canvas.drawPath(satellitePath, satelliteIconPaint);
            satelliteIconPaint.setAlpha(255);
        }
        
        canvas.restore();
        
        // Draw satellite name
        String label = pass.satelliteName;
        float textWidth = timeLabelPaint.measureText(label);
        canvas.drawText(label, xy.x - textWidth/2, xy.y - size - 5, timeLabelPaint);
    }
    
    private void drawTimeLabels(Canvas canvas, SatellitePass pass) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
        
        // Draw start time
        if (pass.orbitPath.size() > 0) {
            GeoPoint startPoint = pass.orbitPath.get(0);
            android.graphics.PointF startXY = mapView.forward(startPoint);
            
            String startLabel = "Start: " + timeFormat.format(new Date(pass.startTime));
            drawTimeLabel(canvas, startXY.x, startXY.y, startLabel, true);
        }
        
        // Draw end time
        if (pass.orbitPath.size() > 0) {
            GeoPoint endPoint = pass.orbitPath.get(pass.orbitPath.size() - 1);
            android.graphics.PointF endXY = mapView.forward(endPoint);
            
            String endLabel = "End: " + timeFormat.format(new Date(pass.endTime));
            drawTimeLabel(canvas, endXY.x, endXY.y, endLabel, false);
        }
        
        // Draw elevation angle
        if (pass.orbitPath.size() > 0) {
            GeoPoint midPoint = pass.orbitPath.get(pass.orbitPath.size() / 2);
            android.graphics.PointF midXY = mapView.forward(midPoint);
            
            String elevLabel = String.format("Max Elev: %.1f°", pass.elevation);
            canvas.drawText(elevLabel, midXY.x - timeLabelPaint.measureText(elevLabel)/2, 
                          midXY.y + 20, timeLabelPaint);
        }
    }
    
    private void drawTimeLabel(Canvas canvas, float x, float y, String label, boolean isStart) {
        float textWidth = timeLabelPaint.measureText(label);
        
        // Background box
        RectF bgRect = new RectF(x - textWidth/2 - 5, y - 15, x + textWidth/2 + 5, y + 5);
        Paint bgPaint = new Paint();
        bgPaint.setColor(0xCC000000);
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(bgRect, 3, 3, bgPaint);
        
        // Border
        Paint borderPaint = new Paint();
        borderPaint.setColor(isStart ? SkyFiPolygonStyle.SKYFI_SUCCESS : SkyFiPolygonStyle.SKYFI_ERROR);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1);
        canvas.drawRoundRect(bgRect, 3, 3, borderPaint);
        
        // Text
        canvas.drawText(label, x - textWidth/2, y, timeLabelPaint);
    }
    
    public void setSatellitePasses(List<SatellitePass> passes) {
        this.satellitePasses = new ArrayList<>(passes);
        mapView.postInvalidate();
    }
    
    public void addSatellitePass(SatellitePass pass) {
        satellitePasses.add(pass);
        mapView.postInvalidate();
    }
    
    public void clearPasses() {
        satellitePasses.clear();
        mapView.postInvalidate();
    }
    
    public void setShowPredictions(boolean show) {
        this.showPredictions = show;
        mapView.postInvalidate();
    }
    
    public void updateAnimation() {
        animationTime = System.currentTimeMillis();
        if (showPredictions && !satellitePasses.isEmpty()) {
            mapView.postInvalidate();
        }
    }
    
    private int adjustAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}