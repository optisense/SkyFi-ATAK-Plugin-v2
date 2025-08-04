package com.skyfi.atak.plugin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Button;

import com.atakmap.android.ipc.DocumentedExtra;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.conversions.CoordinateFormat;
import com.atakmap.coremap.conversions.CoordinateFormatUtilities;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.atak.plugins.impl.PluginContextProvider;
import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.dropdown.DropDownMapComponent;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import com.atakmap.app.preferences.ToolsPreferenceFragment;
import com.skyfi.atak.plugin.skyfiapi.Pong;
import com.skyfi.atak.plugin.skyfiapi.SkyFiAPI;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTWriter;

import java.util.ArrayList;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gov.tak.api.engine.map.coords.GeoCalculations;
import gov.tak.api.engine.map.coords.GeoPoint;
import gov.tak.api.engine.map.coords.IGeoPoint;
import gov.tak.api.plugin.IPlugin;
import gov.tak.api.plugin.IServiceController;
import gov.tak.api.ui.IHostUIService;
import gov.tak.api.ui.Pane;
import gov.tak.api.ui.PaneBuilder;
import gov.tak.api.ui.ToolbarItem;
import gov.tak.api.ui.ToolbarItemAdapter;
import gov.tak.platform.marshal.MarshalManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SkyFiPlugin implements IPlugin, MainRecyclerViewAdapter.ItemClickListener {

    private static final String LOGTAG = "SkyFiPlugin";
    IServiceController serviceController;
    Context pluginContext;
    IHostUIService uiService;
    ToolbarItem toolbarItem;
    Pane templatePane;
    SkyFiAPI apiClient;
    View mainView;
    MainRecyclerViewAdapter mainRecyclerViewAdapter;
    private MapView mapView;
    private TextView radiusTextView;
    private ImageryPreviewManager previewManager;
    private SkyFiDrawingToolsHandler drawingHandler;
    private AOIVisualizationManager aoiVisualizationManager;
    private View currentView;
    private View dashboardView;

    public SkyFiPlugin() {}

    public SkyFiPlugin(IServiceController serviceController) {
        this.serviceController = serviceController;
        final PluginContextProvider ctxProvider = serviceController
                .getService(PluginContextProvider.class);
        if (ctxProvider != null) {
            pluginContext = ctxProvider.getPluginContext();
            pluginContext.setTheme(R.style.ATAKPluginTheme);
        }

        // obtain the UI service
        uiService = serviceController.getService(IHostUIService.class);

        try {
            Looper.prepare();
        } catch (Exception e) {}

        // initialize the toolbar button for the plugin

        // create the button
        toolbarItem = new ToolbarItem.Builder(
                pluginContext.getString(R.string.app_name),
                MarshalManager.marshal(
                        pluginContext.getResources().getDrawable(R.drawable.icon_transparent),
                        android.graphics.drawable.Drawable.class,
                        gov.tak.api.commons.graphics.Bitmap.class))
                .setListener(new ToolbarItemAdapter() {
                    @Override
                    public void onClick(ToolbarItem item) {
                        showPane();
                    }
                })
                .build();

        ToolsPreferenceFragment.register(
                new ToolsPreferenceFragment.ToolPreference(
                        pluginContext.getString(R.string.preferences_title),
                        pluginContext.getString(R.string.preferences_summary),
                        pluginContext.getString(R.string.preferences_title),
                        pluginContext.getResources().getDrawable(R.drawable.icon_transparent),
                        new PreferencesFragment(pluginContext)));

        apiClient = new APIClient().getApiClient();
        apiClient.ping().enqueue(new Callback<Pong>() {
            @Override
            public void onResponse(Call<Pong> call, Response<Pong> response) {
                Log.d(LOGTAG, "Successfully pinged API");
            }

            @Override
            public void onFailure(Call<Pong> call, Throwable throwable) {
                Log.e(LOGTAG, "Failed to ping API", throwable);
            }
        });

        // Defer dropdown receiver registration until MapView is available
        
        // Don't add toolbar item here - it will be added in onStart()
        // This prevents duplicate icons
        Log.d(LOGTAG, "Toolbar item created, will be added in onStart()");
    }


    @Override
    public void onStart() {
        // the plugin is starting, add the button to the toolbar
        if (uiService == null)
            return;

        uiService.addToolbarItem(toolbarItem);
        
        // Get MapView and register dropdown receivers  
        mapView = MapView.getMapView();
        if (mapView != null) {
            registerDropDownReceivers();
            // Initialize preview manager
            previewManager = new ImageryPreviewManager(pluginContext, mapView);
            // Initialize drawing handler
            drawingHandler = new SkyFiDrawingToolsHandler(pluginContext, mapView);
            // Initialize AOI visualization
            aoiVisualizationManager = new AOIVisualizationManager(pluginContext, mapView);
            // Connect the visualization manager to drawing handler
            drawingHandler.setAOIVisualizationManager(aoiVisualizationManager);
        }
    }
    
    private void registerDropDownReceivers() {
        AtakBroadcast.DocumentedIntentFilter documentedIntentFilter = new AtakBroadcast.DocumentedIntentFilter();
        documentedIntentFilter.addAction(Orders.ACTION);
        AtakBroadcast.getInstance().registerReceiver(new Orders(mapView, pluginContext), documentedIntentFilter);

        AtakBroadcast.DocumentedIntentFilter newOrderIntentFilter = new AtakBroadcast.DocumentedIntentFilter();
        newOrderIntentFilter.addAction(NewOrderFragment.ACTION);
        AtakBroadcast.getInstance().registerReceiver(new NewOrderFragment(mapView, pluginContext, ""), newOrderIntentFilter);

        AtakBroadcast.DocumentedIntentFilter archiveSearchFilter = new AtakBroadcast.DocumentedIntentFilter();
        archiveSearchFilter.addAction(ArchiveSearch.ACTION);
        AtakBroadcast.getInstance().registerReceiver(new ArchiveSearch(mapView, pluginContext, ""), archiveSearchFilter);

        AtakBroadcast.DocumentedIntentFilter archivesBrowserFilter = new AtakBroadcast.DocumentedIntentFilter();
        archivesBrowserFilter.addAction(ArchivesBrowser.ACTION);
        AtakBroadcast.getInstance().registerReceiver(new ArchivesBrowser(mapView, pluginContext), archivesBrowserFilter);

        AtakBroadcast.DocumentedIntentFilter taskingOrderFilter = new AtakBroadcast.DocumentedIntentFilter();
        taskingOrderFilter.addAction(TaskingOrderFragment.ACTION);
        AtakBroadcast.getInstance().registerReceiver(new TaskingOrderFragment(mapView, pluginContext), taskingOrderFilter);

        AtakBroadcast.DocumentedIntentFilter profileFilter = new AtakBroadcast.DocumentedIntentFilter();
        profileFilter.addAction(Profile.ACTION);
        AtakBroadcast.getInstance().registerReceiver(new Profile(mapView, pluginContext), profileFilter);

        OrderUtility orderUtility = new OrderUtility(mapView, pluginContext);
        AtakBroadcast.DocumentedIntentFilter filter = new AtakBroadcast.DocumentedIntentFilter();
        filter.addAction("com.atakmap.android.cot_utility.receivers.cotMenu",
                "this intent launches the cot send utility",
                new DocumentedExtra[] {
                        new DocumentedExtra("targetUID",
                                "the map item identifier used to populate the drop down")
                });
        AtakBroadcast.getInstance().registerReceiver(orderUtility, filter);
    }

    @Override
    public void onStop() {
        // the plugin is stopping, remove the button from the toolbar
        if (uiService == null)
            return;

        uiService.removeToolbarItem(toolbarItem);
        
        // Cleanup preview manager
        if (previewManager != null) {
            previewManager.cleanup();
        }
        
        // Cleanup drawing handler
        if (drawingHandler != null) {
            drawingHandler.dispose();
        }
        
        // Cleanup AOI visualization
        if (aoiVisualizationManager != null) {
            aoiVisualizationManager.dispose();
        }
    }

    private void showPane() {
        // instantiate the plugin view if necessary
        if(templatePane == null) {
            // Remember to use the PluginLayoutInflator if you are actually inflating a custom view
            // In this case, using it is not necessary - but I am putting it here to remind
            // developers to look at this Inflator

            dashboardView = PluginLayoutInflater.inflate(pluginContext, R.layout.skyfi_dashboard, null);
            mainView = dashboardView;
            currentView = dashboardView;

            templatePane = new PaneBuilder(mainView)
                    // relative location is set to default; pane will switch location dependent on
                    // current orientation of device screen
                    .setMetaValue(Pane.RELATIVE_LOCATION, Pane.Location.Default)
                    // pane will take up 50% of screen width in landscape mode
                    .setMetaValue(Pane.PREFERRED_WIDTH_RATIO, 0.5D)
                    // pane will take up 50% of screen height in portrait mode
                    .setMetaValue(Pane.PREFERRED_HEIGHT_RATIO, 0.5D)
                    .build();
        }

        // if the plugin pane is not visible, show it!
        if(!uiService.isPaneVisible(templatePane)) {
            uiService.showPane(templatePane, null);
        }

        // Initialize dashboard UI
        initializeDashboard();
        
        // Update metrics
        updateDashboardMetrics();
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (position) {
            case 0:
                // Orders
                Intent intent = new Intent();
                intent.setAction(Orders.ACTION);
                AtakBroadcast.getInstance().sendBroadcast(intent);
                break;
            case 1:
                // New order from my location
                try {
                    SeekBar seekBar = new SeekBar(MapView.getMapView().getContext());
                    // The max area for any tasking order is 2000KM^2 so don't let the user set a diameter above 45KM which would be an area of 2025^2
                    // Set minimum size based on sensor requirements
                    double minDiameterKm = AOIManager.getMinimumAreaForPoint(
                        new com.atakmap.coremap.maps.coords.GeoPoint(
                            MapView.getMapView().getSelfMarker().getPoint().getLatitude(),
                            MapView.getMapView().getSelfMarker().getPoint().getLongitude()
                        ), "default") * 2; // Convert radius to diameter
                    
                    int minSeekBar = Math.max(5, (int)Math.ceil(minDiameterKm));
                    seekBar.setMin(minSeekBar);
                    seekBar.setMax(45);
                    
                    // Set initial value to minimum required size
                    seekBar.setProgress(minSeekBar);
                    seekBar.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, .7f));

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    lp.setLayoutDirection(LinearLayout.HORIZONTAL);

                    LinearLayout linearLayout = new LinearLayout(MapView.getMapView().getContext());
                    linearLayout.setLayoutParams(lp);

                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                            radiusTextView.setText(progress + "KM");
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                    linearLayout.addView(seekBar);

                    radiusTextView = new TextView(MapView.getMapView().getContext());
                    radiusTextView.setText(minSeekBar + "KM");

                    linearLayout.addView(radiusTextView);

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapView.getMapView().getContext());
                    alertDialog.setTitle(pluginContext.getString(R.string.archive_order));
                    alertDialog.setMessage(pluginContext.getString(R.string.set_diameter));
                    alertDialog.setPositiveButton(pluginContext.getString(R.string.ok), (dialogInterface, i) -> {
                        String aoi = squareWkt(seekBar.getProgress());
                        if (aoi != null) {
                            Log.d(LOGTAG, "AOI is " + aoi);
                            Intent newOrderIntent = new Intent();
                            newOrderIntent.setAction(NewOrderFragment.ACTION);
                            newOrderIntent.putExtra("aoi", aoi);
                            AtakBroadcast.getInstance().sendBroadcast(newOrderIntent);
                        }
                    });
                    alertDialog.setNegativeButton(pluginContext.getString(R.string.cancel), null);

                    alertDialog.setView(linearLayout);

                    alertDialog.create().show();
                } catch (Exception e) {
                    Log.e(LOGTAG, "Failed", e);
                }
                break;
            case 2:
                // Draw AOI with ATAK
                startATAKDrawing();
                break;
            case 3:
                // Coordinate Input
                showCoordinateInputDialog();
                break;
            case 4:
                // Manage AOIs
                showAOIManagementDialog();
                break;
            case 5:
                // Toggle preview mode
                if (previewManager != null) {
                    if (previewManager.isPreviewModeEnabled()) {
                        previewManager.disablePreviewMode();
                    } else {
                        previewManager.enablePreviewMode();
                    }
                    // Refresh the menu to update the toggle text
                    showPane();
                }
                break;
            case 6:
                // Set API key
                Preferences prefs = new Preferences();

                AlertDialog.Builder apiKeyAlertDialog = new AlertDialog.Builder(MapView.getMapView().getContext());
                apiKeyAlertDialog.setTitle(pluginContext.getString(R.string.api_key));

                EditText editText = new EditText(MapView.getMapView().getContext());
                editText.setText(prefs.getApiKey());
                LinearLayout.LayoutParams apiKeyLayoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                editText.setLayoutParams(apiKeyLayoutParams);
                apiKeyAlertDialog.setView(editText);
                apiKeyAlertDialog.setPositiveButton(pluginContext.getString(R.string.ok), (dialogInterface, i) -> prefs.setApiKey(editText.getText().toString()));
                apiKeyAlertDialog.create().show();
                break;
            case 7:
                // My Profile
                Intent profileIntent = new Intent();
                profileIntent.setAction(Profile.ACTION);
                AtakBroadcast.getInstance().sendBroadcast(profileIntent);
                break;
        }
    }

    /**
     * Start ATAK's built-in drawing tools for AOI creation
     */
    private void startATAKDrawing() {
        if (drawingHandler != null) {
            // Hide the main pane
            if (uiService != null && templatePane != null && uiService.isPaneVisible(templatePane)) {
                // Close the pane by showing a different one or using back action
                Intent intent = new Intent("com.atakmap.android.maps.UNFOCUS");
                AtakBroadcast.getInstance().sendBroadcast(intent);
            }
            
            // Start drawing with callback
            drawingHandler.startPolygonDrawing(new SkyFiDrawingToolsHandler.ShapeCompleteListener() {
                @Override
                public void onShapeComplete(String shapeUid, List<com.atakmap.coremap.maps.coords.GeoPoint> points, double areaSqKm) {
                    // Points are already in the correct format
                    
                    // Save as AOI
                    try {
                        AOIManager aoiManager = new AOIManager(pluginContext);
                        String aoiName = "AOI_" + System.currentTimeMillis();
                        AOIManager.AOI aoi = aoiManager.createAOI(aoiName, points, areaSqKm, "default");
                        String aoiId = aoi.id;
                        
                        // Show success and offer to create order
                        new AlertDialog.Builder(MapView.getMapView().getContext())
                            .setTitle("AOI Created")
                            .setMessage(String.format("AOI saved: %.2f sq km\\nWould you like to create a tasking order?", areaSqKm))
                            .setPositiveButton("Create Order", (dialog, which) -> {
                                // Launch tasking order with the AOI
                                Intent intent = new Intent();
                                intent.setAction(TaskingOrderFragment.ACTION);
                                intent.putExtra("aoi_id", aoiId);
                                AtakBroadcast.getInstance().sendBroadcast(intent);
                            })
                            .setNegativeButton("Later", null)
                            .show();
                            
                    } catch (Exception e) {
                        Log.e(LOGTAG, "Failed to save AOI", e);
                        Toast.makeText(pluginContext, "Failed to save AOI: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onShapeCancelled() {
                    Log.d(LOGTAG, "Drawing cancelled");
                }
            });
        }
    }
    
    private String squareWkt(double diameter) {
        try {
            double lat = MapView.getMapView().getSelfMarker().getPoint().getLatitude();
            double lon = MapView.getMapView().getSelfMarker().getPoint().getLongitude();

            // Convert to meters
            diameter = diameter * 1000;
            // Convert to radius
            double radius = diameter/2;

            if (lat == 0) {
                new AlertDialog.Builder(MapView.getMapView().getContext())
                        .setTitle(pluginContext.getString(R.string.error))
                        .setMessage(pluginContext.getString(R.string.no_gps))
                        .setPositiveButton(pluginContext.getString(R.string.ok), null)
                        .create()
                        .show();
                return null;
            }

            ArrayList<Coordinate> coordinates = new ArrayList<>();

            // Get the four corners of the square which fits inside the circle defined by the user's diameter input
            GeoPoint selfMarker = new GeoPoint(lat, lon);
            IGeoPoint north = GeoCalculations.pointAtDistance(selfMarker, 0, radius);
            IGeoPoint east = GeoCalculations.pointAtDistance(selfMarker, 90, radius);
            IGeoPoint south = GeoCalculations.pointAtDistance(selfMarker, 180, radius);
            IGeoPoint west = GeoCalculations.pointAtDistance(selfMarker, 270, radius);

            // Expand the square so the circle is now with in the square rather than the square being inside the circle.
            // This ensures that if the user inputs a diameter of 5km, the sides of the square will actually be 5km,
            // rather than the distance from the north east corner to the south west corner is 5km
            Coordinate northEast = new Coordinate(east.getLongitude(), north.getLatitude());
            Coordinate southEast = new Coordinate(east.getLongitude(), south.getLatitude());
            Coordinate southWest = new Coordinate(west.getLongitude(), south.getLatitude());
            Coordinate northWest = new Coordinate(west.getLongitude(), north.getLatitude());

            coordinates.add(northEast);
            coordinates.add(southEast);
            coordinates.add(southWest);
            coordinates.add(northWest);
            // Add the first corner again to close the square
            coordinates.add(northEast);

            GeometryFactory factory = new GeometryFactory(new PrecisionModel(10000000.0));
            Polygon polygon = factory.createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));
            WKTWriter wktWriter = new WKTWriter();
            return wktWriter.write(polygon);
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to make square WKT", e);
        }

        return null;
    }
    
    private void showAOIManagementDialog() {
        AOIManager aoiManager = new AOIManager(pluginContext);
        List<AOIManager.AOI> aois = aoiManager.getAllAOIs();
        
        if (aois.isEmpty()) {
            new AlertDialog.Builder(MapView.getMapView().getContext())
                    .setTitle(pluginContext.getString(R.string.aoi_management))
                    .setMessage(pluginContext.getString(R.string.no_aois_available))
                    .setPositiveButton(pluginContext.getString(R.string.ok), null)
                    .show();
            return;
        }
        
        String[] aoiNames = new String[aois.size()];
        for (int i = 0; i < aois.size(); i++) {
            AOIManager.AOI aoi = aois.get(i);
            aoiNames[i] = aoi.name + " (" + String.format("%.2f sq km", aoi.areaSqKm) + ")";
        }
        
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle(pluginContext.getString(R.string.aoi_management))
                .setItems(aoiNames, (dialog, which) -> {
                    AOIManager.AOI selectedAOI = aois.get(which);
                    showAOIOptionsDialog(aoiManager, selectedAOI);
                })
                .setPositiveButton(pluginContext.getString(R.string.draw_aoi), (dialog, which) -> {
                    startAOIDrawing(aoiManager);
                })
                .setNegativeButton(pluginContext.getString(R.string.cancel), null)
                .show();
    }
    
    private void showAOIOptionsDialog(AOIManager aoiManager, AOIManager.AOI aoi) {
        String[] options = {
            pluginContext.getString(R.string.rename),
            pluginContext.getString(R.string.delete_aoi)
        };
        
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle(aoi.name)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Rename
                            showRenameAOIDialog(aoiManager, aoi);
                            break;
                        case 1: // Delete
                            showDeleteAOIDialog(aoiManager, aoi);
                            break;
                    }
                })
                .setNegativeButton(pluginContext.getString(R.string.cancel), null)
                .show();
    }
    
    private void showRenameAOIDialog(AOIManager aoiManager, AOIManager.AOI aoi) {
        EditText editText = new EditText(MapView.getMapView().getContext());
        editText.setText(aoi.name);
        editText.selectAll();
        
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle(pluginContext.getString(R.string.rename_aoi))
                .setMessage(pluginContext.getString(R.string.enter_new_name))
                .setView(editText)
                .setPositiveButton(pluginContext.getString(R.string.ok), (dialog, which) -> {
                    String newName = editText.getText().toString().trim();
                    if (!newName.isEmpty() && !newName.equals(aoi.name)) {
                        if (aoiManager.renameAOI(aoi.id, newName)) {
                            new AlertDialog.Builder(MapView.getMapView().getContext())
                                    .setTitle(pluginContext.getString(R.string.ok))
                                    .setMessage("AOI renamed successfully")
                                    .setPositiveButton(pluginContext.getString(R.string.ok), null)
                                    .show();
                        } else {
                            new AlertDialog.Builder(MapView.getMapView().getContext())
                                    .setTitle(pluginContext.getString(R.string.error))
                                    .setMessage("Failed to rename AOI")
                                    .setPositiveButton(pluginContext.getString(R.string.ok), null)
                                    .show();
                        }
                    }
                })
                .setNegativeButton(pluginContext.getString(R.string.cancel), null)
                .show();
    }
    
    private void showDeleteAOIDialog(AOIManager aoiManager, AOIManager.AOI aoi) {
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle(pluginContext.getString(R.string.delete_aoi))
                .setMessage(pluginContext.getString(R.string.confirm_delete_aoi))
                .setPositiveButton(pluginContext.getString(R.string.delete), (dialog, which) -> {
                    if (aoiManager.deleteAOI(aoi.id)) {
                        new AlertDialog.Builder(MapView.getMapView().getContext())
                                .setTitle(pluginContext.getString(R.string.ok))
                                .setMessage("AOI deleted successfully")
                                .setPositiveButton(pluginContext.getString(R.string.ok), null)
                                .show();
                    } else {
                        new AlertDialog.Builder(MapView.getMapView().getContext())
                                .setTitle(pluginContext.getString(R.string.error))
                                .setMessage("Failed to delete AOI")
                                .setPositiveButton(pluginContext.getString(R.string.ok), null)
                                .show();
                    }
                })
                .setNegativeButton(pluginContext.getString(R.string.cancel), null)
                .show();
    }
    
    private void startAOIDrawing(AOIManager aoiManager) {
        PolygonDrawingHandler drawingHandler = new PolygonDrawingHandler(pluginContext, mapView);
        
        drawingHandler.startPolygonDrawing(new PolygonDrawingHandler.PolygonCompleteListener() {
            @Override
            public void onPolygonComplete(List<com.atakmap.coremap.maps.coords.GeoPoint> points, double areaSqKm) {
                // Convert ATAK GeoPoints to our GeoPoints
                List<com.atakmap.coremap.maps.coords.GeoPoint> convertedPoints = new ArrayList<>();
                convertedPoints.addAll(points);
                
                // Prompt user for AOI name
                EditText nameInput = new EditText(MapView.getMapView().getContext());
                nameInput.setHint("Enter AOI name");
                
                new AlertDialog.Builder(MapView.getMapView().getContext())
                        .setTitle("Save AOI")
                        .setMessage(String.format("Area: %.2f sq km", areaSqKm))
                        .setView(nameInput)
                        .setPositiveButton(pluginContext.getString(R.string.ok), (dialog, which) -> {
                            String aoiName = nameInput.getText().toString().trim();
                            if (aoiName.isEmpty()) {
                                aoiName = "AOI_" + System.currentTimeMillis();
                            }
                            
                            // Save the AOI
                            aoiManager.createAOI(aoiName, convertedPoints, areaSqKm, "user_drawn");
                            
                            new AlertDialog.Builder(MapView.getMapView().getContext())
                                    .setTitle(pluginContext.getString(R.string.ok))
                                    .setMessage("AOI saved successfully: " + aoiName)
                                    .setPositiveButton(pluginContext.getString(R.string.ok), null)
                                    .show();
                        })
                        .setNegativeButton(pluginContext.getString(R.string.cancel), null)
                        .show();
            }
            
            @Override
            public void onPolygonCancelled() {
                // Drawing was cancelled, nothing to do
            }
        });
    }
    
    private void showCoordinateInputDialog() {
        String[] inputMethods = {
            pluginContext.getString(R.string.latitude) + "/" + pluginContext.getString(R.string.longitude) + " / MGRS",
            pluginContext.getString(R.string.pindrop_tasking)
        };
        
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle(pluginContext.getString(R.string.coordinate_input))
                .setItems(inputMethods, (dialog, which) -> {
                    switch (which) {
                        case 0: // Lat/Lon/MGRS unified dialog
                            showUnifiedCoordinateInputDialog();
                            break;
                        case 1: // Pindrop
                            startPindropTasking();
                            break;
                    }
                })
                .setNegativeButton(pluginContext.getString(R.string.cancel), null)
                .show();
    }
    
    private void showUnifiedCoordinateInputDialog() {
        CoordinateInputDialog.show(MapView.getMapView().getContext(), new CoordinateInputDialog.CoordinateSelectedListener() {
            @Override
            public void onCoordinateSelected(com.atakmap.coremap.maps.coords.GeoPoint point, String displayName) {
                // Convert ATAK GeoPoint to standard coordinates
                createOrderFromCoordinates(point.getLatitude(), point.getLongitude());
            }
            
            @Override
            public void onCancelled() {
                // User cancelled, nothing to do
            }
        });
    }
    
    private void startPindropTasking() {
        // Enable pindrop mode on the map
        // This would typically involve listening for map clicks
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle(pluginContext.getString(R.string.pindrop_tasking))
                .setMessage("Tap on the map to select a location for tasking")
                .setPositiveButton(pluginContext.getString(R.string.ok), (dialog, which) -> {
                    enablePindropMode();
                })
                .setNegativeButton(pluginContext.getString(R.string.cancel), null)
                .show();
    }
    
    private void enablePindropMode() {
        // Add a temporary map click listener for pindrop functionality
        MapEventDispatcher.MapEventDispatchListener pindropListener = new MapEventDispatcher.MapEventDispatchListener() {
            @Override
            public void onMapEvent(MapEvent event) {
                if (MapEvent.MAP_CLICK.equals(event.getType())) {
                    com.atakmap.coremap.maps.coords.GeoPoint point = 
                        mapView.inverse(event.getPointF().x, event.getPointF().y).get();
                    
                    // Remove this listener after first click
                    mapView.getMapEventDispatcher().removeMapEventListener(MapEvent.MAP_CLICK, this);
                    
                    createOrderFromCoordinates(point.getLatitude(), point.getLongitude());
                }
            }
        };
        
        mapView.getMapEventDispatcher().addMapEventListener(MapEvent.MAP_CLICK, pindropListener);
    }
    
    private void createOrderFromCoordinates(double lat, double lon) {
        // Create minimum AOI around the coordinates
        com.atakmap.coremap.maps.coords.GeoPoint centerPoint = new com.atakmap.coremap.maps.coords.GeoPoint(lat, lon);
        List<com.atakmap.coremap.maps.coords.GeoPoint> aoiPoints = AOIManager.createMinimumAOIAroundPoint(centerPoint, "default");
        
        // Convert to WKT
        String aoi = convertPointsToWKT(aoiPoints);
        
        if (aoi != null) {
            // Show order type selection
            String[] orderTypes = {
                pluginContext.getString(R.string.archive_order),
                pluginContext.getString(R.string.tasking_order)
            };
            
            new AlertDialog.Builder(MapView.getMapView().getContext())
                    .setTitle(pluginContext.getString(R.string.new_order))
                    .setMessage(pluginContext.getString(R.string.new_order_message))
                    .setItems(orderTypes, (dialog, which) -> {
                        if (which == 0) {
                            // Archive order
                            Intent newOrderIntent = new Intent();
                            newOrderIntent.setAction(NewOrderFragment.ACTION);
                            newOrderIntent.putExtra("aoi", aoi);
                            AtakBroadcast.getInstance().sendBroadcast(newOrderIntent);
                        } else {
                            // Tasking order
                            Intent taskingIntent = new Intent();
                            taskingIntent.setAction(TaskingOrderFragment.ACTION);
                            taskingIntent.putExtra("aoi", aoi);
                            taskingIntent.putExtra("area", calculateAreaFromWKT(aoi));
                            AtakBroadcast.getInstance().sendBroadcast(taskingIntent);
                        }
                    })
                    .setNegativeButton(pluginContext.getString(R.string.cancel), null)
                    .show();
        }
    }
    
    private String convertPointsToWKT(List<com.atakmap.coremap.maps.coords.GeoPoint> points) {
        if (points == null || points.size() < 3) return null;
        
        try {
            ArrayList<Coordinate> coordinates = new ArrayList<>();
            for (com.atakmap.coremap.maps.coords.GeoPoint point : points) {
                coordinates.add(new Coordinate(point.getLongitude(), point.getLatitude()));
            }
            // Close the polygon
            coordinates.add(new Coordinate(points.get(0).getLongitude(), points.get(0).getLatitude()));
            
            GeometryFactory factory = new GeometryFactory(new PrecisionModel(10000000.0));
            Polygon polygon = factory.createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));
            WKTWriter wktWriter = new WKTWriter();
            return wktWriter.write(polygon);
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to convert points to WKT", e);
            return null;
        }
    }
    
    private double calculateAreaFromWKT(String wkt) {
        try {
            org.locationtech.jts.io.WKTReader reader = new org.locationtech.jts.io.WKTReader();
            org.locationtech.jts.geom.Geometry geometry = reader.read(wkt);
            return TaskingOrderFragment.calculatePolygonArea(geometry.getCoordinates());
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to calculate area from WKT", e);
            return 1.0; // Default fallback
        }
    }
    
    private void initializeDashboard() {
        // Set up click listeners for dashboard cards
        CardView newOrderCard = mainView.findViewById(R.id.new_order_card);
        CardView viewOrdersCard = mainView.findViewById(R.id.view_orders_card);
        CardView manageAoisCard = mainView.findViewById(R.id.manage_aois_card);
        CardView settingsCard = mainView.findViewById(R.id.settings_card);
        
        newOrderCard.setOnClickListener(v -> {
            // Show options for creating new order
            showNewOrderOptions();
        });
        
        viewOrdersCard.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Orders.ACTION);
            AtakBroadcast.getInstance().sendBroadcast(intent);
        });
        
        manageAoisCard.setOnClickListener(v -> {
            showAOIManagementDialog();
        });
        
        settingsCard.setOnClickListener(v -> {
            showSettingsMenu();
        });
    }
    
    private void updateDashboardMetrics() {
        TextView satelliteCount = mainView.findViewById(R.id.satellite_count);
        TextView coveragePercent = mainView.findViewById(R.id.coverage_percent);
        TextView activeOrders = mainView.findViewById(R.id.active_orders);
        TextView apiStatus = mainView.findViewById(R.id.api_status);
        
        // Check API connection status
        apiClient.ping().enqueue(new Callback<com.skyfi.atak.plugin.skyfiapi.Pong>() {
            @Override
            public void onResponse(Call<com.skyfi.atak.plugin.skyfiapi.Pong> call, 
                                 Response<com.skyfi.atak.plugin.skyfiapi.Pong> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MapView.getMapView().post(() -> {
                        apiStatus.setText("Connected");
                        apiStatus.setTextColor(0xFF4CAF50); // Green
                    });
                }
            }
            
            @Override
            public void onFailure(Call<com.skyfi.atak.plugin.skyfiapi.Pong> call, Throwable t) {
                MapView.getMapView().post(() -> {
                    apiStatus.setText("Disconnected");
                    apiStatus.setTextColor(0xFFFF5252); // Red
                });
            }
        });
        
        // For now, use placeholder values for satellite metrics
        // TODO: Implement real satellite feasibility calculation when API is available
        MapView.getMapView().post(() -> {
            satelliteCount.setText("12");
            coveragePercent.setText("87%");
        });
        
        // Get active orders count
        apiClient.getOrders(0, 100).enqueue(new Callback<com.skyfi.atak.plugin.skyfiapi.OrderResponse>() {
            @Override
            public void onResponse(Call<com.skyfi.atak.plugin.skyfiapi.OrderResponse> call,
                                 Response<com.skyfi.atak.plugin.skyfiapi.OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getOrders() != null) {
                    int activeCount = 0;
                    for (com.skyfi.atak.plugin.skyfiapi.Order order : response.body().getOrders()) {
                        if ("ACTIVE".equals(order.getStatus()) || "PENDING".equals(order.getStatus())) {
                            activeCount++;
                        }
                    }
                    final int count = activeCount;
                    MapView.getMapView().post(() -> activeOrders.setText(String.valueOf(count)));
                }
            }
            
            @Override
            public void onFailure(Call<com.skyfi.atak.plugin.skyfiapi.OrderResponse> call, Throwable t) {
                Log.e(LOGTAG, "Failed to get orders", t);
            }
        });
    }
    
    private void showSettingsMenu() {
        // Inflate settings menu layout
        View settingsView = PluginLayoutInflater.inflate(pluginContext, R.layout.settings_menu, null);
        
        // Update the pane with settings view
        currentView = settingsView;
        templatePane = new PaneBuilder(settingsView)
                .setMetaValue(Pane.RELATIVE_LOCATION, Pane.Location.Default)
                .setMetaValue(Pane.PREFERRED_WIDTH_RATIO, 0.5D)
                .setMetaValue(Pane.PREFERRED_HEIGHT_RATIO, 0.5D)
                .build();
        
        uiService.showPane(templatePane, null);
        
        // Set up back button
        ImageView backButton = settingsView.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            // Return to dashboard
            showPane();
        });
        
        // Set up API key setting
        CardView apiKeySetting = settingsView.findViewById(R.id.api_key_setting);
        apiKeySetting.setOnClickListener(v -> {
            EditText input = new EditText(MapView.getMapView().getContext());
            String currentKey = pluginContext.getSharedPreferences("SkyFiPlugin", Context.MODE_PRIVATE)
                .getString("apiKey", "");
            input.setText(currentKey);
            
            new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle("Set API Key")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String apiKey = input.getText().toString();
                    if (!apiKey.isEmpty()) {
                        pluginContext.getSharedPreferences("SkyFiPlugin", Context.MODE_PRIVATE)
                            .edit()
                            .putString("apiKey", apiKey)
                            .apply();
                        Toast.makeText(pluginContext, "API Key saved", Toast.LENGTH_SHORT).show();
                        updateDashboardMetrics(); // Refresh connection status
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
        
        // Set up profile setting
        CardView profileSetting = settingsView.findViewById(R.id.profile_setting);
        profileSetting.setOnClickListener(v -> {
            Intent profileIntent = new Intent();
            profileIntent.setAction(Profile.ACTION);
            AtakBroadcast.getInstance().sendBroadcast(profileIntent);
        });
        
        // Set up preview mode setting
        CardView previewModeSetting = settingsView.findViewById(R.id.preview_mode_setting);
        TextView previewModeTitle = settingsView.findViewById(R.id.preview_mode_title);
        
        // Update title based on current state
        if (previewManager != null && previewManager.isPreviewModeEnabled()) {
            previewModeTitle.setText("Disable Preview Mode");
        }
        
        previewModeSetting.setOnClickListener(v -> {
            if (previewManager != null) {
                if (previewManager.isPreviewModeEnabled()) {
                    previewManager.disablePreviewMode();
                    Toast.makeText(pluginContext, "Preview mode disabled", Toast.LENGTH_SHORT).show();
                    previewModeTitle.setText("Enable Preview Mode");
                } else {
                    previewManager.enablePreviewMode();
                    Toast.makeText(pluginContext, "Preview mode enabled", Toast.LENGTH_SHORT).show();
                    previewModeTitle.setText("Disable Preview Mode");
                }
            }
        });
        
        // Set up about setting
        CardView aboutSetting = settingsView.findViewById(R.id.about_setting);
        aboutSetting.setOnClickListener(v -> {
            showAboutDialog();
        });
    }
    
    private void showAboutDialog() {
        new AlertDialog.Builder(MapView.getMapView().getContext())
            .setTitle("About SkyFi")
            .setMessage("SkyFi ATAK Plugin v2.0\n\n" +
                       "Satellite tasking made simple.\n\n" +
                       "© 2024 OptiSense")
            .setPositiveButton("OK", null)
            .show();
    }
    
    private void showNewOrderOptions() {
        String[] options = {
            "Draw on Map",
            "From GPS Location",
            "Enter Coordinates",
            "From Existing Shape"
        };
        
        new AlertDialog.Builder(MapView.getMapView().getContext())
            .setTitle("Create New AOI")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Draw on Map
                        uiService.closePane(templatePane);
                        startATAKDrawing();
                        break;
                    case 1: // From GPS Location
                        showGPSAOIDialog();
                        break;
                    case 2: // Enter Coordinates
                        showCoordinateInputDialog();
                        break;
                    case 3: // From Existing Shape
                        Toast.makeText(pluginContext, "Select a shape on the map", Toast.LENGTH_LONG).show();
                        uiService.closePane(templatePane);
                        break;
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showGPSAOIDialog() {
        View dialogView = PluginLayoutInflater.inflate(pluginContext, R.layout.gps_aoi_dialog, null);
        
        // Get current location
        com.atakmap.coremap.maps.coords.GeoPoint currentLoc = mapView.getSelfMarker().getPoint();
        
        TextView locationText = dialogView.findViewById(R.id.current_location_text);
        locationText.setText(CoordinateFormatUtilities.formatToString(currentLoc, CoordinateFormat.DD));
        
        EditText nameInput = dialogView.findViewById(R.id.aoi_name_input);
        nameInput.setText("GPS_AOI_" + System.currentTimeMillis());
        
        SeekBar sizeSeekbar = dialogView.findViewById(R.id.size_seekbar);
        TextView sizeText = dialogView.findViewById(R.id.size_text);
        TextView sizeWarning = dialogView.findViewById(R.id.size_warning);
        
        // Calculate minimum diameter for 5 sq km area
        double minDiameterKm = Math.sqrt(5.0) * 2; // For square
        int minSeekBar = (int)Math.ceil(minDiameterKm);
        
        sizeSeekbar.setMin(minSeekBar);
        sizeSeekbar.setMax(50);
        sizeSeekbar.setProgress(minSeekBar);
        
        sizeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double areaSqKm = (progress * progress) / 4.0; // Square area
                sizeText.setText(String.format("%d km diameter (%.1f sq km)", progress, areaSqKm));
                
                if (areaSqKm < 5.0) {
                    sizeWarning.setVisibility(View.VISIBLE);
                } else {
                    sizeWarning.setVisibility(View.GONE);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        AlertDialog dialog = new AlertDialog.Builder(MapView.getMapView().getContext())
            .setView(dialogView)
            .setPositiveButton("Create AOI", null) // Set to null to override later
            .setNegativeButton("Cancel", null)
            .create();
            
        dialog.show();
        
        // Override positive button to prevent auto-dismiss
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String aoiName = nameInput.getText().toString().trim();
            if (aoiName.isEmpty()) {
                Toast.makeText(pluginContext, "Please enter AOI name", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int diameter = sizeSeekbar.getProgress();
            double areaSqKm = (diameter * diameter) / 4.0;
            
            // Create square AOI around GPS location
            List<com.atakmap.coremap.maps.coords.GeoPoint> points = createSquareAround(currentLoc, diameter / 2.0);
            
            try {
                AOIManager aoiManager = new AOIManager(pluginContext);
                AOIManager.AOI aoi = aoiManager.createAOI(aoiName, points, areaSqKm, "gps_location");
                
                Toast.makeText(pluginContext, "AOI created: " + aoiName, Toast.LENGTH_SHORT).show();
                
                // Visualize the AOI
                if (aoiVisualizationManager != null) {
                    aoiVisualizationManager.displayAOI(aoi);
                }
                
                // Zoom to AOI
                mapView.getMapController().panTo(currentLoc, true);
                
                dialog.dismiss();
                
                // Ask if user wants to task satellite
                new AlertDialog.Builder(MapView.getMapView().getContext())
                    .setTitle("AOI Created")
                    .setMessage("Would you like to task a satellite for this AOI?")
                    .setPositiveButton("Yes", (d, w) -> {
                        Intent intent = new Intent();
                        intent.setAction(TaskingOrderFragment.ACTION);
                        intent.putExtra("aoi_id", aoi.id);
                        AtakBroadcast.getInstance().sendBroadcast(intent);
                    })
                    .setNegativeButton("No", null)
                    .show();
                    
            } catch (Exception e) {
                Log.e(LOGTAG, "Failed to create GPS AOI", e);
                Toast.makeText(pluginContext, "Failed to create AOI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private List<com.atakmap.coremap.maps.coords.GeoPoint> createSquareAround(
            com.atakmap.coremap.maps.coords.GeoPoint center, double radiusKm) {
        List<com.atakmap.coremap.maps.coords.GeoPoint> points = new ArrayList<>();
        
        // Convert radius to meters
        double radiusMeters = radiusKm * 1000;
        
        // Calculate corner points
        double lat = center.getLatitude();
        double lon = center.getLongitude();
        
        // Approximate degrees per meter
        double metersPerDegreeLat = 111132.92 - 559.82 * Math.cos(2 * Math.toRadians(lat));
        double metersPerDegreeLon = 111412.84 * Math.cos(Math.toRadians(lat));
        
        double deltaLat = radiusMeters / metersPerDegreeLat;
        double deltaLon = radiusMeters / metersPerDegreeLon;
        
        // Create square corners
        points.add(new com.atakmap.coremap.maps.coords.GeoPoint(lat - deltaLat, lon - deltaLon)); // SW
        points.add(new com.atakmap.coremap.maps.coords.GeoPoint(lat - deltaLat, lon + deltaLon)); // SE  
        points.add(new com.atakmap.coremap.maps.coords.GeoPoint(lat + deltaLat, lon + deltaLon)); // NE
        points.add(new com.atakmap.coremap.maps.coords.GeoPoint(lat + deltaLat, lon - deltaLon)); // NW
        
        return points;
    }
}
