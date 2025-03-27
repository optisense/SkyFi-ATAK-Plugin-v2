package com.skyfi.atak.plugin;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

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

public class ArchiveSearch extends DropDownReceiver implements DropDown.OnStateListener, View.OnClickListener {
    private static final String LOGTAG = "ArchiveSearch";
    public final static String ACTION = "com.skyfi.search_archives";

    private SkyFiAPI apiClient;
    private View mainView;

    ArchivesRequest request = new ArchivesRequest();
    MapView mapView;
    Context context;

    private final EditText from;
    private final EditText to;
    private final EditText maxCloudCoverage;
    private final EditText maxOffNadirAngle;

    // Resolutions
    private final CheckBox low;
    private final CheckBox medium;
    private final CheckBox high;
    private final CheckBox veryHigh;
    private final CheckBox superHigh;
    private final CheckBox ultraHigh;

    // Product Types
    private final CheckBox day;
    private final CheckBox night;
    private final CheckBox video;
    private final CheckBox sar;
    private final CheckBox hyperspectral;
    private final CheckBox multispectral;
    private final CheckBox stereo;

    // Providers
    private final CheckBox siwei;
    private final CheckBox satellogic;
    private final CheckBox umbra;
    private final CheckBox tailwind;
    private final CheckBox geosat;
    private final CheckBox sentinel2;
    private final CheckBox sentinel2_creodias;
    private final CheckBox planet;
    private final CheckBox impro;
    private final CheckBox urban_sky;
    private final CheckBox nsl;
    private final CheckBox vexcel;

    private final Switch openData;
    private final EditText minOverlapRatio;

    private final Button searchButton;

