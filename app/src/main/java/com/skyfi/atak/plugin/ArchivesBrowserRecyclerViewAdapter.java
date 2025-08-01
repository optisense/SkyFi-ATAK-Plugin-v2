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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.Button;

import com.skyfi.atak.plugin.skyfiapi.Archive;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ArchivesBrowserRecyclerViewAdapter extends RecyclerView.Adapter<ArchivesBrowserRecyclerViewAdapter.ViewHolder> {

    private static final String LOGTAG = "ArchivesBrowserRecycler";
    private ArrayList<Archive> mData;
    private LayoutInflater mInflater;
    private ArchivesBrowserRecyclerViewAdapter.ItemClickListener mClickListener;
    private int selectedPosition = -1;
    private Context context;
    private ImageCacheManager cacheManager;
    private Set<String> favoriteArchives = new HashSet<>();

    ArchivesBrowserRecyclerViewAdapter(Context context, ArrayList<Archive> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.cacheManager = ImageCacheManager.getInstance(context);
        loadFavorites();
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
            
            // Set up cache button
            updateCacheButton(holder, archive);
            
            // Set up favorite button
            updateFavoriteButton(holder, archive);
            
            // Cache button click handler
            holder.cacheButton.setOnClickListener(v -> {
                handleCacheButtonClick(holder, archive);
            });
            
            // Favorite button click handler
            holder.favoriteButton.setOnClickListener(v -> {
                toggleFavorite(archive);
                updateFavoriteButton(holder, archive);
            });
            
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
        ProgressBar cacheProgress;
        ImageView cacheComplete;
        ImageButton favoriteButton;
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
            cacheProgress = itemView.findViewById(R.id.cache_progress);
            cacheComplete = itemView.findViewById(R.id.cache_complete);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
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
    
    // Update the data and refresh the view
    public void updateData(ArrayList<Archive> newData) {
        this.mData = newData;
        notifyDataSetChanged();
    }

    // allows clicks events to be caught
    void setClickListener(ArchivesBrowserRecyclerViewAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
    
    private void updateCacheButton(ViewHolder holder, Archive archive) {
        String archiveId = archive.getArchiveId();
        boolean isCached = cacheManager.isCached(archiveId);
        
        if (isCached) {
            holder.cacheButton.setVisibility(View.GONE);
            holder.cacheProgress.setVisibility(View.GONE);
            holder.cacheComplete.setVisibility(View.VISIBLE);
        } else {
            holder.cacheButton.setVisibility(View.VISIBLE);
            holder.cacheProgress.setVisibility(View.GONE);
            holder.cacheComplete.setVisibility(View.GONE);
        }
    }
    
    private void updateFavoriteButton(ViewHolder holder, Archive archive) {
        boolean isFavorite = favoriteArchives.contains(archive.getArchiveId());
        holder.favoriteButton.setImageResource(
            isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
        );
        holder.favoriteButton.setColorFilter(
            isFavorite ? context.getResources().getColor(R.color.skyfi_accent) : 
                         context.getResources().getColor(R.color.skyfi_light_gray)
        );
    }
    
    private void handleCacheButtonClick(ViewHolder holder, Archive archive) {
        // Show progress
        holder.cacheButton.setVisibility(View.GONE);
        holder.cacheProgress.setVisibility(View.VISIBLE);
        
        // Get the full resolution image URL
        String imageUrl = getFullResolutionUrl(archive);
        if (imageUrl == null) {
            Toast.makeText(context, "No image URL available", Toast.LENGTH_SHORT).show();
            holder.cacheButton.setVisibility(View.VISIBLE);
            holder.cacheProgress.setVisibility(View.GONE);
            return;
        }
        
        // Cache the image
        cacheManager.cacheImage(archive.getArchiveId(), imageUrl, archive.toString(), 
            new ImageCacheManager.CacheCallback() {
                @Override
                public void onCached(String imageId, File cachedFile) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        holder.cacheProgress.setVisibility(View.GONE);
                        holder.cacheComplete.setVisibility(View.VISIBLE);
                        Toast.makeText(context, "Image cached successfully", Toast.LENGTH_SHORT).show();
                    });
                }
                
                @Override
                public void onProgress(int percent) {
                    // Could update progress bar here if using determinate progress
                }
                
                @Override
                public void onError(String error) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        holder.cacheProgress.setVisibility(View.GONE);
                        holder.cacheButton.setVisibility(View.VISIBLE);
                        Toast.makeText(context, "Cache failed: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });
    }
    
    private String getFullResolutionUrl(Archive archive) {
        // Try to get the highest resolution URL available
        if (archive.getThumbnailUrls() != null && !archive.getThumbnailUrls().isEmpty()) {
            // Get the first available image URL
            Map.Entry<String, String> entry = archive.getThumbnailUrls().entrySet().iterator().next();
            return entry.getValue();
        }
        return null;
    }
    
    private void toggleFavorite(Archive archive) {
        String archiveId = archive.getArchiveId();
        if (favoriteArchives.contains(archiveId)) {
            favoriteArchives.remove(archiveId);
        } else {
            favoriteArchives.add(archiveId);
        }
        saveFavorites();
    }
    
    private void loadFavorites() {
        // Load from SharedPreferences
        android.content.SharedPreferences prefs = context.getSharedPreferences("skyfi_prefs", Context.MODE_PRIVATE);
        favoriteArchives = prefs.getStringSet("favorite_archives", new HashSet<>());
    }
    
    private void saveFavorites() {
        // Save to SharedPreferences
        android.content.SharedPreferences prefs = context.getSharedPreferences("skyfi_prefs", Context.MODE_PRIVATE);
        prefs.edit().putStringSet("favorite_archives", favoriteArchives).apply();
    }

}
