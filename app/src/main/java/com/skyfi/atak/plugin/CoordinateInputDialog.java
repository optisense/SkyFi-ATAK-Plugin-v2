package com.skyfi.atak.plugin;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.conversions.CoordinateFormat;
import com.atakmap.coremap.conversions.CoordinateFormatUtilities;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.MGRSPoint;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTWriter;

import java.util.ArrayList;

import gov.tak.api.engine.map.coords.GeoCalculations;
import gov.tak.api.engine.map.coords.IGeoPoint;

/**
 * Dialog for inputting coordinates in various formats
 */
public class CoordinateInputDialog {
    private static final String LOGTAG = "CoordinateInputDialog";
    
    public interface CoordinateInputListener {
        void onCoordinateSelected(String wkt, double areaKm2, String coordinateString);
        void onCancelled();
    }
    
    private final Context context;
    private final CoordinateInputListener listener;
    private AlertDialog dialog;
    private RadioGroup formatGroup;
    private LinearLayout inputContainer;
    private SeekBar radiusSeekBar;
    private TextView radiusText;
    private TextView sensorRequirementsText;
    private TextView validationWarningText;
    private LinearLayout providerStatusContainer;
    private Button okButton;
    private int selectedRadius = 5;
    
    // Input fields for different formats
    private EditText latLonInput;
    private EditText mgrsInput;
    private EditText latInput;
    private EditText lonInput;
    
