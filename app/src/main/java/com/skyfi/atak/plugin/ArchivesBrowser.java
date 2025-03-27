package com.skyfi.atak.plugin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.dropdown.DropDown;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;
import com.skyfi.atak.plugin.skyfiapi.Archive;
import com.skyfi.atak.plugin.skyfiapi.ArchiveResponse;
import com.skyfi.atak.plugin.skyfiapi.ArchivesRequest;
import com.skyfi.atak.plugin.skyfiapi.SkyFiAPI;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ArchivesBrowser extends DropDownReceiver implements DropDown.OnStateListener, ArchivesBrowserRecyclerViewAdapter.ItemClickListener {
    public final static String ACTION = "com.skyfi.archive_browser";
    private final static String LOGTAG = "SkyFiArchiveBrowser";
    private View mainView;
    private ArrayList<Archive> archives = new ArrayList<>();
    private final ArchivesBrowserRecyclerViewAdapter recyclerViewAdapter;
    private SkyFiAPI apiClient;
    private final RecyclerView recyclerView;
    private Context context;
    private int pageNumber = 0;
    private int pageSize = 10;
    private ArrayList<String> pageHashes = new ArrayList<>();
    private ArchivesRequest request = new ArchivesRequest();

    Button nextButton;
    Button previousButton;
    SwipeRefreshLayout refreshPage;

    protected ArchivesBrowser(MapView mapView, Context context) {
        super(mapView);

        this.context = context;
        mainView = PluginLayoutInflater.inflate(context, R.layout.archives, null);

        recyclerView = mainView.findViewById(R.id.archives_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewAdapter = new ArchivesBrowserRecyclerViewAdapter(context, archives);
        recyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(recyclerViewAdapter);

        nextButton = mainView.findViewById(R.id.next_button);
        previousButton = mainView.findViewById(R.id.previous_button);

        nextButton.setOnClickListener(view -> {
            postArchives();
            pageNumber++;
        });

        previousButton.setOnClickListener(view -> {
            postArchives();
            pageNumber--;
        });

        refreshPage = mainView.findViewById(R.id.pull_to_refresh);
        refreshPage.setOnRefreshListener(this::postArchives);
    }

    private void getArchives() {
        apiClient = new APIClient().getApiClient();
        apiClient.searchArchives(request).enqueue(new Callback<ArchiveResponse>() {
            @Override
            public void onResponse(@NonNull Call<ArchiveResponse> call, @NonNull Response<ArchiveResponse> response) {
                parseResponse(call, response);
            }

            @Override
            public void onFailure(@NonNull Call<ArchiveResponse> call, @NonNull Throwable throwable) {
                Log.e(LOGTAG, "Failed to search archives", throwable);
                showError("Failed to search archives", throwable.getMessage());
            }
        });
    }

    private void postArchives() {
        apiClient = new APIClient().getApiClient();
        apiClient.searchArchivesNextPage(pageHashes.get(pageNumber)).enqueue(new Callback<ArchiveResponse>() {
            @Override
            public void onResponse(@NonNull Call<ArchiveResponse> call, @NonNull Response<ArchiveResponse> response) {
                parseResponse(call, response);
            }

            @Override
            public void onFailure(Call<ArchiveResponse> call, Throwable throwable) {

            }
        });
    }

    private void parseResponse(@NonNull Call<ArchiveResponse> call, @NonNull Response<ArchiveResponse> response) {
        refreshPage.setRefreshing(false);
        if (response.body() != null) {
            try {
                ArchiveResponse archiveResponse = response.body();
                String hash = null;
                try {
                    if (archiveResponse.getNextPage() != null) {
                        String url = "https://app.skyfi.com" + archiveResponse.getNextPage();
                        Uri uri = Uri.parse(url);
                        hash = uri.getQueryParameter("page");
                        if (!pageHashes.contains(hash)) {
                            pageHashes.add(hash);
                        }
                    }
                }
                catch (Exception e) {
                    Log.d(LOGTAG, "Failed to parse hash", e);
                }

                archives.clear();
                archives.addAll(archiveResponse.getArchives());

                if (pageNumber == 0) {
                    nextButton.setVisibility(VISIBLE);
                    previousButton.setVisibility(GONE);
                }
                else if (archiveResponse.getNextPage() != null){
                    nextButton.setVisibility(VISIBLE);
                    previousButton.setVisibility(VISIBLE);
                }
                else {
                    nextButton.setVisibility(GONE);
                    previousButton.setVisibility(VISIBLE);
                }

                synchronized (recyclerViewAdapter) {
                    recyclerViewAdapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                Log.e(LOGTAG, "Failed to search archives", e);
                showError("Failed to search archives", e.getMessage());
            }
        }
        else {
            int responseCode = response.code();
            Log.e(LOGTAG, "Archive search response is null: " + responseCode);
            try {
                Log.d(LOGTAG, call.request().body().toString());
                Log.e(LOGTAG, response.errorBody().string());
                showError("Error searching archives", "Response Code: " + responseCode);
            } catch (Exception e) {
                Log.e(LOGTAG, "Failed to fail", e);
            }
        }
    }

    private void showError(String title, String message) {
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
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
        if (intent.getAction() == null) return;

        if (intent.getAction().equals(ACTION)) {

            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                showDropDown(mainView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH, FULL_HEIGHT, false);
            } else {
                showDropDown(mainView, FULL_WIDTH, HALF_HEIGHT, FULL_WIDTH, HALF_HEIGHT, false);
            }

            request = (ArchivesRequest) intent.getSerializableExtra("request");

            getArchives();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Archive archive = archives.get(position);
        Log.d(LOGTAG, archive.toString());
    }
}
