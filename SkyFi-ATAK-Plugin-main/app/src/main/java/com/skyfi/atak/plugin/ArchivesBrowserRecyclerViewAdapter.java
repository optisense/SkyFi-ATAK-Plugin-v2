package com.skyfi.atak.plugin;

import android.content.Context;
import com.atakmap.coremap.log.Log;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.skyfi.atak.plugin.skyfiapi.Archive;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ArchivesBrowserRecyclerViewAdapter extends RecyclerView.Adapter<ArchivesBrowserRecyclerViewAdapter.ViewHolder> {

    private static final String LOGTAG = "ArchivesBrowserRecycler";
    private ArrayList<Archive> mData;
    private LayoutInflater mInflater;
    private ArchivesBrowserRecyclerViewAdapter.ItemClickListener mClickListener;
    private int selectedPosition = -1;
    private Context context;

    ArchivesBrowserRecyclerViewAdapter(Context context, ArrayList<Archive> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.archive_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Archive archive = mData.get(position);
        Log.d(LOGTAG, "adapter: " + archive.toString());
        try {
            /*if (position == selectedPosition) {
                holder.linearLayout.setBackgroundColor(Color.BLUE);
            } else {
                holder.linearLayout.setBackgroundColor(context.getColor(R.color.darker_gray));
            }*/

            Handler handler = new Handler(Looper.getMainLooper());
            Thread thread = new Thread(() -> {
                try {
                    Map.Entry<String, String> entry = archive.getThumbnailUrls().entrySet().iterator().next();
                    String key = entry.getKey();
                    String value = entry.getValue();
                    URL thumbnailUrl = new URL(value);
                    URLConnection connection = thumbnailUrl.openConnection();
                    Bitmap image = BitmapFactory.decodeStream(connection.getInputStream());
                    handler.post(() -> {
                        holder.thumbnail.setImageBitmap(image);
                        holder.thumbnail.setAlpha(1f);
                    });

                } catch (Exception e) {
                    Log.e(LOGTAG, "Failed to set thumbnail", e);
                }
            });

            thread.start();


            holder.productType.setText(archive.getProductType());
            holder.resolution.setText(archive.getResolution());
            holder.captureTimestamp.setText(archive.getCaptureTimestamp().toString());
            holder.provider.setText(archive.getProvider());
            holder.cloudCoverage.setText(String.format(context.getString(R.string.cloud_coverage), archive.getCloudCoveragePercent()));
            holder.pricePerSqkm.setText(String.format(context.getString(R.string.price_per_sqkm), archive.getPriceForOneSquareKm()));
        } catch (Exception e) {
            Log.d(LOGTAG, "Failed", e);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView thumbnail;
        TextView productType;
        TextView resolution;
        TextView captureTimestamp;
        TextView provider;
        TextView cloudCoverage;
        TextView pricePerSqkm;
        RecyclerView recyclerView;

        ViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            productType = itemView.findViewById(R.id.product_type);
            cloudCoverage = itemView.findViewById(R.id.cloud_coverage);
            captureTimestamp = itemView.findViewById(R.id.capture_timestamp);
            resolution = itemView.findViewById(R.id.resolution);
            provider = itemView.findViewById(R.id.provider);
            pricePerSqkm = itemView.findViewById(R.id.price_per_sqkm);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
                selectedPosition = getAdapterPosition();
            }
        }
    }

    // convenience method for getting data at click position
    Archive getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ArchivesBrowserRecyclerViewAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
