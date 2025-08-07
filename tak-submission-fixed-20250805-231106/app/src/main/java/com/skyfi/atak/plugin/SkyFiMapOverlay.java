package com.skyfi.atak.plugin;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.atakmap.android.maps.MapView;
import com.atakmap.android.widgets.MapWidget;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;
import com.atakmap.map.layer.control.SurfaceRendererControl;
import com.atakmap.coremap.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom map overlay for SkyFi satellite imagery and visual indicators
 */
public class SkyFiMapOverlay extends MapWidget {
    private static final String TAG = "SkyFiMapOverlay";
    
    // SkyFi colors
    private static final int SKYFI_PRIMARY = 0xFF0066FF;
    private static final int SKYFI_SECONDARY = 0xFF00D4FF;
    private static final int SKYFI_ACCENT = 0xFFFF6B35;
    
    private final MapView mapView;
    private final Paint polygonPaint;
    private final Paint selectedPaint;
    private final Paint taskedPaint;
    private final Paint textPaint;
    private final Paint areaPaint;
    
    private List<TaskedArea> taskedAreas = new ArrayList<>();
    private GeoPoint[] currentPolygon = null;
    private boolean isDrawingMode = false;
    private float opacity = 1.0f;
    
    public static class TaskedArea {
        public final String id;
        public final GeoPoint[] points;
        public final double areaKm2;
        public final long timestamp;
        public final String status;
        
        public TaskedArea(String id, GeoPoint[] points, double areaKm2, String status) {
            this.id = id;
            this.points = points;
            this.areaKm2 = areaKm2;
            this.timestamp = System.currentTimeMillis();
            this.status = status;
        }
    }
    
    public SkyFiMapOverlay(MapView mapView) {
        this.mapView = mapView;
        
        // Initialize polygon paint with SkyFi primary color
        polygonPaint = new Paint();
        polygonPaint.setColor(SKYFI_PRIMARY);
        polygonPaint.setStyle(Paint.Style.STROKE);
        polygonPaint.setStrokeWidth(3f);
        polygonPaint.setAntiAlias(true);
        
        // Selected polygon paint with glow effect
        selectedPaint = new Paint();
        selectedPaint.setColor(SKYFI_SECONDARY);
        selectedPaint.setStyle(Paint.Style.STROKE);
        selectedPaint.setStrokeWidth(5f);
        selectedPaint.setAntiAlias(true);
        selectedPaint.setShadowLayer(10f, 0, 0, SKYFI_SECONDARY);
        
        // Tasked area paint with fill
        taskedPaint = new Paint();
        taskedPaint.setColor(SKYFI_ACCENT);
        taskedPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        taskedPaint.setStrokeWidth(2f);
        taskedPaint.setAlpha(50); // Semi-transparent fill
        taskedPaint.setAntiAlias(true);
        
        // Text paint for labels
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(14f);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setAntiAlias(true);
        textPaint.setShadowLayer(3f, 1f, 1f, Color.BLACK);
        
        // Area display paint
        areaPaint = new Paint();
        areaPaint.setColor(SKYFI_ACCENT);
        areaPaint.setTextSize(18f);
        areaPaint.setTypeface(Typeface.DEFAULT_BOLD);
        areaPaint.setAntiAlias(true);
        areaPaint.setShadowLayer(4f, 2f, 2f, Color.BLACK);
    }
    
    public void onDraw(Canvas canvas) {
        // Draw tasked areas
        for (TaskedArea area : taskedAreas) {
            drawTaskedArea(canvas, area);
        }
        
        // Draw current polygon if in drawing mode
        if (isDrawingMode && currentPolygon != null && currentPolygon.length > 0) {
            drawCurrentPolygon(canvas);
        }
    }
    
    private void drawTaskedArea(Canvas canvas, TaskedArea area) {
        if (area.points == null || area.points.length < 3) return;
        
        Path path = new Path();
        boolean first = true;
        
        for (GeoPoint point : area.points) {
            PointF xy = mapView.forward(point);
            if (first) {
                path.moveTo(xy.x, xy.y);
                first = false;
            } else {
                path.lineTo(xy.x, xy.y);
            }
        }
        path.close();
        
        // Draw fill
        Paint fillPaint = new Paint(taskedPaint);
        fillPaint.setAlpha((int)(50 * opacity));
        canvas.drawPath(path, fillPaint);
        
        // Draw outline
        Paint outlinePaint = new Paint(taskedPaint);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setAlpha((int)(255 * opacity));
        canvas.drawPath(path, outlinePaint);
        
        // Draw status label at centroid
        GeoPoint centroid = calculateCentroid(area.points);
        if (centroid != null) {
            PointF xy = mapView.forward(centroid);
            
            // Draw background for text
            String label = String.format("%.1f km² - %s", area.areaKm2, area.status);
            float textWidth = textPaint.measureText(label);
            RectF bgRect = new RectF(xy.x - textWidth/2 - 10, 
                                     xy.y - 20, 
                                     xy.x + textWidth/2 + 10, 
                                     xy.y + 5);
            
            Paint bgPaint = new Paint();
            bgPaint.setColor(Color.BLACK);
            bgPaint.setAlpha(180);
            bgPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(bgRect, 5, 5, bgPaint);
            
            // Draw text
            canvas.drawText(label, xy.x - textWidth/2, xy.y, textPaint);
        }
    }
    
