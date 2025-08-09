package com.optisense.skyfi.atak;

import android.util.Log;

import com.atakmap.android.drawing.mapItems.DrawingCircle;
import com.atakmap.android.drawing.mapItems.DrawingRectangle;
import com.atakmap.android.drawing.mapItems.DrawingShape;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.Polyline;
import com.atakmap.android.maps.Shape;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Extracts geometry and coordinates from various ATAK shape types
 * Handles polygons, circles, rectangles, freehand drawings, and other shape types
 */
public class ShapeGeometryExtractor {
    private static final String TAG = "SkyFi.ShapeExtractor";
    
    // Constants for validation
    private static final double MIN_VALID_LATITUDE = -90.0;
    private static final double MAX_VALID_LATITUDE = 90.0;
    private static final double MIN_VALID_LONGITUDE = -180.0;
    private static final double MAX_VALID_LONGITUDE = 180.0;
    private static final double MIN_AREA_SQ_METERS = 1.0; // 1 square meter minimum
    private static final double MAX_AREA_SQ_KM = 1000000.0; // 1 million sq km max (roughly size of Egypt)
    private static final int MAX_POINTS_PER_SHAPE = 10000; // Prevent memory issues
    private static final double MIN_DISTANCE_BETWEEN_POINTS = 0.1; // meters
    private static final double DEGENERATE_SHAPE_THRESHOLD = 1.0; // meters
    
    // Thread safety for concurrent access
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ConcurrentHashMap<String, Long> processingShapes = new ConcurrentHashMap<>();
    
    public static class ShapeGeometry {
        public final List<GeoPoint> points;
        public final double areaSqKm;
        public final String shapeType;
        public final boolean isValid;
        public final String validationError;
        public final GeometryMetrics metrics;
        
        public ShapeGeometry(List<GeoPoint> points, double areaSqKm, String shapeType, boolean isValid, String validationError, GeometryMetrics metrics) {
            this.points = points != null ? Collections.unmodifiableList(new ArrayList<>(points)) : Collections.emptyList();
            this.areaSqKm = areaSqKm;
            this.shapeType = shapeType;
            this.isValid = isValid;
            this.validationError = validationError;
            this.metrics = metrics;
        }
        
        // Convenience constructor for valid geometries
        public ShapeGeometry(List<GeoPoint> points, double areaSqKm, String shapeType, GeometryMetrics metrics) {
            this(points, areaSqKm, shapeType, true, null, metrics);
        }
        
        // Factory method for invalid geometries
        public static ShapeGeometry invalid(String shapeType, String error) {
            return new ShapeGeometry(null, 0.0, shapeType, false, error, null);
        }
    }
    
    public static class GeometryMetrics {
        public final int originalPointCount;
        public final int processedPointCount;
        public final double perimeterKm;
        public final boolean crossesDateLine;
        public final boolean nearPolarRegion;
        public final boolean hasSelfIntersections;
        public final long processingTimeMs;
        
        public GeometryMetrics(int originalPointCount, int processedPointCount, double perimeterKm, 
                              boolean crossesDateLine, boolean nearPolarRegion, boolean hasSelfIntersections, 
                              long processingTimeMs) {
            this.originalPointCount = originalPointCount;
            this.processedPointCount = processedPointCount;
            this.perimeterKm = perimeterKm;
            this.crossesDateLine = crossesDateLine;
            this.nearPolarRegion = nearPolarRegion;
            this.hasSelfIntersections = hasSelfIntersections;
            this.processingTimeMs = processingTimeMs;
        }
    }
    
