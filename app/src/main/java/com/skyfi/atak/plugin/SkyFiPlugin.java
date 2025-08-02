package com.skyfi.atak.plugin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

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
        
        // Add the toolbar item immediately since we're using the new plugin API
        if (uiService != null) {
            uiService.addToolbarItem(toolbarItem);
            Log.d(LOGTAG, "Added toolbar item to UI service");
        } else {
            Log.e(LOGTAG, "UI service is null, cannot add toolbar item");
        }
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
    }

    private void showPane() {
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
        if(!uiService.isPaneVisible(templatePane)) {
            uiService.showPane(templatePane, null);
        }

        ArrayList<String> options = new ArrayList<>();
        options.add(pluginContext.getString(R.string.view_orders));
        options.add(pluginContext.getString(R.string.new_order_my_location));
        options.add(pluginContext.getString(R.string.coordinate_input));
        options.add(pluginContext.getString(R.string.manage_aois));
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
                // Coordinate Input
                showCoordinateInputDialog();
                break;
            case 3:
                // Manage AOIs
                showAOIManagementDialog();
                break;
            case 4:
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
            case 5:
                Intent profileIntent = new Intent();
                profileIntent.setAction(Profile.ACTION);
                AtakBroadcast.getInstance().sendBroadcast(profileIntent);
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
            pluginContext.getString(R.string.latitude) + "/" + pluginContext.getString(R.string.longitude),
            pluginContext.getString(R.string.mgrs_coordinates),
            pluginContext.getString(R.string.pindrop_tasking)
        };
        
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle(pluginContext.getString(R.string.coordinate_input))
                .setItems(inputMethods, (dialog, which) -> {
                    switch (which) {
                        case 0: // Lat/Lon
                            showLatLonInputDialog();
                            break;
                        case 1: // MGRS
                            showMGRSInputDialog();
                            break;
                        case 2: // Pindrop
                            startPindropTasking();
                            break;
                    }
                })
                .setNegativeButton(pluginContext.getString(R.string.cancel), null)
                .show();
    }
    
    private void showLatLonInputDialog() {
        View inputView = PluginLayoutInflater.inflate(pluginContext, R.layout.coordinate_input_dialog, null);
        EditText latInput = inputView.findViewById(R.id.latitude_input);
        EditText lonInput = inputView.findViewById(R.id.longitude_input);
        
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle(pluginContext.getString(R.string.latitude) + "/" + pluginContext.getString(R.string.longitude))
                .setView(inputView)
                .setPositiveButton(pluginContext.getString(R.string.ok), (dialog, which) -> {
                    try {
                        double lat = Double.parseDouble(latInput.getText().toString());
                        double lon = Double.parseDouble(lonInput.getText().toString());
                        
                        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                            throw new NumberFormatException("Invalid coordinates");
                        }
                        
                        createOrderFromCoordinates(lat, lon);
                    } catch (NumberFormatException e) {
                        new AlertDialog.Builder(MapView.getMapView().getContext())
                                .setTitle(pluginContext.getString(R.string.error))
                                .setMessage("Invalid coordinates. Please enter valid latitude (-90 to 90) and longitude (-180 to 180)")
                                .setPositiveButton(pluginContext.getString(R.string.ok), null)
                                .show();
                    }
                })
                .setNegativeButton(pluginContext.getString(R.string.cancel), null)
                .show();
    }
    
    private void showMGRSInputDialog() {
        EditText mgrsInput = new EditText(MapView.getMapView().getContext());
        mgrsInput.setHint("e.g., 38SMB4484");
        
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle(pluginContext.getString(R.string.mgrs_coordinates))
                .setView(mgrsInput)
                .setPositiveButton(pluginContext.getString(R.string.ok), (dialog, which) -> {
                    String mgrsString = mgrsInput.getText().toString().trim();
                    if (!mgrsString.isEmpty()) {
                        try {
                            // For now, show error that MGRS is not yet supported
                            new AlertDialog.Builder(MapView.getMapView().getContext())
                                    .setTitle(pluginContext.getString(R.string.error))
                                    .setMessage("MGRS input coming soon")
                                    .setPositiveButton(pluginContext.getString(R.string.ok), null)
                                    .show();
                            return;
                            // TODO: Implement MGRS parsing when API is clarified
                        } catch (Exception e) {
                            new AlertDialog.Builder(MapView.getMapView().getContext())
                                    .setTitle(pluginContext.getString(R.string.error))
                                    .setMessage("Invalid MGRS coordinate: " + mgrsString)
                                    .setPositiveButton(pluginContext.getString(R.string.ok), null)
                                    .show();
                        }
                    }
                })
                .setNegativeButton(pluginContext.getString(R.string.cancel), null)
                .show();
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
}
