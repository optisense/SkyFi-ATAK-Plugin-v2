package com.skyfi.atak.plugin;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.dropdown.DropDown;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.MapView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ArchiveOrderFragment extends DropDownReceiver implements DropDown.OnStateListener, ArchiveRecyclerViewAdapter.ItemClickListener {
    private final static String LOGTAG = "ArchiveOrderFragment";

    private MapView mapView;
    private Context context;
    private String aoi;
    private View mainView;
    //private final RecyclerView recyclerView;
    private ArchiveRecyclerViewAdapter archiveRecyclerViewAdapter;

    protected ArchiveOrderFragment(MapView mapView, Context context, String aoi) {
        super(mapView);

        this.mapView = mapView;
        this.context = context;
        this.aoi = aoi;

        mainView = PluginLayoutInflater.inflate(context, R.layout.archive_order, null);
        /*recyclerView = mainView.findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        archiveRecyclerViewAdapter = new MainRecyclerViewAdapter(context, options);
        archiveRecyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(archiveRecyclerViewAdapter);*/
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

    @Override
    public void onItemClick(View view, int position) {

    }
}
