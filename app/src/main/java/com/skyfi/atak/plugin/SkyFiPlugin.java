package com.skyfi.atak.plugin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

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
                        pluginContext.getResources().getDrawable(R.drawable.ic_launcher),
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
                        pluginContext.getResources().getDrawable(R.drawable.ic_launcher),
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

        AtakBroadcast.DocumentedIntentFilter documentedIntentFilter = new AtakBroadcast.DocumentedIntentFilter();
        documentedIntentFilter.addAction(Orders.ACTION);
        registerDropDownReceiver(new Orders(MapView.getMapView(), pluginContext), documentedIntentFilter);

        AtakBroadcast.DocumentedIntentFilter newOrderIntentFilter = new AtakBroadcast.DocumentedIntentFilter();
        newOrderIntentFilter.addAction(NewOrderFragment.ACTION);
        registerDropDownReceiver(new NewOrderFragment(MapView.getMapView(), pluginContext, ""), newOrderIntentFilter);

        AtakBroadcast.DocumentedIntentFilter archiveSearchFilter = new AtakBroadcast.DocumentedIntentFilter();
        archiveSearchFilter.addAction(ArchiveSearch.ACTION);
        registerDropDownReceiver(new ArchiveSearch(MapView.getMapView(), pluginContext, ""), archiveSearchFilter);

        AtakBroadcast.DocumentedIntentFilter archivesBrowserFilter = new AtakBroadcast.DocumentedIntentFilter();
        archivesBrowserFilter.addAction(ArchivesBrowser.ACTION);
        registerDropDownReceiver(new ArchivesBrowser(MapView.getMapView(), pluginContext), archivesBrowserFilter);

        AtakBroadcast.DocumentedIntentFilter taskingOrderFilter = new AtakBroadcast.DocumentedIntentFilter();
        taskingOrderFilter.addAction(TaskingOrderFragment.ACTION);
        registerDropDownReceiver(new TaskingOrderFragment(MapView.getMapView(), pluginContext), taskingOrderFilter);

        AtakBroadcast.DocumentedIntentFilter profileFilter = new AtakBroadcast.DocumentedIntentFilter();
        profileFilter.addAction(Profile.ACTION);
        registerDropDownReceiver(new Profile(MapView.getMapView(), pluginContext), profileFilter);

        OrderUtility orderUtility = new OrderUtility(MapView.getMapView(), pluginContext);
        AtakBroadcast.DocumentedIntentFilter filter = new AtakBroadcast.DocumentedIntentFilter();
        filter.addAction("com.atakmap.android.cot_utility.receivers.cotMenu",
                "this intent launches the cot send utility",
                new DocumentedExtra[] {
                        new DocumentedExtra("targetUID",
                                "the map item identifier used to populate the drop down")
                });
        registerDropDownReceiver(orderUtility, filter);
    }

    @Override
    public void onCreate(Context context, Intent intent, MapView mapView) {
        super.onCreate(context, intent, mapView);
        this.mapView = mapView;
    }

    @Override
    public void onStart() {
        // the plugin is starting, add the button to the toolbar
        if (uiService == null)
            return;

        uiService.addToolbarItem(toolbarItem);
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
                    radiusTextView.setText("0KM");

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
            case 3:
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

            // Get the four corners of the square
            GeoPoint selfMarker = new GeoPoint(lat, lon);
            IGeoPoint corner1 = GeoCalculations.pointAtDistance(selfMarker, 45, radius);
            coordinates.add(new Coordinate(corner1.getLongitude(), corner1.getLatitude()));

            IGeoPoint corner2 = GeoCalculations.pointAtDistance(selfMarker, 135, radius);
            coordinates.add(new Coordinate(corner2.getLongitude(), corner2.getLatitude()));

            IGeoPoint corner3 = GeoCalculations.pointAtDistance(selfMarker, 225, radius);
            coordinates.add(new Coordinate(corner3.getLongitude(), corner3.getLatitude()));

            IGeoPoint corner4 = GeoCalculations.pointAtDistance(selfMarker, 315, radius);
            coordinates.add(new Coordinate(corner4.getLongitude(), corner4.getLatitude()));

            // Add the first corner again to close the square
            coordinates.add(new Coordinate(corner1.getLongitude(), corner1.getLatitude()));

            GeometryFactory factory = new GeometryFactory(new PrecisionModel(10000000.0));
            Polygon polygon = factory.createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));
            WKTWriter wktWriter = new WKTWriter();
            return wktWriter.write(polygon);
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to make square WKT", e);
        }

        return null;
    }
}
