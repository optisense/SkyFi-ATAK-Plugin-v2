package com.skyfi.atak.plugin.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.skyfi.atak.plugin.R;

/**
 * Custom Status Badge view for displaying order statuses
 */
public class StatusBadge extends LinearLayout {
    
    public enum Status {
        SUCCESS,
        WARNING,
        ERROR,
        PENDING,
        PROCESSING,
        COMPLETED
    }
    
    private ImageView iconView;
    private TextView textView;
    private Status currentStatus;
    
    public StatusBadge(Context context) {
        super(context);
        init(context);
    }
    
    public StatusBadge(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public StatusBadge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackground(ContextCompat.getDrawable(context, R.drawable.skyfi_badge_background));
        setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6));
        
        // Create icon view
        iconView = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dpToPx(16),
                dpToPx(16)
        );
        iconParams.setMargins(0, 0, dpToPx(6), 0);
        iconView.setLayoutParams(iconParams);
        addView(iconView);
        
        // Create text view
        textView = new TextView(context);
        textView.setTextColor(ContextCompat.getColor(context, R.color.white));
        textView.setTextSize(12);
        addView(textView);
        
        // Set default status
        setStatus(Status.PENDING);
    }
    
    public void setStatus(Status status) {
        currentStatus = status;
        
        switch (status) {
            case SUCCESS:
            case COMPLETED:
                iconView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.skyfi_status_success));
                textView.setText("Completed");
                setBackgroundTint(ContextCompat.getColor(getContext(), R.color.skyfi_success));
                break;
                
            case WARNING:
                iconView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.skyfi_status_warning));
                textView.setText("Warning");
                setBackgroundTint(ContextCompat.getColor(getContext(), R.color.skyfi_warning));
                break;
                
            case ERROR:
                iconView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.skyfi_status_error));
                textView.setText("Error");
                setBackgroundTint(ContextCompat.getColor(getContext(), R.color.skyfi_error));
                break;
                
            case PENDING:
                iconView.setImageDrawable(createCircleDrawable(R.color.skyfi_light_gray));
                textView.setText("Pending");
                setBackgroundTint(ContextCompat.getColor(getContext(), R.color.skyfi_light_gray));
                break;
                
            case PROCESSING:
                iconView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.skyfi_progress_circular));
                textView.setText("Processing");
                setBackgroundTint(ContextCompat.getColor(getContext(), R.color.skyfi_primary));
                break;
        }
    }
    
    public void setStatusText(String text) {
        textView.setText(text);
    }
    
    public Status getStatus() {
        return currentStatus;
    }
    
    private Drawable createCircleDrawable(int colorResId) {
        // Create a simple circle drawable for pending state
        android.graphics.drawable.GradientDrawable circle = new android.graphics.drawable.GradientDrawable();
        circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        circle.setColor(ContextCompat.getColor(getContext(), colorResId));
        circle.setSize(dpToPx(16), dpToPx(16));
        return circle;
    }
    
    private void setBackgroundTint(int color) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getBackground().setTint(color);
        }
    }
    
    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}