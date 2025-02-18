package com.skyfi.atak.plugin;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.skyfi.atak.plugin.skyfiapi.Order;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OrdersRecyclerViewAdapter extends RecyclerView.Adapter<OrdersRecyclerViewAdapter.ViewHolder> {

    private static final String LOGTAG = "SkyFiOrdersRecycler";
    private List<Order> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    OrdersRecyclerViewAdapter(Context context, List<Order> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.order_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Order order = mData.get(position);
        try {
            holder.orderName.setText(order.getCreatedAt().toString());
            holder.aoiSqkm.setText(String.valueOf(order.getAoiSqkm()));
            holder.cost.setText(String.format("$%s", order.getOrderCost()));
            if (order.getArchive() != null) {
                holder.cloudCoverage.setText(String.format("%s%%", order.getArchive().getCloudCoveragePercent()));
                holder.resolution.setText(order.getArchive().getResolution());
            }
            else {
                holder.cloudCoverage.setText("?");
                holder.resolution.setText("?");
            }
        } catch (Exception e) {
            Log.d(LOGTAG, "Failed", e);
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView orderName;
        TextView aoiSqkm;
        TextView cloudCoverage;
        TextView cost;
        TextView resolution;

        ViewHolder(View itemView) {
            super(itemView);
            orderName = itemView.findViewById(R.id.order_name);
            aoiSqkm = itemView.findViewById(R.id.aoi_sqkm);
            cloudCoverage = itemView.findViewById(R.id.cloud_coverage);
            cost = itemView.findViewById(R.id.cost);
            resolution = itemView.findViewById(R.id.resolution);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    Order getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}