    protected ArchiveSearch(MapView mapView, Context context, String aoi) {
        super(mapView);

        this.mapView = mapView;
        this.context = context;
        request.setAoi(aoi);

        mainView = PluginLayoutInflater.inflate(context, R.layout.archive_search, null);

        from = mainView.findViewById(R.id.from);
        from.setOnClickListener(this);
        to = mainView.findViewById(R.id.to);
        to.setOnClickListener(this);

        low = mainView.findViewById(R.id.resolution_low);
        low.setOnClickListener(this);

        medium = mainView.findViewById(R.id.resolution_medium);
        medium.setOnClickListener(this);

        high = mainView.findViewById(R.id.resolution_high);
        high.setOnClickListener(this);

        veryHigh = mainView.findViewById(R.id.resolution_very_high);
        veryHigh.setOnClickListener(this);

        superHigh = mainView.findViewById(R.id.resolution_super_high);
        superHigh.setOnClickListener(this);

        ultraHigh = mainView.findViewById(R.id.resolution_ultra_high);
        ultraHigh.setOnClickListener(this);

        day = mainView.findViewById(R.id.product_type_day);
        day.setOnClickListener(this);

        night = mainView.findViewById(R.id.product_type_night);
        night.setOnClickListener(this);

        video = mainView.findViewById(R.id.product_type_video);
        video.setOnClickListener(this);

        sar = mainView.findViewById(R.id.product_type_sar);
        sar.setOnClickListener(this);

        hyperspectral = mainView.findViewById(R.id.product_type_hyperspectral);
        hyperspectral.setOnClickListener(this);

        multispectral = mainView.findViewById(R.id.product_type_multispectral);
        multispectral.setOnClickListener(this);

        stereo = mainView.findViewById(R.id.product_type_stereo);
        stereo.setOnClickListener(this);

        siwei = mainView.findViewById(R.id.provider_siwei);
        siwei.setOnClickListener(this);

        satellogic = mainView.findViewById(R.id.provider_satellogic);
        satellogic.setOnClickListener(this);

        umbra = mainView.findViewById(R.id.provider_umbra);
        umbra.setOnClickListener(this);

        tailwind = mainView.findViewById(R.id.provider_tailwind);
        tailwind.setOnClickListener(this);

        geosat = mainView.findViewById(R.id.provider_geosat);
        geosat.setOnClickListener(this);

        sentinel2 = mainView.findViewById(R.id.provider_sentinel2);
        sentinel2.setOnClickListener(this);

        sentinel2_creodias = mainView.findViewById(R.id.provider_sentinel2_creodias);
        sentinel2_creodias.setOnClickListener(this);

        planet = mainView.findViewById(R.id.provider_planet);
        planet.setOnClickListener(this);

        impro = mainView.findViewById(R.id.provider_impro);
        impro.setOnClickListener(this);

        urban_sky = mainView.findViewById(R.id.provider_urban_sky);
        urban_sky.setOnClickListener(this);

        nsl = mainView.findViewById(R.id.provider_nsl);
        nsl.setOnClickListener(this);

        vexcel = mainView.findViewById(R.id.provider_vexcel);
        vexcel.setOnClickListener(this);

        openData = mainView.findViewById(R.id.open_data);
        openData.setOnClickListener(this);

        searchButton = mainView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(this);

        maxCloudCoverage = mainView.findViewById(R.id.max_cloud_coverage);
        maxCloudCoverage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                try {
                    float value = Float.parseFloat(charSequence.toString());
                    if (value >= 0 && value <= 100)
                        request.setMaxCloudCoveragePercent(value);
                    else
                        Toast.makeText(context, R.string.max_cloud_coverage_error, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(context, R.string.max_cloud_coverage_error, Toast.LENGTH_LONG).show();
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
                    float value = Float.parseFloat(charSequence.toString());
                    if (value >= 0 && value <= 50)
                        request.setMaxOffNadirAngle(value);
                    else
                        Toast.makeText(context, R.string.max_off_nadir_error, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(context, R.string.max_off_nadir_error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        minOverlapRatio = mainView.findViewById(R.id.min_overlap_ratio);
        minOverlapRatio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                try {
                    float value = Float.parseFloat(charSequence.toString());
                    if (value >= 0 && value <= 1)
                        request.setMinOverlapRatio(value);
                    else
                        Toast.makeText(context, R.string.min_overlap_ratio_error, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(context, R.string.min_overlap_ratio_error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void searchArchives() {
        apiClient = new APIClient().getApiClient();
        apiClient.searchArchives(request).enqueue(new Callback<ArchiveResponse>() {
            @Override
            public void onResponse(@NonNull Call<ArchiveResponse> call, @NonNull Response<ArchiveResponse> response) {
                if (response.body() != null) {
                    try {
                        Log.d(LOGTAG, response.body().toString());
                    } catch (Exception e) {
                        Log.e(LOGTAG, "Failed to search archives", e);
                    }
                }
                else {
                    Log.e(LOGTAG, "Archive search response is null: " + response.code() + " - " + response.message());
                    try {
                        Log.d(LOGTAG, call.request().body().toString());
                        Log.e(LOGTAG, response.errorBody().string());
                    } catch (Exception e) {
                        Log.e(LOGTAG, "Failed to fail", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArchiveResponse> call, @NonNull Throwable throwable) {
                Log.e(LOGTAG, "Failed to search archives", throwable);
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
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) return;

        if (intent.getAction().equals(ACTION)) {

            for (String extra : intent.getExtras().keySet()) {
                Log.d(LOGTAG, "Extra: " + extra);
            }
            String aoi = intent.getStringExtra("aoi");
            request.setAoi(aoi);
            Log.d(LOGTAG, "Got AOI: " + aoi);

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
                request.setFromDate(year + "-" + monthString + "-" + dayString + "T00:00:00+00:00");
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
                request.setToDate(year + "-" + monthString + "-" + dayString + "T00:00:00+00:00");
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
        else if (view.getId() == R.id.resolution_low) {
            if (low.isChecked())
                request.addResolution(context.getString(R.string.low));
            else
                request.removeResolution(context.getString(R.string.low));
        }
        else if (view.getId() == R.id.resolution_medium) {
            if (medium.isChecked())
                request.addResolution(context.getString(R.string.medium));
            else
                request.removeResolution(context.getString(R.string.medium));
        }
        else if (view.getId() == R.id.resolution_high) {
            if (high.isChecked())
                request.addResolution(context.getString(R.string.high));
            else
                request.removeResolution(context.getString(R.string.high));
        }
        else if (view.getId() == R.id.resolution_very_high) {
            if (veryHigh.isChecked())
                request.addResolution(context.getString(R.string.very_high));
            else
                request.removeResolution(context.getString(R.string.very_high));
        }
        else if (view.getId() == R.id.resolution_super_high) {
            if (superHigh.isChecked())
                request.addResolution(context.getString(R.string.super_high));
            else
                request.removeResolution(context.getString(R.string.super_high));
        }
        else if (view.getId() == R.id.resolution_ultra_high) {
            if (ultraHigh.isChecked())
                request.addResolution(context.getString(R.string.ultra_high));
            else
                request.removeResolution(context.getString(R.string.ultra_high));
        }
        else if (view.getId() == R.id.product_type_day) {
            if (day.isChecked())
                request.addProductType(context.getString(R.string.day));
            else
                request.removeProductType(context.getString(R.string.day));
        }
        else if (view.getId() == R.id.product_type_night) {
            if (night.isChecked())
                request.addProductType(context.getString(R.string.night));
            else
                request.removeProductType(context.getString(R.string.night));
        }
        else if (view.getId() == R.id.product_type_video) {
            if (video.isChecked())
                request.addProductType(context.getString(R.string.video));
            else
                request.removeProductType(context.getString(R.string.video));
        }
        else if (view.getId() == R.id.product_type_sar) {
            if (sar.isChecked())
                request.addProductType(context.getString(R.string.sar));
            else
                request.removeProductType(context.getString(R.string.sar));
        }
        else if (view.getId() == R.id.product_type_hyperspectral) {
            if (hyperspectral.isChecked())
                request.addProductType(context.getString(R.string.hyperspectral));
            else
                request.removeProductType(context.getString(R.string.hyperspectral));
        }
        else if (view.getId() == R.id.product_type_multispectral) {
            if (multispectral.isChecked())
                request.addProductType(context.getString(R.string.multispectral));
            else
                request.removeProductType(context.getString(R.string.multispectral));
        }
        else if (view.getId() == R.id.product_type_stereo) {
            if (stereo.isChecked())
                request.addProductType(context.getString(R.string.stereo));
            else
                request.removeProductType(  context.getString(R.string.stereo));
        }
        else if (view.getId() == R.id.provider_siwei) {
            if (siwei.isChecked())
                request.addProvider(context.getString(R.string.siwei));
            else
                request.removeProvider(context.getString(R.string.siwei));
        }
        else if (view.getId() == R.id.provider_satellogic) {
            if (satellogic.isChecked())
                request.addProvider(context.getString(R.string.satellogic));
            else
                request.removeProvider(context.getString(R.string.satellogic));
        }
        else if (view.getId() == R.id.provider_umbra) {
            if (umbra.isChecked())
                request.addProvider(context.getString(R.string.umbra));
            else
                request.removeProvider(context.getString(R.string.umbra));
        }
        else if (view.getId() == R.id.provider_tailwind) {
            if (tailwind.isChecked())
                request.addProvider(context.getString(R.string.tailwind));
            else
                request.removeProvider(context.getString(R.string.tailwind));
        }
        else if (view.getId() == R.id.provider_geosat) {
            if (geosat.isChecked())
                request.addProvider(context.getString(R.string.geosat));
            else
                request.removeProvider(context.getString(R.string.geosat));
        }
        else if (view.getId() == R.id.provider_sentinel2) {
            if (sentinel2.isChecked())
                request.addProvider(context.getString(R.string.sentinel2));
            else
                request.removeProvider(context.getString(R.string.sentinel2));
        }
        else if (view.getId() == R.id.provider_sentinel2_creodias) {
            if (sentinel2_creodias.isChecked())
                request.addProvider(context.getString(R.string.sentinel2_creodias));
            else
                request.removeProvider(context.getString(R.string.sentinel2_creodias));
        }
        else if (view.getId() == R.id.provider_planet) {
            if (planet.isChecked())
                request.addProvider(context.getString(R.string.planet));
            else
                request.removeProvider(context.getString(R.string.planet));
        }
        else if (view.getId() == R.id.provider_impro) {
            if (impro.isChecked())
                request.addProvider(context.getString(R.string.impro));
            else
                request.removeProvider(context.getString(R.string.impro));
        }
        else if (view.getId() == R.id.provider_urban_sky) {
            if (urban_sky.isChecked())
                request.addProvider(context.getString(R.string.urban_sky));
            else
                request.removeProvider(context.getString(R.string.urban_sky));
        }
        else if (view.getId() == R.id.provider_nsl) {
            if (nsl.isChecked())
                request.addProvider(context.getString(R.string.nsl));
            else
                request.removeProvider(context.getString(R.string.nsl));
        }
        else if (view.getId() == R.id.provider_vexcel   ) {
            if (vexcel.isChecked())
                request.addProvider(context.getString(R.string.vexcel));
            else
                request.removeProvider(context.getString(R.string.vexcel));
        }
        else if (view.getId() == R.id.open_data) {
            request.setOpenData(openData.isChecked());
        }
        else if (view.getId() == R.id.search_button) {
            searchArchives();
        }
    }
}
