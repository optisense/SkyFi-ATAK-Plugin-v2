package com.skyfi.atak.plugin;

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
                        // For now, show error that MGRS is not yet supported
                        Toast.makeText(context, "MGRS input coming soon", Toast.LENGTH_SHORT).show();
                        return;
                        // TODO: Implement MGRS parsing when API is clarified
                        // displayName = mgrs;
                        
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
        // In a real implementation, this would get the current location from ATAK's GPS service
        // For now, return a default location
        // TODO: Integrate with ATAK's location service
        return new GeoPoint(39.7392, -104.9903); // Denver, CO
    }
}