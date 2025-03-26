package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;

import com.atakmap.android.ipc.DocumentedExtra;
import com.atakmap.coremap.log.Log;
import android.view.View;

import com.atak.plugins.impl.PluginContextProvider;
import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.dropdown.DropDownMapComponent;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import com.atakmap.app.preferences.ToolsPreferenceFragment;
import com.skyfi.atak.plugin.skyfiapi.Pong;
import com.skyfi.atak.plugin.skyfiapi.SkyFiAPI;

import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

import static android.content.Context.RECEIVER_NOT_EXPORTED;

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
        //options.add(pluginContext.getString(R.string.settings));

        RecyclerView recyclerView = mainView.findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(pluginContext));
        mainRecyclerViewAdapter = new MainRecyclerViewAdapter(pluginContext, options);
        mainRecyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(mainRecyclerViewAdapter);
    }

    private void registerReceiverUsingPluginContext(Context pluginContext, String name, DropDownReceiver rec, String actionName) {
        Log.d(LOGTAG, "Registering " + name + " receiver with intent filter");
        AtakBroadcast.DocumentedIntentFilter mainIntentFilter = new AtakBroadcast.DocumentedIntentFilter();
        mainIntentFilter.addAction(actionName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mainView.getContext().registerReceiver(rec, mainIntentFilter, RECEIVER_NOT_EXPORTED);
        } else {
            mainView.getContext().registerReceiver(rec, mainIntentFilter);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (position) {
            case 0:
                Log.d(LOGTAG, "Launching orders");
                Intent intent = new Intent();
                intent.setAction(Orders.ACTION);
                AtakBroadcast.getInstance().sendBroadcast(intent);
                break;
        }
    }
}
