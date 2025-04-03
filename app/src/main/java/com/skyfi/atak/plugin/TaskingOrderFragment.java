package com.skyfi.atak.plugin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.icu.util.Calendar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.dropdown.DropDown;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;
import com.skyfi.atak.plugin.skyfiapi.Order;
import com.skyfi.atak.plugin.skyfiapi.PricingQuery;
import com.skyfi.atak.plugin.skyfiapi.PricingResponse;
import com.skyfi.atak.plugin.skyfiapi.SkyFiAPI;
import com.skyfi.atak.plugin.skyfiapi.TaskingOrder;

import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskingOrderFragment extends DropDownReceiver implements DropDown.OnStateListener, View.OnClickListener {
    private static final String LOGTAG = "TaskingOrder";
    public final static String ACTION = "com.skyfi.tasking_order";

    private SkyFiAPI apiClient;
    private View mainView;

    MapView mapView;
    Context context;
    PricingResponse allPricing;
    double area;

    private final EditText from;
    private final EditText to;
    private final EditText maxCloudCoverage;
    private final EditText maxOffNadirAngle;
    private final Button placeOrderButton;
    private final Button resetFormButton;
    private final CheckBox prioritize;
    private final TextView priorityPrice;
    private final TextView totalPrice;

    // Resolutions
    private final RadioGroup resolutions;
    private final RadioButton high;
    private final RadioButton veryHigh;
    private final RadioButton superHigh;
    private final RadioButton ultraHigh;

    // Product Types
    private final RadioGroup productTypes;
    private final RadioButton day;
    private final RadioButton sar;
    private final RadioButton stereo;

    // Providers
    private final RadioGroup providers;
    private final RadioButton siwei;
    private final RadioButton satellogic;
    private final RadioButton umbra;
    private final RadioButton geosat;
    private final RadioButton planet;
    private final RadioButton impro;

    private TaskingOrder taskingOrder = new TaskingOrder();

    protected TaskingOrderFragment(MapView mapView, Context context) {
        super(mapView);

        this.mapView = mapView;
        this.context = context;

        mainView = PluginLayoutInflater.inflate(context, R.layout.tasking_order, null);

        from = mainView.findViewById(R.id.from);
        from.setOnClickListener(this);

        to = mainView.findViewById(R.id.to);
        to.setOnClickListener(this);

        prioritize = mainView.findViewById(R.id.priority);
        prioritize.setOnClickListener(this);

        priorityPrice = mainView.findViewById(R.id.priority_price);

        totalPrice = mainView.findViewById(R.id.total_price);


        // Resolutions
        resolutions = mainView.findViewById(R.id.resolutions);

        high = mainView.findViewById(R.id.resolution_high);
        high.setOnClickListener(this);

        veryHigh = mainView.findViewById(R.id.resolution_very_high);
        veryHigh.setOnClickListener(this);

        superHigh = mainView.findViewById(R.id.resolution_super_high);
        superHigh.setOnClickListener(this);

        ultraHigh = mainView.findViewById(R.id.resolution_ultra_high);
        ultraHigh.setOnClickListener(this);

        // Product Types
        productTypes = mainView.findViewById(R.id.product_types);

        day = mainView.findViewById(R.id.product_type_day);
        day.setOnClickListener(this);

        sar = mainView.findViewById(R.id.product_type_sar);
        sar.setOnClickListener(this);

        stereo = mainView.findViewById(R.id.product_type_stereo);
        stereo.setOnClickListener(this);

        // Providers
        providers = mainView.findViewById(R.id.providers);

        siwei = mainView.findViewById(R.id.provider_siwei);
        siwei.setOnClickListener(this);

        satellogic = mainView.findViewById(R.id.provider_satellogic);
        satellogic.setOnClickListener(this);

        umbra = mainView.findViewById(R.id.provider_umbra);
        umbra.setOnClickListener(this);

        geosat = mainView.findViewById(R.id.provider_geosat);
        geosat.setOnClickListener(this);

        planet = mainView.findViewById(R.id.provider_planet);
        planet.setOnClickListener(this);

        impro = mainView.findViewById(R.id.provider_impro);
        impro.setOnClickListener(this);

        placeOrderButton = mainView.findViewById(R.id.place_order_button);
        placeOrderButton.setOnClickListener(this);

        resetFormButton = mainView.findViewById(R.id.reset_button);
        resetFormButton.setOnClickListener(this);

        maxCloudCoverage = mainView.findViewById(R.id.max_cloud_coverage);
        maxCloudCoverage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                try {
                    if (charSequence != null && !charSequence.toString().isEmpty()) {
                        float value = Float.parseFloat(charSequence.toString());
                        taskingOrder.setMaxCloudCoveragePercent(value);
                    }
                } catch (Exception e) {
                    showError(context.getString(R.string.error), context.getString(R.string.max_cloud_coverage_error));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        maxOffNadirAngle = mainView.findViewById(R.id.max_off_nadir);
        maxOffNadirAngle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                try {
                    if (charSequence != null && !charSequence.toString().isEmpty()) {
                        float value = Float.parseFloat(charSequence.toString());
                        taskingOrder.setMaxOffNadirAngle(value);
                    }
                } catch (Exception e) {
                    showError(context.getString(R.string.max_off_nadir_error), e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.from) {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(MapView.getMapView().getContext(), (datePicker, year, month, day) -> {
                String monthString;
                String dayString;

                month++;
                if (month < 10)
                    monthString = "0" + month;
                else
                    monthString = String.valueOf(month);

                if (day < 10)
                    dayString = "0" + day;
                else
                    dayString = String.valueOf(day);

                from.setText(year + "-" + monthString + "-" + dayString);
                taskingOrder.setWindowStart(year + "-" + monthString + "-" + dayString + "T00:00:00+00:00");
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
        else if (view.getId() == R.id.to) {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(MapView.getMapView().getContext(), (datePicker, year, month, day) -> {
                String monthString;
                String dayString;

                month++;
                if (month < 10)
                    monthString = "0" + month;
                else
                    monthString = String.valueOf(month);

                if (day < 10)
                    dayString = "0" + day;
                else
                    dayString = String.valueOf(day);

                to.setText(year + "-" + monthString + "-" + dayString);
                taskingOrder.setWindowEnd(year + "-" + monthString + "-" + dayString + "T00:00:00+00:00");
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }

        // Product Types
        else if (view.getId() == R.id.product_type_day) {
            if (day.isChecked()) {
                taskingOrder.setProductType(context.getString(R.string.day));
                high.setEnabled(true);
                veryHigh.setEnabled(true);
                superHigh.setEnabled(true);
                ultraHigh.setEnabled(false);

                resetProviders();
                resolutions.clearCheck();
            }
        }
        else if (view.getId() == R.id.product_type_sar) {
            if (sar.isChecked()) {
                taskingOrder.setProductType(context.getString(R.string.sar));
                high.setEnabled(true);
                veryHigh.setEnabled(true);
                superHigh.setEnabled(true);
                ultraHigh.setEnabled(true);

                umbra.setEnabled(true);
                satellogic.setEnabled(false);
                geosat.setEnabled(false);
                siwei.setEnabled(false);
                impro.setEnabled(false);
                planet.setEnabled(false);
            }
        }
        else if (view.getId() == R.id.product_type_stereo) {
            if (stereo.isChecked()) {
                taskingOrder.setProductType(context.getString(R.string.stereo));
                high.setEnabled(false);
                veryHigh.setEnabled(true);
                superHigh.setEnabled(true);
                ultraHigh.setEnabled(false);

                resolutions.clearCheck();
                resetProviders();
            }
        }

        // Resolutions
        else if (view.getId() == R.id.resolution_high) {
            Log.d(LOGTAG, "high " + high.isChecked());

            if (high.isChecked()) {
                taskingOrder.setResolution(context.getString(R.string.high));

                if (day.isChecked()) {
                    umbra.setEnabled(false);
                    satellogic.setEnabled(true);
                    geosat.setEnabled(true);
                    siwei.setEnabled(false);
                    impro.setEnabled(false);
                    planet.setEnabled(false);
                }
                else if (sar.isChecked()) {
                    umbra.setEnabled(true);
                    satellogic.setEnabled(false);
                    geosat.setEnabled(false);
                    siwei.setEnabled(false);
                    impro.setEnabled(false);
                    planet.setEnabled(false);
                }
                else if (stereo.isChecked()) {
                    umbra.setEnabled(false);
                    satellogic.setEnabled(false);
                    geosat.setEnabled(false);
                    siwei.setEnabled(false);
                    impro.setEnabled(false);
                    planet.setEnabled(false);
                }
            }
        }
        else if (view.getId() == R.id.resolution_very_high) {
            if (veryHigh.isChecked()) {
                taskingOrder.setResolution(context.getString(R.string.very_high));

                if (day.isChecked()) {
                    umbra.setEnabled(false);
                    satellogic.setEnabled(false);
                    geosat.setEnabled(false);
                    siwei.setEnabled(true);
                    impro.setEnabled(true);
                    planet.setEnabled(true);
                }
                else if (sar.isChecked()) {
                    umbra.setEnabled(true);
                    satellogic.setEnabled(false);
                    geosat.setEnabled(false);
                    siwei.setEnabled(false);
                    impro.setEnabled(false);
                    planet.setEnabled(false);
                }
                else if (stereo.isChecked()) {
                    umbra.setEnabled(false);
                    satellogic.setEnabled(false);
                    geosat.setEnabled(false);
                    siwei.setEnabled(true);
                    impro.setEnabled(true);
                    planet.setEnabled(false);
                }
            }
        }
        else if (view.getId() == R.id.resolution_super_high) {
            if (superHigh.isChecked()) {
                taskingOrder.setResolution(context.getString(R.string.super_high));

                if (sar.isChecked()) {
                    umbra.setEnabled(true);
                    satellogic.setEnabled(false);
                    geosat.setEnabled(false);
                    siwei.setEnabled(false);
                    impro.setEnabled(false);
                    planet.setEnabled(false);
                }
                else if (stereo.isChecked()) {
                    umbra.setEnabled(false);
                    satellogic.setEnabled(false);
                    geosat.setEnabled(false);
                    siwei.setEnabled(true);
                    impro.setEnabled(false);
                    planet.setEnabled(false);
                }
                else if (day.isChecked()) {
                    umbra.setEnabled(false);
                    satellogic.setEnabled(false);
                    geosat.setEnabled(false);
                    siwei.setEnabled(true);
                    impro.setEnabled(false);
                    planet.setEnabled(false);
                }
            }
        }
        else if (view.getId() == R.id.resolution_ultra_high) {
            if (ultraHigh.isChecked()) {
                taskingOrder.setResolution(context.getString(R.string.ultra_high));

                umbra.setEnabled(true);
                satellogic.setEnabled(false);
                geosat.setEnabled(false);
                siwei.setEnabled(false);
                impro.setEnabled(false);
                planet.setEnabled(false);
            }
        }

        // Providers
        else if (view.getId() == R.id.provider_siwei) {
            if (siwei.isChecked())
                taskingOrder.setRequiredProvider(context.getString(R.string.siwei));
        }
        else if (view.getId() == R.id.provider_satellogic) {
            if (satellogic.isChecked())
                taskingOrder.setRequiredProvider(context.getString(R.string.satellogic));
        }
        else if (view.getId() == R.id.provider_umbra) {
            if (umbra.isChecked())
                taskingOrder.setRequiredProvider(context.getString(R.string.umbra));
        }
        else if (view.getId() == R.id.provider_geosat) {
            if (geosat.isChecked())
                taskingOrder.setRequiredProvider(context.getString(R.string.geosat));
        }
        else if (view.getId() == R.id.provider_planet) {
            if (planet.isChecked())
                taskingOrder.setRequiredProvider(context.getString(R.string.planet));
        }
        else if (view.getId() == R.id.provider_impro) {
            if (impro.isChecked())
                taskingOrder.setRequiredProvider(context.getString(R.string.impro));
        }

        else if (view.getId() == R.id.priority) {
            taskingOrder.setPriorityItem(prioritize.isChecked());
            updateTotalPrice();
        }

        else if (view.getId() == R.id.place_order_button) {
            Log.d(LOGTAG, taskingOrder.toString());
            new AlertDialog.Builder(MapView.getMapView().getContext())
                    .setTitle(context.getString(R.string.place_order))
                    .setMessage(context.getString(R.string.are_you_sure))
                    .setPositiveButton(context.getString(R.string.ok), (dialogInterface, i) -> placeOrder())
                    .setNegativeButton(context.getString(R.string.cancel), null)
                    .create()
                    .show();
        }
        else if (view.getId() == R.id.reset_button) {
            resetForm();
        }

        updateTotalPrice();
    }

    private void resetProviders() {
        providers.clearCheck();
        umbra.setEnabled(true);
        satellogic.setEnabled(true);
        geosat.setEnabled(true);
        siwei.setEnabled(true);
        impro.setEnabled(true);
        planet.setEnabled(true);
    }

    private void placeOrder() {
        if (taskingOrder.getMaxCloudCoveragePercent() != null && (taskingOrder.getMaxCloudCoveragePercent() < 0 || taskingOrder.getMaxCloudCoveragePercent() > 100)) {
            showError(context.getString(R.string.order_error), context.getString(R.string.max_cloud_coverage_error));
            return;
        }

        if (taskingOrder.getMaxOffNadirAngle() != null && (taskingOrder.getMaxOffNadirAngle() < 0 || taskingOrder.getMaxOffNadirAngle() > 50)) {
            showError(context.getString(R.string.order_error), context.getString(R.string.max_off_nadir_error));
            return;
        }

        try {
            if (taskingOrder.getWindowStart() == null || taskingOrder.getWindowStart().isEmpty()) {
                showError(context.getString(R.string.order_error), context.getString(R.string.window_start_error));
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00+00:00");
            LocalDate windowsStartDate = LocalDate.parse(taskingOrder.getWindowStart(), formatter);
            Calendar windowStart = Calendar.getInstance();
            windowStart.set(windowsStartDate.getYear(), windowsStartDate.getMonthValue(), windowsStartDate.getDayOfMonth());

            Calendar nowPlus36Hours = Calendar.getInstance();
            nowPlus36Hours.add(Calendar.HOUR, 36);

            if (windowStart.before(nowPlus36Hours)) {
                showError(context.getString(R.string.order_error), context.getString(R.string.window_start_error));
                return;
            }

            if (taskingOrder.getWindowEnd() == null || taskingOrder.getWindowEnd().isEmpty()) {
                showError(context.getString(R.string.order_error), context.getString(R.string.window_end_error));
                return;
            }

            LocalDate windowsEndDate = LocalDate.parse(taskingOrder.getWindowEnd(), formatter);
            Calendar windowEnd = Calendar.getInstance();
            windowEnd.set(windowsEndDate.getYear(), windowsEndDate.getMonthValue(), windowsEndDate.getDayOfMonth());

            if (windowEnd.before(windowStart)) {
                showError(context.getString(R.string.order_error), context.getString(R.string.window_end_error));
                return;
            }
        } catch (Exception e) {
            Log.d(LOGTAG, "Invalid window start: " + taskingOrder.getWindowStart(), e);
            showError(context.getString(R.string.order_error), context.getString(R.string.window_start_error));
            return;
        }

        apiClient = new APIClient().getApiClient();
        apiClient.taskingOrder(taskingOrder).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {
                if (response.isSuccessful()) {
                    showError(context.getString(R.string.tasking_order), context.getString(R.string.order_placed));
                }
                else {
                    int responseCode = response.code();
                    Log.e(LOGTAG, "Tasking order failed: " + responseCode);
                    try {
                        String errorString = response.errorBody().string();
                        Log.d(LOGTAG, "Order error: " + errorString);
                        JSONObject errorJson = new JSONObject(errorString);
                        String message = errorJson.getJSONArray("detail").getJSONObject(0).getString("msg");
                        Log.d(LOGTAG, call.request().body().toString());
                        Log.e(LOGTAG, message);
                        showError(context.getString(R.string.order_error), message);
                    } catch (Exception e) {
                        Log.e(LOGTAG, "Failed to fail", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Order> call, @NonNull Throwable throwable) {
                Log.e(LOGTAG, "Failed to place tasking order", throwable);
                showError(context.getString(R.string.tasking_order), throwable.getMessage());
            }
        });
    }

    private void getPricing() {
        apiClient = new APIClient().getApiClient();
        apiClient.getTaskingPricing(new PricingQuery(taskingOrder.getAoi())).enqueue(new Callback<PricingResponse>() {
            @Override
            public void onResponse(@NonNull Call<PricingResponse> call, @NonNull Response<PricingResponse> response) {
                if (response.isSuccessful()) {
                    allPricing = response.body();
                    Log.d(LOGTAG, "Got Pricing: " + allPricing);
                }
                else {
                    Log.e(LOGTAG, "Failed to get pricing");
                    try {
                        String errorString = response.errorBody().string();
                        Log.w(LOGTAG, "error is " + errorString);
                        JSONObject errorJson = new JSONObject(errorString);
                        String message = errorJson.getJSONArray("detail").getJSONObject(0).getString("msg");
                        Log.d(LOGTAG, call.request().body().toString());
                        Log.e(LOGTAG, message);
                        showError(context.getString(R.string.get_pricing_failed), message);
                    } catch (Exception e) {
                        Log.e(LOGTAG, "Failed to fail", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PricingResponse> call, @NonNull Throwable throwable) {
                Log.e(LOGTAG, "Failed to get pricing", throwable);
                showError(context.getString(R.string.get_pricing_failed), throwable.getMessage());
            }
        });
    }

    private void showError(String title, String message) {
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.ok), null)
                .create()
                .show();
    }

    private void updateTotalPrice() {
        if (taskingOrder.getProductType() == null || taskingOrder.getResolution() == null || taskingOrder.getAoi() == null) {
            return;
        }

        for (PricingResponse.ProductType productType : allPricing.getProductTypes()) {

            if (taskingOrder.getProductType().equals(productType.getProductType())) {

                for (PricingResponse.ProductType.Resolution resolution : productType.getResolutions()) {

                    if (resolution.getResolution().equals(taskingOrder.getResolution())) {
                        try {
                            PricingResponse.ProductType.Pricing pricing = resolution.getPricing();

                            WKTReader reader = new WKTReader();
                            Geometry geometry = reader.read(taskingOrder.getAoi());
                            double area = calculatePolygonArea(geometry.getCoordinates());

                            double total = area * pricing.getTaskingPriceOneSqkm();
                            if (!pricing.getPriorityEnabled()) {
                                prioritize.setChecked(false);
                                pricing.setPriorityEnabled(false);
                            }
                            else if (prioritize.isChecked()) {
                                total += area * pricing.getPriorityTaskingPriceOneSqkm();
                            }

                            totalPrice.setText(String.format(context.getString(R.string.total_price), total));

                            priorityPrice.setText(String.format(context.getString(R.string.priority_extra_cost), area * pricing.getPriorityTaskingPriceOneSqkm()));
                            break;
                        }
                        catch (Exception e) {
                            Log.e(LOGTAG, "Failed to calculate price", e);
                        }
                    }
                    else {
                        Log.d(LOGTAG, resolution.getResolution() + " != " + taskingOrder.getResolution());
                    }
                }
                break;
            }
        }
    }

    public static double calculatePolygonArea(Coordinate[] coordinates) {
        double area = 0;

        if (coordinates.length > 2) {
            for (int i = 0; i < coordinates.length-1; i++) {
                Coordinate p1, p2;
                p1 = coordinates[i];
                p2 = coordinates[i + 1];
                area += Math.toRadians(p2.x - p1.x) * (2 + Math.sin(Math.toRadians(p1.y)) + Math.sin(Math.toRadians(p2.y)));
            }

            int radiusOfEarthKM = 6371;
            area = area * radiusOfEarthKM * radiusOfEarthKM / 2;
        }

        return Math.abs(area);
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

    private void resetForm() {
        getPricing();
        taskingOrder = new TaskingOrder();
        from.setText("");
        to.setText("");
        maxCloudCoverage.setText("");
        maxOffNadirAngle.setText("");
        day.setChecked(false);
        sar.setChecked(false);
        stereo.setChecked(false);
        high.setChecked(false);
        high.setEnabled(true);
        veryHigh.setChecked(false);
        veryHigh.setEnabled(true);
        superHigh.setChecked(false);
        superHigh.setEnabled(true);
        ultraHigh.setChecked(false);
        ultraHigh.setEnabled(true);
        siwei.setChecked(false);
        siwei.setEnabled(true);
        satellogic.setChecked(false);
        satellogic.setEnabled(true);
        umbra.setChecked(false);
        umbra.setEnabled(true);
        geosat.setChecked(false);
        geosat.setEnabled(true);
        planet.setChecked(false);
        planet.setEnabled(true);
        impro.setChecked(false);
        impro.setEnabled(true);
        prioritize.setChecked(false);

        productTypes.clearCheck();
        resolutions.clearCheck();
        providers.clearCheck();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) return;

        if (intent.getAction().equals(ACTION)) {
            resetForm();

            String aoi = intent.getStringExtra("aoi");
            area = intent.getDoubleExtra("area", 0);
            Log.d(LOGTAG, "Got area " + area);
            taskingOrder.setAoi(aoi);

            if (aoi == null)
                Log.e(LOGTAG, "aoi null");

            priorityPrice.setText(String.format(this.context.getString(R.string.priority_extra_cost), 0f));
            totalPrice.setText(String.format(this.context.getString(R.string.total_price), 0f));

            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                showDropDown(mainView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH, FULL_HEIGHT, false);
            } else {
                showDropDown(mainView, FULL_WIDTH, HALF_HEIGHT, FULL_WIDTH, HALF_HEIGHT, false);
            }
        }
    }
}
