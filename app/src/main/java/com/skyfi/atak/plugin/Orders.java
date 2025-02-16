package com.skyfi.atak.plugin;

import android.os.Bundle;
import android.util.Log;

import com.skyfi.atak.plugin.skyfiapi.Order;
import com.skyfi.atak.plugin.skyfiapi.OrderResponse;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Orders extends Fragment {
    private final static String LOGTAG = "Orders";

    public Orders() {
        super(R.layout.orders);

        RecyclerView recyclerView = getView().findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainRecyclerViewAdapter = new MainRecyclerViewAdapter(pluginContext, options);
        mainRecyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(mainRecyclerViewAdapter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new APIClient().getApiClient().getOrders().enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                for (Order order : response.body().getOrders()) {
                    Log.d(LOGTAG, order.toString());
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable throwable) {
                // TODO: AlertDialog
                Log.e(LOGTAG, "Failed to get orders: " + throwable.getLocalizedMessage(), throwable);
            }
        });
    }
}
