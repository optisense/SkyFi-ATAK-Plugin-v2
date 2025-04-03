package com.skyfi.atak.plugin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.dropdown.DropDown;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.MapView;
import com.skyfi.atak.plugin.skyfiapi.MyProfile;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile extends DropDownReceiver implements DropDown.OnStateListener, OrdersRecyclerViewAdapter.ItemClickListener {
    private final static String LOGTAG = "SkyFiProfile";
    public final static String ACTION = "com.skyfi.profile";
    private View mainView;
    private TextView name;
    private TextView email;
    private TextView totalBudget;
    private TextView budgetUsage;
    private TextView remainingBudget;
    private TextView ccOnFile;
    private Context context;

    protected Profile(MapView mapView, Context context) {
        super(mapView);

        this.context = context;
        mainView = PluginLayoutInflater.inflate(context, R.layout.my_profile, null);
        name = mainView.findViewById(R.id.name);
        email = mainView.findViewById(R.id.email);
        totalBudget = mainView.findViewById(R.id.total_budget);
        budgetUsage = mainView.findViewById(R.id.budget_usage);
        remainingBudget = mainView.findViewById(R.id.budget_remaining);
        ccOnFile = mainView.findViewById(R.id.cc_on_file);;
    }

    private void getProfile() {
        new APIClient().getApiClient().getProfile().enqueue(new Callback<MyProfile>() {
            @Override
            public void onResponse(@NonNull Call<MyProfile> call, @NonNull Response<MyProfile> response) {
                if (response.isSuccessful()) {
                    MyProfile profile = response.body();
                    if (profile != null) {
                        name.setText(String.format(context.getString(R.string.name), profile.getFirstName(), profile.getLastName()));
                        email.setText(String.format(context.getString(R.string.email), profile.getEmail()));
                        totalBudget.setText(String.format(context.getString(R.string.total_budget), profile.getBudgetAmount()));
                        budgetUsage.setText(String.format(context.getString(R.string.budget_usage), profile.getCurrentBudgetUsage()));
                        remainingBudget.setText(String.format(context.getString(R.string.budget_remaining), profile.getBudgetRemaining()));
                        if (profile.getHasValidSharedCard())
                            ccOnFile.setText(String.format(context.getString(R.string.cc_on_file), context.getString(R.string.yes)));
                        else
                            ccOnFile.setText(String.format(context.getString(R.string.cc_on_file), context.getString(R.string.no)));
                    }
                } else {
                    new AlertDialog.Builder(MapView.getMapView().getContext())
                            .setTitle(context.getString(R.string.error))
                            .setMessage(context.getString(R.string.profile_error))
                            .setPositiveButton(context.getString(R.string.ok), null)
                            .create()
                            .show();
                    try {
                        Log.e(LOGTAG, response.errorBody().string() + " " + response.code());
                    } catch (Exception e) {
                        Log.e(LOGTAG, "no error message " + response.code());
                    }

                    name.setText(String.format(context.getString(R.string.name), "?", ""));
                    email.setText(String.format(context.getString(R.string.email), "?"));
                    totalBudget.setText(String.format(context.getString(R.string.total_budget), 0f));
                    budgetUsage.setText(String.format(context.getString(R.string.budget_usage), 0f));
                    remainingBudget.setText(String.format(context.getString(R.string.budget_remaining), 0f));
                    ccOnFile.setText(String.format(context.getString(R.string.cc_on_file), context.getString(R.string.no)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyProfile> call, @NonNull Throwable throwable) {
                Log.e(LOGTAG, "Error retrieving profile", throwable);

                new AlertDialog.Builder(MapView.getMapView().getContext())
                        .setTitle(context.getString(R.string.error))
                        .setMessage(throwable.getMessage())
                        .setPositiveButton(context.getString(R.string.ok), null)
                        .create()
                        .show();

                name.setText(String.format(context.getString(R.string.name), "?", ""));
                email.setText(String.format(context.getString(R.string.email), "?"));
                totalBudget.setText(String.format(context.getString(R.string.total_budget), 0f));
                budgetUsage.setText(String.format(context.getString(R.string.budget_usage), 0f));
                remainingBudget.setText(String.format(context.getString(R.string.budget_remaining), 0f));
                ccOnFile.setText(String.format(context.getString(R.string.cc_on_file), context.getString(R.string.no)));
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
        Log.d(LOGTAG, "Profile onreceive");
        if (intent.getAction() != null && intent.getAction().equals(ACTION)) {
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                showDropDown(mainView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH, FULL_HEIGHT, false);
            } else {
                showDropDown(mainView, FULL_WIDTH, HALF_HEIGHT, FULL_WIDTH, HALF_HEIGHT, false);
            }

            getProfile();
        }
    }

    @Override
    public void onItemClick(View view, int position) {

    }
}
