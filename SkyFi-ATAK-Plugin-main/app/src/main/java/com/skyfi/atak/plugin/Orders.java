package com.skyfi.atak.plugin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.layers.LayersMapComponent;
import com.atakmap.android.maps.Polyline;
import com.atakmap.android.util.ATAKUtilities;
import com.atakmap.coremap.io.IOProviderFactory;
import com.atakmap.coremap.log.Log;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.dropdown.DropDown;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.skyfi.atak.plugin.skyfiapi.Order;
import com.skyfi.atak.plugin.skyfiapi.OrderResponse;
import com.skyfi.atak.plugin.skyfiapi.SkyFiAPI;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.atakmap.android.importexport.ImportExportMapComponent.ACTION_IMPORT_DATA;
import static com.atakmap.android.layers.LayersManagerBroadcastReceiver.ACTION_SELECT_LAYER;
import static com.atakmap.android.layers.LayersManagerBroadcastReceiver.EXTRA_LAYER_NAME;
import static com.atakmap.android.layers.LayersManagerBroadcastReceiver.EXTRA_SELECTION;

public class Orders extends DropDownReceiver implements DropDown.OnStateListener, OrdersRecyclerViewAdapter.ItemClickListener {
    public final static String ACTION = "com.skyfi.orders";
    private final static String LOGTAG = "SkyFiOrders";
    private View mainView;
    private ArrayList<Order> orders = new ArrayList<>();
    private final OrdersRecyclerViewAdapter ordersRecyclerViewAdapter;
    private SkyFiAPI apiClient;
    private final RecyclerView recyclerView;
    private Context context;
    private int pageNumber = 0;
    private int pageSize = 10;
    private String mobacUri;
    private AORFilterManager aorFilterManager;

    // AOR Filter controls for Orders
    private RadioGroup ordersAorFilterGroup;
    private RadioButton ordersAorFilterWorld;
    private RadioButton ordersAorFilterRegion;
    private TextView ordersAorFilterDescription;

    Button nextButton;
    Button previousButton;
    SwipeRefreshLayout updateOrders;

    public Orders(MapView mapView, Context context) {
        super(mapView);
        this.context = context;
        this.aorFilterManager = AORFilterManager.getInstance(context);
        mainView = PluginLayoutInflater.inflate(context, R.layout.orders, null);

        recyclerView = mainView.findViewById(R.id.order_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        ordersRecyclerViewAdapter = new OrdersRecyclerViewAdapter(context, orders);
        ordersRecyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(ordersRecyclerViewAdapter);

        nextButton = mainView.findViewById(R.id.next_button);
        previousButton = mainView.findViewById(R.id.previous_button);

        nextButton.setOnClickListener(view -> {
            pageNumber++;
            getOrders();
        });

        previousButton.setOnClickListener(view -> {
            pageNumber--;
            getOrders();
        });

        updateOrders = mainView.findViewById(R.id.pull_to_refresh);
        updateOrders.setOnRefreshListener(this::getOrders);
        
        // Initialize AOR filter controls
        ordersAorFilterGroup = mainView.findViewById(R.id.orders_aor_filter_group);
        ordersAorFilterWorld = mainView.findViewById(R.id.orders_aor_filter_world);
        ordersAorFilterRegion = mainView.findViewById(R.id.orders_aor_filter_region);
        ordersAorFilterDescription = mainView.findViewById(R.id.orders_aor_filter_description);
        
        // Set up AOR filter listeners
        ordersAorFilterGroup.setOnCheckedChangeListener(this::onOrdersAORFilterChanged);
        
        // Set initial filter state
        updateOrdersAORFilterUI();
    }
    
    /**
     * Handle AOR filter radio button changes for Orders
     */
    private void onOrdersAORFilterChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.orders_aor_filter_world) {
            aorFilterManager.setFilterMode(AORFilterManager.FilterMode.WORLD);
        } else if (checkedId == R.id.orders_aor_filter_region) {
            aorFilterManager.setFilterMode(AORFilterManager.FilterMode.REGION);
            // Update current region based on map view
            aorFilterManager.updateCurrentRegion(getMapView());
        }
        updateOrdersAORFilterDescription();
        
