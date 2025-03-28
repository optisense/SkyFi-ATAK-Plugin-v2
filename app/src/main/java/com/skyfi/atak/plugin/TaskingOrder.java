package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.atakmap.android.dropdown.DropDown;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.MapView;
import com.skyfi.atak.plugin.skyfiapi.SkyFiAPI;

public class TaskingOrder extends DropDownReceiver implements DropDown.OnStateListener, View.OnClickListener {
    private static final String LOGTAG = "TaskingOrder";
    public final static String ACTION = "com.skyfi.tasking_order";

    private SkyFiAPI apiClient;
    private View mainView;

    MapView mapView;
    Context context;

    protected TaskingOrder(MapView mapView) {
        super(mapView);
    }

    @Override
    public void onClick(View view) {

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

    }
}
