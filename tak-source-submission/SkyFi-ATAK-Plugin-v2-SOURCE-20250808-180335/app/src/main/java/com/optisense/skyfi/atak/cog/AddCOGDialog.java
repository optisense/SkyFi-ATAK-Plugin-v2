package com.optisense.skyfi.atak.cog;
import com.skyfi.atak.plugin.R;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoBounds;

import com.skyfi.atak.plugin.R;

/**
 * Dialog for adding Cloud Optimized GeoTIFF layers
 */
public class AddCOGDialog {
    
    private static final String TAG = "SkyFi.AddCOGDialog";
    
    private final Context context;
    private final MapView mapView;
    private final COGLayerManager layerManager;
    
    public AddCOGDialog(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        this.layerManager = COGLayerManager.getInstance(context, mapView);
    }
    
    /**
     * Show dialog to add a COG from a URL
     */
    public void showAddDialog() {
        // Create custom view for dialog
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_cog, null);
        
        EditText nameInput = dialogView.findViewById(R.id.cog_name_input);
        EditText urlInput = dialogView.findViewById(R.id.cog_url_input);
        EditText minLatInput = dialogView.findViewById(R.id.cog_min_lat);
        EditText minLonInput = dialogView.findViewById(R.id.cog_min_lon);
        EditText maxLatInput = dialogView.findViewById(R.id.cog_max_lat);
        EditText maxLonInput = dialogView.findViewById(R.id.cog_max_lon);
        
        // Set default values
        nameInput.setText("SkyFi Imagery");
        urlInput.setHint("https://example.com/imagery.tif");
        
        // Default to current map bounds
        GeoBounds currentBounds = mapView.getBounds();
        if (currentBounds != null) {
            minLatInput.setText(String.valueOf(currentBounds.getSouth()));
            minLonInput.setText(String.valueOf(currentBounds.getWest()));
            maxLatInput.setText(String.valueOf(currentBounds.getNorth()));
            maxLonInput.setText(String.valueOf(currentBounds.getEast()));
        } else {
            // Default world bounds
            minLatInput.setText("-90");
            minLonInput.setText("-180");
            maxLatInput.setText("90");
            maxLonInput.setText("180");
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Cloud Optimized GeoTIFF")
               .setView(dialogView)
               .setPositiveButton("Add", null) // Set to null, we'll override later
               .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
               .setNeutralButton("From Order", (dialog, which) -> {
                   showOrderSelectionDialog();
               });
        
        AlertDialog dialog = builder.create();
        
        // Override positive button to validate before closing
        dialog.setOnShowListener(dialogInterface -> {
            Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addButton.setOnClickListener(view -> {
                String name = nameInput.getText().toString().trim();
                String url = urlInput.getText().toString().trim();
                
                if (name.isEmpty()) {
                    nameInput.setError("Name is required");
                    return;
                }
                
                if (url.isEmpty()) {
                    urlInput.setError("URL is required");
                    return;
                }
                
                try {
                    double minLat = Double.parseDouble(minLatInput.getText().toString());
                    double minLon = Double.parseDouble(minLonInput.getText().toString());
                    double maxLat = Double.parseDouble(maxLatInput.getText().toString());
                    double maxLon = Double.parseDouble(maxLonInput.getText().toString());
                    
                    GeoBounds bounds = new GeoBounds(minLat, minLon, maxLat, maxLon);
                    
                    // Add the COG layer
                    String layerId = layerManager.addCOGLayer(name, url, bounds);
                    
                    if (layerId != null) {
                        Toast.makeText(context, "COG layer added: " + name, Toast.LENGTH_SHORT).show();
                        layerManager.setLayerVisibility(layerId, true);
                        layerManager.panToLayer(layerId);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "Failed to add COG layer", Toast.LENGTH_LONG).show();
                    }
                    
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Invalid coordinates", Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        dialog.show();
    }
    
    /**
     * Show dialog to select from recent SkyFi orders
     */
    private void showOrderSelectionDialog() {
        // This would integrate with SkyFi API to list recent orders
        // For now, show a placeholder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select SkyFi Order")
               .setMessage("Integration with SkyFi orders coming soon!\n\n" +
                          "This will allow you to select from your recent " +
                          "satellite imagery orders and automatically load them as COG layers.")
               .setPositiveButton("OK", null);
        builder.show();
    }
    
    /**
     * Add a COG from a SkyFi order
     */
    public void addFromSkyFiOrder(String orderId, String orderName, String cogUrl,
                                  double minLat, double minLon, double maxLat, double maxLon) {
        String layerId = layerManager.addCOGFromOrder(orderId, orderName, cogUrl,
                                                      minLat, minLon, maxLat, maxLon);
        
        if (layerId != null) {
            Toast.makeText(context, "SkyFi order loaded: " + orderName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to load SkyFi order", Toast.LENGTH_LONG).show();
        }
    }
}