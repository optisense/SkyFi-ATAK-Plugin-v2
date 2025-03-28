package com.skyfi.atak.plugin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.View;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.dropdown.DropDown;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;
import com.skyfi.atak.plugin.skyfiapi.Archive;
import com.skyfi.atak.plugin.skyfiapi.PricingQuery;
import com.skyfi.atak.plugin.skyfiapi.PricingResponse;
import com.skyfi.atak.plugin.skyfiapi.SkyFiAPI;
import com.skyfi.atak.plugin.skyfiapi.TaskingOrder;

import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskingOrderPricing extends DropDownReceiver implements DropDown.OnStateListener, ArchivesBrowserRecyclerViewAdapter.ItemClickListener {
    public final static String ACTION = "com.skyfi.tasking_order_pricing";
    private final static String LOGTAG = "SkyFiTaskingPricing";
    private View mainView;
    private ArrayList<Archive> archives = new ArrayList<>();
    private final ArchivesBrowserRecyclerViewAdapter recyclerViewAdapter;
    private SkyFiAPI apiClient;
    private final RecyclerView recyclerView;
    private Context context;
    private String aoi;
    private TaskingOrder taskingOrder = new TaskingOrder();
    private PricingQuery pricingQuery = new PricingQuery("");
    private PricingResponse pricingResponse;
    private SwipeRefreshLayout refreshPage;

    protected TaskingOrderPricing(MapView mapView, Context context) {
        super(mapView);

        this.context = context;

        mainView = PluginLayoutInflater.inflate(context, R.layout.tasking_order_pricing, null);

        recyclerView = mainView.findViewById(R.id.tasking_order_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewAdapter = new ArchivesBrowserRecyclerViewAdapter(context, archives);
        recyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(recyclerViewAdapter);

        refreshPage = mainView.findViewById(R.id.pull_to_refresh);
        refreshPage.setOnRefreshListener(() -> {
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

    private void getPricing() {
        refreshPage.setRefreshing(true);

        apiClient = new APIClient().getApiClient();
        apiClient.getTaskingPricing(pricingQuery).enqueue(new Callback<PricingResponse>() {
            @Override
            public void onResponse(@NonNull Call<PricingResponse> call, @NonNull Response<PricingResponse> response) {
                refreshPage.setRefreshing(false);

                if (response.isSuccessful()) {
                    pricingResponse = response.body();

                }
                else {
                    int responseCode = response.code();
                    Log.e(LOGTAG, "Archive search response is null: " + responseCode);
                    try {
                        JSONObject errorJson = new JSONObject(response.errorBody().string());
                        String message = errorJson.getJSONArray("detail").getJSONObject(0).getString("msg");
                        Log.d(LOGTAG, call.request().body().toString());
                        Log.e(LOGTAG, message);
                        showError("Error searching archives", message);
                    } catch (Exception e) {
                        Log.e(LOGTAG, "Failed to fail", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PricingResponse> call, @NonNull Throwable throwable) {
                refreshPage.setRefreshing(false);
            }
        });
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

            aoi = intent.getStringExtra("aoi");
            pricingQuery.setAoi(aoi);
            getPricing();
        }
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    private void showError(String title, String message) {
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }
}
