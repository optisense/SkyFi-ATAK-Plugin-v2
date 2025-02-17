package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.dropdown.DropDown;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.MapView;
import com.skyfi.atak.plugin.skyfiapi.Archive;
import com.skyfi.atak.plugin.skyfiapi.Order;
import com.skyfi.atak.plugin.skyfiapi.OrderResponse;
import com.skyfi.atak.plugin.skyfiapi.SkyFiAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
        Log.d(LOGTAG, "Constructor");
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
                    Log.d(LOGTAG, "Got orders");
                    //orders.addAll(Arrays.asList(response.body().getOrders()));
                    synchronized (ordersRecyclerViewAdapter) {
                        ordersRecyclerViewAdapter.notifyDataSetChanged();
                    }
                    for (Order order : response.body().getOrders()) {
                        Log.d(LOGTAG, order.toString());
                        orders.add(order);
                    }
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

    }
}