    private void drawCurrentPolygon(Canvas canvas) {
        if (currentPolygon.length < 2) return;
        
        Path path = new Path();
        boolean first = true;
        
        for (GeoPoint point : currentPolygon) {
            PointF xy = mapView.forward(point);
            if (first) {
                path.moveTo(xy.x, xy.y);
                first = false;
            } else {
                path.lineTo(xy.x, xy.y);
            }
        }
        
        // Close path if we have at least 3 points
        if (currentPolygon.length >= 3) {
            path.close();
            
            // Draw semi-transparent fill
            Paint fillPaint = new Paint();
            fillPaint.setColor(SKYFI_PRIMARY);
            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setAlpha(30);
            canvas.drawPath(path, fillPaint);
        }
        
        // Draw outline with animation effect
        Paint animatedPaint = new Paint(selectedPaint);
        animatedPaint.setPathEffect(createAnimatedPathEffect());
        canvas.drawPath(path, animatedPaint);
        
        // Draw area calculation if polygon is closed
        if (currentPolygon.length >= 3) {
            double area = calculatePolygonArea(currentPolygon);
            GeoPoint centroid = calculateCentroid(currentPolygon);
            if (centroid != null) {
                PointF xy = mapView.forward(centroid);
                String areaText = String.format("%.2f km²", area);
                float textWidth = areaPaint.measureText(areaText);
                canvas.drawText(areaText, xy.x - textWidth/2, xy.y, areaPaint);
            }
        }
        
        // Draw vertex markers
        Paint vertexPaint = new Paint();
        vertexPaint.setColor(SKYFI_SECONDARY);
        vertexPaint.setStyle(Paint.Style.FILL);
        vertexPaint.setAntiAlias(true);
        
        for (int i = 0; i < currentPolygon.length; i++) {
            PointF xy = mapView.forward(currentPolygon[i]);
            canvas.drawCircle(xy.x, xy.y, 8f, vertexPaint);
            
            // Draw vertex number
            Paint numberPaint = new Paint(textPaint);
            numberPaint.setTextSize(12f);
            canvas.drawText(String.valueOf(i + 1), xy.x - 4, xy.y + 4, numberPaint);
        }
    }
    
    private android.graphics.PathEffect createAnimatedPathEffect() {
        float phase = (System.currentTimeMillis() % 1000) / 50f;
        return new android.graphics.DashPathEffect(new float[]{20, 10}, phase);
    }
    
    private GeoPoint calculateCentroid(GeoPoint[] points) {
        if (points == null || points.length == 0) return null;
        
        double sumLat = 0, sumLon = 0;
        for (GeoPoint point : points) {
            sumLat += point.getLatitude();
            sumLon += point.getLongitude();
        }
        
        return new GeoPoint(sumLat / points.length, sumLon / points.length);
    }
    
    private double calculatePolygonArea(GeoPoint[] points) {
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
        for (GeoPoint p : points) {
            avgLat += p.getLatitude();
        }
        avgLat /= points.length;
        
        double metersPerDegreeLat = 111320.0;
        double metersPerDegreeLon = 111320.0 * Math.cos(Math.toRadians(avgLat));
        
        return area * (metersPerDegreeLat * metersPerDegreeLon) / 1000000.0;
    }
    
    public void setDrawingMode(boolean enabled) {
        this.isDrawingMode = enabled;
        if (!enabled) {
            currentPolygon = null;
        }
        mapView.postInvalidate();
    }
    
    public void updateCurrentPolygon(GeoPoint[] points) {
        this.currentPolygon = points;
        mapView.postInvalidate();
    }
    
    public void addTaskedArea(TaskedArea area) {
        taskedAreas.add(area);
        mapView.postInvalidate();
    }
    
    public void removeTaskedArea(String id) {
        taskedAreas.removeIf(area -> area.id.equals(id));
        mapView.postInvalidate();
    }
    
    public void setOpacity(float opacity) {
        this.opacity = Math.max(0.2f, Math.min(1.0f, opacity));
        mapView.postInvalidate();
    }
    
    public float getOpacity() {
        return opacity;
    }
    
    public void clearAll() {
        taskedAreas.clear();
        currentPolygon = null;
        mapView.postInvalidate();
    }
}