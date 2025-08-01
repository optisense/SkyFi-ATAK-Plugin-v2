package com.skyfi.atak.plugin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.atakmap.android.ipc.DocumentedExtra;
import com.atakmap.coremap.log.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

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
import java.util.UUID;
import java.io.File;

import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.drawing.mapItems.DrawingShape;
import com.atakmap.android.toolbar.ToolManagerBroadcastReceiver;
import com.atakmap.android.drawing.DrawingToolsToolbar;
import android.widget.Toast;
import android.os.Handler;
import com.atakmap.coremap.maps.coords.GeoPoint;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gov.tak.api.engine.map.coords.GeoCalculations;
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

public class SkyFiPlugin extends DropDownMapComponent implements IPlugin, MainRecyclerViewAdapter.ItemClickListener {

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
    private SkyFiMapInteractionHandler mapInteractionHandler;
    private SkyFiMapOverlay mapOverlay;
    
    // Dropdown receivers
    private Profile profileDropDown;
    private Orders ordersDropDown;
    
    // Polygon drawing state
    private boolean isPolygonDrawingMode = false;
    private String polygonDrawingSessionId = null;
    private View polygonModeIndicator = null;
    private Handler mainHandler = null;

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

        // Receivers will be registered after MapView is available
    }

    @Override
    public void onCreate(Context context, Intent intent, MapView mapView) {
        Log.d(TAG, "onCreate called - initializing SkyFi plugin");
        super.onCreate(context, intent, mapView);
        this.mapView = mapView;
        this.pluginContext = context;
        
        // Initialize Handler
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize map interaction handlers early
        try {
            this.mapOverlay = new SkyFiMapOverlay(mapView);
            this.mapInteractionHandler = new SkyFiMapInteractionHandler(pluginContext, mapView, mapOverlay);
            Log.d(TAG, "Map interaction handlers initialized");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize map interaction handlers", e);
        }
        
        // Register receivers here when MapView is available
        registerAllReceivers();
    }
    
    private void registerAllReceivers() {
        Log.d(TAG, "Registering all receivers - mapView: " + (mapView != null) + ", pluginContext: " + (pluginContext != null));
        
        // Register Orders and Profile receivers
        ordersDropDown = new Orders(mapView, pluginContext);
        AtakBroadcast.DocumentedIntentFilter ordersFilter = new AtakBroadcast.DocumentedIntentFilter();
        ordersFilter.addAction(Orders.ACTION);
        AtakBroadcast.getInstance().registerReceiver(ordersDropDown, ordersFilter);
        Log.d(TAG, "Registered Orders receiver");

        profileDropDown = new Profile(mapView, pluginContext);
        AtakBroadcast.DocumentedIntentFilter profileFilter = new AtakBroadcast.DocumentedIntentFilter();
        profileFilter.addAction(Profile.ACTION);
        AtakBroadcast.getInstance().registerReceiver(profileDropDown, profileFilter);
        Log.d(TAG, "Registered Profile receiver");
        
        // Register other receivers
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
    public void onStart() {
        Log.d(TAG, "onStart called");
        
        // Initialize MapView if not already done
        if (mapView == null) {
            mapView = MapView.getMapView();
            Log.d(TAG, "Got MapView: " + (mapView != null));
        }
        
        // Register receivers when plugin starts
        if (profileDropDown == null || ordersDropDown == null) {
            Log.d(TAG, "Receivers not initialized, calling registerAllReceivers from onStart");
            registerAllReceivers();
        }
        
        // the plugin is starting, add the button to the toolbar
        if (uiService == null) {
            Log.e(TAG, "uiService is null in onStart");
            return;
        }
        
        if (toolbarItem == null) {
            Log.e(TAG, "toolbarItem is null in onStart");
            return;
        }

        Log.d(TAG, "Adding toolbar item");
        uiService.addToolbarItem(toolbarItem);
        Log.d(TAG, "Toolbar item added successfully");
    }

    @Override
    public void onStop() {
        // the plugin is stopping, remove the button from the toolbar
        if (uiService == null)
            return;

        // Clean up polygon drawing mode if active
        if (isPolygonDrawingMode) {
            exitPolygonDrawingMode();
        }
        
        // Clean up handlers and resources
        if (mapInteractionHandler != null) {
            mapInteractionHandler.cleanup();
        }
        
        mainHandler.removeCallbacksAndMessages(null);

        uiService.removeToolbarItem(toolbarItem);
    }

    private void showPane() {
        try {
            // Check if UI service is available
            if (uiService == null) {
                Log.e(TAG, "UI Service is null, cannot show pane");
                return;
            }
            
            // instantiate the plugin view if necessary
            if(templatePane == null) {
                // Remember to use the PluginLayoutInflator if you are actually inflating a custom view
                // In this case, using it is not necessary - but I am putting it here to remind
                // developers to look at this Inflator

                mainView = PluginLayoutInflater.inflate(pluginContext, R.layout.main_layout, null);

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
            if(templatePane != null && !uiService.isPaneVisible(templatePane)) {
                uiService.showPane(templatePane, null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing pane", e);
        }

        ArrayList<String> options = new ArrayList<>();
        options.add(pluginContext.getString(R.string.view_orders));
        options.add(pluginContext.getString(R.string.new_order_my_location));
        options.add(pluginContext.getString(R.string.enter_coordinates)); // New coordinate input
        options.add(pluginContext.getString(R.string.draw_polygon)); // Polygon drawing
        options.add(pluginContext.getString(R.string.saved_aois)); // AOI management
        options.add(pluginContext.getString(R.string.set_api_key));
        options.add(pluginContext.getString(R.string.my_profile));

        RecyclerView recyclerView = mainView.findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(pluginContext));
        mainRecyclerViewAdapter = new MainRecyclerViewAdapter(pluginContext, options);
        mainRecyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(mainRecyclerViewAdapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        showPane();
        switch (position) {
            case 0:
                // Orders
                Log.d(TAG, "View Orders clicked");
                Intent intent = new Intent();
                intent.setAction(Orders.ACTION);
                AtakBroadcast.getInstance().sendBroadcast(intent);
                Log.d(TAG, "Broadcast sent for Orders.ACTION: " + Orders.ACTION);
                break;
            case 1:
                // New order from my location
                try {
                    SeekBar seekBar = new SeekBar(MapView.getMapView().getContext());
                    // The max area for any tasking order is 2000KM^2 so don't let the user set a diameter above 45KM which would be an area of 2025^2
                    seekBar.setMin(5);
                    seekBar.setMax(45);
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
                    radiusTextView.setText("5KM");

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
                // Enter coordinates - new feature
                showCoordinateInputDialog();
                break;
            case 3:
                // Draw polygon AOI
                startPolygonDrawing();
                break;
            case 4:
                // Saved AOIs
                showSavedAOIsDialog();
                break;
            case 5:
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
            case 6:
                Log.d(TAG, "My Profile clicked");
                Toast.makeText(pluginContext, "My Profile clicked - sending broadcast", Toast.LENGTH_SHORT).show();
                Intent profileIntent = new Intent();
                profileIntent.setAction(Profile.ACTION);
                AtakBroadcast.getInstance().sendBroadcast(profileIntent);
                Log.d(TAG, "Broadcast sent for Profile.ACTION: " + Profile.ACTION);
                
                // Also try showing dropdown directly for testing
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (profileDropDown != null) {
                        Log.d(TAG, "Trying to show profile dropdown directly");
                        profileDropDown.onReceive(pluginContext, profileIntent);
                    } else {
                        Log.e(TAG, "profileDropDown is null!");
                        Toast.makeText(pluginContext, "Profile dropdown not initialized", Toast.LENGTH_SHORT).show();
                    }
                }, 100);
                break;
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
            gov.tak.api.engine.map.coords.GeoPoint selfMarker = new gov.tak.api.engine.map.coords.GeoPoint(lat, lon);
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
    
    private void showCoordinateInputDialog() {
        CoordinateInputDialog dialog = new CoordinateInputDialog(pluginContext, 
            new CoordinateInputDialog.CoordinateInputListener() {
                @Override
                public void onCoordinateSelected(String wkt, double areaKm2, String coordinateString) {
                    // Create AOI from coordinates
                    AOIManager aoiManager = new AOIManager(pluginContext);
                    AOIManager.AOI aoi = new AOIManager.AOI(
                        aoiManager.generateAOIId(),
                        "AOI at " + coordinateString,
                        wkt
                    );
                    aoi.areaKm2 = areaKm2;
                    aoiManager.saveAOI(aoi);
                    
                    // Launch new order with this AOI
                    Intent newOrderIntent = new Intent();
                    newOrderIntent.setAction(NewOrderFragment.ACTION);
                    newOrderIntent.putExtra("aoi", wkt);
                    newOrderIntent.putExtra("aoiName", aoi.name);
                    AtakBroadcast.getInstance().sendBroadcast(newOrderIntent);
                }
                
                @Override
                public void onCancelled() {
                    // User cancelled
                }
            });
        dialog.show();
    }
    
    private void startPolygonDrawing() {
        // Show options dialog - draw new or select existing
        new AlertDialog.Builder(MapView.getMapView().getContext())
            .setTitle(pluginContext.getString(R.string.draw_polygon))
            .setMessage("Choose how to create your AOI:")
            .setPositiveButton("Draw New Polygon", (dialog, which) -> {
                showDrawingInstructions();
            })
            .setNeutralButton("Select Existing", (dialog, which) -> {
                startPolygonSelectionMode();
            })
            .setNegativeButton(pluginContext.getString(R.string.cancel), null)
            .create()
            .show();
    }
    
    private void showDrawingInstructions() {
        new AlertDialog.Builder(MapView.getMapView().getContext())
            .setTitle(pluginContext.getString(R.string.draw_polygon))
            .setMessage(pluginContext.getString(R.string.draw_polygon_instructions))
            .setPositiveButton(pluginContext.getString(R.string.start_drawing), (dialog, which) -> {
                enterPolygonDrawingMode();
            })
            .setNegativeButton(pluginContext.getString(R.string.cancel), null)
            .create()
            .show();
    }
    
    private void startPolygonSelectionMode() {
        try {
            // Initialize handlers if not already done
            if (mapInteractionHandler == null) {
                mapOverlay = new SkyFiMapOverlay(mapView);
                mapInteractionHandler = new SkyFiMapInteractionHandler(pluginContext, mapView, mapOverlay);
            }
            
            // Set up the polygon completion listener for selection
            SkyFiMapInteractionHandler.PolygonCompleteListener selectionListener = new SkyFiMapInteractionHandler.PolygonCompleteListener() {
                @Override
                public void onPolygonComplete(com.atakmap.coremap.maps.coords.GeoPoint[] points, double areaKm2, String wkt) {
                    handleSelectedPolygon(points, areaKm2, wkt);
                }
                
                @Override
                public void onPolygonCancelled() {
                    Toast.makeText(pluginContext, "Polygon selection cancelled", Toast.LENGTH_SHORT).show();
                }
            };
            
            mapInteractionHandler.setPolygonCompleteListener(selectionListener);
            
            // Start polygon selection mode
            mapInteractionHandler.startPolygonSelection(selectionListener);
            
            // Highlight selectable polygons
            mapInteractionHandler.highlightSelectablePolygons();
            
            Log.d(LOGTAG, "Started polygon selection mode");
            
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to start polygon selection mode", e);
            Toast.makeText(pluginContext, "Failed to start polygon selection", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void handleSelectedPolygon(com.atakmap.coremap.maps.coords.GeoPoint[] points, double areaKm2, String wkt) {
        // Show completion options for selected polygon
        showPolygonCompleteDialog(points, areaKm2, wkt, null);
    }
    
    private void enterPolygonDrawingMode() {
        try {
            // Ensure SharedPreferences directory exists
            try {
                File sharedPrefsDir = new File(pluginContext.getFilesDir().getParent(), "shared_prefs");
                if (!sharedPrefsDir.exists()) {
                    sharedPrefsDir.mkdirs();
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not ensure shared_prefs directory", e);
            }
            
            // Initialize handlers if not already done
            if (mapInteractionHandler == null) {
                mapOverlay = new SkyFiMapOverlay(mapView);
                mapInteractionHandler = new SkyFiMapInteractionHandler(pluginContext, mapView, mapOverlay);
            }
            
            isPolygonDrawingMode = true;
            polygonDrawingSessionId = UUID.randomUUID().toString();
            
            // Set up the polygon completion listener
            SkyFiMapInteractionHandler.PolygonCompleteListener drawingListener = new SkyFiMapInteractionHandler.PolygonCompleteListener() {
                @Override
                public void onPolygonComplete(com.atakmap.coremap.maps.coords.GeoPoint[] points, double areaKm2, String wkt) {
                    handlePolygonComplete(points, areaKm2, wkt);
                }
                
                @Override
                public void onPolygonCancelled() {
                    exitPolygonDrawingMode();
                }
            };
            
            mapInteractionHandler.setPolygonCompleteListener(drawingListener);
            
            // Start polygon drawing mode with enhanced visuals
            mapInteractionHandler.startPolygonDrawing(drawingListener);
            
            // Show drawing mode indicator
            showPolygonModeIndicator();
            
            // Register for MapEvents to detect completed shapes
            mapView.getMapEventDispatcher().addMapEventListener(MapEvent.ITEM_ADDED, mapEventListener);
            mapView.getMapEventDispatcher().addMapEventListener(MapEvent.ITEM_REFRESH, mapEventListener);
            
            Log.d(LOGTAG, "Entered polygon drawing mode with session: " + polygonDrawingSessionId);
            
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to enter polygon drawing mode", e);
            Toast.makeText(pluginContext, "Failed to start polygon drawing", Toast.LENGTH_SHORT).show();
            exitPolygonDrawingMode();
        }
    }
    
    private final MapEventDispatcher.MapEventDispatchListener mapEventListener = new MapEventDispatcher.MapEventDispatchListener() {
        @Override
        public void onMapEvent(MapEvent event) {
            if (!isPolygonDrawingMode || polygonDrawingSessionId == null) return;
            
            String eventType = event.getType();
            MapItem item = event.getItem();
            
            if (MapEvent.ITEM_ADDED.equals(eventType) && item instanceof DrawingShape) {
                handleDrawingItemAdded((DrawingShape) item);
            } else if (MapEvent.ITEM_REFRESH.equals(eventType) && item instanceof DrawingShape) {
                handleDrawingItemRefresh((DrawingShape) item);
            }
        }
    };
    
    private void handleDrawingItemAdded(DrawingShape shape) {
        // Apply SkyFi styling to the new shape
        if (shape != null) {
            SkyFiPolygonStyle.applyDrawingStyle(shape);
            Log.d(LOGTAG, "Applied drawing style to new shape: " + shape.getUID());
        }
    }
    
    private void handleDrawingItemRefresh(DrawingShape shape) {
        if (shape != null && shape.getNumPoints() >= 3) {
            // Check if this is a completed polygon (closed shape)
            if (isPolygonComplete(shape)) {
                // Process the completed polygon with delay to ensure shape is fully formed
                mainHandler.postDelayed(() -> processCompletedPolygon(shape), 500);
            }
        }
    }
    
    private boolean isPolygonComplete(DrawingShape shape) {
        try {
            if (shape.getNumPoints() < 3) return false;
            
            // Check if shape is closed - compare first and last points
            com.atakmap.coremap.maps.coords.GeoPoint[] points = shape.getPoints();
            if (points.length < 3) return false;
            
            com.atakmap.coremap.maps.coords.GeoPoint first = points[0];
            com.atakmap.coremap.maps.coords.GeoPoint last = points[points.length - 1];
            
            // Consider closed if points are very close
            double distance = first.distanceTo(last);
            return distance < 10.0; // 10 meters tolerance
            
        } catch (Exception e) {
            Log.e(LOGTAG, "Error checking if polygon is complete", e);
            return false;
        }
    }
    
    private void processCompletedPolygon(DrawingShape shape) {
        if (!isPolygonDrawingMode || shape == null) return;
        
        try {
            com.atakmap.coremap.maps.coords.GeoPoint[] points = shape.getPoints();
            if (points.length < 3) {
                Toast.makeText(pluginContext, pluginContext.getString(R.string.invalid_polygon), Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Calculate area
            double areaKm2 = calculatePolygonArea(points);
            
            // Validate area size
            if (areaKm2 > 2000) {
                new AlertDialog.Builder(MapView.getMapView().getContext())
                    .setTitle(pluginContext.getString(R.string.aoi_too_large))
                    .setMessage(String.format(pluginContext.getString(R.string.area_too_large_msg), 
                                AOISizeValidator.formatArea(areaKm2)))
                    .setPositiveButton(pluginContext.getString(R.string.draw_again), (dialog, which) -> {
                        // Remove current shape and continue drawing
                        shape.removeFromGroup();
                    })
                    .setNegativeButton(pluginContext.getString(R.string.cancel), (dialog, which) -> {
                        exitPolygonDrawingMode();
                    })
                    .create()
                    .show();
                return;
            }
            
            // Convert to WKT
            String wkt = convertPointsToWkt(points);
            if (wkt == null) {
                Toast.makeText(pluginContext, pluginContext.getString(R.string.failed_process_polygon), Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Apply area-based styling
            SkyFiPolygonStyle.applyAreaBasedStyle(shape, areaKm2);
            
            // Show completion options
            showPolygonCompleteDialog(points, areaKm2, wkt, shape);
            
        } catch (Exception e) {
            Log.e(LOGTAG, "Error processing completed polygon", e);
            Toast.makeText(pluginContext, pluginContext.getString(R.string.error_processing_polygon), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showPolygonCompleteDialog(com.atakmap.coremap.maps.coords.GeoPoint[] points, double areaKm2, String wkt, DrawingShape shape) {
        String areaText = AOISizeValidator.formatArea(areaKm2);
        String message = String.format("Polygon completed!\nArea: %s\n\nWhat would you like to do?", areaText);
        
        new AlertDialog.Builder(MapView.getMapView().getContext())
            .setTitle(pluginContext.getString(R.string.save_polygon_aoi))
            .setMessage(message)
            .setPositiveButton(pluginContext.getString(R.string.save_and_use), (dialog, which) -> {
                saveAndUsePolygon(points, areaKm2, wkt, shape);
            })
            .setNeutralButton(pluginContext.getString(R.string.use_only), (dialog, which) -> {
                usePolygonOnly(areaKm2, wkt, shape);
            })
            .setNegativeButton(pluginContext.getString(R.string.discard), (dialog, which) -> {
                shape.removeFromGroup();
                exitPolygonDrawingMode();
            })
            .setCancelable(false)
            .create()
            .show();
    }
    
    private void saveAndUsePolygon(com.atakmap.coremap.maps.coords.GeoPoint[] points, double areaKm2, String wkt, DrawingShape shape) {
        // Show name input dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MapView.getMapView().getContext());
        builder.setTitle(pluginContext.getString(R.string.aoi_name));
        
        EditText nameInput = new EditText(MapView.getMapView().getContext());
        nameInput.setHint("Enter AOI name");
        nameInput.setText(String.format("Polygon AOI %s", java.text.DateFormat.getDateTimeInstance().format(new java.util.Date())));
        builder.setView(nameInput);
        
        builder.setPositiveButton(pluginContext.getString(R.string.ok), (dialog, which) -> {
            String aoiName = nameInput.getText().toString().trim();
            if (aoiName.isEmpty()) {
                aoiName = "Unnamed Polygon AOI";
            }
            
            // Save AOI
            AOIManager aoiManager = new AOIManager(pluginContext);
            AOIManager.AOI aoi = new AOIManager.AOI(
                aoiManager.generateAOIId(),
                aoiName,
                wkt
            );
            aoi.areaKm2 = areaKm2;
            aoiManager.saveAOI(aoi);
            
            // Launch new order
            launchNewOrderWithAOI(wkt, aoiName);
            
            // Clean up
            shape.removeFromGroup();
            exitPolygonDrawingMode();
            
            Toast.makeText(pluginContext, "AOI saved and order started!", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton(pluginContext.getString(R.string.cancel), null);
        builder.create().show();
    }
    
    private void usePolygonOnly(double areaKm2, String wkt, DrawingShape shape) {
        // Use polygon without saving
        String tempName = String.format("Temp Polygon (%.1f km²)", areaKm2);
        launchNewOrderWithAOI(wkt, tempName);
        
        // Clean up
        shape.removeFromGroup();
        exitPolygonDrawingMode();
        
        Toast.makeText(pluginContext, "Using polygon for order", Toast.LENGTH_SHORT).show();
    }
    
    private void launchNewOrderWithAOI(String wkt, String aoiName) {
        Intent newOrderIntent = new Intent();
        newOrderIntent.setAction(NewOrderFragment.ACTION);
        newOrderIntent.putExtra("aoi", wkt);
        newOrderIntent.putExtra("aoiName", aoiName);
        AtakBroadcast.getInstance().sendBroadcast(newOrderIntent);
    }
    
    private void handlePolygonComplete(com.atakmap.coremap.maps.coords.GeoPoint[] points, double areaKm2, String wkt) {
        // This method is called by the map interaction handler
        try {
            showPolygonCompleteDialog(points, areaKm2, wkt, null);
        } catch (Exception e) {
            Log.e(LOGTAG, "Error handling polygon completion", e);
            Toast.makeText(pluginContext, "Error completing polygon", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showPolygonModeIndicator() {
        if (polygonModeIndicator != null) return;
        
        mainHandler.post(() -> {
            try {
                // Create indicator view
                TextView indicator = new TextView(mapView.getContext());
                indicator.setText(pluginContext.getString(R.string.drawing_mode_indicator));
                indicator.setBackgroundColor(0xCC2196F3);
                indicator.setTextColor(android.graphics.Color.WHITE);
                indicator.setPadding(20, 10, 20, 10);
                indicator.setGravity(android.view.Gravity.CENTER);
                
                // Make it clickable to exit drawing mode
                indicator.setOnClickListener(v -> {
                    new AlertDialog.Builder(MapView.getMapView().getContext())
                        .setTitle(pluginContext.getString(R.string.exit_drawing_mode))
                        .setMessage(pluginContext.getString(R.string.exit_drawing_mode_message))
                        .setPositiveButton(pluginContext.getString(R.string.ok), (dialog, which) -> {
                            exitPolygonDrawingMode();
                        })
                        .setNegativeButton(pluginContext.getString(R.string.continue_drawing), null)
                        .create()
                        .show();
                });
                
                // Add to map view
                if (mapView instanceof android.view.ViewGroup) {
                    android.widget.FrameLayout.LayoutParams params = 
                        new android.widget.FrameLayout.LayoutParams(
                            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
                        );
                    params.gravity = android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL;
                    params.topMargin = 100;
                    
                    ((android.view.ViewGroup) mapView).addView(indicator, params);
                    polygonModeIndicator = indicator;
                }
                
            } catch (Exception e) {
                Log.e(LOGTAG, "Error showing polygon mode indicator", e);
            }
        });
    }
    
    private void exitPolygonDrawingMode() {
        try {
            isPolygonDrawingMode = false;
            polygonDrawingSessionId = null;
            
            // Clean up UI
            hidePolygonModeIndicator();
            
            // Unregister event listeners
            if (mapEventListener != null) {
                mapView.getMapEventDispatcher().removeMapEventListener(MapEvent.ITEM_ADDED, mapEventListener);
                mapView.getMapEventDispatcher().removeMapEventListener(MapEvent.ITEM_REFRESH, mapEventListener);
            }
            
            // Stop interaction handler if active
            if (mapInteractionHandler != null) {
                mapInteractionHandler.stopPolygonDrawing();
            }
            
            // Deactivate drawing tools
            Intent intent = new Intent();
            intent.setAction("com.atakmap.android.toolbar.UNSET_TOOL");
            intent.putExtra("tool", "com.atakmap.android.drawing.DrawingToolsToolbar");
            AtakBroadcast.getInstance().sendBroadcast(intent);
            
            Log.d(LOGTAG, "Exited polygon drawing mode");
            
        } catch (Exception e) {
            Log.e(LOGTAG, "Error exiting polygon drawing mode", e);
        }
    }
    
    private void hidePolygonModeIndicator() {
        if (polygonModeIndicator != null && polygonModeIndicator.getParent() != null) {
            mainHandler.post(() -> {
                try {
                    ((android.view.ViewGroup) polygonModeIndicator.getParent()).removeView(polygonModeIndicator);
                    polygonModeIndicator = null;
                } catch (Exception e) {
                    Log.e(LOGTAG, "Error hiding polygon mode indicator", e);
                }
            });
        }
    }
    
    private String convertPointsToWkt(com.atakmap.coremap.maps.coords.GeoPoint[] points) {
        try {
            ArrayList<Coordinate> coordinates = new ArrayList<>();
            for (com.atakmap.coremap.maps.coords.GeoPoint point : points) {
                coordinates.add(new Coordinate(point.getLongitude(), point.getLatitude()));
            }
            
            // Ensure polygon is closed
            if (!coordinates.get(0).equals(coordinates.get(coordinates.size() - 1))) {
                coordinates.add(coordinates.get(0));
            }
            
            GeometryFactory factory = new GeometryFactory(new PrecisionModel(10000000.0));
            Polygon polygon = factory.createPolygon(coordinates.toArray(new Coordinate[0]));
            WKTWriter wktWriter = new WKTWriter();
            return wktWriter.write(polygon);
            
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to convert points to WKT", e);
            return null;
        }
    }
    
    private double calculatePolygonArea(com.atakmap.coremap.maps.coords.GeoPoint[] points) {
        if (points == null || points.length < 3) return 0.0;
        
        // Use shoelace formula for area calculation
        double area = 0.0;
        int n = points.length;
        
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            area += points[i].getLongitude() * points[j].getLatitude();
            area -= points[j].getLongitude() * points[i].getLatitude();
        }
        
        area = Math.abs(area) / 2.0;
        
        // Convert from square degrees to square kilometers
        double avgLat = 0;
        for (com.atakmap.coremap.maps.coords.GeoPoint p : points) {
            avgLat += p.getLatitude();
        }
        avgLat /= points.length;
        
        double metersPerDegreeLat = 111320.0;
        double metersPerDegreeLon = 111320.0 * Math.cos(Math.toRadians(avgLat));
        
        return area * (metersPerDegreeLat * metersPerDegreeLon) / 1000000.0;
    }
    
    private void showSavedAOIsDialog() {
        AOIManager aoiManager = new AOIManager(pluginContext);
        ArrayList<AOIManager.AOI> aois = aoiManager.getAllAOIs();
        
        if (aois.isEmpty()) {
            new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle(pluginContext.getString(R.string.saved_aois))
                .setMessage("No saved AOIs found. Create one using coordinate input or polygon drawing.")
                .setPositiveButton(pluginContext.getString(R.string.ok), null)
                .create()
                .show();
            return;
        }
        
        // Create list of AOI names
        String[] aoiNames = new String[aois.size()];
        for (int i = 0; i < aois.size(); i++) {
            AOIManager.AOI aoi = aois.get(i);
            aoiNames[i] = aoi.name + " (" + String.format("%.1f km²", aoi.areaKm2) + ")";
        }
        
        new AlertDialog.Builder(MapView.getMapView().getContext())
            .setTitle(pluginContext.getString(R.string.saved_aois))
            .setItems(aoiNames, (dialog, which) -> {
                AOIManager.AOI selectedAOI = aois.get(which);
                // Launch new order with selected AOI
                Intent newOrderIntent = new Intent();
                newOrderIntent.setAction(NewOrderFragment.ACTION);
                newOrderIntent.putExtra("aoi", selectedAOI.wkt);
                newOrderIntent.putExtra("aoiName", selectedAOI.name);
                AtakBroadcast.getInstance().sendBroadcast(newOrderIntent);
            })
            .setNegativeButton(pluginContext.getString(R.string.cancel), null)
            .create()
            .show();
    }
}
