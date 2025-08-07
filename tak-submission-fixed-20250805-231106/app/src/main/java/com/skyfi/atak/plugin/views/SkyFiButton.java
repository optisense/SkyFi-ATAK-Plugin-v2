package com.skyfi.atak.plugin.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.skyfi.atak.plugin.R;

/**
 * Custom SkyFi Button with built-in loading states
 */
public class SkyFiButton extends RelativeLayout {
    
    private TextView textView;
    private ProgressBar progressBar;
    private String buttonText;
    private boolean isLoading = false;
    
    public SkyFiButton(Context context) {
        super(context);
        init(context, null);
    }
    
    public SkyFiButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public SkyFiButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        // Set button background
        setBackground(ContextCompat.getDrawable(context, R.drawable.skyfi_button_gradient));
        setClickable(true);
        setFocusable(true);
        
        // Set ripple effect
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            setForeground(ContextCompat.getDrawable(context, R.drawable.skyfi_ripple_effect));
        }
        
        // Create TextView for button text
        textView = new TextView(context);
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        textParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        textView.setLayoutParams(textParams);
        textView.setTextColor(ContextCompat.getColor(context, R.color.white));
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER);
        addView(textView);
        
        // Create ProgressBar for loading state
        progressBar = new ProgressBar(context);
        RelativeLayout.LayoutParams progressParams = new RelativeLayout.LayoutParams(
                dpToPx(24),
                dpToPx(24)
        );
        progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(progressParams);
        progressBar.setIndeterminateDrawable(
                ContextCompat.getDrawable(context, R.drawable.skyfi_progress_circular)
        );
        progressBar.setVisibility(View.GONE);
        addView(progressBar);
        
        // Process custom attributes
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.text});
            buttonText = a.getString(0);
            if (buttonText != null) {
                setText(buttonText);
            }
            a.recycle();
        }
        
        // Set default padding if not set
        if (getPaddingLeft() == 0 && getPaddingRight() == 0) {
            setPadding(dpToPx(24), dpToPx(12), dpToPx(24), dpToPx(12));
        }
    }
    
    public void setText(String text) {
        buttonText = text;
        textView.setText(text);
    }
    
    public void setLoading(boolean loading) {
        isLoading = loading;
        if (loading) {
            textView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            setEnabled(false);
        } else {
            textView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            setEnabled(true);
        }
    }
    
    public boolean isLoading() {
        return isLoading;
    }
    
    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        textView.setAlpha(enabled ? 1.0f : 0.6f);
    }
}