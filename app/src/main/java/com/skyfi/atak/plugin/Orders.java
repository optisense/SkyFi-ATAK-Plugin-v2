package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.Intent;

import com.atakmap.android.contentservices.ServiceListing;
import com.atakmap.android.layers.LayersManagerBroadcastReceiver;
import com.atakmap.android.maps.tilesets.mobac.WMSQueryLayers;
import com.atakmap.android.maps.tilesets.mobac.WMTSQueryLayers;
import com.atakmap.android.maps.tilesets.mobac.WebMapLayer;
import com.atakmap.android.maps.tilesets.mobac.WebMapLayerService;
import com.atakmap.coremap.log.Log;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.dropdown.DropDown;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.maps.coords.GeoBounds;
import com.atakmap.map.layer.raster.mobac.CustomMobacMapSource;
import com.atakmap.map.layer.raster.mobac.CustomWmsMobacMapSource;
import com.atakmap.map.layer.raster.mobac.MobacMapSourceFactory;
import com.skyfi.atak.plugin.skyfiapi.Order;
import com.skyfi.atak.plugin.skyfiapi.OrderResponse;
import com.skyfi.atak.plugin.skyfiapi.SkyFiAPI;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Orders extends DropDownReceiver implements DropDown.OnStateListener, OrdersRecyclerViewAdapter.ItemClickListener {
    public final static String ACTION = "com.skyfi.orders";
    private final static String LOGTAG = "SkyFiOrders";
    private View mainView;
    private LayoutInflater inflater;
    private ArrayList<Order> orders = new ArrayList<>();
    private final OrdersRecyclerViewAdapter ordersRecyclerViewAdapter;
    private Intent intent;
    private SkyFiAPI apiClient;
    private final RecyclerView recyclerView;
    private Context context;

    public Orders(MapView mapView, Context context) {
        super(mapView);
        this.context = context;
        //inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mainView = PluginLayoutInflater.inflate(context, R.layout.orders, null);

        recyclerView = mainView.findViewById(R.id.order_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        ordersRecyclerViewAdapter = new OrdersRecyclerViewAdapter(context, orders);
        ordersRecyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(ordersRecyclerViewAdapter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.intent = intent;

        showDropDown(mainView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH, FULL_HEIGHT, false);
        apiClient = new APIClient().getApiClient();
        apiClient.getOrders().enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.body() != null) {
                    orders.clear();
                    orders.addAll(Arrays.asList(response.body().getOrders()));
                    synchronized (ordersRecyclerViewAdapter) {
                        ordersRecyclerViewAdapter.notifyDataSetChanged();
                    }
                } else {
                    Log.e(LOGTAG, "Order response body is null");
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable throwable) {
                Log.e(LOGTAG, "Failed to get orders", throwable);
            }
        });
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
            String tileUrl = order.getTilesUrl().replace("{z}", "{$z}");
            tileUrl = tileUrl.replace("{x}", "{$x}");
            tileUrl = tileUrl.replace("{y}", "{$y}");

            Log.d(LOGTAG, "Got order " + tileUrl);

            // Jackson doesn't work for whatever reason so the XML file is created manually.
            // ATAK does it this way too
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version='1.0' encoding='UTF-8'?>\n");
            sb.append("<customMapSource><name>SkyFi</name><minZoom>0</minZoom><maxZoom>20</maxZoom>");
            sb.append("<tileType>png</tileType><tileUpdate>None</tileUpdate><url>");
            sb.append(tileUrl);
            sb.append("</url><backgroundColor>#000000</backgroundColor></customMapSource>");

            File f = new File(Environment.getExternalStorageDirectory().getPath() + "/atak/imagery/skyfi.xml");
            if (!f.exists())
                f.createNewFile();
            else {
                f.delete();
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/atak/imagery/skyfi.xml");
            fw.write(sb.toString());
            fw.close();

        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to make map source", e);
        }
    }
}
