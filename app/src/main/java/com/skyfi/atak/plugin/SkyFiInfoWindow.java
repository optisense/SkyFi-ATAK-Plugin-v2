package com.skyfi.atak.plugin;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.Shape;
import com.atakmap.android.util.ATAKUtilities;
import com.atakmap.coremap.conversions.CoordinateFormat;
import com.atakmap.coremap.conversions.CoordinateFormatUtilities;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Custom info window for SkyFi points of interest with enhanced styling
 */
public class SkyFiInfoWindow {
    private static final String TAG = "SkyFiInfoWindow";
    
    private final Context context;
    private final MapView mapView;
    private View infoWindowView;
    private ViewGroup container;
    
    // UI Components
    private TextView titleText;
    private TextView subtitleText;
    private TextView coordinatesText;
    private TextView areaText;
    private TextView statusText;
    private TextView timestampText;
    private LinearLayout actionContainer;
    private View statusIndicator;
    
    public interface InfoWindowActionListener {
        void onTaskAreaClicked(MapItem item);
        void onViewDetailsClicked(MapItem item);
        void onShareClicked(MapItem item);
        void onDeleteClicked(MapItem item);
    }
    
    private InfoWindowActionListener actionListener;
    
    public SkyFiInfoWindow(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        setupInfoWindow();
    }
    
