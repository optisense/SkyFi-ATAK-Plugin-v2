package com.skyfi.atak.plugin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.skyfi.atak.plugin.skyfiapi.Archive;
import com.skyfi.atak.plugin.skyfiapi.ArchiveOrder;
import com.skyfi.atak.plugin.skyfiapi.ArchiveResponse;
import com.skyfi.atak.plugin.skyfiapi.ArchivesRequest;
import com.skyfi.atak.plugin.skyfiapi.SkyFiAPI;

import org.json.JSONObject;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageryPreviewManager implements MapEventDispatcher.MapEventDispatchListener {
    private static final String LOGTAG = "ImageryPreviewManager";
    private static final double PREVIEW_RADIUS_KM = 5.0; // 5km radius for preview search
    private static final int MAX_PREVIEW_RESULTS = 6; // Max thumbnails to show in preview

    private Context context;
    private MapView mapView;
    private boolean previewModeEnabled = false;
    private PopupWindow currentPopup;
    private SkyFiAPI apiClient;
    private PreviewThumbnailAdapter thumbnailAdapter;

    // UI components
    private View popupView;
    private ProgressBar loadingIndicator;
    private LinearLayout contentContainer;
    private TextView locationText;
    private TextView noResultsText;
    private RecyclerView thumbnailGrid;
    private Button fullSearchButton;
    private Button orderButton;
    private Button closeButton;

    public ImageryPreviewManager(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        this.apiClient = new APIClient().getApiClient();
        initializePopupView();
    }

    private void initializePopupView() {
        // Inflate the popup layout
        popupView = PluginLayoutInflater.inflate(context, R.layout.image_preview_popup, null);

        // Get references to UI components
        loadingIndicator = popupView.findViewById(R.id.loading_indicator);
        contentContainer = popupView.findViewById(R.id.content_container);
        locationText = popupView.findViewById(R.id.location_text);
        noResultsText = popupView.findViewById(R.id.no_results_text);
        thumbnailGrid = popupView.findViewById(R.id.thumbnail_grid);
        fullSearchButton = popupView.findViewById(R.id.full_search_button);
        orderButton = popupView.findViewById(R.id.order_button);
        closeButton = popupView.findViewById(R.id.close_button);

        // Setup thumbnail grid
        thumbnailGrid.setLayoutManager(new GridLayoutManager(context, 3));
        thumbnailAdapter = new PreviewThumbnailAdapter(context);
        thumbnailGrid.setAdapter(thumbnailAdapter);

        // Setup click listeners
        closeButton.setOnClickListener(v -> hidePreview());

        thumbnailAdapter.setOnThumbnailClickListener(this::onThumbnailSelected);
    }

    public void enablePreviewMode() {
        if (!previewModeEnabled) {
            previewModeEnabled = true;
            mapView.getMapEventDispatcher().addMapEventListener(MapEvent.MAP_CLICK, this);
            Toast.makeText(context, context.getString(R.string.preview_mode_enabled), Toast.LENGTH_SHORT).show();
            Log.d(LOGTAG, "Preview mode enabled");
        }
    }

    public void disablePreviewMode() {
        if (previewModeEnabled) {
            previewModeEnabled = false;
            mapView.getMapEventDispatcher().removeMapEventListener(MapEvent.MAP_CLICK, this);
            hidePreview();
            Toast.makeText(context, context.getString(R.string.preview_mode_disabled), Toast.LENGTH_SHORT).show();
            Log.d(LOGTAG, "Preview mode disabled");
        }
    }

    public boolean isPreviewModeEnabled() {
        return previewModeEnabled;
    }

    @Override
    public void onMapEvent(MapEvent event) {
        if (!previewModeEnabled || !MapEvent.MAP_CLICK.equals(event.getType())) {
            return;
        }

        // Convert screen coordinates to geo coordinates
        PointF point = event.getPointF();
        GeoPoint geoPoint = mapView.inverse(point.x, point.y).get();

        if (geoPoint != null) {
            showPreviewAtLocation(geoPoint, point);
        }
    }

    private void showPreviewAtLocation(GeoPoint geoPoint, PointF screenPoint) {
        Log.d(LOGTAG, "Showing preview at: " + geoPoint.getLatitude() + ", " + geoPoint.getLongitude());

        // Hide any existing popup
        hidePreview();

        // Update location text
        locationText.setText(String.format("Lat: %.3f, Lon: %.3f", 
            geoPoint.getLatitude(), geoPoint.getLongitude()));

        // Show loading state
        showLoadingState();

        // Create popup window
        currentPopup = new PopupWindow(popupView, 
            ViewGroup.LayoutParams.WRAP_CONTENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT, 
            true);

        // Show popup near the clicked point
        int[] location = new int[2];
        mapView.getLocationOnScreen(location);
        int x = (int) (location[0] + screenPoint.x - 150); // Center popup on click point
        int y = (int) (location[1] + screenPoint.y - 200);

        // Ensure popup stays on screen
        x = Math.max(0, Math.min(x, mapView.getWidth() - 300));
        y = Math.max(0, Math.min(y, mapView.getHeight() - 400));

        currentPopup.showAtLocation(mapView, Gravity.NO_GRAVITY, x, y);

        // Search for imagery at this location
        searchImageryAtLocation(geoPoint);
    }

    private void searchImageryAtLocation(GeoPoint geoPoint) {
        // Create a small AOI around the clicked point
        String aoi = createAOIAroundPoint(geoPoint, PREVIEW_RADIUS_KM);
        if (aoi == null) {
            showError("Failed to create search area");
            return;
        }

        // Create a basic archive search request
        ArchivesRequest request = new ArchivesRequest();
        request.setAoi(aoi);
        // Set some basic filters for faster preview
        request.setMaxCloudCoveragePercent(30.0f); // Only low cloud coverage
        request.setOpenData(false); // Exclude open data for faster response

        // Store the request for full search button
        fullSearchButton.setOnClickListener(v -> {
            hidePreview();
            launchFullSearch(aoi);
        });

        // Make the API call
        apiClient.searchArchives(request).enqueue(new Callback<ArchiveResponse>() {
            @Override
            public void onResponse(@NonNull Call<ArchiveResponse> call, @NonNull Response<ArchiveResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleSearchResponse(response.body(), aoi);
                } else {
                    Log.e(LOGTAG, "Search failed with code: " + response.code());
                    showNoResults();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArchiveResponse> call, @NonNull Throwable t) {
                Log.e(LOGTAG, "Search request failed", t);
                showNoResults();
            }
        });
    }

    private void handleSearchResponse(ArchiveResponse response, String aoi) {
        mapView.post(() -> {
            if (currentPopup == null || !currentPopup.isShowing()) {
                return; // Popup was closed while loading
            }

            List<Archive> archives = response.getArchives();
            if (archives != null && !archives.isEmpty()) {
                // Limit results for preview
                List<Archive> limitedArchives = archives.subList(0, Math.min(archives.size(), MAX_PREVIEW_RESULTS));
                
                // Sort by capture date (newest first)
                Collections.sort(limitedArchives, (a1, a2) -> {
                    if (a1.getCaptureTimestamp() == null || a2.getCaptureTimestamp() == null) {
                        return 0;
                    }
                    return a2.getCaptureTimestamp().compareTo(a1.getCaptureTimestamp());
                });

                showResults(limitedArchives, aoi);
            } else {
                showNoResults();
            }
        });
    }

    private void showLoadingState() {
        loadingIndicator.setVisibility(View.VISIBLE);
        contentContainer.setVisibility(View.GONE);
    }

    private void showResults(List<Archive> archives, String aoi) {
        loadingIndicator.setVisibility(View.GONE);
        contentContainer.setVisibility(View.VISIBLE);
        noResultsText.setVisibility(View.GONE);
        thumbnailGrid.setVisibility(View.VISIBLE);

        // Update thumbnail adapter
        thumbnailAdapter.setArchives(archives);

        // Show order button if we have results
        if (!archives.isEmpty()) {
            orderButton.setVisibility(View.VISIBLE);
            orderButton.setOnClickListener(v -> {
                hidePreview();
                launchOrderDialog(archives.get(0), aoi); // Order the first (most recent) result
            });
        }
    }

    private void showNoResults() {
        mapView.post(() -> {
            if (currentPopup == null || !currentPopup.isShowing()) {
                return;
            }

            loadingIndicator.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
            noResultsText.setVisibility(View.VISIBLE);
            thumbnailGrid.setVisibility(View.GONE);
            orderButton.setVisibility(View.GONE);
        });
    }

    private void showError(String message) {
        mapView.post(() -> {
            hidePreview();
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }

    private void hidePreview() {
        if (currentPopup != null && currentPopup.isShowing()) {
            currentPopup.dismiss();
            currentPopup = null;
        }
    }

    private void onThumbnailSelected(Archive archive) {
        // Show a simple dialog with archive details
        new AlertDialog.Builder(mapView.getContext())
                .setTitle("Archive Details")
                .setMessage(String.format("Provider: %s\nCapture Date: %s\nResolution: %s", 
                    archive.getProvider(),
                    archive.getCaptureTimestamp(),
                    archive.getResolution()))
                .setPositiveButton("Order", (dialog, which) -> {
                    GeoPoint centerPoint = extractCenterFromArchive(archive);
                    if (centerPoint != null) {
                        String aoi = createAOIAroundPoint(centerPoint, PREVIEW_RADIUS_KM);
                        if (aoi != null) {
                            launchOrderDialog(archive, aoi);
                        }
                    } else {
                        showError("Unable to determine archive center location");
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void launchFullSearch(String aoi) {
        Intent intent = new Intent();
        intent.setAction(ArchiveSearch.ACTION);
        intent.putExtra("aoi", aoi);
        AtakBroadcast.getInstance().sendBroadcast(intent);
    }

    private void launchOrderDialog(Archive archive, String aoi) {
        // Create an archive order similar to ArchivesBrowser
        new AlertDialog.Builder(mapView.getContext())
                .setTitle(context.getString(R.string.place_order))
                .setMessage(context.getString(R.string.are_you_sure))
                .setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
                    placeOrder(archive, aoi);
                })
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show();
    }

    private void placeOrder(Archive archive, String aoi) {
        ArchiveOrder order = new ArchiveOrder();
        order.setArchiveId(archive.getArchiveId());
        order.setAoi(aoi);

        apiClient.archiveOrder(order).enqueue(new Callback<ArchiveResponse>() {
            @Override
            public void onResponse(@NonNull Call<ArchiveResponse> call, @NonNull Response<ArchiveResponse> response) {
                mapView.post(() -> {
                    if (response.code() == 201) {
                        new AlertDialog.Builder(mapView.getContext())
                                .setTitle(context.getString(R.string.place_order))
                                .setMessage(context.getString(R.string.order_placed))
                                .setPositiveButton(context.getString(R.string.ok), null)
                                .show();
                    } else {
                        try {
                            String errorMessage = "Failed to place order";
                            if (response.errorBody() != null) {
                                JSONObject errorJson = new JSONObject(response.errorBody().string());
                                errorMessage = errorJson.getJSONArray("detail").getJSONObject(0).getString("msg");
                            }
                            Log.e(LOGTAG, "Failed to place order: " + errorMessage);
                            new AlertDialog.Builder(mapView.getContext())
                                    .setTitle(context.getString(R.string.error_placing_order))
                                    .setMessage(errorMessage)
                                    .setPositiveButton(context.getString(R.string.ok), null)
                                    .show();
                        } catch (Exception e) {
                            Log.e(LOGTAG, "Failed to parse error response", e);
                            showError("Failed to place order");
                        }
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<ArchiveResponse> call, @NonNull Throwable throwable) {
                Log.e(LOGTAG, "Failed to place order", throwable);
                mapView.post(() -> {
                    new AlertDialog.Builder(mapView.getContext())
                            .setTitle(context.getString(R.string.error_placing_order))
                            .setMessage(throwable.getMessage())
                            .setPositiveButton(context.getString(R.string.ok), null)
                            .show();
                });
            }
        });
    }

    private String createAOIAroundPoint(GeoPoint centerPoint, double radiusKm) {
        try {
            double radiusMeters = radiusKm * 1000;

            // Create a square AOI around the point
            double latOffset = radiusMeters / 111000.0; // Approximate meters per degree latitude
            double lonOffset = radiusMeters / (111000.0 * Math.cos(Math.toRadians(centerPoint.getLatitude())));

            double north = centerPoint.getLatitude() + latOffset;
            double south = centerPoint.getLatitude() - latOffset;
            double east = centerPoint.getLongitude() + lonOffset;
            double west = centerPoint.getLongitude() - lonOffset;

            // Create polygon coordinates
            ArrayList<Coordinate> coordinates = new ArrayList<>();
            coordinates.add(new Coordinate(west, north));  // NW
            coordinates.add(new Coordinate(east, north));  // NE
            coordinates.add(new Coordinate(east, south));  // SE
            coordinates.add(new Coordinate(west, south));  // SW
            coordinates.add(new Coordinate(west, north));  // Close polygon

            GeometryFactory factory = new GeometryFactory(new PrecisionModel(10000000.0));
            Polygon polygon = factory.createPolygon(coordinates.toArray(new Coordinate[0]));
            WKTWriter wktWriter = new WKTWriter();

            return wktWriter.write(polygon);
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to create AOI around point", e);
            return null;
        }
    }

    /**
     * Extract center coordinates from Archive footprint
     */
    private GeoPoint extractCenterFromArchive(Archive archive) {
        if (archive.getFootprint() == null) {
            return null;
        }
        
        try {
            WKTReader reader = new WKTReader();
            org.locationtech.jts.geom.Geometry geometry = reader.read(archive.getFootprint());
            
            // Get the centroid of the geometry
            org.locationtech.jts.geom.Point centroid = geometry.getCentroid();
            double lat = centroid.getY();
            double lon = centroid.getX();
            
            return new GeoPoint(lat, lon);
            
        } catch (Exception e) {
            Log.w(LOGTAG, "Could not parse archive footprint: " + archive.getFootprint(), e);
            
            // Fallback: try to extract center point from footprint string using simple parsing
            try {
                // Look for coordinate patterns in the footprint string
                String[] parts = archive.getFootprint().split("[,\\s]+");
                if (parts.length >= 2) {
                    double lat = Double.parseDouble(parts[0]);
                    double lon = Double.parseDouble(parts[1]);
                    return new GeoPoint(lat, lon);
                }
            } catch (Exception e2) {
                Log.w(LOGTAG, "Could not extract center point from footprint", e2);
            }
        }
        
        return null;
    }

    public void cleanup() {
        disablePreviewMode();
        hidePreview();
    }
}