package com.optisense.skyfi.atak;

import com.atakmap.android.cot.MarkerDetailHandler;
import com.atakmap.android.maps.Marker;
import com.atakmap.coremap.cot.event.CotDetail;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.log.Log;

/**
 * Handles SkyFi-specific CoT details for markers and shapes
 * Allows SkyFi metadata to be transmitted via CoT events
 */
public class SkyFiDetailHandler implements MarkerDetailHandler {
    
    private static final String TAG = "SkyFi.DetailHandler";
    
    @Override
    public void toCotDetail(final Marker marker, final CotDetail detail) {
        Log.d(TAG, "Converting marker to CoT detail: " + marker.getUID());
        
        // Create SkyFi-specific detail element
        CotDetail skyfiDetail = new CotDetail("__skyfi");
        
        // Add SkyFi metadata if present
        if (marker.hasMetaValue("skyfi_aoi_id")) {
            skyfiDetail.setAttribute("aoi_id", marker.getMetaString("skyfi_aoi_id", ""));
        }
        
        if (marker.hasMetaValue("skyfi_area_sqkm")) {
            skyfiDetail.setAttribute("area_sqkm", 
                String.valueOf(marker.getMetaDouble("skyfi_area_sqkm", 0)));
        }
        
        if (marker.hasMetaValue("skyfi_sensor_type")) {
            skyfiDetail.setAttribute("sensor_type", 
                marker.getMetaString("skyfi_sensor_type", "optical"));
        }
        
        if (marker.hasMetaValue("skyfi_order_id")) {
            skyfiDetail.setAttribute("order_id", 
                marker.getMetaString("skyfi_order_id", ""));
        }
        
        if (marker.hasMetaValue("skyfi_converted_to_aoi")) {
            skyfiDetail.setAttribute("is_aoi", 
                marker.getMetaString("skyfi_converted_to_aoi", "false"));
        }
        
        if (marker.hasMetaValue("skyfi_geometry_type")) {
            skyfiDetail.setAttribute("geometry_type", 
                marker.getMetaString("skyfi_geometry_type", ""));
        }
        
        // Only add the detail if it has attributes
        if (skyfiDetail.getAttributes() != null && skyfiDetail.getAttributes().length > 0) {
            detail.addChild(skyfiDetail);
            Log.d(TAG, "Added SkyFi CoT detail with " + 
                skyfiDetail.getAttributes().length + " attributes");
        }
    }
    
    @Override
    public void toMarkerMetadata(final Marker marker, CotEvent event, CotDetail detail) {
        Log.d(TAG, "Extracting SkyFi detail from CoT event: " + event.getUID());
        
        // Look for __skyfi detail element
        CotDetail skyfiDetail = detail.getFirstChildByName(0, "__skyfi");
        
        if (skyfiDetail != null) {
            // Extract SkyFi metadata
            String aoiId = skyfiDetail.getAttribute("aoi_id");
            if (aoiId != null && !aoiId.isEmpty()) {
                marker.setMetaString("skyfi_aoi_id", aoiId);
            }
            
            String areaSqKm = skyfiDetail.getAttribute("area_sqkm");
            if (areaSqKm != null && !areaSqKm.isEmpty()) {
                try {
                    marker.setMetaDouble("skyfi_area_sqkm", Double.parseDouble(areaSqKm));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid area value: " + areaSqKm);
                }
            }
            
            String sensorType = skyfiDetail.getAttribute("sensor_type");
            if (sensorType != null && !sensorType.isEmpty()) {
                marker.setMetaString("skyfi_sensor_type", sensorType);
            }
            
            String orderId = skyfiDetail.getAttribute("order_id");
            if (orderId != null && !orderId.isEmpty()) {
                marker.setMetaString("skyfi_order_id", orderId);
            }
            
            String isAoi = skyfiDetail.getAttribute("is_aoi");
            if (isAoi != null && !isAoi.isEmpty()) {
                marker.setMetaString("skyfi_converted_to_aoi", isAoi);
            }
            
            String geometryType = skyfiDetail.getAttribute("geometry_type");
            if (geometryType != null && !geometryType.isEmpty()) {
                marker.setMetaString("skyfi_geometry_type", geometryType);
            }
            
            Log.d(TAG, "Extracted SkyFi metadata from CoT detail");
        }
    }
}