    /**
     * Extract geometry from any supported MapItem with comprehensive validation
     */
    public ShapeGeometry extractGeometry(MapItem item) {
        long startTime = System.currentTimeMillis();
        
        // Pre-validation checks
        if (item == null) {
            Log.w(TAG, "Cannot extract geometry from null item");
            return ShapeGeometry.invalid("null", "MapItem is null");
        }
        
        String itemType = safeGetItemType(item);
        String itemUID = safeGetItemUID(item);
        
        // Check if this shape is already being processed (prevent concurrent access)
        if (processingShapes.containsKey(itemUID)) {
            Log.w(TAG, "Shape " + itemUID + " is already being processed");
            return ShapeGeometry.invalid(itemType, "Shape is currently being processed by another operation");
        }
        
        // Mark shape as being processed
        processingShapes.put(itemUID, System.currentTimeMillis());
        
        try {
            lock.readLock().lock();
            
            Log.d(TAG, "Extracting geometry from item type: " + itemType + " (class: " + item.getClass().getSimpleName() + ", UID: " + itemUID + ")");
            
            // Validate shape access permissions
            ShapeGeometry accessValidation = validateShapeAccess(item);
            if (!accessValidation.isValid) {
                return accessValidation;
            }
            
            ShapeGeometry geometry;
            
            // Handle specific shape types with timeout protection
            try {
                if (item instanceof DrawingCircle) {
                    geometry = extractCircleGeometry((DrawingCircle) item, startTime);
                } else if (item instanceof DrawingRectangle) {
                    geometry = extractRectangleGeometry((DrawingRectangle) item, startTime);
                } else if (item instanceof DrawingShape) {
                    geometry = extractDrawingShapeGeometry((DrawingShape) item, startTime);
                } else if (item instanceof Shape) {
                    geometry = extractShapeGeometry((Shape) item, startTime);
                } else if (item instanceof Polyline) {
                    geometry = extractPolylineGeometry((Polyline) item, startTime);
                } else {
                    // Try generic extraction based on type string
                    geometry = extractGenericGeometry(item, startTime);
                }
                
                // Final validation of extracted geometry
                if (geometry != null && geometry.isValid) {
                    geometry = validateExtractedGeometry(geometry, itemType);
                }
                
                return geometry;
                
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "Out of memory processing shape " + itemUID, e);
                return ShapeGeometry.invalid(itemType, "Shape too complex - insufficient memory");
            } catch (Exception e) {
                Log.e(TAG, "Error extracting geometry from " + itemType + " (UID: " + itemUID + ")", e);
                return ShapeGeometry.invalid(itemType, "Extraction failed: " + e.getMessage());
            }
            
        } finally {
            lock.readLock().unlock();
            processingShapes.remove(itemUID);
            
            long processingTime = System.currentTimeMillis() - startTime;
            if (processingTime > 5000) { // Warn if processing took more than 5 seconds
                Log.w(TAG, "Shape processing took " + processingTime + "ms for " + itemType + " (UID: " + itemUID + ")");
            }
        }
    }
    
    /**
     * Extract geometry from DrawingCircle
     */
    private ShapeGeometry extractCircleGeometry(DrawingCircle circle, long startTime) {
        Log.d(TAG, "Extracting circle geometry");
        
        try {
            GeoPoint center = safeGetCircleCenter(circle);
            double radiusMeters = safeGetCircleRadius(circle);
            
            if (center == null) {
                return ShapeGeometry.invalid("circle", "Unable to access circle center");
            }
            
            // Validate circle parameters (center already validated above)
            if (radiusMeters < 0) {
                return ShapeGeometry.invalid("circle", "Unable to access circle radius");
            }
            
            if (radiusMeters <= 0 || radiusMeters > 1000000) { // Max 1000km radius
                return ShapeGeometry.invalid("circle", "Invalid radius: " + radiusMeters + " meters");
            }
            
            // Convert circle to polygon approximation
            List<GeoPoint> points = createCirclePolygon(center, radiusMeters, 32);
            double areaSqKm = Math.PI * Math.pow(radiusMeters / 1000.0, 2); // π * r²
            
            // Calculate metrics
            double perimeterKm = 2 * Math.PI * (radiusMeters / 1000.0);
            boolean nearPolar = Math.abs(center.getLatitude()) > 70;
            
            GeometryMetrics metrics = new GeometryMetrics(
                1, // Original point count (center)
                points.size(), // Processed point count
                perimeterKm,
                false, // Circles don't cross dateline in this implementation
                nearPolar,
                false, // No self-intersections
                System.currentTimeMillis() - startTime
            );
            
            Log.d(TAG, "Circle: center=" + center + ", radius=" + radiusMeters + "m, area=" + areaSqKm + " sq km");
            
            return new ShapeGeometry(points, areaSqKm, "circle", metrics);
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting circle geometry", e);
            return ShapeGeometry.invalid("circle", "Circle extraction failed: " + e.getMessage());
        }
    }
    
    /**
     * Extract geometry from DrawingRectangle
     */
    private ShapeGeometry extractRectangleGeometry(DrawingRectangle rectangle, long startTime) {
        Log.d(TAG, "Extracting rectangle geometry");
        
        try {
            List<GeoPoint> points = new ArrayList<>();
            int originalPointCount = safeGetNumPoints(rectangle);
            
            // Get rectangle corners with defensive access
            // For DrawingRectangle, use alternative method to get points
            try {
                // Try to get points from the Shape interface
                GeoPoint[] shapePoints = rectangle.getPoints();
                if (shapePoints != null) {
                    for (GeoPoint point : shapePoints) {
                        if (point != null && isValidCoordinate(point)) {
                            points.add(point);
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error accessing rectangle points", e);
            }
            
            if (points.size() < 3) {
                return ShapeGeometry.invalid("rectangle", "Insufficient valid points: " + points.size());
            }
            
            // Validate and process points
            points = validateAndProcessPoints(points);
            if (points.isEmpty()) {
                return ShapeGeometry.invalid("rectangle", "All points failed validation");
            }
            
            double areaSqKm = calculatePolygonArea(points);
            double perimeterKm = calculatePerimeter(points);
            
            // Check for geometric issues
            boolean hasSelfIntersections = checkSelfIntersections(points);
            boolean nearPolar = false;
            for (GeoPoint p : points) {
                if (Math.abs(p.getLatitude()) > 70) {
                    nearPolar = true;
                    break;
                }
            }
            boolean crossesDateLine = checkDateLineCrossing(points);
            
            GeometryMetrics metrics = new GeometryMetrics(
                originalPointCount,
                points.size(),
                perimeterKm,
                crossesDateLine,
                nearPolar,
                hasSelfIntersections,
                System.currentTimeMillis() - startTime
            );
            
            Log.d(TAG, "Rectangle: " + points.size() + " points, area=" + areaSqKm + " sq km");
            
            return new ShapeGeometry(points, areaSqKm, "rectangle", metrics);
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting rectangle geometry", e);
            return ShapeGeometry.invalid("rectangle", "Rectangle extraction failed: " + e.getMessage());
        }
    }
    
    /**
     * Extract geometry from DrawingShape (includes polygons, freehand)
     */
    private ShapeGeometry extractDrawingShapeGeometry(DrawingShape shape, long startTime) {
        Log.d(TAG, "Extracting drawing shape geometry");
        
        try {
            List<GeoPoint> points = new ArrayList<>();
            int originalPointCount = safeGetNumPoints(shape);
            
            // Prevent processing extremely large shapes
            if (originalPointCount > MAX_POINTS_PER_SHAPE) {
                return ShapeGeometry.invalid("drawing_shape", 
                    "Shape too complex: " + originalPointCount + " points (max: " + MAX_POINTS_PER_SHAPE + ")");
            }
            
            // Get points from drawing shape with defensive access
            try {
                // Try to get points from the Shape interface instead
                GeoPoint[] shapePoints = shape.getPoints();
                if (shapePoints != null) {
                    for (GeoPoint point : shapePoints) {
                        if (point != null && isValidCoordinate(point)) {
                            points.add(point);
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error accessing drawing shape points", e);
            }
            
            if (points.size() < 3) {
                return ShapeGeometry.invalid("drawing_shape", "Insufficient valid points: " + points.size());
            }
            
            // Process and validate points
            points = validateAndProcessPoints(points);
            if (points.isEmpty()) {
                return ShapeGeometry.invalid("drawing_shape", "All points failed validation");
            }
            
            double areaSqKm = calculatePolygonArea(points);
            
            // Validate minimum area
            if (areaSqKm * 1_000_000 < MIN_AREA_SQ_METERS) {
                return ShapeGeometry.invalid("drawing_shape", 
                    "Area too small: " + areaSqKm + " sq km (min: " + (MIN_AREA_SQ_METERS / 1_000_000) + " sq km)");
            }
            
            double perimeterKm = calculatePerimeter(points);
            boolean hasSelfIntersections = checkSelfIntersections(points);
            boolean nearPolar = false;
            for (GeoPoint p : points) {
                if (Math.abs(p.getLatitude()) > 70) {
                    nearPolar = true;
                    break;
                }
            }
            boolean crossesDateLine = checkDateLineCrossing(points);
            
            GeometryMetrics metrics = new GeometryMetrics(
                originalPointCount,
                points.size(),
                perimeterKm,
                crossesDateLine,
                nearPolar,
                hasSelfIntersections,
                System.currentTimeMillis() - startTime
            );
            
            Log.d(TAG, "Drawing shape: " + points.size() + " points, area=" + areaSqKm + " sq km");
            
            return new ShapeGeometry(points, areaSqKm, "drawing_shape", metrics);
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting drawing shape geometry", e);
            return ShapeGeometry.invalid("drawing_shape", "Drawing shape extraction failed: " + e.getMessage());
        }
    }
    
    /**
     * Extract geometry from generic Shape
     */
    private ShapeGeometry extractShapeGeometry(Shape shape, long startTime) {
        Log.d(TAG, "Extracting generic shape geometry");
        
        try {
            List<GeoPoint> points = new ArrayList<>();
            
            // Try to get points from Shape interface
            GeoPoint[] shapePoints = shape.getPoints();
            int originalPointCount = shapePoints != null ? shapePoints.length : 0;
            
            if (shapePoints != null) {
                for (GeoPoint point : shapePoints) {
                    if (point != null && isValidCoordinate(point)) {
                        points.add(point);
                    }
                }
            }
            
            if (points.size() < 3) {
                return ShapeGeometry.invalid("shape", "Insufficient valid points: " + points.size());
            }
            
            points = validateAndProcessPoints(points);
            if (points.isEmpty()) {
                return ShapeGeometry.invalid("shape", "All points failed validation");
            }
            
            double areaSqKm = calculatePolygonArea(points);
            double perimeterKm = calculatePerimeter(points);
            
            boolean hasSelfIntersections = checkSelfIntersections(points);
            boolean nearPolar = false;
            for (GeoPoint p : points) {
                if (Math.abs(p.getLatitude()) > 70) {
                    nearPolar = true;
                    break;
                }
            }
            boolean crossesDateLine = checkDateLineCrossing(points);
            
            GeometryMetrics metrics = new GeometryMetrics(
                originalPointCount,
                points.size(),
                perimeterKm,
                crossesDateLine,
                nearPolar,
                hasSelfIntersections,
                System.currentTimeMillis() - startTime
            );
            
            Log.d(TAG, "Generic shape: " + points.size() + " points, area=" + areaSqKm + " sq km");
            
            return new ShapeGeometry(points, areaSqKm, "shape", metrics);
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting shape geometry", e);
            return ShapeGeometry.invalid("shape", "Shape extraction failed: " + e.getMessage());
        }
    }
    
    /**
     * Extract geometry from Polyline (convert to polygon if closed)
     */
    private ShapeGeometry extractPolylineGeometry(Polyline polyline, long startTime) {
        Log.d(TAG, "Extracting polyline geometry");
        
        try {
            List<GeoPoint> points = new ArrayList<>();
            
            // Get points from polyline
            GeoPoint[] linePoints = polyline.getPoints();
            int originalPointCount = linePoints != null ? linePoints.length : 0;
            
            if (linePoints != null) {
                for (GeoPoint point : linePoints) {
                    if (point != null && isValidCoordinate(point)) {
                        points.add(point);
                    }
                }
            }
            
            if (points.size() < 2) {
                return ShapeGeometry.invalid("polyline", "Insufficient valid points: " + points.size());
            }
            
            // Check if polyline is closed (first and last points are the same)
            boolean isClosed = false;
            if (points.size() > 2) {
                GeoPoint first = points.get(0);
                GeoPoint last = points.get(points.size() - 1);
                double distance = calculateDistance(first, last);
                isClosed = distance < 10; // Within 10 meters
            }
            
            double areaSqKm = 0;
            if (isClosed && points.size() > 2) {
                points = validateAndProcessPoints(points);
                areaSqKm = calculatePolygonArea(points);
            } else {
                // For open polylines, we can't calculate a meaningful area
                Log.d(TAG, "Polyline is not closed, cannot calculate area");
                return ShapeGeometry.invalid("polyline", "Polyline is not closed - cannot create AOI from open line");
            }
            
            double perimeterKm = calculatePerimeter(points);
            boolean hasSelfIntersections = checkSelfIntersections(points);
            boolean nearPolar = false;
            for (GeoPoint p : points) {
                if (Math.abs(p.getLatitude()) > 70) {
                    nearPolar = true;
                    break;
                }
            }
            boolean crossesDateLine = checkDateLineCrossing(points);
            
            GeometryMetrics metrics = new GeometryMetrics(
                originalPointCount,
                points.size(),
                perimeterKm,
                crossesDateLine,
                nearPolar,
                hasSelfIntersections,
                System.currentTimeMillis() - startTime
            );
            
            Log.d(TAG, "Polyline: " + points.size() + " points, closed=" + isClosed + ", area=" + areaSqKm + " sq km");
            
            return new ShapeGeometry(points, areaSqKm, "polyline", metrics);
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting polyline geometry", e);
            return ShapeGeometry.invalid("polyline", "Polyline extraction failed: " + e.getMessage());
        }
    }
    
    /**
     * Try to extract geometry from unknown item types using reflection and metadata
     */
    private ShapeGeometry extractGenericGeometry(MapItem item, long startTime) {
        Log.d(TAG, "Attempting generic geometry extraction for: " + item.getType());
        
        try {
            List<GeoPoint> points = new ArrayList<>();
            
            // Try to get bounds or center point as fallback
            GeoPoint point = safeGetItemPoint(item);
            if (point != null && isValidCoordinate(point)) {
                Log.d(TAG, "Found single point: " + point);
                // Create a small square around the point
                points = createMinimumSquareAround(point, 100); // 100m radius
            } else {
                return ShapeGeometry.invalid("generic", "No valid geometry found in item");
            }
            
            double areaSqKm = calculatePolygonArea(points);
            double perimeterKm = calculatePerimeter(points);
            
            GeometryMetrics metrics = new GeometryMetrics(
                1, // Original point count
                points.size(),
                perimeterKm,
                false,
                Math.abs(point.getLatitude()) > 70,
                false,
                System.currentTimeMillis() - startTime
            );
            
            return new ShapeGeometry(points, areaSqKm, "generic", metrics);
            
        } catch (Exception e) {
            Log.e(TAG, "Generic extraction failed", e);
            return ShapeGeometry.invalid("generic", "Generic extraction failed: " + e.getMessage());
        }
    }
    
    /**
     * Create a polygon approximation of a circle
     */
    private List<GeoPoint> createCirclePolygon(GeoPoint center, double radiusMeters, int numPoints) {
        List<GeoPoint> points = new ArrayList<>();
        
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
     * Create a minimum square around a point
     */
    private List<GeoPoint> createMinimumSquareAround(GeoPoint center, double radiusMeters) {
        List<GeoPoint> points = new ArrayList<>();
        
        // Calculate lat/lon offsets
        double latOffset = radiusMeters / 111320.0;
        double lonOffset = radiusMeters / (111320.0 * Math.cos(Math.toRadians(center.getLatitude())));
        
        // Create square corners
        points.add(new GeoPoint(center.getLatitude() - latOffset, center.getLongitude() - lonOffset)); // SW
        points.add(new GeoPoint(center.getLatitude() - latOffset, center.getLongitude() + lonOffset)); // SE
        points.add(new GeoPoint(center.getLatitude() + latOffset, center.getLongitude() + lonOffset)); // NE
        points.add(new GeoPoint(center.getLatitude() + latOffset, center.getLongitude() - lonOffset)); // NW
        
        return points;
    }
    
    /**
     * Calculate area of a polygon using the shoelace formula
     */
    private double calculatePolygonArea(List<GeoPoint> points) {
        if (points == null || points.size() < 3) {
            return 0.0;
        }
        
        try {
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
            
            // Convert to square kilometers
            double avgLat = 0;
            for (GeoPoint p : points) {
                avgLat += p.getLatitude();
            }
            avgLat /= points.size();
            
            double metersPerDegreeLat = 111132.92 - 559.82 * Math.cos(2 * Math.toRadians(avgLat));
            double metersPerDegreeLon = 111412.84 * Math.cos(Math.toRadians(avgLat));
            
            return area * metersPerDegreeLat * metersPerDegreeLon / 1_000_000;
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculating polygon area", e);
            return 0.0;
        }
    }
    
    /**
     * Check if a shape is likely to be a closed polygon
     */
    public boolean isClosedPolygon(List<GeoPoint> points) {
        if (points == null || points.size() < 3) {
            return false;
        }
        
        GeoPoint first = points.get(0);
        GeoPoint last = points.get(points.size() - 1);
        
        double distance = calculateDistance(first, last);
        return distance < 50; // Within 50 meters
    }
    
    /**
     * Validate shape access permissions and basic integrity
     */
    private ShapeGeometry validateShapeAccess(MapItem item) {
        try {
            String itemType = safeGetItemType(item);
            String itemUID = safeGetItemUID(item);
            
            // Check if item is accessible
            if (itemUID == null || itemUID.isEmpty() || "unknown".equals(itemUID)) {
                return ShapeGeometry.invalid(itemType, "Shape has no valid identifier");
            }
            
            // Check if item is visible/active with defensive access
            boolean isVisible = safeGetItemVisible(item);
            if (!isVisible) {
                return ShapeGeometry.invalid(itemType, "Shape is not visible");
            }
            
            return new ShapeGeometry(null, 0, itemType, true, null, null); // Valid access
            
        } catch (Exception e) {
            Log.e(TAG, "Error validating shape access", e);
            return ShapeGeometry.invalid(safeGetItemType(item), "Access validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate coordinate bounds
     */
    private boolean isValidCoordinate(GeoPoint point) {
        if (point == null) return false;
        
        double lat = point.getLatitude();
        double lon = point.getLongitude();
        
        return lat >= MIN_VALID_LATITUDE && lat <= MAX_VALID_LATITUDE &&
               lon >= MIN_VALID_LONGITUDE && lon <= MAX_VALID_LONGITUDE &&
               !Double.isNaN(lat) && !Double.isNaN(lon) &&
               Double.isFinite(lat) && Double.isFinite(lon);
    }
    
    /**
     * Validate and process points list - remove duplicates, invalid points, etc.
     */
    private List<GeoPoint> validateAndProcessPoints(List<GeoPoint> inputPoints) {
        if (inputPoints == null || inputPoints.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<GeoPoint> processedPoints = new ArrayList<>();
        GeoPoint lastPoint = null;
        
        for (GeoPoint point : inputPoints) {
            if (point == null || !isValidCoordinate(point)) {
                continue;
            }
            
            // Skip duplicate points that are too close
            if (lastPoint != null) {
                double distance = calculateDistance(lastPoint, point);
                if (distance < MIN_DISTANCE_BETWEEN_POINTS) {
                    continue;
                }
            }
            
            processedPoints.add(point);
            lastPoint = point;
        }
        
        return processedPoints;
    }
    
    /**
     * Calculate perimeter of a polygon
     */
    private double calculatePerimeter(List<GeoPoint> points) {
        if (points == null || points.size() < 2) {
            return 0.0;
        }
        
        double perimeter = 0.0;
        for (int i = 0; i < points.size(); i++) {
            int nextIndex = (i + 1) % points.size();
            perimeter += calculateDistance(points.get(i), points.get(nextIndex));
        }
        
        return perimeter / 1000.0; // Convert to kilometers
    }
    
    /**
     * Check for self-intersections in polygon
     */
    private boolean checkSelfIntersections(List<GeoPoint> points) {
        // Simplified check - for production use a proper computational geometry library
        if (points.size() < 4) return false;
        
        try {
            // Basic check: see if any non-adjacent edges intersect
            for (int i = 0; i < points.size() - 1; i++) {
                for (int j = i + 2; j < points.size() - 1; j++) {
                    if (j == points.size() - 1 && i == 0) continue; // Skip adjacent edges
                    
                    // This is a simplified intersection test
                    // In production, use proper line segment intersection algorithms
                    if (roughLineIntersectionCheck(points.get(i), points.get(i + 1),
                            points.get(j), points.get(j + 1))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error checking self-intersections", e);
        }
        
        return false;
    }
    
    /**
     * Rough check for line intersection (simplified)
     */
    private boolean roughLineIntersectionCheck(GeoPoint p1, GeoPoint p2, GeoPoint p3, GeoPoint p4) {
        // This is a very simplified check - in production use proper computational geometry
        double lat1 = p1.getLatitude(), lon1 = p1.getLongitude();
        double lat2 = p2.getLatitude(), lon2 = p2.getLongitude();
        double lat3 = p3.getLatitude(), lon3 = p3.getLongitude();
        double lat4 = p4.getLatitude(), lon4 = p4.getLongitude();
        
        // Check if bounding boxes intersect
        double minLat1 = Math.min(lat1, lat2), maxLat1 = Math.max(lat1, lat2);
        double minLon1 = Math.min(lon1, lon2), maxLon1 = Math.max(lon1, lon2);
        double minLat2 = Math.min(lat3, lat4), maxLat2 = Math.max(lat3, lat4);
        double minLon2 = Math.min(lon3, lon4), maxLon2 = Math.max(lon3, lon4);
        
        return !(maxLat1 < minLat2 || minLat1 > maxLat2 || maxLon1 < minLon2 || minLon1 > maxLon2);
    }
    
    /**
     * Check if polygon crosses the international date line
     */
    private boolean checkDateLineCrossing(List<GeoPoint> points) {
        if (points == null || points.size() < 2) return false;
        
        for (int i = 0; i < points.size() - 1; i++) {
            double lon1 = points.get(i).getLongitude();
            double lon2 = points.get(i + 1).getLongitude();
            
            // Check for large longitude jumps (likely date line crossing)
            if (Math.abs(lon1 - lon2) > 180) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Validate extracted geometry against requirements
     */
    private ShapeGeometry validateExtractedGeometry(ShapeGeometry geometry, String itemType) {
        if (geometry == null || !geometry.isValid) {
            return geometry;
        }
        
        // Check degenerate shapes (too small perimeter)
        if (geometry.metrics != null && geometry.metrics.perimeterKm * 1000 < DEGENERATE_SHAPE_THRESHOLD) {
            return ShapeGeometry.invalid(itemType, 
                "Shape is degenerate (perimeter too small: " + geometry.metrics.perimeterKm + " km)");
        }
        
        // Check maximum area (sanity check)
        if (geometry.areaSqKm > MAX_AREA_SQ_KM) {
            return ShapeGeometry.invalid(itemType, 
                "Shape area too large: " + geometry.areaSqKm + " sq km (max: " + MAX_AREA_SQ_KM + " sq km)");
        }
        
        // Warn about polar regions
        if (geometry.metrics != null && geometry.metrics.nearPolarRegion) {
            Log.w(TAG, "Shape is near polar region - area calculations may be inaccurate");
        }
        
        // Warn about date line crossing
        if (geometry.metrics != null && geometry.metrics.crossesDateLine) {
            Log.w(TAG, "Shape crosses international date line - special handling may be needed");
        }
        
        return geometry; // Valid
    }
    
    /**
     * Validate that extracted geometry meets minimum requirements
     */
    public boolean isValidGeometry(ShapeGeometry geometry, double minAreaSqKm) {
        if (geometry == null || geometry.points == null || !geometry.isValid) {
            return false;
        }
        
        if (geometry.points.size() < 3) {
            Log.w(TAG, "Geometry has fewer than 3 points");
            return false;
        }
        
        if (geometry.areaSqKm < minAreaSqKm) {
            Log.w(TAG, "Geometry area (" + geometry.areaSqKm + " sq km) is below minimum (" + minAreaSqKm + " sq km)");
            return false;
        }
        
        return true;
    }
    
    /**
     * Cleanup method for memory management
     */
    public void cleanup() {
        lock.writeLock().lock();
        try {
            processingShapes.clear();
            Log.d(TAG, "ShapeGeometryExtractor cleanup completed");
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    // Defensive MapItem access methods
    
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
    
    /**
     * Safely get MapItem visibility with error handling
     */
    private boolean safeGetItemVisible(MapItem item) {
        try {
            return item.getVisible();
        } catch (Exception e) {
            Log.w(TAG, "Failed to get item visibility", e);
            return false; // Default to not visible if we can't access
        }
    }
    
    /**
     * Safely get number of points from shape with error handling
     */
    private int safeGetNumPoints(Object shape) {
        try {
            if (shape instanceof Shape) {
                Shape s = (Shape) shape;
                GeoPoint[] points = s.getPoints();
                return points != null ? points.length : 0;
            }
            return 0;
        } catch (Exception e) {
            Log.w(TAG, "Failed to get number of points from shape", e);
            return 0;
        }
    }
    
    /**
     * Safely get circle center with error handling
     */
    private GeoPoint safeGetCircleCenter(DrawingCircle circle) {
        try {
            GeoPointMetaData centerMeta = circle.getCenter();
            if (centerMeta != null) {
                return centerMeta.get();
            }
            return null;
        } catch (Exception e) {
            Log.w(TAG, "Failed to get circle center", e);
            return null;
        }
    }
    
    /**
     * Safely get circle radius with error handling
     */
    private double safeGetCircleRadius(DrawingCircle circle) {
        try {
            return circle.getRadius();
        } catch (Exception e) {
            Log.w(TAG, "Failed to get circle radius", e);
            return -1.0; // Invalid radius indicator
        }
    }
    
    /**
     * Calculate distance between two points using basic haversine formula
     */
    private double calculateDistance(GeoPoint p1, GeoPoint p2) {
        if (p1 == null || p2 == null) {
            return 0.0;
        }
        
        double lat1 = Math.toRadians(p1.getLatitude());
        double lon1 = Math.toRadians(p1.getLongitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double lon2 = Math.toRadians(p2.getLongitude());
        
        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;
        
        double a = Math.sin(dlat/2) * Math.sin(dlat/2) + 
                   Math.cos(lat1) * Math.cos(lat2) * 
                   Math.sin(dlon/2) * Math.sin(dlon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
        return 6371000 * c; // Earth radius in meters
    }
    
    /**
     * Safely get MapItem center point with error handling
     */
    private GeoPoint safeGetItemPoint(MapItem item) {
        try {
            // For ATAK compatibility, try different methods to get a point
            if (item instanceof Shape) {
                Shape shape = (Shape) item;
                GeoPoint[] points = shape.getPoints();
                if (points != null && points.length > 0) {
                    return points[0]; // Return first point as center approximation
                }
            }
            
            // Try to get center from metadata if available
            // This is a fallback approach for generic MapItems
            return null; // Cannot determine point for this item type
            
        } catch (Exception e) {
            Log.w(TAG, "Failed to get item point", e);
            return null;
        }
    }
}