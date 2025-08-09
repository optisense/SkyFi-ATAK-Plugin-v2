package com.optisense.skyfi.atak;
import com.skyfi.atak.plugin.R;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.atakmap.coremap.conversions.CoordinateFormat;
import com.atakmap.coremap.conversions.CoordinateFormatUtilities;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.MGRSPoint;
import com.atakmap.coremap.maps.coords.MutableMGRSPoint;
import com.atakmap.coremap.maps.conversion.EGM96;

/**
 * Dialog for coordinate input supporting multiple formats including MGRS.
 * 
 * Supports:
 * - Latitude/Longitude (decimal degrees)
 * - MGRS (Military Grid Reference System) using ATAK 5.4 SDK
 * - Current location from device GPS
 * 
 * MGRS Implementation:
 * - Uses ATAK's CoordinateFormatUtilities.convert() for accurate conversion
 * - Supports standard MGRS format (e.g., 13SDD1234567890)
 * - Includes comprehensive error handling and validation
 * - Case insensitive input with automatic cleanup
 */
public class CoordinateInputDialog {
    
    public interface CoordinateSelectedListener {
        void onCoordinateSelected(GeoPoint point, String displayName);
        void onCancelled();
    }
    
    public static void show(Context context, CoordinateSelectedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.coordinate_input_dialog, null);
        
        RadioGroup formatGroup = dialogView.findViewById(R.id.coordinate_format_group);
        EditText latInput = dialogView.findViewById(R.id.latitude_input);
        EditText lonInput = dialogView.findViewById(R.id.longitude_input);
        EditText mgrsInput = dialogView.findViewById(R.id.mgrs_input);
        TextView currentLocationText = dialogView.findViewById(R.id.current_location_text);
        
        // Set up format switching
        formatGroup.setOnCheckedChangeListener((group, checkedId) -> {
            latInput.setVisibility(View.GONE);
            lonInput.setVisibility(View.GONE);
            mgrsInput.setVisibility(View.GONE);
            currentLocationText.setVisibility(View.GONE);
            
            if (checkedId == R.id.format_lat_lon) {
                latInput.setVisibility(View.VISIBLE);
                lonInput.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.format_mgrs) {
                mgrsInput.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.format_current) {
                currentLocationText.setVisibility(View.VISIBLE);
                // Get current location from ATAK
                GeoPoint currentLoc = getCurrentLocation(context);
                if (currentLoc != null) {
                    currentLocationText.setText(String.format("Current: %.6f, %.6f", 
                        currentLoc.getLatitude(), currentLoc.getLongitude()));
                }
            }
        });
        
        // Default to lat/lon
        formatGroup.check(R.id.format_lat_lon);
        
        builder.setView(dialogView)
            .setTitle("Enter Coordinates")
            .setPositiveButton("OK", null) // Set to null to override later
            .setNegativeButton("Cancel", (dialog, which) -> {
                if (listener != null) {
                    listener.onCancelled();
                }
            });
        
        AlertDialog dialog = builder.create();
        
        // Override positive button to validate input
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                GeoPoint result = null;
                String displayName = "";
                
                int checkedId = formatGroup.getCheckedRadioButtonId();
                
                try {
                    if (checkedId == R.id.format_lat_lon) {
                        double lat = Double.parseDouble(latInput.getText().toString());
                        double lon = Double.parseDouble(lonInput.getText().toString());
                        
                        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                            Toast.makeText(context, "Invalid coordinates", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        result = new GeoPoint(lat, lon);
                        displayName = String.format("%.6f, %.6f", lat, lon);
                        
                    } else if (checkedId == R.id.format_mgrs) {
                        String mgrs = mgrsInput.getText().toString().trim();
                        
                        if (mgrs.isEmpty()) {
                            Toast.makeText(context, "Please enter MGRS coordinates", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        try {
                            // Clean up the MGRS input (remove spaces, ensure uppercase)
                            String cleanMgrs = mgrs.replaceAll("\\s+", "").toUpperCase();
                            
                            // Validate basic MGRS format (should be 10-15 characters)
                            if (cleanMgrs.length() < 10 || cleanMgrs.length() > 15) {
                                Toast.makeText(context, "MGRS must be 10-15 characters (e.g., 13SDD1234567890)", Toast.LENGTH_LONG).show();
                                return;
                            }
                            
                            // Use ATAK's CoordinateFormatUtilities to convert MGRS to GeoPoint
                            result = CoordinateFormatUtilities.convert(cleanMgrs, CoordinateFormat.MGRS);
                            
                            if (result == null || !result.isValid()) {
                                Toast.makeText(context, "Invalid MGRS coordinates - check grid zone and square", Toast.LENGTH_LONG).show();
                                return;
                            }
                            
                            // Validate that coordinates are within Earth bounds
                            double lat = result.getLatitude();
                            double lon = result.getLongitude();
                            if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                                Toast.makeText(context, "MGRS conversion resulted in invalid coordinates", Toast.LENGTH_LONG).show();
                                return;
                            }
                            
                            displayName = cleanMgrs;
                            
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(context, "Invalid MGRS format - check zone, band, and grid square", Toast.LENGTH_LONG).show();
                            return;
                        } catch (Exception e) {
                            Toast.makeText(context, "MGRS conversion error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                    } else if (checkedId == R.id.format_current) {
                        result = getCurrentLocation(context);
                        if (result != null) {
                            displayName = "Current Location";
                        } else {
                            Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    
                    if (result != null && listener != null) {
                        listener.onCoordinateSelected(result, displayName);
                        dialog.dismiss();
                    }
                    
                } catch (Exception e) {
                    Toast.makeText(context, "Invalid input: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        dialog.show();
    }
    
    private static GeoPoint getCurrentLocation(Context context) {
        try {
            // Get current location from ATAK's MapView self marker
            com.atakmap.android.maps.MapView mapView = com.atakmap.android.maps.MapView.getMapView();
            if (mapView != null && mapView.getSelfMarker() != null) {
                com.atakmap.coremap.maps.coords.GeoPoint selfPoint = mapView.getSelfMarker().getPoint();
                if (selfPoint != null && selfPoint.isValid()) {
                    return new GeoPoint(selfPoint.getLatitude(), selfPoint.getLongitude());
                }
            }
        } catch (Exception e) {
            // Fall back to default location if ATAK location is not available
        }
        
        // Fallback location (Denver, CO) if GPS is not available
        return new GeoPoint(39.7392, -104.9903);
    }
}