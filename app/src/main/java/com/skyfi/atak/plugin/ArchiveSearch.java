package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.View;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.dropdown.DropDown;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;
import com.skyfi.atak.plugin.skyfiapi.ArchiveResponse;
import com.skyfi.atak.plugin.skyfiapi.ArchivesRequest;
import com.skyfi.atak.plugin.skyfiapi.SkyFiAPI;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArchiveSearch extends DropDownReceiver implements DropDown.OnStateListener {
    private static final String LOGTAG = "ArchiveSearch";
    public final static String ACTION = "com.skyfi.search_archives";

    private SkyFiAPI apiClient;
    private View mainView;

    MapView mapView;
    Context context;
    String aoi;
    String fromDate;
    String toDate;
    float maxCloudCoverage;
    float maxOffNadirAngle;
    String[] resolutions;
    String[] productTypes;
    String[] providers;
    boolean openData;
    float minOverlapRatio;
    int pageSize;

    protected ArchiveSearch(MapView mapView, Context context, String aoi) {
        super(mapView);

        Log.d(LOGTAG, "Archive Search!!");

        this.mapView = mapView;
        this.context = context;
        this.aoi = aoi;

        mainView = PluginLayoutInflater.inflate(context, R.layout.archive_search, null);
    }

    private void searchArchives() {
        apiClient = new APIClient().getApiClient();
        apiClient.searchArchives(getArchivesRequest()).enqueue(new Callback<ArchiveResponse>() {
            @Override
            public void onResponse(Call<ArchiveResponse> call, Response<ArchiveResponse> response) {
                if (response.body() != null) {
                    try {

                    } catch (Exception e) {
                        Log.e(LOGTAG, "Failed to search archives", e);
                    }
                }
                else {
                    Log.e(LOGTAG, "Archive search response is null");
                }
            }

            @Override
            public void onFailure(Call<ArchiveResponse> call, Throwable throwable) {
                Log.e(LOGTAG, "Failed to search archives", throwable);
            }
        });
    }

    @NonNull
    private ArchivesRequest getArchivesRequest() {
        ArchivesRequest request = new ArchivesRequest();
        request.setAoi(aoi);
        request.setFromDate(fromDate);
        request.setToDate(toDate);
        request.setMaxCloudCoveragePercent(maxCloudCoverage);
        request.setMaxOffNadirAngle(maxOffNadirAngle);
        request.setResolutions(resolutions);
        request.setProductTypes(productTypes);
        request.setProviders(providers);
        request.setOpenData(openData);
        request.setMinOverlapRatio(minOverlapRatio);
        request.setPageSize(pageSize);
        return request;
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
    public void onReceive(Context context, Intent intent) {
        Log.d(LOGTAG, "onReceive: " + intent.getAction());
        if (intent.getAction() == null) return;

        if (intent.getAction().equals(ACTION)) {

            String aoi = intent.getStringExtra("aoi");
            this.aoi = aoi;

            if (aoi != null)
                Log.d(LOGTAG, aoi);
            else
                Log.e(LOGTAG, "aoi null");

            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                showDropDown(mainView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH, FULL_HEIGHT, false);
            } else {
                showDropDown(mainView, FULL_WIDTH, HALF_HEIGHT, FULL_WIDTH, HALF_HEIGHT, false);
            }
        }
    }
}