    public CoordinateInputDialog(Context context, CoordinateInputListener listener) {
        this.context = context;
        this.listener = listener;
    }
    
    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapView.getMapView().getContext());
        builder.setTitle(context.getString(R.string.enter_coordinates));
        
        View view = createDialogView();
        builder.setView(view);
        
        builder.setPositiveButton(context.getString(R.string.ok), null);
        builder.setNegativeButton(context.getString(R.string.cancel), 
            (dialog, which) -> listener.onCancelled());
        
        dialog = builder.create();
        dialog.show();
        
        // Override positive button to prevent auto-dismiss on error
        okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        okButton.setOnClickListener(v -> processCoordinate());
        
        updateInputFields();
    }
    
    private View createDialogView() {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);
        
        // Format selection
        TextView formatLabel = new TextView(context);
        formatLabel.setText("Coordinate Format:");
        layout.addView(formatLabel);
        
        formatGroup = new RadioGroup(context);
        
        RadioButton pinDropRadio = new RadioButton(context);
        pinDropRadio.setText("Pin Drop on Map");
        pinDropRadio.setId(View.generateViewId());
        formatGroup.addView(pinDropRadio);
        
        RadioButton latLonRadio = new RadioButton(context);
        latLonRadio.setText("Lat/Lon (Decimal)");
        latLonRadio.setId(View.generateViewId());
        formatGroup.addView(latLonRadio);
        
        RadioButton latLonSeparateRadio = new RadioButton(context);
        latLonSeparateRadio.setText("Lat/Lon (Separate)");
        latLonSeparateRadio.setId(View.generateViewId());
        formatGroup.addView(latLonSeparateRadio);
        
        RadioButton mgrsRadio = new RadioButton(context);
        mgrsRadio.setText("MGRS");
        mgrsRadio.setId(View.generateViewId());
        formatGroup.addView(mgrsRadio);
        
        RadioButton currentLocationRadio = new RadioButton(context);
        currentLocationRadio.setText("Current Location");
        currentLocationRadio.setId(View.generateViewId());
        formatGroup.addView(currentLocationRadio);
        
        formatGroup.check(currentLocationRadio.getId());
        formatGroup.setOnCheckedChangeListener((group, checkedId) -> updateInputFields());
        
        layout.addView(formatGroup);
        
        // Input container
        inputContainer = new LinearLayout(context);
        inputContainer.setOrientation(LinearLayout.VERTICAL);
        inputContainer.setPadding(0, 20, 0, 20);
        layout.addView(inputContainer);
        
        // AOI size selection
        TextView sizeLabel = new TextView(context);
        sizeLabel.setText("AOI Size:");
        sizeLabel.setPadding(0, 20, 0, 10);
        layout.addView(sizeLabel);
        
        LinearLayout sizeLayout = new LinearLayout(context);
        sizeLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        radiusSeekBar = new SeekBar(context);
        radiusSeekBar.setMin(5);
        radiusSeekBar.setMax(45);
        radiusSeekBar.setProgress(selectedRadius);
        radiusSeekBar.setLayoutParams(new LinearLayout.LayoutParams(0, 
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedRadius = progress;
                radiusText.setText(progress + " km");
                updateSensorRequirements();
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        sizeLayout.addView(radiusSeekBar);
        
        radiusText = new TextView(context);
        radiusText.setText(selectedRadius + " km");
        radiusText.setPadding(10, 0, 0, 0);
        sizeLayout.addView(radiusText);
        
        layout.addView(sizeLayout);
        
        // Sensor requirements
        sensorRequirementsText = new TextView(context);
        sensorRequirementsText.setPadding(0, 10, 0, 0);
        sensorRequirementsText.setTextSize(12);
        layout.addView(sensorRequirementsText);
        
        // Validation warning
        validationWarningText = new TextView(context);
        validationWarningText.setPadding(10, 10, 10, 10);
        validationWarningText.setTextSize(14);
        validationWarningText.setTextColor(0xFFFFFFFF);
        layout.addView(validationWarningText);
        
        // Provider status container
        providerStatusContainer = new LinearLayout(context);
        providerStatusContainer.setOrientation(LinearLayout.VERTICAL);
        providerStatusContainer.setPadding(0, 10, 0, 0);
        layout.addView(providerStatusContainer);
        
        updateSensorRequirements();
        
        return layout;
    }
    
    private void updateInputFields() {
        inputContainer.removeAllViews();
        
        int checkedId = formatGroup.getCheckedRadioButtonId();
        RadioButton checked = formatGroup.findViewById(checkedId);
        if (checked == null) return;
        
        String format = checked.getText().toString();
        
        if (format.contains("Pin Drop")) {
            TextView instruction = new TextView(context);
            instruction.setText("Close this dialog and tap on the map to select location");
            inputContainer.addView(instruction);
            
            // TODO: Implement map tap listener
            
        } else if (format.contains("Decimal")) {
            latLonInput = new EditText(context);
            latLonInput.setHint("e.g., 40.7128, -74.0060");
            latLonInput.setInputType(InputType.TYPE_CLASS_TEXT);
            inputContainer.addView(latLonInput);
            
        } else if (format.contains("Separate")) {
            latInput = new EditText(context);
            latInput.setHint("Latitude (e.g., 40.7128)");
            latInput.setInputType(InputType.TYPE_CLASS_NUMBER | 
                InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            inputContainer.addView(latInput);
            
            lonInput = new EditText(context);
            lonInput.setHint("Longitude (e.g., -74.0060)");
            lonInput.setInputType(InputType.TYPE_CLASS_NUMBER | 
                InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            inputContainer.addView(lonInput);
            
        } else if (format.contains("MGRS")) {
            mgrsInput = new EditText(context);
            mgrsInput.setHint("e.g., 33TWN8396406925 or 18T WL 89009 13758");
            mgrsInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            inputContainer.addView(mgrsInput);
            
            TextView mgrsHelp = new TextView(context);
            mgrsHelp.setText("Enter MGRS coordinates with or without spaces");
            mgrsHelp.setTextSize(12);
            mgrsHelp.setPadding(0, 5, 0, 0);
            inputContainer.addView(mgrsHelp);
            
        } else if (format.contains("Current")) {
            TextView instruction = new TextView(context);
            instruction.setText("Will use your current GPS location");
            inputContainer.addView(instruction);
        }
    }
    
    private void updateSensorRequirements() {
        double areaKm2 = selectedRadius * selectedRadius;
        String requirements = String.format("AOI Area: %s\n", AOISizeValidator.formatArea(areaKm2));
        sensorRequirementsText.setText(requirements);
        
        // Get validation results
        AOISizeValidator.ValidationResult validation = AOISizeValidator.validateAOISize(areaKm2);
        
        // Update warning text
        validationWarningText.setText(validation.overallWarning);
        int bgColor = AOISizeValidator.getWarningColor(validation.warningLevel);
        validationWarningText.setBackgroundColor(bgColor & 0xCCFFFFFF); // Semi-transparent
        
        // Update provider status
        providerStatusContainer.removeAllViews();
        
        TextView providerHeader = new TextView(context);
        providerHeader.setText("Provider Compatibility:");
        providerHeader.setTextSize(12);
        providerHeader.setPadding(0, 0, 0, 5);
        providerStatusContainer.addView(providerHeader);
        
        for (AOISizeValidator.ProviderCompatibility pc : validation.providerResults) {
            TextView providerText = new TextView(context);
            String icon = pc.isCompatible ? "✓" : "✗";
            int textColor = pc.isCompatible ? 0xFF4CAF50 : 0xFFFF5252;
            
            providerText.setText(String.format("%s %s - %s", 
                icon,
                pc.provider.substring(0, 1).toUpperCase() + pc.provider.substring(1),
                pc.reason));
            providerText.setTextColor(textColor);
            providerText.setTextSize(11);
            providerText.setPadding(20, 2, 0, 2);
            providerStatusContainer.addView(providerText);
        }
        
        // Enable/disable OK button based on validation
        if (okButton != null) {
            okButton.setEnabled(validation.hasCompatibleProviders && areaKm2 <= 2000);
            if (!okButton.isEnabled()) {
                okButton.setText(areaKm2 > 2000 ? "Area Too Large" : "Area Too Small");
            } else {
                okButton.setText(context.getString(R.string.ok));
            }
        }
    }
    
    private void processCoordinate() {
        try {
            GeoPoint point = null;
            String coordinateString = "";
            
            int checkedId = formatGroup.getCheckedRadioButtonId();
            RadioButton checked = formatGroup.findViewById(checkedId);
            if (checked == null) return;
            
            String format = checked.getText().toString();
            
            if (format.contains("Current")) {
                point = MapView.getMapView().getSelfMarker().getPoint();
                if (point.getLatitude() == 0 && point.getLongitude() == 0) {
                    showError("No GPS location available");
                    return;
                }
                coordinateString = String.format("%.6f, %.6f", 
                    point.getLatitude(), point.getLongitude());
                    
            } else if (format.contains("Decimal") && latLonInput != null) {
                String input = latLonInput.getText().toString().trim();
                if (input.isEmpty()) {
                    showError("Please enter coordinates");
                    return;
                }
                
                String[] parts = input.split(",");
                if (parts.length != 2) {
                    showError("Invalid format. Use: lat, lon");
                    return;
                }
                
                double lat = Double.parseDouble(parts[0].trim());
                double lon = Double.parseDouble(parts[1].trim());
                point = new GeoPoint(lat, lon);
                coordinateString = input;
                
            } else if (format.contains("Separate")) {
                if (latInput == null || lonInput == null) return;
                
                String latStr = latInput.getText().toString().trim();
                String lonStr = lonInput.getText().toString().trim();
                
                if (latStr.isEmpty() || lonStr.isEmpty()) {
                    showError("Please enter both latitude and longitude");
                    return;
                }
                
                double lat = Double.parseDouble(latStr);
                double lon = Double.parseDouble(lonStr);
                point = new GeoPoint(lat, lon);
                coordinateString = String.format("%.6f, %.6f", lat, lon);
                
            } else if (format.contains("MGRS") && mgrsInput != null) {
                String mgrs = mgrsInput.getText().toString().trim();
                if (mgrs.isEmpty()) {
                    showError("Please enter MGRS coordinates");
                    return;
                }
                
                try {
                    // Clean up MGRS string - remove extra spaces and convert to uppercase
                    mgrs = mgrs.replaceAll("\\s+", " ").trim().toUpperCase();
                    
                    // Try to parse MGRS using CoordinateFormatUtilities
                    // The convert method can parse MGRS strings and return a GeoPoint
                    point = CoordinateFormatUtilities.convert(mgrs, CoordinateFormat.MGRS);
                    
                    if (point == null) {
                        showError("Invalid MGRS format. Example: 33TWN8396406925 or 18T WL 89009 13758");
                        return;
                    }
                    
                    // Validate the parsed coordinates
                    if (Math.abs(point.getLatitude()) > 90 || Math.abs(point.getLongitude()) > 180) {
                        showError("Invalid MGRS coordinates - outside valid range");
                        return;
                    }
                    
                    coordinateString = mgrs;
                    Log.d(LOGTAG, "Parsed MGRS: " + mgrs + " to " + point.getLatitude() + ", " + point.getLongitude());
                    
                } catch (Exception e) {
                    Log.e(LOGTAG, "Failed to parse MGRS: " + mgrs, e);
                    showError("Invalid MGRS format. Example: 33TWN8396406925 or 18T WL 89009 13758");
                    return;
                }
            }
            
            if (point != null) {
                String wkt = createSquareWKT(point, selectedRadius);
                double areaKm2 = selectedRadius * selectedRadius;
                
                if (listener != null) {
                    listener.onCoordinateSelected(wkt, areaKm2, coordinateString);
                }
                dialog.dismiss();
            }
            
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to process coordinate", e);
            showError("Invalid coordinate format");
        }
    }
    
    private String createSquareWKT(GeoPoint center, double radiusKm) {
        double radiusMeters = radiusKm * 1000;
        
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        
        // Calculate square corners using simple lat/lon offset
        double latOffset = radiusMeters / 111320.0; // meters to degrees latitude
        double lonOffset = radiusMeters / (111320.0 * Math.cos(Math.toRadians(center.getLatitude())));
        
        GeoPoint north = new GeoPoint(center.getLatitude() + latOffset, center.getLongitude());
        GeoPoint east = new GeoPoint(center.getLatitude(), center.getLongitude() + lonOffset);
        GeoPoint south = new GeoPoint(center.getLatitude() - latOffset, center.getLongitude());
        GeoPoint west = new GeoPoint(center.getLatitude(), center.getLongitude() - lonOffset);
        
        // Create square
        Coordinate northEast = new Coordinate(east.getLongitude(), north.getLatitude());
        Coordinate southEast = new Coordinate(east.getLongitude(), south.getLatitude());
        Coordinate southWest = new Coordinate(west.getLongitude(), south.getLatitude());
        Coordinate northWest = new Coordinate(west.getLongitude(), north.getLatitude());
        
        coordinates.add(northEast);
        coordinates.add(southEast);
        coordinates.add(southWest);
        coordinates.add(northWest);
        coordinates.add(northEast); // Close the polygon
        
        try {
            GeometryFactory factory = new GeometryFactory(new PrecisionModel(10000000.0));
            Polygon polygon = factory.createPolygon(coordinates.toArray(new Coordinate[0]));
            WKTWriter wktWriter = new WKTWriter();
            return wktWriter.write(polygon);
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to create WKT", e);
            return null;
        }
    }
    
    private void showError(String message) {
        new AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }
}