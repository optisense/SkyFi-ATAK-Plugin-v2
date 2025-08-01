package com.skyfi.atak.plugin;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.SeekBar;
import android.widget.TextView;
import com.atakmap.coremap.log.Log;
// Removed Material Slider import

/**
 * Enhanced dialog for controlling the opacity of image overlays on the map
 * with smooth transitions and visual feedback
 */
public class OpacityControlDialog {
    private static final String LOGTAG = "OpacityControlDialog";
    
    private final Context context;
    private final OpacityChangeListener listener;
    private final Preferences preferences;
    private AlertDialog dialog;
    private SeekBar opacitySlider;
    private TextView opacityValueText;
    private TextView opacityText;
    private float currentOpacity;
    private ValueAnimator opacityAnimator;
    
    public interface OpacityChangeListener {
        void onOpacityChanged(float opacity);
        void onShowHideToggled(boolean show);
        void onPresetSelected(float opacity);
    }
    
    public OpacityControlDialog(Context context, OpacityChangeListener listener) {
        this.context = context;
        this.listener = listener;
        this.preferences = new Preferences();
        this.currentOpacity = preferences.getDefaultOpacity() / 100f; // Convert to 0-1 range
    }
    
    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.opacity_control_dialog, null);
        
        // Initialize views
        opacitySlider = dialogView.findViewById(R.id.opacity_seekbar);
        opacityValueText = dialogView.findViewById(R.id.opacity_value);
        opacityText = dialogView.findViewById(R.id.opacity_text);
        
        // Set initial values
        opacitySlider.setProgress((int)(currentOpacity * 100));
        updateOpacityText(currentOpacity);
        
        // Add preset buttons
        View preset25 = dialogView.findViewById(R.id.preset_25);
        View preset50 = dialogView.findViewById(R.id.preset_50);
        View preset75 = dialogView.findViewById(R.id.preset_75);
        View preset100 = dialogView.findViewById(R.id.preset_100);
        
        if (preset25 != null) {
            preset25.setOnClickListener(v -> animateToOpacity(0.25f));
        }
        if (preset50 != null) {
            preset50.setOnClickListener(v -> animateToOpacity(0.50f));
        }
        if (preset75 != null) {
            preset75.setOnClickListener(v -> animateToOpacity(0.75f));
        }
        if (preset100 != null) {
            preset100.setOnClickListener(v -> animateToOpacity(1.0f));
        }
        
        // Set up SeekBar listener
        opacitySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentOpacity = progress / 100f;
                    updateOpacityText(currentOpacity);
                    updateOpacityVisualFeedback(currentOpacity);
                    
                    // Notify listener for real-time updates
                    if (listener != null) {
                        listener.onOpacityChanged(currentOpacity);
                    }
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Cancel any ongoing animation
                if (opacityAnimator != null && opacityAnimator.isRunning()) {
                    opacityAnimator.cancel();
                }
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Save preference when user stops dragging
                preferences.setDefaultOpacity((int)(currentOpacity * 100));
                Log.d(LOGTAG, "Opacity saved: " + (int)(currentOpacity * 100) + "%");
            }
        });
        
        // Build the dialog
        builder.setView(dialogView)
                .setTitle("SkyFi Overlay Controls")
                .setPositiveButton("Apply & Show", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onShowHideToggled(true);
                        }
                    }
                })
                .setNegativeButton("Hide Overlay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onShowHideToggled(false);
                        }
                    }
                })
                .setNeutralButton("Close", null);
        
        dialog = builder.create();
        dialog.show();
        
        // Initial visual feedback
        updateOpacityVisualFeedback(currentOpacity);
    }
    
    private void animateToOpacity(float targetOpacity) {
        if (opacityAnimator != null && opacityAnimator.isRunning()) {
            opacityAnimator.cancel();
        }
        
        opacityAnimator = ValueAnimator.ofFloat(currentOpacity, targetOpacity);
        opacityAnimator.setDuration(300);
        opacityAnimator.setInterpolator(new DecelerateInterpolator());
        
        opacityAnimator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            currentOpacity = animatedValue;
            opacitySlider.setProgress((int)(animatedValue * 100));
            updateOpacityText(animatedValue);
            updateOpacityVisualFeedback(animatedValue);
            
            if (listener != null) {
                listener.onOpacityChanged(animatedValue);
            }
        });
        
        opacityAnimator.start();
        
        // Save preference
        preferences.setDefaultOpacity((int)(targetOpacity * 100));
        
        if (listener != null) {
            listener.onPresetSelected(targetOpacity);
        }
    }
    
    private void updateOpacityText(float opacity) {
        if (opacityValueText != null) {
            int percentage = (int)(opacity * 100);
            opacityValueText.setText(String.valueOf(percentage));
            
            // Update color based on opacity level
            if (percentage < 30) {
                opacityValueText.setTextColor(0xFFFF6B35); // SkyFi accent
            } else if (percentage < 60) {
                opacityValueText.setTextColor(0xFF00D4FF); // SkyFi secondary
            } else {
                opacityValueText.setTextColor(0xFF0066FF); // SkyFi primary
            }
        }
    }
    
    private void updateOpacityVisualFeedback(float opacity) {
        // Update dialog background opacity as visual feedback
        if (dialog != null && dialog.getWindow() != null) {
            View decorView = dialog.getWindow().getDecorView();
            if (decorView != null) {
                View background = decorView.findViewById(android.R.id.content);
                if (background != null) {
                    // Subtle background tint based on opacity
                    int alpha = (int)(opacity * 30); // Max 30 alpha for subtle effect
                    background.setBackgroundColor(Color.argb(alpha, 0, 102, 255)); // SkyFi primary with variable alpha
                }
            }
        }
        
        // Update description text
        if (opacityText != null) {
            if (opacity < 0.3f) {
                opacityText.setText("Very transparent - base map clearly visible");
            } else if (opacity < 0.6f) {
                opacityText.setText("Balanced - imagery and map both visible");
            } else if (opacity < 0.9f) {
                opacityText.setText("Mostly opaque - imagery prominent");
            } else {
                opacityText.setText("Full opacity - imagery only");
            }
        }
    }
    
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if (opacityAnimator != null && opacityAnimator.isRunning()) {
            opacityAnimator.cancel();
        }
    }
    
    public float getCurrentOpacity() {
        return currentOpacity;
    }
}