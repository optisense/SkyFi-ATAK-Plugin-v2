package com.skyfi.atak.plugin.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.skyfi.atak.plugin.R;

/**
 * Example usage of custom SkyFi UI components
 * This class demonstrates how to use the custom views and drawables
 */
public class ExampleUsage {
    
    /**
     * Example of creating a SkyFi styled layout programmatically
     */
    public static View createExampleLayout(Context context) {
        LinearLayout rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(dpToPx(context, 16), dpToPx(context, 16), 
                              dpToPx(context, 16), dpToPx(context, 16));
        rootLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.skyfi_dark));
        
        // Add SkyFi Toolbar
        SkyFiToolbar toolbar = new SkyFiToolbar(context);
        toolbar.setTitle("Satellite Tasking");
        toolbar.setSubtitle("Active Orders");
        rootLayout.addView(toolbar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        
        // Add spacing
        View spacer1 = new View(context);
        spacer1.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(context, 16)
        ));
        rootLayout.addView(spacer1);
        
        // Add SkyFi Card with content
        SkyFiCard card = new SkyFiCard(context);
        card.setClickable(true);
        
        // Card content
        TextView cardTitle = new TextView(context);
        cardTitle.setText("New Tasking Order");
        cardTitle.setTextSize(18);
        cardTitle.setTextColor(ContextCompat.getColor(context, R.color.skyfi_dark));
        card.addView(cardTitle);
        
        StatusBadge statusBadge = new StatusBadge(context);
        statusBadge.setStatus(StatusBadge.Status.PROCESSING);
        statusBadge.setStatusText("Processing Order");
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        badgeParams.topMargin = dpToPx(context, 8);
        card.addView(statusBadge, badgeParams);
        
        rootLayout.addView(card, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        
        // Add spacing
        View spacer2 = new View(context);
        spacer2.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(context, 16)
        ));
        rootLayout.addView(spacer2);
        
        // Add input field with SkyFi styling
        EditText inputField = new EditText(context);
        inputField.setHint("Enter coordinates");
        inputField.setBackground(ContextCompat.getDrawable(context, R.drawable.skyfi_input_background));
        inputField.setTextColor(ContextCompat.getColor(context, R.color.skyfi_dark));
        inputField.setHintTextColor(ContextCompat.getColor(context, R.color.skyfi_gray));
        rootLayout.addView(inputField, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        
        // Add spacing
        View spacer3 = new View(context);
        spacer3.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(context, 16)
        ));
        rootLayout.addView(spacer3);
        
        // Add different button styles
        SkyFiButton primaryButton = new SkyFiButton(context);
        primaryButton.setText("Submit Order");
        primaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                primaryButton.setLoading(true);
                // Simulate API call
                v.postDelayed(() -> primaryButton.setLoading(false), 2000);
            }
        });
        rootLayout.addView(primaryButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        
        // Add accent button
        TextView accentButton = new TextView(context);
        accentButton.setText("Quick Action");
        accentButton.setTextColor(ContextCompat.getColor(context, R.color.white));
        accentButton.setGravity(android.view.Gravity.CENTER);
        accentButton.setBackground(ContextCompat.getDrawable(context, R.drawable.skyfi_button_accent));
        LinearLayout.LayoutParams accentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        accentParams.topMargin = dpToPx(context, 8);
        rootLayout.addView(accentButton, accentParams);
        
        // Add outline button
        TextView outlineButton = new TextView(context);
        outlineButton.setText("Cancel");
        outlineButton.setTextColor(ContextCompat.getColor(context, R.color.skyfi_primary));
        outlineButton.setGravity(android.view.Gravity.CENTER);
        outlineButton.setBackground(ContextCompat.getDrawable(context, R.drawable.skyfi_button_outline));
        LinearLayout.LayoutParams outlineParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        outlineParams.topMargin = dpToPx(context, 8);
        rootLayout.addView(outlineButton, outlineParams);
        
        // Add horizontal progress bar
        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.skyfi_progress_horizontal));
        progressBar.setMax(100);
        progressBar.setProgress(65);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(context, 8)
        );
        progressParams.topMargin = dpToPx(context, 16);
        rootLayout.addView(progressBar, progressParams);
        
        return rootLayout;
    }
    
    /**
     * Example of using animations with the components
     */
    public static void animateCard(SkyFiCard card, Context context) {
        // Animate card on touch
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(android.view.animation.AnimationUtils.loadAnimation(
                        context, R.anim.skyfi_button_press));
                v.postDelayed(() -> {
                    v.startAnimation(android.view.animation.AnimationUtils.loadAnimation(
                            context, R.anim.skyfi_button_release));
                }, 100);
            }
        });
    }
    
    /**
     * Example of creating a chip layout
     */
    public static LinearLayout createChipLayout(Context context, String[] tags) {
        LinearLayout chipContainer = new LinearLayout(context);
        chipContainer.setOrientation(LinearLayout.HORIZONTAL);
        
        for (String tag : tags) {
            TextView chip = new TextView(context);
            chip.setText(tag);
            chip.setTextColor(ContextCompat.getColor(context, R.color.skyfi_gray));
            chip.setTextSize(14);
            chip.setBackground(ContextCompat.getDrawable(context, R.drawable.skyfi_chip_background));
            
            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            chipParams.rightMargin = dpToPx(context, 8);
            chipContainer.addView(chip, chipParams);
            
            // Make chips clickable and change state
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setSelected(!v.isSelected());
                    if (v.isSelected()) {
                        ((TextView) v).setTextColor(ContextCompat.getColor(context, R.color.white));
                    } else {
                        ((TextView) v).setTextColor(ContextCompat.getColor(context, R.color.skyfi_gray));
                    }
                }
            });
        }
        
        return chipContainer;
    }
    
    private static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}