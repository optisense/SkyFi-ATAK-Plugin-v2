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
import com.skyfi.atak.plugin.skyfiapi.FeasibilityInfo;
import com.skyfi.atak.plugin.SatelliteFeasibilityCalculator;

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
    private final Button changeAOIButton;
    private final CheckBox prioritize;
    private final CheckBox assuredTasking;
    private final TextView priorityPrice;
    private final TextView assuredTaskingPrice;
    private final TextView totalPrice;
    
    // Feasibility UI elements
    private final TextView feasibilityPasses;
    private final TextView feasibilityLevel;
    private final TextView feasibilityExplanation;

    // Sensor Types
    private final RadioGroup sensorTypes;
    private final RadioButton asapSensor;
    private final RadioButton eoSensor;
    private final RadioButton sarSensor;
    private final RadioButton adsBSensor;

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
        
        assuredTasking = mainView.findViewById(R.id.assured_tasking);
        assuredTasking.setOnClickListener(this);

        priorityPrice = mainView.findViewById(R.id.priority_price);

        assuredTaskingPrice = mainView.findViewById(R.id.assured_tasking_price);

        totalPrice = mainView.findViewById(R.id.total_price);
        
        // Feasibility UI elements
        feasibilityPasses = mainView.findViewById(R.id.feasibility_passes);
        feasibilityLevel = mainView.findViewById(R.id.feasibility_level);
        feasibilityExplanation = mainView.findViewById(R.id.feasibility_explanation);

        // Sensor Types
        sensorTypes = mainView.findViewById(R.id.sensor_types);

        asapSensor = mainView.findViewById(R.id.sensor_type_asap);
        asapSensor.setOnClickListener(this);

        eoSensor = mainView.findViewById(R.id.sensor_type_eo);
        eoSensor.setOnClickListener(this);

        sarSensor = mainView.findViewById(R.id.sensor_type_sar);
        sarSensor.setOnClickListener(this);

        adsBSensor = mainView.findViewById(R.id.sensor_type_ads_b);
        adsBSensor.setOnClickListener(this);

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

        changeAOIButton = mainView.findViewById(R.id.change_aoi_button);
        changeAOIButton.setOnClickListener(this);

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
                updateFeasibility();
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
                updateFeasibility();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }

        // Sensor Types
        else if (view.getId() == R.id.sensor_type_asap) {
            if (asapSensor.isChecked()) {
                taskingOrder.setSensorType(context.getString(R.string.asap));
                // ASAP typically maps to high-speed electro-optical (EO) imagery
                setProductTypeBasedOnSensor("ASAP");
                updateFeasibility();
            }
        }
        else if (view.getId() == R.id.sensor_type_eo) {
            if (eoSensor.isChecked()) {
                taskingOrder.setSensorType(context.getString(R.string.eo));
                // EO sensors typically provide DAY/NIGHT optical imagery
                setProductTypeBasedOnSensor("EO");
                updateFeasibility();
            }
        }
        else if (view.getId() == R.id.sensor_type_sar) {
            if (sarSensor.isChecked()) {
                taskingOrder.setSensorType(context.getString(R.string.sar));
                // SAR sensors provide synthetic aperture radar imagery
                setProductTypeBasedOnSensor("SAR");
                updateFeasibility();
            }
        }
        else if (view.getId() == R.id.sensor_type_ads_b) {
            if (adsBSensor.isChecked()) {
                taskingOrder.setSensorType(context.getString(R.string.ads_b));
                // ADS-B is typically for tracking aircraft/vessels
                setProductTypeBasedOnSensor("ADS-B");
                updateFeasibility();
            }
        }

        // Product Types
        else if (view.getId() == R.id.product_type_day) {
            if (day.isChecked()) {
                taskingOrder.setProductType(context.getString(R.string.day));
                // Update sensor type selection to match (prefer EO for DAY)
                sensorTypes.clearCheck();
                eoSensor.setChecked(true);
                taskingOrder.setSensorType(context.getString(R.string.eo));
                
                high.setEnabled(true);
                veryHigh.setEnabled(true);
                superHigh.setEnabled(true);
                ultraHigh.setEnabled(false);

                resetProviders();
                resolutions.clearCheck();
                updateAssuredTaskingAvailability();
            }
        }
        else if (view.getId() == R.id.product_type_sar) {
            if (sar.isChecked()) {
                taskingOrder.setProductType(context.getString(R.string.sar));
                // Update sensor type selection to match
                sensorTypes.clearCheck();
                sarSensor.setChecked(true);
                taskingOrder.setSensorType(context.getString(R.string.sar));
                
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
                updateAssuredTaskingAvailability();
            }
        }
        else if (view.getId() == R.id.product_type_stereo) {
            if (stereo.isChecked()) {
                taskingOrder.setProductType(context.getString(R.string.stereo));
                // Update sensor type selection to match (prefer EO for STEREO)
                sensorTypes.clearCheck();
                eoSensor.setChecked(true);
                taskingOrder.setSensorType(context.getString(R.string.eo));
                
                high.setEnabled(false);
                veryHigh.setEnabled(true);
                superHigh.setEnabled(true);
                ultraHigh.setEnabled(false);

                resolutions.clearCheck();
                resetProviders();
                updateAssuredTaskingAvailability();
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
            updateAssuredTaskingAvailability();
        }
        else if (view.getId() == R.id.provider_satellogic) {
            if (satellogic.isChecked())
                taskingOrder.setRequiredProvider(context.getString(R.string.satellogic));
            updateAssuredTaskingAvailability();
        }
        else if (view.getId() == R.id.provider_umbra) {
            if (umbra.isChecked())
                taskingOrder.setRequiredProvider(context.getString(R.string.umbra));
            updateAssuredTaskingAvailability();
        }
        else if (view.getId() == R.id.provider_geosat) {
            if (geosat.isChecked())
                taskingOrder.setRequiredProvider(context.getString(R.string.geosat));
            updateAssuredTaskingAvailability();
        }
        else if (view.getId() == R.id.provider_planet) {
            if (planet.isChecked())
                taskingOrder.setRequiredProvider(context.getString(R.string.planet));
            updateAssuredTaskingAvailability();
        }
        else if (view.getId() == R.id.provider_impro) {
            if (impro.isChecked())
                taskingOrder.setRequiredProvider(context.getString(R.string.impro));
            updateAssuredTaskingAvailability();
        }

        else if (view.getId() == R.id.priority) {
            taskingOrder.setPriorityItem(prioritize.isChecked());
            updateTotalPrice();
        }
        
        else if (view.getId() == R.id.assured_tasking) {
            taskingOrder.setAssuredTasking(assuredTasking.isChecked());
            Log.d(LOGTAG, "Assured tasking: " + assuredTasking.isChecked());
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
        else if (view.getId() == R.id.change_aoi_button) {
            showCoordinateInputDialog();
        }

        updateTotalPrice();
    }

    private void setProductTypeBasedOnSensor(String sensorType) {
        // Clear current product type selection
        productTypes.clearCheck();
        
        // Reset and enable all product types initially
        day.setEnabled(true);
        sar.setEnabled(true);
        stereo.setEnabled(true);
        
        switch (sensorType) {
            case "ASAP":
                // ASAP sensors typically provide high-priority optical imagery
                day.setChecked(true);
                taskingOrder.setProductType(context.getString(R.string.day));
                // Disable non-applicable types for ASAP
                sar.setEnabled(false);
                stereo.setEnabled(false);
                break;
            case "EO":
                // EO (Electro-Optical) sensors can provide various optical products
                day.setChecked(true);
                taskingOrder.setProductType(context.getString(R.string.day));
                // Keep all optical options available
                sar.setEnabled(false);
                break;
            case "SAR":
                // SAR sensors provide radar imagery
                sar.setChecked(true);
                taskingOrder.setProductType(context.getString(R.string.sar));
                // Disable optical types for SAR
                day.setEnabled(false);
                stereo.setEnabled(false);
                break;
            case "ADS-B":
                // ADS-B is for tracking, might use various sensor types
                // Default to DAY for now, but keep options open
                day.setChecked(true);
                taskingOrder.setProductType(context.getString(R.string.day));
                break;
        }
        
        // Reset resolution and provider selections since they depend on product type
        resolutions.clearCheck();
        resetProviders();
        updateAssuredTaskingAvailability();
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

    private void updateAssuredTaskingAvailability() {
        // Determine if assured tasking is available based on product type and provider
        String productType = taskingOrder.getProductType();
        String provider = taskingOrder.getRequiredProvider();
        
        boolean isAssuredTaskingAvailable = true;
        
        // Space Force requirements: Assured tasking typically not available for:
        // - SAR imagery (requires special handling)
        // - Certain providers that don't support guaranteed delivery
        if (productType != null) {
            if (productType.equals(context.getString(R.string.sar))) {
                // SAR typically requires special assured tasking handling
                isAssuredTaskingAvailable = false;
            }
        }
        
        // Provider-specific limitations
        if (provider != null) {
            if (provider.equals(context.getString(R.string.satellogic)) || 
                provider.equals(context.getString(R.string.geosat))) {
                // Some providers may not support assured tasking
                isAssuredTaskingAvailable = false;
            }
        }
        
        assuredTasking.setEnabled(isAssuredTaskingAvailable);
        
        if (!isAssuredTaskingAvailable) {
            assuredTasking.setChecked(false);
            taskingOrder.setAssuredTasking(false);
            Log.d(LOGTAG, "Assured tasking disabled for product type: " + productType + ", provider: " + provider);
        } else {
            Log.d(LOGTAG, "Assured tasking available for product type: " + productType + ", provider: " + provider);
        }
        
        updateTotalPrice();
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

                            // Handle assured tasking pricing
                            if (pricing.getAssuredTaskingEnabled() != null && !pricing.getAssuredTaskingEnabled()) {
                                assuredTasking.setChecked(false);
                                assuredTasking.setEnabled(false);
                            } else {
                                assuredTasking.setEnabled(true);
                                if (assuredTasking.isChecked() && pricing.getAssuredTaskingPriceOneSqkm() != null) {
                                    total += area * pricing.getAssuredTaskingPriceOneSqkm();
                                }
                            }

                            totalPrice.setText(String.format(context.getString(R.string.total_price), total));

                            priorityPrice.setText(String.format(context.getString(R.string.priority_extra_cost), area * pricing.getPriorityTaskingPriceOneSqkm()));
                            
                            // Show assured tasking price if available
                            if (pricing.getAssuredTaskingPriceOneSqkm() != null) {
                                assuredTaskingPrice.setText(String.format(context.getString(R.string.assured_tasking_extra_cost), area * pricing.getAssuredTaskingPriceOneSqkm()));
                            } else {
                                assuredTaskingPrice.setText(String.format(context.getString(R.string.assured_tasking_extra_cost), 0f));
                            }
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
    
    /**
     * Update feasibility information based on current sensor type, dates, and location
     */
    private void updateFeasibility() {
        try {
            // Only calculate if we have required information
            if (taskingOrder.getSensorType() == null || 
                taskingOrder.getWindowStart() == null || 
                taskingOrder.getWindowEnd() == null ||
                taskingOrder.getAoi() == null) {
                resetFeasibilityDisplay();
                return;
            }
            
            // Extract coordinates from AOI (center point)
            double[] coordinates = extractCenterFromAOI(taskingOrder.getAoi());
            if (coordinates == null) {
                resetFeasibilityDisplay();
                return;
            }
            
            // Calculate feasibility
            FeasibilityInfo feasibility = SatelliteFeasibilityCalculator.calculateFeasibility(
                taskingOrder.getSensorType(),
                coordinates[0], // latitude
                coordinates[1], // longitude
                taskingOrder.getWindowStart(),
                taskingOrder.getWindowEnd()
            );
            
            // Update UI
            updateFeasibilityDisplay(feasibility);
            
        } catch (Exception e) {
            Log.e(LOGTAG, "Error updating feasibility", e);
            resetFeasibilityDisplay();
        }
    }
    
    /**
     * Extract center coordinates from WKT AOI string
     */
    private double[] extractCenterFromAOI(String aoi) {
        try {
            WKTReader reader = new WKTReader();
            Geometry geometry = reader.read(aoi);
            Coordinate centroid = geometry.getCentroid().getCoordinate();
            return new double[]{centroid.y, centroid.x}; // lat, lon
        } catch (Exception e) {
            Log.e(LOGTAG, "Error extracting center from AOI", e);
            return null;
        }
    }
    
    /**
     * Update feasibility display with calculated information
     */
    private void updateFeasibilityDisplay(FeasibilityInfo feasibility) {
        if (feasibility == null) {
            resetFeasibilityDisplay();
            return;
        }
        
        // Update expected passes
        feasibilityPasses.setText(String.format(
            context.getString(R.string.expected_passes), 
            feasibility.getExpectedPasses()
        ));
        
        // Update feasibility level with color
        FeasibilityInfo.FeasibilityLevel level = feasibility.getFeasibilityLevel();
        feasibilityLevel.setText(level.getDisplayName());
        feasibilityLevel.setBackgroundColor(level.getColor());
        feasibilityLevel.setTextColor(android.graphics.Color.WHITE);
        feasibilityLevel.setVisibility(View.VISIBLE);
        
        // Update explanation
        if (feasibility.getExplanation() != null && !feasibility.getExplanation().isEmpty()) {
            feasibilityExplanation.setText(feasibility.getExplanation());
            feasibilityExplanation.setVisibility(View.VISIBLE);
        } else {
            feasibilityExplanation.setVisibility(View.GONE);
        }
        
        Log.d(LOGTAG, "Updated feasibility: " + feasibility.toString());
    }
    
    /**
     * Reset feasibility display to default state
     */
    private void resetFeasibilityDisplay() {
        feasibilityPasses.setText(context.getString(R.string.feasibility_unknown));
        feasibilityLevel.setText("");
        feasibilityLevel.setVisibility(View.GONE);
        feasibilityExplanation.setVisibility(View.GONE);
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
        taskingOrder.setSensorType(null);
        from.setText("");
        to.setText("");
        maxCloudCoverage.setText("");
        maxOffNadirAngle.setText("");
        asapSensor.setChecked(false);
        eoSensor.setChecked(false);
        sarSensor.setChecked(false);
        adsBSensor.setChecked(false);
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
        assuredTasking.setChecked(false);
        assuredTasking.setEnabled(true);

        sensorTypes.clearCheck();
        productTypes.clearCheck();
        resolutions.clearCheck();
        providers.clearCheck();
        
        // Reset feasibility display
        resetFeasibilityDisplay();
    }

    private void showCoordinateInputDialog() {
        String[] inputMethods = {
            context.getString(R.string.latitude) + "/" + context.getString(R.string.longitude) + " / MGRS",
            context.getString(R.string.pindrop_tasking)
        };
        
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle("Select Coordinate Input Method")
                .setItems(inputMethods, (dialog, which) -> {
                    switch (which) {
                        case 0: // Lat/Lon/MGRS unified dialog
                            showUnifiedCoordinateInputDialog();
                            break;
                        case 1: // Pindrop
                            startPindropTasking();
                            break;
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show();
    }
    
    private void showUnifiedCoordinateInputDialog() {
        CoordinateInputDialog.show(MapView.getMapView().getContext(), new CoordinateInputDialog.CoordinateSelectedListener() {
            @Override
            public void onCoordinateSelected(com.atakmap.coremap.maps.coords.GeoPoint point, String displayName) {
                // Create AOI around the selected coordinates and update the tasking order
                updateAOIFromCoordinates(point.getLatitude(), point.getLongitude());
            }
            
            @Override
            public void onCancelled() {
                // User cancelled, nothing to do
            }
        });
    }
    
    private void startPindropTasking() {
        new AlertDialog.Builder(MapView.getMapView().getContext())
                .setTitle("Pin Drop Tasking")
                .setMessage("Tap on the map to select a location for tasking")
                .setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
                    enablePindropMode();
                })
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show();
    }
    
    private void enablePindropMode() {
        // Add a temporary map click listener for pindrop functionality
        com.atakmap.android.maps.MapEventDispatcher.MapEventDispatchListener pindropListener = 
            new com.atakmap.android.maps.MapEventDispatcher.MapEventDispatchListener() {
            @Override
            public void onMapEvent(com.atakmap.android.maps.MapEvent event) {
                if (com.atakmap.android.maps.MapEvent.MAP_CLICK.equals(event.getType())) {
                    com.atakmap.coremap.maps.coords.GeoPoint point = 
                        mapView.inverse(event.getPointF().x, event.getPointF().y).get();
                    
                    // Remove this listener after first click
                    mapView.getMapEventDispatcher().removeMapEventListener(
                        com.atakmap.android.maps.MapEvent.MAP_CLICK, this);
                    
                    updateAOIFromCoordinates(point.getLatitude(), point.getLongitude());
                }
            }
        };
        
        mapView.getMapEventDispatcher().addMapEventListener(
            com.atakmap.android.maps.MapEvent.MAP_CLICK, pindropListener);
    }
    
    private void updateAOIFromCoordinates(double lat, double lon) {
        // Create minimum AOI around the coordinates
        com.atakmap.coremap.maps.coords.GeoPoint centerPoint = 
            new com.atakmap.coremap.maps.coords.GeoPoint(lat, lon);
        java.util.List<com.atakmap.coremap.maps.coords.GeoPoint> aoiPoints = 
            AOIManager.createMinimumAOIAroundPoint(centerPoint, "default");
        
        // Convert to WKT
        String aoi = convertPointsToWKT(aoiPoints);
        
        if (aoi != null) {
            taskingOrder.setAoi(aoi);
            area = calculateAreaFromWKT(aoi);
            
            // Refresh pricing if all required fields are set
            if (allPricing != null) {
                updateTotalPrice();
            }
            
            // Update feasibility with new location
            updateFeasibility();
            
            // Show confirmation
            new AlertDialog.Builder(MapView.getMapView().getContext())
                    .setTitle("Location Updated")
                    .setMessage(String.format("New location: %.6f, %.6f\nArea: %.2f sq km", 
                        lat, lon, area))
                    .setPositiveButton(context.getString(R.string.ok), null)
                    .show();
        }
    }
    
    private String convertPointsToWKT(java.util.List<com.atakmap.coremap.maps.coords.GeoPoint> points) {
        if (points == null || points.size() < 3) return null;
        
        try {
            java.util.ArrayList<Coordinate> coordinates = new java.util.ArrayList<>();
            for (com.atakmap.coremap.maps.coords.GeoPoint point : points) {
                coordinates.add(new Coordinate(point.getLongitude(), point.getLatitude()));
            }
            // Close the polygon
            coordinates.add(new Coordinate(points.get(0).getLongitude(), points.get(0).getLatitude()));
            
            org.locationtech.jts.geom.GeometryFactory factory = 
                new org.locationtech.jts.geom.GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(10000000.0));
            org.locationtech.jts.geom.Polygon polygon = 
                factory.createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));
            org.locationtech.jts.io.WKTWriter wktWriter = new org.locationtech.jts.io.WKTWriter();
            return wktWriter.write(polygon);
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to convert points to WKT", e);
            return null;
        }
    }
    
    private double calculateAreaFromWKT(String wkt) {
        try {
            org.locationtech.jts.io.WKTReader reader = new org.locationtech.jts.io.WKTReader();
            org.locationtech.jts.geom.Geometry geometry = reader.read(wkt);
            return calculatePolygonArea(geometry.getCoordinates());
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to calculate area from WKT", e);
            return 1.0; // Default fallback
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) return;

        if (intent.getAction().equals(ACTION)) {
            resetForm();
            
            // Set default task priority
            taskingOrder.setTaskPriority("WHEN_AVAILABLE");

            String aoi = intent.getStringExtra("aoi");
            area = intent.getDoubleExtra("area", 0);
            Log.d(LOGTAG, "Got area " + area);
            taskingOrder.setAoi(aoi);

            if (aoi == null)
                Log.e(LOGTAG, "aoi null");

            priorityPrice.setText(String.format(this.context.getString(R.string.priority_extra_cost), 0f));
            assuredTaskingPrice.setText(String.format(this.context.getString(R.string.assured_tasking_extra_cost), 0f));
            totalPrice.setText(String.format(this.context.getString(R.string.total_price), 0f));
            
            // Update feasibility for the new AOI
            updateFeasibility();

            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                showDropDown(mainView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH, FULL_HEIGHT, false);
            } else {
                showDropDown(mainView, FULL_WIDTH, HALF_HEIGHT, FULL_WIDTH, HALF_HEIGHT, false);
            }
        }
    }
}