    private void setupInfoWindow() {
        // Create custom info window layout programmatically
        LinearLayout linearContainer = new LinearLayout(context);
        linearContainer.setOrientation(LinearLayout.VERTICAL);
        container = linearContainer;
        container.setBackgroundDrawable(createBackgroundDrawable());
        container.setPadding(20, 20, 20, 20);
        
        // Title section
        LinearLayout titleSection = new LinearLayout(context);
        titleSection.setOrientation(LinearLayout.HORIZONTAL);
        titleSection.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        statusIndicator = new View(context);
        statusIndicator.setLayoutParams(new LinearLayout.LayoutParams(12, 12));
        GradientDrawable statusDrawable = new GradientDrawable();
        statusDrawable.setShape(GradientDrawable.OVAL);
        statusDrawable.setColor(SkyFiPolygonStyle.SKYFI_PRIMARY);
        statusIndicator.setBackground(statusDrawable);
        ((LinearLayout.LayoutParams)statusIndicator.getLayoutParams()).setMargins(0, 6, 10, 0);
        
        titleText = new TextView(context);
        titleText.setTextSize(18);
        titleText.setTextColor(Color.WHITE);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        
        titleSection.addView(statusIndicator);
        titleSection.addView(titleText);
        container.addView(titleSection);
        
        // Subtitle
        subtitleText = new TextView(context);
        subtitleText.setTextSize(14);
        subtitleText.setTextColor(0xFFCCCCCC);
        subtitleText.setPadding(22, 5, 0, 10);
        container.addView(subtitleText);
        
        // Divider
        View divider1 = new View(context);
        divider1.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1
        ));
        divider1.setBackgroundColor(0x33FFFFFF);
        ((LinearLayout.LayoutParams)divider1.getLayoutParams()).setMargins(0, 10, 0, 10);
        container.addView(divider1);
        
        // Info section
        LinearLayout infoSection = new LinearLayout(context);
        infoSection.setOrientation(LinearLayout.VERTICAL);
        infoSection.setPadding(0, 5, 0, 5);
        
        // Coordinates
        coordinatesText = new TextView(context);
        coordinatesText.setTextSize(12);
        coordinatesText.setTextColor(SkyFiPolygonStyle.SKYFI_SECONDARY);
        coordinatesText.setPadding(0, 2, 0, 2);
        infoSection.addView(coordinatesText);
        
        // Area
        areaText = new TextView(context);
        areaText.setTextSize(14);
        areaText.setTextColor(SkyFiPolygonStyle.SKYFI_ACCENT);
        areaText.setPadding(0, 2, 0, 2);
        infoSection.addView(areaText);
        
        // Status
        statusText = new TextView(context);
        statusText.setTextSize(12);
        statusText.setTextColor(0xFFFFFFFF);
        statusText.setPadding(0, 2, 0, 2);
        infoSection.addView(statusText);
        
        // Timestamp
        timestampText = new TextView(context);
        timestampText.setTextSize(11);
        timestampText.setTextColor(0xFF999999);
        timestampText.setPadding(0, 2, 0, 2);
        infoSection.addView(timestampText);
        
        container.addView(infoSection);
        
        // Divider
        View divider2 = new View(context);
        divider2.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1
        ));
        divider2.setBackgroundColor(0x33FFFFFF);
        ((LinearLayout.LayoutParams)divider2.getLayoutParams()).setMargins(0, 10, 0, 10);
        container.addView(divider2);
        
        // Action buttons
        actionContainer = new LinearLayout(context);
        actionContainer.setOrientation(LinearLayout.HORIZONTAL);
        actionContainer.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        container.addView(actionContainer);
    }
    
    private GradientDrawable createBackgroundDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(12);
        drawable.setColor(0xE6000000); // Semi-transparent black
        drawable.setStroke(2, SkyFiPolygonStyle.SKYFI_PRIMARY);
        return drawable;
    }
    
    public void showForMapItem(MapItem item) {
        if (item == null) return;
        
        // Update content based on item type
        if (item instanceof Shape) {
            showForShape((Shape) item);
        } else if (item instanceof Marker) {
            showForMarker((Marker) item);
        }
        
        // Position and show the window
        positionWindow(item);
    }
    
    private void showForShape(Shape shape) {
        // Set title
        String title = shape.getTitle();
        if (title == null || title.isEmpty()) {
            title = "SkyFi AOI";
        }
        titleText.setText(title);
        
        // Set subtitle based on type
        String type = shape.getMetaString("skyfi_style_type", "polygon");
        subtitleText.setText("Type: " + capitalize(type));
        
        // Set coordinates (centroid)
        GeoPoint center = shape.getCenter().get();
        String coords = CoordinateFormatUtilities.formatToString(
            center, CoordinateFormat.MGRS
        );
        coordinatesText.setText("Center: " + coords);
        
        // Set area if available
        double area = shape.getMetaDouble("skyfi_area_km2", -1);
        if (area > 0) {
            areaText.setText(String.format("Area: %.2f km²", area));
            areaText.setVisibility(View.VISIBLE);
        } else {
            areaText.setVisibility(View.GONE);
        }
        
        // Set status
        String status = shape.getMetaString("skyfi_task_status", "");
        if (!status.isEmpty()) {
            statusText.setText("Status: " + capitalize(status));
            statusText.setVisibility(View.VISIBLE);
            updateStatusIndicator(status);
        } else {
            statusText.setVisibility(View.GONE);
        }
        
        // Set timestamp
        long timestamp = shape.getMetaLong("timestamp", System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.US);
        timestampText.setText("Created: " + sdf.format(new Date(timestamp)));
        
        // Setup action buttons
        setupShapeActions(shape);
    }
    
    private void showForMarker(Marker marker) {
        // Set title
        titleText.setText(marker.getTitle());
        
        // Set type
        subtitleText.setText("SkyFi Point of Interest");
        
        // Set coordinates
        String coords = CoordinateFormatUtilities.formatToString(
            marker.getPoint(), CoordinateFormat.MGRS
        );
        coordinatesText.setText("Location: " + coords);
        
        // Hide area for markers
        areaText.setVisibility(View.GONE);
        
        // Set status if available
        String status = marker.getMetaString("skyfi_status", "");
        if (!status.isEmpty()) {
            statusText.setText("Status: " + capitalize(status));
            statusText.setVisibility(View.VISIBLE);
            updateStatusIndicator(status);
        } else {
            statusText.setVisibility(View.GONE);
        }
        
        // Set timestamp
        long timestamp = marker.getMetaLong("timestamp", System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.US);
        timestampText.setText("Added: " + sdf.format(new Date(timestamp)));
        
        // Setup action buttons
        setupMarkerActions(marker);
    }
    
    private void setupShapeActions(final Shape shape) {
        actionContainer.removeAllViews();
        
        // Task Area button
        TextView taskButton = createActionButton("Task Area", SkyFiPolygonStyle.SKYFI_PRIMARY);
        taskButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onTaskAreaClicked(shape);
            }
        });
        actionContainer.addView(taskButton);
        
        // View Details button
        TextView detailsButton = createActionButton("Details", SkyFiPolygonStyle.SKYFI_SECONDARY);
        detailsButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onViewDetailsClicked(shape);
            }
        });
        actionContainer.addView(detailsButton);
        
        // Share button
        TextView shareButton = createActionButton("Share", SkyFiPolygonStyle.SKYFI_ACCENT);
        shareButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onShareClicked(shape);
            }
        });
        actionContainer.addView(shareButton);
    }
    
    private void setupMarkerActions(final Marker marker) {
        actionContainer.removeAllViews();
        
        // View Details button
        TextView detailsButton = createActionButton("Details", SkyFiPolygonStyle.SKYFI_SECONDARY);
        detailsButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onViewDetailsClicked(marker);
            }
        });
        actionContainer.addView(detailsButton);
        
        // Share button
        TextView shareButton = createActionButton("Share", SkyFiPolygonStyle.SKYFI_ACCENT);
        shareButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onShareClicked(marker);
            }
        });
        actionContainer.addView(shareButton);
        
        // Delete button
        TextView deleteButton = createActionButton("Delete", SkyFiPolygonStyle.SKYFI_ERROR);
        deleteButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteClicked(marker);
            }
        });
        actionContainer.addView(deleteButton);
    }
    
    private TextView createActionButton(String text, int color) {
        TextView button = new TextView(context);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(12);
        button.setPadding(15, 8, 15, 8);
        
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(8);
        background.setColor(color);
        button.setBackground(background);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 10, 0);
        button.setLayoutParams(params);
        
        return button;
    }
    
    private void updateStatusIndicator(String status) {
        GradientDrawable drawable = (GradientDrawable) statusIndicator.getBackground();
        
        switch (status.toLowerCase()) {
            case "active":
            case "completed":
                drawable.setColor(SkyFiPolygonStyle.SKYFI_SUCCESS);
                break;
            case "pending":
                drawable.setColor(SkyFiPolygonStyle.SKYFI_WARNING);
                break;
            case "failed":
            case "error":
                drawable.setColor(SkyFiPolygonStyle.SKYFI_ERROR);
                break;
            default:
                drawable.setColor(SkyFiPolygonStyle.SKYFI_PRIMARY);
                break;
        }
    }
    
    private void positionWindow(MapItem item) {
        // Calculate position based on item location
        GeoPoint point = null;
        if (item instanceof Marker) {
            point = ((Marker) item).getPoint();
        } else if (item instanceof Shape) {
            point = ((Shape) item).getCenter().get();
        }
        
        if (point != null) {
            // Convert geo point to screen coordinates
            android.graphics.PointF screenPoint = mapView.forward(point);
            
            // Position the window above the item
            if (container.getParent() != null) {
                ((ViewGroup) container.getParent()).removeView(container);
            }
            
            // Add to map view
            mapView.addView(container);
            
            // Position with offset
            container.setX(screenPoint.x - container.getWidth() / 2);
            container.setY(screenPoint.y - container.getHeight() - 50);
        }
    }
    
    public void hide() {
        if (container != null && container.getParent() != null) {
            ((ViewGroup) container.getParent()).removeView(container);
        }
    }
    
    public void setActionListener(InfoWindowActionListener listener) {
        this.actionListener = listener;
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}