package com.skyfi.atak.plugin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.atakmap.android.drawing.mapItems.DrawingRectangle;
import com.atakmap.android.drawing.mapItems.DrawingShape;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.editableShapes.Rectangle;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.Association;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.SimpleRectangle;
import com.atakmap.android.menu.PluginMenuParser;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTWriter;

import java.util.ArrayList;

public class OrderUtility extends DropDownReceiver implements MapEventDispatcher.MapEventDispatchListener {
    private static final String LOGTAG = "OrderUtility";

    MapView mapView;
    Context context;
    String aoi;

    protected OrderUtility(MapView mapView, Context context) {
        super(mapView);
        this.mapView = mapView;
        this.context = context;

        getMapView().getMapEventDispatcher().addMapEventListener(MapEvent.ITEM_ADDED, this);
    }

    @Override
    protected void disposeImpl() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action != null && action.equals("com.atakmap.android.cot_utility.receivers.cotMenu")) {
            MapItem mapItem = findTarget(intent.getStringExtra("targetUID"));
            if (mapItem != null) {
                if (mapItem instanceof SimpleRectangle) {
                    SimpleRectangle rectangle = (SimpleRectangle) mapItem;
                    aoi = getWkt(rectangle.getPoints());

                    if (aoi != null) {
                        Intent newOrderIntent = new Intent();
                        newOrderIntent.setAction(NewOrderFragment.ACTION);
                        newOrderIntent.putExtra("aoi", aoi);
                        AtakBroadcast.getInstance().sendBroadcast(newOrderIntent);
                    }
                }
                else if (mapItem instanceof Association && mapItem.getType().equals("rectangle_line")) {
                    Association association = (Association) mapItem;
                    aoi = getWkt(association.getPoints());

                    if (aoi != null) {
                        Intent newOrderIntent = new Intent();
                        newOrderIntent.setAction(NewOrderFragment.ACTION);
                        newOrderIntent.putExtra("aoi", aoi);
                        AtakBroadcast.getInstance().sendBroadcast(newOrderIntent);
                    }
                }
                else if (mapItem instanceof DrawingShape) {
                    // Handle polygon shapes
                    DrawingShape shape = (DrawingShape) mapItem;
                    aoi = getWkt(shape.getPoints());
                    
                    if (aoi != null) {
                        // Calculate area
                        double areaKm2 = calculatePolygonArea(shape.getPoints());
                        
                        // Show dialog to save as AOI or use directly
                        new AlertDialog.Builder(MapView.getMapView().getContext())
                            .setTitle("Use Polygon AOI")
                            .setMessage(String.format("Area: %.2f km²\n\nWhat would you like to do?", areaKm2))
                            .setPositiveButton("Save & Use for Order", (dialog, which) -> {
                                // Save as AOI
                                AOIManager aoiManager = new AOIManager(context);
                                AOIManager.AOI aoiObj = new AOIManager.AOI(
                                    aoiManager.generateAOIId(),
                                    "Polygon AOI " + new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date()),
                                    aoi
                                );
                                aoiObj.areaKm2 = areaKm2;
                                aoiManager.saveAOI(aoiObj);
                                
                                // Launch new order
                                Intent newOrderIntent = new Intent();
                                newOrderIntent.setAction(NewOrderFragment.ACTION);
                                newOrderIntent.putExtra("aoi", aoi);
                                newOrderIntent.putExtra("aoiName", aoiObj.name);
                                AtakBroadcast.getInstance().sendBroadcast(newOrderIntent);
                            })
                            .setNeutralButton("Use for Order Only", (dialog, which) -> {
                                // Just use for order without saving
                                Intent newOrderIntent = new Intent();
                                newOrderIntent.setAction(NewOrderFragment.ACTION);
                                newOrderIntent.putExtra("aoi", aoi);
                                AtakBroadcast.getInstance().sendBroadcast(newOrderIntent);
                            })
                            .setNegativeButton("Cancel", null)
                            .create()
                            .show();
                    }
                }
                else if (mapItem instanceof DrawingShape) {
                    // Handle polygon shapes
                    DrawingShape shape = (DrawingShape) mapItem;
                    aoi = getWkt(shape.getPoints());
                    
                    if (aoi != null) {
                        // Calculate area
                        double areaKm2 = calculatePolygonArea(shape.getPoints());
                        
                        // Show dialog to save as AOI or use directly
                        new AlertDialog.Builder(MapView.getMapView().getContext())
                            .setTitle("Use Polygon AOI")
                            .setMessage(String.format("Area: %.2f km²\n\nWhat would you like to do?", areaKm2))
                            .setPositiveButton("Save & Use for Order", (dialog, which) -> {
                                // Save as AOI
                                AOIManager aoiManager = new AOIManager(context);
                                AOIManager.AOI aoiObj = new AOIManager.AOI(
                                    aoiManager.generateAOIId(),
                                    "Polygon AOI " + new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date()),
                                    aoi
                                );
                                aoiObj.areaKm2 = areaKm2;
                                aoiManager.saveAOI(aoiObj);
                                
                                // Launch new order
                                Intent newOrderIntent = new Intent();
                                newOrderIntent.setAction(NewOrderFragment.ACTION);
                                newOrderIntent.putExtra("aoi", aoi);
                                newOrderIntent.putExtra("aoiName", aoiObj.name);
                                AtakBroadcast.getInstance().sendBroadcast(newOrderIntent);
                            })
                            .setNeutralButton("Use for Order Only", (dialog, which) -> {
                                // Just use for order without saving
                                Intent newOrderIntent = new Intent();
                                newOrderIntent.setAction(NewOrderFragment.ACTION);
                                newOrderIntent.putExtra("aoi", aoi);
                                AtakBroadcast.getInstance().sendBroadcast(newOrderIntent);
                            })
                            .setNegativeButton("Cancel", null)
                            .create()
                            .show();
                    }
                }
                else {
                    Log.d(LOGTAG, "Unknown " + mapItem.getClass() + " " + mapItem.getType());
                }
            }
            else {
                Log.d(LOGTAG, "mapitem null");
            }
        }
    }

    private String getWkt(GeoPoint[] points) {
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        Coordinate firstCoord = null;
        for (GeoPoint point : points) {
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
            Log.e(LOGTAG, "Failed to convert to WKT", e);
        }

        return null;
    }

    private String getMenu() {
        return PluginMenuParser.getMenu(context, "menu.xml");
    }

    private MapItem findTarget(final String targetUID) {
        MapItem mapItem = null;
        if (targetUID != null) {
            mapItem = getMapView().getMapItem(targetUID);
        }
        return mapItem;
    }

    @Override
    public void onMapEvent(MapEvent mapEvent) {
        ArrayList<String> rectangle = new ArrayList<>();
        rectangle.add("corner_u-d-r");
        rectangle.add("side_u-d-r");
        rectangle.add("rectangle_line");

        // Rectangle and polygon
        if (mapEvent.getItem() instanceof SimpleRectangle || mapEvent.getItem() instanceof Rectangle || mapEvent.getItem() instanceof DrawingRectangle || mapEvent.getItem() instanceof DrawingShape) {
            mapEvent.getItem().setMetaString("menu", getMenu());
        }
        // Polygon
        else if (mapEvent.getItem() instanceof Marker && mapEvent.getItem().getType().equals("shape_marker")) {
            mapEvent.getItem().setMetaString("menu", getMenu());
        }
        // Polygon
        else if (mapEvent.getItem() instanceof Marker && mapEvent.getItem().getType().equals("b-m-p-w")) {
            mapEvent.getItem().setMetaString("menu", getMenu());
        }
        // Rectangle
        else if (mapEvent.getItem() instanceof Marker && rectangle.contains(mapEvent.getItem().getType())) {
            mapEvent.getItem().setMetaString("menu", getMenu());
        }
        // Rectangle
        else if (mapEvent.getItem() instanceof Association && rectangle.contains(mapEvent.getItem().getType())) {
            mapEvent.getItem().setMetaString("menu", getMenu());
        }
        else {
            Log.d(LOGTAG,mapEvent.getItem().getClass() + " " + mapEvent.getItem().getTitle() + " " + mapEvent.getItem().getType());
        }
    }
    
    private double calculatePolygonArea(GeoPoint[] points) {
        // Simple area calculation using shoelace formula
        // This is approximate and works best for small areas
        double area = 0.0;
        int n = points.length;
        
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            area += points[i].getLongitude() * points[j].getLatitude();
            area -= points[j].getLongitude() * points[i].getLatitude();
        }
        
        area = Math.abs(area) / 2.0;
        
        // Convert from square degrees to square kilometers (approximate)
        // This is a rough approximation that works for small areas
        double avgLat = 0;
        for (GeoPoint p : points) {
            avgLat += p.getLatitude();
        }
        avgLat /= points.length;
        
        double metersPerDegreeLat = 111320.0;
        double metersPerDegreeLon = 111320.0 * Math.cos(Math.toRadians(avgLat));
        
        return area * (metersPerDegreeLat * metersPerDegreeLon) / 1000000.0;
    }
}
