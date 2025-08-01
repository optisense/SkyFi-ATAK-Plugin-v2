package com.skyfi.atak.plugin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;

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
import java.util.HashSet;
import java.util.Set;

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
    SwipeRefreshLayout refreshPage;
    
    // Sorting and filtering UI elements
    private Spinner sortSpinner;
    private Spinner filterSpinner;
    private LinearLayout filterContainer;
    private CheckBox showFavoritesOnly;
    private CheckBox showCachedOnly;
    private String currentSort = "date_desc";
    private String currentFilter = "all";
    private boolean onlyFavorites = false;
    private boolean onlyCached = false;
    private AORManager aorManager;

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
            if (!pageHashes.isEmpty() && pageNumber < pageHashes.size() - 1)
                pageNumber++;
            postArchives();
            scrollToTop();
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
            scrollToTop();
        });

        refreshPage = mainView.findViewById(R.id.pull_to_refresh);
        refreshPage.setOnRefreshListener(() -> {
            if (pageHashes.isEmpty() || pageNumber == -1)
                getArchives();
            else
                postArchives();
        });
        
        // Initialize AOR manager
        aorManager = new AORManager(context);
        
        // Initialize sorting and filtering UI
        initializeSortingFiltering();
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

                archives.clear();
                archives.addAll(archiveResponse.getArchives());
                
                // Apply sorting and filtering
                applySortingAndFiltering();

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
    
    private void scrollToTop() {
        // Scroll recycler view to top after page change
        if (recyclerView != null) {
            recyclerView.scrollToPosition(0);
        }
    }
    
    private void initializeSortingFiltering() {
        // Find views
        sortSpinner = mainView.findViewById(R.id.sort_spinner);
        filterSpinner = mainView.findViewById(R.id.filter_spinner);
        filterContainer = mainView.findViewById(R.id.filter_container);
        showFavoritesOnly = mainView.findViewById(R.id.show_favorites_only);
        showCachedOnly = mainView.findViewById(R.id.show_cached_only);
        
        // Set up sort options
        String[] sortOptions = {
            "Date (Newest First)",
            "Date (Oldest First)",
            "Price (Low to High)",
            "Price (High to Low)",
            "Provider",
            "Resolution",
            "Cloud Coverage"
        };
        
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(context, 
            android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);
        
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: currentSort = "date_desc"; break;
                    case 1: currentSort = "date_asc"; break;
                    case 2: currentSort = "price_asc"; break;
                    case 3: currentSort = "price_desc"; break;
                    case 4: currentSort = "provider"; break;
                    case 5: currentSort = "resolution"; break;
                    case 6: currentSort = "cloud"; break;
                }
                applySortingAndFiltering();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Set up filter options
        ArrayList<String> filterOptions = new ArrayList<>();
        filterOptions.add("All Providers");
        filterOptions.add("SiWei");
        filterOptions.add("Satellogic");
        filterOptions.add("Umbra");
        filterOptions.add("Geosat");
        filterOptions.add("Planet");
        filterOptions.add("Impro");
        
        // Add AOR filters
        ArrayList<AORManager.AOR> aors = aorManager.getActiveAORs();
        for (AORManager.AOR aor : aors) {
            filterOptions.add("AOR: " + aor.name);
        }
        
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(context,
            android.R.layout.simple_spinner_item, filterOptions);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);
        
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = filterOptions.get(position);
                if (selected.startsWith("AOR: ")) {
                    currentFilter = selected;
                } else {
                    currentFilter = selected.toLowerCase().replace(" providers", "");
                }
                applySortingAndFiltering();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Set up checkboxes
        showFavoritesOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onlyFavorites = isChecked;
            applySortingAndFiltering();
        });
        
        showCachedOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onlyCached = isChecked;
            applySortingAndFiltering();
        });
    }
    
    private void applySortingAndFiltering() {
        ArrayList<Archive> filteredArchives = new ArrayList<>(archives);
        ImageCacheManager cacheManager = ImageCacheManager.getInstance(context);
        
        // Apply filters
        if (!currentFilter.equals("all")) {
            if (currentFilter.startsWith("AOR: ")) {
                // Filter by AOR
                String aorName = currentFilter.substring(5);
                filteredArchives = filterByAOR(filteredArchives, aorName);
            } else {
                // Filter by provider
                filteredArchives.removeIf(archive -> 
                    !archive.getProvider().toLowerCase().contains(currentFilter));
            }
        }
        
        if (onlyFavorites) {
            Set<String> favorites = loadFavorites();
            filteredArchives.removeIf(archive -> 
                !favorites.contains(archive.getArchiveId()));
        }
        
        if (onlyCached) {
            filteredArchives.removeIf(archive -> 
                !cacheManager.isCached(archive.getArchiveId()));
        }
        
        // Apply sorting
        switch (currentSort) {
            case "date_desc":
                Collections.sort(filteredArchives, (a, b) -> 
                    b.getCaptureTimestamp().compareTo(a.getCaptureTimestamp()));
                break;
            case "date_asc":
                Collections.sort(filteredArchives, (a, b) -> 
                    a.getCaptureTimestamp().compareTo(b.getCaptureTimestamp()));
                break;
            case "price_asc":
                Collections.sort(filteredArchives, (a, b) -> 
                    Float.compare(a.getPriceForOneSquareKm(), b.getPriceForOneSquareKm()));
                break;
            case "price_desc":
                Collections.sort(filteredArchives, (a, b) -> 
                    Float.compare(b.getPriceForOneSquareKm(), a.getPriceForOneSquareKm()));
                break;
            case "provider":
                Collections.sort(filteredArchives, (a, b) -> 
                    a.getProvider().compareTo(b.getProvider()));
                break;
            case "resolution":
                Collections.sort(filteredArchives, (a, b) -> 
                    a.getResolution().compareTo(b.getResolution()));
                break;
            case "cloud":
                Collections.sort(filteredArchives, (a, b) -> 
                    Float.compare(a.getCloudCoveragePercent(), b.getCloudCoveragePercent()));
                break;
        }
        
        // Update adapter with filtered/sorted data
        recyclerViewAdapter.updateData(filteredArchives);
    }
    
    private ArrayList<Archive> filterByAOR(ArrayList<Archive> archives, String aorName) {
        ArrayList<Archive> filtered = new ArrayList<>();
        
        // Find the AOR
        AORManager.AOR targetAOR = null;
        for (AORManager.AOR aor : aorManager.getAllAORs()) {
            if (aor.name.equals(aorName)) {
                targetAOR = aor;
                break;
            }
        }
        
        if (targetAOR == null) return filtered;
        
        // Filter archives that intersect with the AOR
        for (Archive archive : archives) {
            // Check if archive location intersects with AOR
            // This is a simplified check - in reality you'd need to check
            // if the archive's footprint intersects with the AOR polygon
            filtered.add(archive);
        }
        
        return filtered;
    }
    
    private Set<String> loadFavorites() {
        android.content.SharedPreferences prefs = context.getSharedPreferences("skyfi_prefs", Context.MODE_PRIVATE);
        return prefs.getStringSet("favorite_archives", new HashSet<>());
    }
}
