package com.optisense.skyfi.atak;
import com.skyfi.atak.plugin.R;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class OpacityControlDialog {
    
    public interface OpacityChangeListener {
        void onOpacityChanged(int opacity); // 0-100
    }
    
    public static void show(Context context, String title, int currentOpacity, OpacityChangeListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.opacity_control_dialog, null);
        
        SeekBar opacitySeekBar = dialogView.findViewById(R.id.opacity_seekbar);
        TextView opacityValueText = dialogView.findViewById(R.id.opacity_value_text);
        TextView previewText = dialogView.findViewById(R.id.opacity_preview_text);
        
        // Set initial values
        opacitySeekBar.setProgress(currentOpacity);
        opacityValueText.setText(currentOpacity + "%");
        updatePreviewText(previewText, currentOpacity);
        
        // Set up seekbar listener
        opacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                opacityValueText.setText(progress + "%");
                updatePreviewText(previewText, progress);
                if (listener != null) {
                    listener.onOpacityChanged(progress);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
            .setView(dialogView)
            .setPositiveButton("OK", (dialog, which) -> {
                // Final opacity value already set via listener
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                // Revert to original opacity
                if (listener != null) {
                    listener.onOpacityChanged(currentOpacity);
                }
            });
        
        builder.create().show();
    }
    
    private static void updatePreviewText(TextView previewText, int opacity) {
        if (opacity == 0) {
            previewText.setText("Transparent (Hidden)");
        } else if (opacity < 25) {
            previewText.setText("Very Transparent");
        } else if (opacity < 50) {
            previewText.setText("Semi-Transparent");
        } else if (opacity < 75) {
            previewText.setText("Slightly Transparent");
        } else if (opacity < 100) {
            previewText.setText("Nearly Opaque");
        } else {
            previewText.setText("Opaque (Fully Visible)");
        }
    }
}