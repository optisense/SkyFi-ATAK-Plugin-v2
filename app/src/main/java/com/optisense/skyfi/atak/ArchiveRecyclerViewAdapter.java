package com.optisense.skyfi.atak;
import com.skyfi.atak.plugin.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.optisense.skyfi.atak.skyfiapi.ArchiveResponse;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ArchiveRecyclerViewAdapter extends RecyclerView.Adapter<ArchiveRecyclerViewAdapter.ViewHolder> {

    private final static String LOGTAG = "ArchiveRecyclerView";
    private ArchiveResponse mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private int selectedPosition = -1;
    private Context context;

    ArchiveRecyclerViewAdapter(Context context, ArchiveResponse data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;

    }

    @NonNull
    @Override
    public ArchiveRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.archive_row, parent, false);
        return new ArchiveRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArchiveRecyclerViewAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView orderName;
        TextView aoiSqkm;
        TextView cloudCoverage;
        TextView cost;
        TextView resolution;
        TextView status;
        TextView provider;
        LinearLayout linearLayout;
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

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    void setClickListener(ArchiveRecyclerViewAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
}
