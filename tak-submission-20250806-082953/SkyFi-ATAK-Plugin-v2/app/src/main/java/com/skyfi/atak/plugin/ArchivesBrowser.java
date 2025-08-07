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
import com.skyfi.atak.plugin.skyfiapi.ArchiveOrder;
import com.skyfi.atak.plugin.skyfiapi.ArchiveResponse;
import com.skyfi.atak.plugin.skyfiapi.ArchivesRequest;
import com.skyfi.atak.plugin.skyfiapi.SkyFiAPI;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    // Start the pageNumber at -1 since we don't know the total results and the first page is a GET and the subsequent pages are a POST
    private int pageNumber = -1;
    private ArrayList<String> pageHashes = new ArrayList<>();
    private ArchivesRequest request = new ArchivesRequest();
    private String aoi;

    Button nextButton;
    Button previousButton;
    Button cacheButton;
    Button filterButton;
    Button sortButton;
    SwipeRefreshLayout refreshPage;
    ImageCacheManager cacheManager;
    AORFilterManager aorFilterManager;
    private ArrayList<Archive> originalArchives = new ArrayList<>();
    private String currentSortMode = "date";

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
        cacheButton = mainView.findViewById(R.id.cache_button);
        filterButton = mainView.findViewById(R.id.filter_button);
        sortButton = mainView.findViewById(R.id.sort_button);
        
        // Initialize managers
        cacheManager = ImageCacheManager.getInstance(context);
        aorFilterManager = new AORFilterManager(context);

        nextButton.setOnClickListener(view -> {
            if (!pageHashes.isEmpty() && pageNumber < pageHashes.size() - 1)
                pageNumber++;
            postArchives();
            // Auto-scroll to top
            recyclerView.scrollToPosition(0);
        });

        previousButton.setOnClickListener(view -> {
            if (pageNumber > 0) {
                postArchives();
                pageNumber--;
            }
            else if (pageNumber == 0) {
                pageNumber--;
                getArchives();
            }
            // Auto-scroll to top
            recyclerView.scrollToPosition(0);
        });

        cacheButton.setOnClickListener(view -> {
            cacheCurrentImages();
        });
        
        filterButton.setOnClickListener(view -> {
            showAORFilterDialog();
        });
        
        sortButton.setOnClickListener(view -> {
            showSortDialog();
        });

        refreshPage = mainView.findViewById(R.id.pull_to_refresh);
        refreshPage.setOnRefreshListener(() -> {
            if (pageHashes.isEmpty() || pageNumber == -1)
                getArchives();
            else
                postArchives();
        });
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
        // Don't update if there's only one page of results
        if (pageHashes.isEmpty())
            return;

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
                aoi = archiveResponse.getRequest().getAoi();

                try {
                    if (archiveResponse.getNextPage() != null) {
                        String url = "https://app.skyfi.com" + archiveResponse.getNextPage();
                        Uri uri = Uri.parse(url);
                        String hash = uri.getQueryParameter("page");
                        if (!pageHashes.contains(hash)) {
                            pageHashes.add(hash);
                        }
                    }
                }
                catch (Exception e) {
                    Log.e(LOGTAG, "Failed to parse page hash", e);
                }

                // Store original archives before filtering
                originalArchives.clear();
                originalArchives.addAll(archiveResponse.getArchives());
                
                // Apply AOR filtering
                List<Archive> filteredArchives = aorFilterManager.filterArchivesByAOR(originalArchives);
                archives.clear();
                archives.addAll(filteredArchives);

                if (pageNumber == -1 && archiveResponse.getNextPage() != null) {
                    // First page
                    nextButton.setVisibility(VISIBLE);
                    previousButton.setVisibility(GONE);
                }
                else if (archiveResponse.getNextPage() != null){
                    nextButton.setVisibility(VISIBLE);
                    previousButton.setVisibility(VISIBLE);
                }
                else if (pageHashes.isEmpty()) {
                    // Only one page of results
                    nextButton.setVisibility(GONE);
                    previousButton.setVisibility(GONE);
                }
                else {
                    // Last page
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
        pageNumber = -1;
        pageHashes.clear();
        request = new ArchivesRequest();

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

        new AlertDialog.Builder(MapView.getMapView().getContext())
            .setTitle(context.getString(R.string.place_order))
            .setMessage(context.getString(R.string.are_you_sure))
            .setPositiveButton(context.getString(R.string.ok), (dialogInterface, i) -> placeOrder(archive))
            .setNegativeButton(context.getString(R.string.cancel), null)
                .create()
                .show();
    }

    private void placeOrder(Archive archive) {
        refreshPage.setRefreshing(true);
        ArchiveOrder order = new ArchiveOrder();
        order.setArchiveId(archive.getArchiveId());
        order.setAoi(aoi);

        apiClient = new APIClient().getApiClient();
        apiClient.archiveOrder(order).enqueue(new Callback<ArchiveResponse>() {
            @Override
            public void onResponse(@NonNull Call<ArchiveResponse> call, @NonNull Response<ArchiveResponse> response) {
                refreshPage.setRefreshing(false);
                if (response.code() == 201) {
                    new AlertDialog.Builder(MapView.getMapView().getContext())
                            .setTitle(context.getString(R.string.place_order))
                            .setMessage(context.getString(R.string.order_placed))
                            .setPositiveButton(context.getString(R.string.ok), null)
                            .create()
                            .show();
                }
                else {
                    try {
                        JSONObject errorJson = new JSONObject(response.errorBody().string());
                        String message = errorJson.getJSONArray("detail").getJSONObject(0).getString("msg");
                        Log.e(LOGTAG, "Failed to place order: " + message);
                        new AlertDialog.Builder(MapView.getMapView().getContext())
                                .setTitle(context.getString(R.string.error_placing_order))
                                .setMessage(message)
                                .setPositiveButton(context.getString(R.string.ok), null)
                                .create()
                                .show();
                    } catch (Exception e) {
                        Log.e(LOGTAG, "Failed to fail", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArchiveResponse> call, @NonNull Throwable throwable) {
                refreshPage.setRefreshing(false);
                Log.e(LOGTAG, "Failed to place order", throwable);
                new AlertDialog.Builder(MapView.getMapView().getContext())
                        .setTitle(context.getString(R.string.error_placing_order))
                        .setMessage(throwable.getMessage())
                        .setPositiveButton(context.getString(R.string.ok), null)
                        .create()
                        .show();
            }
        });
    }
    
    private void cacheCurrentImages() {
        if (archives.isEmpty()) {
            showError(context.getString(R.string.cache_failed), context.getString(R.string.no_images_to_cache));
            return;
        }
        
        // Extract image URLs from current archives
        java.util.List<String> imageUrls = new java.util.ArrayList<>();
        for (Archive archive : archives) {
            // Get thumbnail URLs from the HashMap
            if (archive.getThumbnailUrls() != null) {
                for (String thumbnailUrl : archive.getThumbnailUrls().values()) {
                    if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                        imageUrls.add(thumbnailUrl);
                    }
                }
            }
            // Also add titles URL if available (likely higher resolution)
            if (archive.getTitlesUrl() != null && !archive.getTitlesUrl().isEmpty()) {
                imageUrls.add(archive.getTitlesUrl());
            }
        }
        
        if (imageUrls.isEmpty()) {
            showError(context.getString(R.string.cache_failed), context.getString(R.string.no_images_to_cache));
            return;
        }
        
        // Show progress dialog
        AlertDialog progressDialog = new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle(context.getString(R.string.caching_progress))
                .setMessage("0 / " + imageUrls.size())
                .setCancelable(false)
                .create();
        progressDialog.show();
        
        // Start caching with progress callback
        cacheManager.cacheHighResImages(imageUrls, new ImageCacheManager.ProgressCallback() {
            @Override
            public void onProgress(int progress, int total) {
                // Update progress dialog on main thread
                MapView.getMapView().post(() -> {
                    if (progressDialog.isShowing()) {
                        progressDialog.setMessage(progress + " / " + total);
                    }
                });
            }
            
            @Override
            public void onComplete(boolean success, String message) {
                // Show completion on main thread
                MapView.getMapView().post(() -> {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    
                    String title = success ? context.getString(R.string.cache_complete) : context.getString(R.string.cache_failed);
                    showError(title, message);
                });
            }
        });
    }
    
    private void showAORFilterDialog() {
        List<String> aorOptions = aorFilterManager.getAvailableAORs();
        String[] aorArray = aorOptions.toArray(new String[0]);
        
        // Convert "all" to a more user-friendly name
        for (int i = 0; i < aorArray.length; i++) {
            if ("all".equals(aorArray[i])) {
                aorArray[i] = context.getString(R.string.all_aors);
            }
        }
        
        int selectedIndex = Math.max(0, aorOptions.indexOf(aorFilterManager.getSelectedAOR()));
        
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle(context.getString(R.string.select_aor))
                .setSingleChoiceItems(aorArray, selectedIndex, (dialog, which) -> {
                    String selectedAOR = aorOptions.get(which);
                    aorFilterManager.setSelectedAOR(selectedAOR);
                    applyCurrentFilter();
                    dialog.dismiss();
                })
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show();
    }
    
    private void applyCurrentFilter() {
        if (!originalArchives.isEmpty()) {
            List<Archive> filteredArchives = aorFilterManager.filterArchivesByAOR(originalArchives);
            archives.clear();
            archives.addAll(filteredArchives);
            
            synchronized (recyclerViewAdapter) {
                recyclerViewAdapter.notifyDataSetChanged();
            }
            
            // Update filter button text to show current filter
            String selectedAOR = aorFilterManager.getSelectedAOR();
            if ("all".equals(selectedAOR)) {
                filterButton.setText(context.getString(R.string.aor_filter));
            } else {
                filterButton.setText(selectedAOR);
            }
        }
    }
    
    private void showSortDialog() {
        String[] sortOptions = {
            context.getString(R.string.sort_by_date),
            context.getString(R.string.sort_by_location),
            context.getString(R.string.sort_by_source)
        };
        
        String[] sortModes = {"date", "location", "source"};
        
        int selectedIndex = 0;
        for (int i = 0; i < sortModes.length; i++) {
            if (sortModes[i].equals(currentSortMode)) {
                selectedIndex = i;
                break;
            }
        }
        
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle("Sort Images")
                .setSingleChoiceItems(sortOptions, selectedIndex, (dialog, which) -> {
                    currentSortMode = sortModes[which];
                    sortArchives();
                    sortButton.setText(sortOptions[which]);
                    dialog.dismiss();
                })
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show();
    }
    
    private void sortArchives() {
        if (archives.isEmpty()) return;
        
        java.util.Collections.sort(archives, (a1, a2) -> {
            switch (currentSortMode) {
                case "date":
                    return a2.getCaptureTimestamp().compareTo(a1.getCaptureTimestamp()); // Newest first
                case "location":
                    // Sort by provider as proxy for location/region
                    return a1.getProvider().compareTo(a2.getProvider());
                case "source":
                    return a1.getProvider().compareTo(a2.getProvider());
                default:
                    return 0;
            }
        });
        
        synchronized (recyclerViewAdapter) {
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }
}
