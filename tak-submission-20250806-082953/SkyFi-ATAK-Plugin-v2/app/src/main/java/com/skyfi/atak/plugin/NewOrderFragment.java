package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import com.atakmap.coremap.log.Log;
import android.view.View;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.dropdown.DropDown;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;

import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NewOrderFragment extends DropDownReceiver implements DropDown.OnStateListener, MainRecyclerViewAdapter.ItemClickListener {
    private final static String LOGTAG = "NewOrderFragment";
    public final static String ACTION = "com.skyfi.new_order";

    private View mainView;
    private final RecyclerView recyclerView;
    private String aoi;
    private MainRecyclerViewAdapter mainRecyclerViewAdapter;

    protected NewOrderFragment(MapView mapView, Context context, String aoi) {
        super(mapView);

        this.aoi = aoi;

        ArrayList<String> options = new ArrayList<>();
        options.add(context.getString(R.string.tasking_order));
        options.add(context.getString(R.string.archive_order));

        mainView = PluginLayoutInflater.inflate(context, R.layout.main_layout, null);
        recyclerView = mainView.findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        mainRecyclerViewAdapter = new MainRecyclerViewAdapter(context, options);
        mainRecyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(mainRecyclerViewAdapter);
    }

    private void searchArchives() {

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

            aoi = intent.getStringExtra("aoi");
            if (aoi == null)
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
    public void onItemClick(View view, int position) {
        Intent intent = new Intent();
        switch (position) {
            case 0:
                intent.setAction(TaskingOrderFragment.ACTION);
                intent.putExtra("aoi", aoi);
                AtakBroadcast.getInstance().sendBroadcast(intent);
                break;

            case 1:
                intent.setAction(ArchiveSearch.ACTION);
                intent.putExtra("aoi", aoi);
                AtakBroadcast.getInstance().sendBroadcast(intent);
                break;
        }
    }
}
