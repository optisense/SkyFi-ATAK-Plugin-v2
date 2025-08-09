package com.optisense.skyfi.atak;
import com.skyfi.atak.plugin.R;

import android.content.Context;
import com.atakmap.coremap.log.Log;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.optisense.skyfi.atak.skyfiapi.Archive;

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
    private ImagePreferencesManager imagePrefsManager;
    private ImageCacheManager imageCacheManager;

    ArchivesBrowserRecyclerViewAdapter(Context context, ArrayList<Archive> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.imagePrefsManager = ImagePreferencesManager.getInstance(context);
        this.imageCacheManager = ImageCacheManager.getInstance(context);
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
            holder.pricePerSqkm.setText(String.format(context.getString(R.string.price_per_sqkm), archive.getPriceForOneSquareKm() / 100.0));
            
            // Update button states based on preferences and cache status
            updateButtonStates(holder, archive.getArchiveId());
            updateCacheButtonState(holder, archive);
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
        Button cacheButton;
        Button archiveButton;
        Button favoriteButton;
        LinearLayout progressLayout;
        ProgressBar cacheProgress;
        TextView cacheProgressText;
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
            cacheButton = itemView.findViewById(R.id.cache_button);
            archiveButton = itemView.findViewById(R.id.archive_button);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
            progressLayout = itemView.findViewById(R.id.progress_layout);
            cacheProgress = itemView.findViewById(R.id.cache_progress);
            cacheProgressText = itemView.findViewById(R.id.cache_progress_text);
            
            itemView.setOnClickListener(this);
            cacheButton.setOnClickListener(this);
            archiveButton.setOnClickListener(this);
            favoriteButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;
            
            Archive archive = mData.get(position);
            
            if (view.getId() == R.id.cache_button) {
                handleCacheButtonClick(this, archive);
            } else if (view.getId() == R.id.archive_button) {
                imagePrefsManager.toggleArchived(archive.getArchiveId());
                updateButtonStates(this, archive.getArchiveId());
            } else if (view.getId() == R.id.favorite_button) {
                imagePrefsManager.toggleFavorite(archive.getArchiveId());
                updateButtonStates(this, archive.getArchiveId());
            } else {
                // Regular item click
                if (mClickListener != null) {
                    mClickListener.onItemClick(view, position);
                    selectedPosition = position;
                }
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
    
    private void updateButtonStates(ViewHolder holder, String archiveId) {
        boolean isArchived = imagePrefsManager.isArchived(archiveId);
        boolean isFavorite = imagePrefsManager.isFavorite(archiveId);
        
        holder.archiveButton.setText(isArchived ? context.getString(R.string.unarchive) : context.getString(R.string.archive_image));
        holder.favoriteButton.setText(isFavorite ? context.getString(R.string.unfavorite) : context.getString(R.string.favorite_image));
        
        // Change button appearance based on state
        holder.archiveButton.setAlpha(isArchived ? 0.7f : 1.0f);
        holder.favoriteButton.setAlpha(isFavorite ? 0.7f : 1.0f);
    }
    
    private void updateCacheButtonState(ViewHolder holder, Archive archive) {
        // Check if image is already cached
        Map.Entry<String, String> entry = archive.getThumbnailUrls().entrySet().iterator().next();
        String imageUrl = entry.getValue();
        boolean isCached = imageCacheManager.isCached(imageUrl);
        
        holder.cacheButton.setText(isCached ? "Cached" : context.getString(R.string.cache_images));
        holder.cacheButton.setAlpha(isCached ? 0.7f : 1.0f);
        holder.cacheButton.setEnabled(!isCached);
    }
    
    private void handleCacheButtonClick(ViewHolder holder, Archive archive) {
        // Get the image URL to cache
        Map.Entry<String, String> entry = archive.getThumbnailUrls().entrySet().iterator().next();
        String imageUrl = entry.getValue();
        
        // Check if already cached
        if (imageCacheManager.isCached(imageUrl)) {
            Toast.makeText(context, context.getString(R.string.image_already_cached), Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress indicator
        holder.progressLayout.setVisibility(View.VISIBLE);
        holder.cacheButton.setEnabled(false);
        holder.cacheProgressText.setText(context.getString(R.string.caching_progress));
        
        // Start caching in background thread
        Thread cacheThread = new Thread(() -> {
            try {
                URL url = new URL(imageUrl);
                URLConnection connection = url.openConnection();
                Bitmap image = BitmapFactory.decodeStream(connection.getInputStream());
                
                if (image != null) {
                    imageCacheManager.cacheImage(imageUrl, image, success -> {
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        mainHandler.post(() -> {
                            holder.progressLayout.setVisibility(View.GONE);
                            if (success) {
                                updateCacheButtonState(holder, archive);
                                Toast.makeText(context, context.getString(R.string.cache_complete), Toast.LENGTH_SHORT).show();
                            } else {
                                holder.cacheButton.setEnabled(true);
                                Toast.makeText(context, context.getString(R.string.cache_failed), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                } else {
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(() -> {
                        holder.progressLayout.setVisibility(View.GONE);
                        holder.cacheButton.setEnabled(true);
                        Toast.makeText(context, context.getString(R.string.cache_failed), Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e(LOGTAG, "Failed to cache image", e);
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> {
                    holder.progressLayout.setVisibility(View.GONE);
                    holder.cacheButton.setEnabled(true);
                    Toast.makeText(context, context.getString(R.string.cache_failed), Toast.LENGTH_SHORT).show();
                });
            }
        });
        
        cacheThread.start();
    }

}
