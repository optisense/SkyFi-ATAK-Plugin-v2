package com.skyfi.atak.plugin.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.skyfi.atak.plugin.R;

/**
 * Custom SkyFi Card view for consistent card styling
 */
public class SkyFiCard extends LinearLayout {
    
    private boolean isElevated = true;
    
    public SkyFiCard(Context context) {
        super(context);
        init(context, null);
    }
    
    public SkyFiCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public SkyFiCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        // Set orientation to vertical by default
        setOrientation(VERTICAL);
        
        // Apply card background
        setBackground(ContextCompat.getDrawable(context, R.drawable.skyfi_card_background));
        
        // Add ripple effect for clickable cards
        if (isClickable()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                setForeground(ContextCompat.getDrawable(context, R.drawable.skyfi_ripple_effect));
            }
        }
        
        // Process custom attributes
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(
                    attrs, 
                    new int[]{android.R.attr.clickable, android.R.attr.elevation}
            );
            
            boolean clickable = a.getBoolean(0, false);
            setClickable(clickable);
            setFocusable(clickable);
            
            float elevation = a.getDimension(1, dpToPx(4));
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                setElevation(elevation);
            }
            
            a.recycle();
        }
        
        // Set default padding if not set
        if (getPaddingLeft() == 0 && getPaddingRight() == 0) {
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        }
    }
    
    public void setElevated(boolean elevated) {
        isElevated = elevated;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            setElevation(elevated ? dpToPx(4) : 0);
        }
    }
    
    public void animateElevation(boolean raised) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            animate()
                    .translationZ(raised ? dpToPx(8) : 0)
                    .setDuration(200)
                    .start();
        }
    }
    
    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}