package com.skyfi.atak.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atakmap.coremap.log.Log;
import com.skyfi.atak.plugin.skyfiapi.Archive;

import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PreviewThumbnailAdapter extends RecyclerView.Adapter<PreviewThumbnailAdapter.ViewHolder> {
    private static final String LOGTAG = "PreviewThumbnailAdapter";
    
    private Context context;
    private List<Archive> archives;
    private OnThumbnailClickListener clickListener;
    private SimpleDateFormat dateFormat;

    public interface OnThumbnailClickListener {
        void onThumbnailClick(Archive archive);
    }

    public PreviewThumbnailAdapter(Context context) {
        this.context = context;
        this.archives = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
    }

    public void setArchives(List<Archive> archives) {
        this.archives.clear();
        if (archives != null) {
            this.archives.addAll(archives);
        }
        notifyDataSetChanged();
    }

    public void setOnThumbnailClickListener(OnThumbnailClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.preview_thumbnail_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Archive archive = archives.get(position);

        // Set provider text
        holder.providerText.setText(archive.getProvider() != null ? archive.getProvider() : "Unknown");

        // Set date text
        try {
            if (archive.getCaptureTimestamp() != null) {
                holder.dateText.setText(dateFormat.format(archive.getCaptureTimestamp()));
            } else {
                holder.dateText.setText("N/A");
            }
        } catch (Exception e) {
            holder.dateText.setText("N/A");
        }

        // Load thumbnail image using the same pattern as ArchivesBrowserRecyclerViewAdapter
        String thumbnailUrl = getThumbnailUrl(archive);
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            loadThumbnailImage(holder.thumbnailImage, thumbnailUrl);
        } else {
            holder.thumbnailImage.setImageResource(R.drawable.placeholder);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onThumbnailClick(archive);
            }
        });
    }

    @Override
    public int getItemCount() {
        return archives.size();
    }

    private String getThumbnailUrl(Archive archive) {
        // Try to get a thumbnail URL from the archive
        if (archive.getThumbnailUrls() != null && !archive.getThumbnailUrls().isEmpty()) {
            return archive.getThumbnailUrls().values().iterator().next();
        }
        // Fallback to titles URL if available
        return archive.getTitlesUrl();
    }

    private void loadThumbnailImage(ImageView imageView, String imageUrl) {
        Handler handler = new Handler(Looper.getMainLooper());
        Thread thread = new Thread(() -> {
            try {
                URL thumbnailUrl = new URL(imageUrl);
                URLConnection connection = thumbnailUrl.openConnection();
                Bitmap image = BitmapFactory.decodeStream(connection.getInputStream());
                handler.post(() -> {
                    if (image != null) {
                        imageView.setImageBitmap(image);
                        imageView.setAlpha(1f);
                    } else {
                        imageView.setImageResource(R.drawable.placeholder);
                    }
                });
            } catch (Exception e) {
                Log.e(LOGTAG, "Failed to load thumbnail: " + imageUrl, e);
                handler.post(() -> imageView.setImageResource(R.drawable.placeholder));
            }
        });
        thread.start();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImage;
        TextView providerText;
        TextView dateText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImage = itemView.findViewById(R.id.thumbnail_image);
            providerText = itemView.findViewById(R.id.provider_text);
            dateText = itemView.findViewById(R.id.date_text);
        }
    }
}