        // Refresh orders with new filter
        getOrders();
    }
    
    /**
     * Update the AOR filter UI for Orders based on current settings
     */
    private void updateOrdersAORFilterUI() {
        AORFilterManager.FilterMode currentMode = aorFilterManager.getFilterMode();
        
        if (currentMode == AORFilterManager.FilterMode.WORLD) {
            ordersAorFilterWorld.setChecked(true);
        } else {
            ordersAorFilterRegion.setChecked(true);
        }
        
        updateOrdersAORFilterDescription();
    }
    
    /**
     * Update the description text for the Orders AOR filter
     */
    private void updateOrdersAORFilterDescription() {
        String description;
        if (aorFilterManager.getFilterMode() == AORFilterManager.FilterMode.WORLD) {
            description = context.getString(R.string.aor_filter_description_world);
        } else {
            description = context.getString(R.string.aor_filter_description_region);
        }
        ordersAorFilterDescription.setText(description);
    }
    
    /**
     * Filter orders based on current AOR filter settings
     */
    private List<Order> filterOrdersByRegion(List<Order> ordersList) {
        if (aorFilterManager.getFilterMode() == AORFilterManager.FilterMode.WORLD) {
            return ordersList; // Return all orders if in world mode
        }
        
        List<Order> filteredOrders = new ArrayList<>();
        for (Order order : ordersList) {
            if (isOrderInRegion(order)) {
                filteredOrders.add(order);
            }
        }
        
        Log.d(LOGTAG, "Filtered " + ordersList.size() + " orders to " + filteredOrders.size() + " in region");
        return filteredOrders;
    }
    
    /**
     * Check if an order intersects with the current region
     */
    private boolean isOrderInRegion(Order order) {
        if (aorFilterManager.getFilterMode() == AORFilterManager.FilterMode.WORLD || order == null || order.getAoi() == null) {
            return true; // Show all if in world mode or no AOI available
        }
        
        try {
            // Parse the order's AOI and check if it intersects with current region
            WKTReader wktReader = new WKTReader();
            Geometry orderGeometry = wktReader.read(order.getAoi());
            
            // Use AORFilterManager to check if it's in region (this will handle the region boundary logic)
            Coordinate[] coords = orderGeometry.getCoordinates();
            if (coords.length > 0) {
                // Check if any point of the order's geometry is in the region
                for (Coordinate coord : coords) {
                    if (aorFilterManager.isPointInRegion(coord.y, coord.x)) {
                        return true;
                    }
                }
            }
            return false;
            
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to check order region intersection", e);
            return true; // Show if unable to determine
        }
    }

    private void getOrders() {
        recyclerView.setVisibility(GONE);
        nextButton.setVisibility(GONE);
        previousButton.setVisibility(GONE);
        updateOrders.setRefreshing(true);
        
        apiClient = new APIClient().getApiClient();
        apiClient.getOrders(pageNumber, pageSize).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                updateOrders.setRefreshing(false);

                if (response.body() != null) {
                    try {
                        if (!response.isSuccessful()) {
                            Log.e(LOGTAG, "Failed to get orders: " + response.message() + " " + response.code());
                            showAlert(context.getString(R.string.failed_to_get_orders), response.code() + ": " + response.message());
                            return;
                        }

                        recyclerView.setVisibility(VISIBLE);

                        int total = response.body().getTotal();
                        pageNumber = response.body().getRequest().getPageNumber();
                        pageSize = response.body().getRequest().getPageSize();
                        int totalPages = (int) Math.ceil((double) total / pageSize);

                        if (pageNumber + 1 < totalPages)
                            nextButton.setVisibility(VISIBLE);
                        else
                            nextButton.setVisibility(GONE);

                        if (pageNumber > 0)
                            previousButton.setVisibility(VISIBLE);
                        else
                            previousButton.setVisibility(GONE);

                        orders.clear();
                        
                        // Apply AOR filtering to orders
                        Order[] allOrders = response.body().getOrders();
                        List<Order> filteredOrders = filterOrdersByRegion(Arrays.asList(allOrders));
                        orders.addAll(filteredOrders);
                        synchronized (ordersRecyclerViewAdapter) {
                            ordersRecyclerViewAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        Log.e(LOGTAG, "Failed to get orders", e);
                        showAlert(context.getString(R.string.failed_to_get_orders), e.toString());
                    }
                } else{
                    Log.e(LOGTAG, "Order response body is null");
                    showAlert(context.getString(R.string.failed_to_get_orders), context.getString(R.string.orders_null));
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable throwable) {
                updateOrders.setRefreshing(false);
                Log.e(LOGTAG, "Failed to get orders: " + throwable.getMessage());
                showAlert(context.getString(R.string.failed_to_get_orders), throwable.getMessage());
            }
        });
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.ok), (dialogInterface, i) -> {})
                .create().show();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) return;

        if (intent.getAction().equals(ACTION)) {

            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                showDropDown(mainView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH, FULL_HEIGHT, false);
            } else {
                showDropDown(mainView, FULL_WIDTH, HALF_HEIGHT, FULL_WIDTH, HALF_HEIGHT, false);
            }

            getOrders();
        }
    }

    @Override
    public void onDropDownSelectionRemoved() {

    }

    @Override
    public void onDropDownClose() {

    }

    @Override
    public void onDropDownSizeChanged(double v, double v1) {

    }

    @Override
    public void onDropDownVisible(boolean b) {

    }

    @Override
    protected void disposeImpl() {

    }

    @Override
    public void onItemClick(View view, int position) {
        try {
            Order order = orders.get(position);

            if (order.getTilesUrl() == null) {
                new AlertDialog.Builder(MapView.getMapView().getContext())
                        .setTitle(context.getString(R.string.order_pending_title))
                        .setMessage(context.getString(R.string.order_pending))
                        .setPositiveButton(context.getString(R.string.ok), (dialogInterface, i) -> {})
                        .create().show();
                return;
            }

            String tileUrl = order.getTilesUrl().replace("{z}", "{$z}");
            tileUrl = tileUrl.replace("{x}", "{$x}");
            tileUrl = tileUrl.replace("{y}", "{$y}");

            // Jackson doesn't work for whatever reason so the XML file is created manually.
            // ATAK does it this way too
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version='1.0' encoding='UTF-8'?>\n");
            sb.append(String.format("<customMultiLayerMapSource><name>SkyFi %s</name><layers>", order.getOrderName()));

            // Google
            sb.append("<customMapSource><url>http://mt1.google.com/vt/lyrs=y&amp;x={$x}&amp;y={$y}&amp;z={$z}</url><layers>Google</layers>");
            sb.append("<name>Google Street</name><minZoom>0</minZoom><maxZoom>22</maxZoom><tileType>jpg</tileType></customMapSource>");

            //SkyFi
            sb.append("<customMapSource><url>");
            sb.append(tileUrl);
            sb.append("</url><minZoom>0</minZoom><maxZoom>22</maxZoom><name>skyfi</name><tileType>png</tileType><layers>skyfi</layers></customMapSource>");
            sb.append("</layers></customMultiLayerMapSource>");

            File f = new File(Environment.getExternalStorageDirectory().getPath() + "/atak/imagery/skyfi_" + order.getOrderName() + ".xml");
            if (IOProviderFactory.exists(f))
                IOProviderFactory.delete(f);
            IOProviderFactory.createNewFile(f);
            FileWriter fw = IOProviderFactory.getFileWriter(f);
            fw.write(sb.toString());
            fw.close();

            mobacUri = Uri.fromFile(f).toString();

            Intent intent = new Intent();
            intent.setAction(ACTION_IMPORT_DATA);
            intent.putExtra("contentType", LayersMapComponent.IMPORTER_CONTENT_TYPE);
            intent.putExtra("mimeType", LayersMapComponent.IMPORTER_DEFAULT_MIME_TYPE);
            intent.putExtra("showNotifications", false);
            intent.putExtra("uri", mobacUri);
            AtakBroadcast.getInstance().sendBroadcast(intent);

            // Wait five seconds before selecting the new map
            // Otherwise there is a race condition when selecting the new map occurs before the import has completed
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                Intent selectLayer = new Intent();
                selectLayer.setAction(ACTION_SELECT_LAYER);
                selectLayer.putExtra(EXTRA_LAYER_NAME, "SkyFi " + order.getOrderName());
                selectLayer.putExtra(EXTRA_SELECTION, "SkyFi " + order.getOrderName());
                AtakBroadcast.getInstance().sendBroadcast(selectLayer);
                Log.d(LOGTAG, "selected " + "SkyFi " + order.getOrderName());
            }, 5000);

            // Get the order's vertices and move the map to fit the imagery
            WKTReader wktReader = new WKTReader();
            Geometry aoi = wktReader.read(order.getAoi());

            List<GeoPoint> geoPoints = new ArrayList<>();

            for (Coordinate coordinate : aoi.getCoordinates()) {
                geoPoints.add(new GeoPoint(coordinate.getY(), coordinate.getX()));
            }

            GeoPoint[] points = geoPoints.toArray(new GeoPoint[geoPoints.size()]);

            ATAKUtilities.scaleToFit(MapView.getMapView(), points, 1000, 1000);
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to make map source", e);
        }
    }
}
