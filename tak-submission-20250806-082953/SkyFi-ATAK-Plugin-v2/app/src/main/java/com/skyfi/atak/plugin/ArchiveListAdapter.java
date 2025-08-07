package com.skyfi.atak.plugin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skyfi.atak.plugin.skyfiapi.ArchiveResponse;

public class ArchiveListAdapter extends BaseAdapter {

    private final static String LOGTAG = "ArchiveListAdapter";
    private ArchiveResponse mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private int selectedPosition = -1;
    private Context context;

    ArchiveListAdapter(Context context, ArchiveResponse data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        // TODO: Return actual count based on mData when properly implemented
        return 0;
    }

    @Override
    public Object getItem(int position) {
        // TODO: Return actual item from mData when properly implemented
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.archive_row, parent, false);
            holder = new ViewHolder();
            holder.orderName = convertView.findViewById(R.id.order_name);
            holder.aoiSqkm = convertView.findViewById(R.id.aoi_sqkm);
            holder.cloudCoverage = convertView.findViewById(R.id.cloud_coverage);
            holder.cost = convertView.findViewById(R.id.cost);
            holder.resolution = convertView.findViewById(R.id.resolution);
            holder.status = convertView.findViewById(R.id.status);
            holder.provider = convertView.findViewById(R.id.provider);
            holder.linearLayout = convertView.findViewById(R.id.linear_layout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        // TODO: Bind data to views when properly implemented
        
        // Set click listener
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onItemClick(v, position);
                    selectedPosition = position;
                    notifyDataSetChanged();
                }
            }
        });
        
        // Handle selection state
        if (position == selectedPosition) {
            // TODO: Apply selected state styling
        } else {
            // TODO: Apply normal state styling
        }
        
        return convertView;
    }

    // ViewHolder pattern for better performance
    static class ViewHolder {
        TextView orderName;
        TextView aoiSqkm;
        TextView cloudCoverage;
        TextView cost;
        TextView resolution;
        TextView status;
        TextView provider;
        LinearLayout linearLayout;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
}