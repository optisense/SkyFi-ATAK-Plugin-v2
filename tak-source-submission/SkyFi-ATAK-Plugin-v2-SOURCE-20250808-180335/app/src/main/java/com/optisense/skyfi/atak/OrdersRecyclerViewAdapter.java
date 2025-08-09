package com.optisense.skyfi.atak;
import com.skyfi.atak.plugin.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.optisense.skyfi.atak.skyfiapi.Order;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OrdersRecyclerViewAdapter extends RecyclerView.Adapter<OrdersRecyclerViewAdapter.ViewHolder> {

    private static final String LOGTAG = "SkyFiOrdersRecycler";
    private List<Order> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private OpacityChangeListener mOpacityChangeListener;
    private int selectedPosition = -1;
    private Context context;
    private Preferences preferences;

    // data is passed into the constructor
    OrdersRecyclerViewAdapter(Context context, List<Order> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.preferences = new Preferences();
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
            if (position == selectedPosition) {
                holder.linearLayout.setBackgroundColor(Color.BLUE);
            } else {
                holder.linearLayout.setBackgroundColor(context.getColor(R.color.darker_gray));
            }

            holder.orderName.setText(order.getOrderName());
            holder.aoiSqkm.setText(String.valueOf(order.getAoiSqkm()));
            holder.cost.setText(String.format("$%.2f", order.getOrderCost() / 100.0));

            holder.status.setText(order.getStatus());
            if (order.getStatus().equals("PROCESSING_COMPLETE"))
                holder.status.setTextColor(Color.GREEN);
            else
                holder.status.setTextColor(Color.YELLOW);

            if (order.getArchive() != null) {
                double cloudCoveragePercent = Math.round(order.getArchive().getCloudCoveragePercent());
                holder.cloudCoverage.setText(String.format("%s%%", cloudCoveragePercent));
                holder.resolution.setText(order.getArchive().getResolution());
                holder.provider.setText(order.getArchive().getProvider());
            }
            else {
                holder.cloudCoverage.setText(String.format("%s%%", order.getMaxCloudCoveragePercent()));
                holder.resolution.setText(order.getResolution());
                holder.provider.setText(order.getRequiredProvider());
            }
            
            // Show opacity control for SAR imagery or completed orders with tiles
            boolean isSarImagery = isSarImagery(order);
            boolean hasActiveLayer = order.getTilesUrl() != null && order.getStatus().equals("PROCESSING_COMPLETE");
            
            if (isSarImagery && hasActiveLayer) {
                holder.opacityControlSection.setVisibility(View.VISIBLE);
                setupOpacityControls(holder, order);
            } else {
                holder.opacityControlSection.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.d(LOGTAG, "Failed", e);
        }
    }
    
    private boolean isSarImagery(Order order) {
        // Check if this is SAR imagery based on order properties
        return order.getSarProductTypes() != null && order.getSarProductTypes().length > 0;
    }
    
    private void setupOpacityControls(ViewHolder holder, Order order) {
        String layerName = "SkyFi " + order.getOrderName();
        int currentOpacity = preferences.getLayerOpacity(layerName);
        
        holder.opacitySeekBar.setProgress(currentOpacity);
        holder.opacityValue.setText(currentOpacity + "%");
        
        holder.opacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    holder.opacityValue.setText(progress + "%");
                    preferences.setLayerOpacity(layerName, progress);
                    
                    if (mOpacityChangeListener != null) {
                        mOpacityChangeListener.onOpacityChanged(layerName, progress);
                    }
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        holder.opacityAdvancedButton.setOnClickListener(v -> {
            if (mOpacityChangeListener != null) {
                mOpacityChangeListener.onShowAdvancedOpacityDialog(layerName, currentOpacity);
            }
        });
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
        TextView status;
        TextView provider;
        LinearLayout linearLayout;
        LinearLayout opacityControlSection;
        SeekBar opacitySeekBar;
        TextView opacityValue;
        Button opacityAdvancedButton;
        RecyclerView recyclerView;

        ViewHolder(View itemView) {
            super(itemView);
            orderName = itemView.findViewById(R.id.order_name);
            aoiSqkm = itemView.findViewById(R.id.aoi_sqkm);
            cloudCoverage = itemView.findViewById(R.id.cloud_coverage);
            cost = itemView.findViewById(R.id.cost);
            resolution = itemView.findViewById(R.id.resolution);
            status = itemView.findViewById(R.id.status);
            provider = itemView.findViewById(R.id.provider);
            linearLayout = itemView.findViewById(R.id.linear_layout);
            opacityControlSection = itemView.findViewById(R.id.opacity_control_section);
            opacitySeekBar = itemView.findViewById(R.id.opacity_seekbar);
            opacityValue = itemView.findViewById(R.id.opacity_value);
            opacityAdvancedButton = itemView.findViewById(R.id.opacity_advanced_button);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
                selectedPosition = getAdapterPosition();
                notifyDataSetChanged();
            }

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
    
    // allows opacity change events to be caught
    void setOpacityChangeListener(OpacityChangeListener opacityChangeListener) {
        this.mOpacityChangeListener = opacityChangeListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
    
    // parent activity will implement this method to respond to opacity changes
    public interface OpacityChangeListener {
        void onOpacityChanged(String layerName, int opacity);
        void onShowAdvancedOpacityDialog(String layerName, int currentOpacity);
    }
}