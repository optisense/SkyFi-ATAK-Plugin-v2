package com.skyfi.atak.plugin.views;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.skyfi.atak.plugin.R;

/**
 * Custom Toolbar with SkyFi branding
 */
public class SkyFiToolbar extends RelativeLayout {
    
    private TextView titleView;
    private TextView subtitleView;
    private ImageView logoView;
    private ImageView actionButton;
    private View gradientOverlay;
    
    public SkyFiToolbar(Context context) {
        super(context);
        init(context);
    }
    
    public SkyFiToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public SkyFiToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context) {
        // Set toolbar height
        setMinimumHeight(dpToPx(56));
        setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
        
        // Create gradient background
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        ContextCompat.getColor(context, R.color.skyfi_primary),
                        ContextCompat.getColor(context, R.color.skyfi_secondary)
                }
        );
        setBackground(gradientDrawable);
        
        // Add elevation
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            setElevation(dpToPx(4));
        }
        
        // Create logo
        logoView = new ImageView(context);
        RelativeLayout.LayoutParams logoParams = new RelativeLayout.LayoutParams(
                dpToPx(32),
                dpToPx(32)
        );
        logoParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        logoParams.addRule(RelativeLayout.CENTER_VERTICAL);
        logoView.setLayoutParams(logoParams);
        logoView.setId(View.generateViewId());
        // Set placeholder logo - replace with actual SkyFi logo
        logoView.setImageDrawable(createLogoPlaceholder());
        addView(logoView);
        
        // Create title container
        RelativeLayout titleContainer = new RelativeLayout(context);
        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        containerParams.addRule(RelativeLayout.RIGHT_OF, logoView.getId());
        containerParams.addRule(RelativeLayout.CENTER_VERTICAL);
        containerParams.setMargins(dpToPx(16), 0, dpToPx(16), 0);
        titleContainer.setLayoutParams(containerParams);
        
        // Create title
        titleView = new TextView(context);
        titleView.setId(View.generateViewId());
        titleView.setTextColor(ContextCompat.getColor(context, R.color.white));
        titleView.setTextSize(20);
        titleView.setText("SkyFi");
        titleView.setTypeface(titleView.getTypeface(), android.graphics.Typeface.BOLD);
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        titleView.setLayoutParams(titleParams);
        titleContainer.addView(titleView);
        
        // Create subtitle
        subtitleView = new TextView(context);
        subtitleView.setTextColor(ContextCompat.getColor(context, R.color.white));
        subtitleView.setAlpha(0.8f);
        subtitleView.setTextSize(12);
        subtitleView.setVisibility(View.GONE);
        RelativeLayout.LayoutParams subtitleParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        subtitleParams.addRule(RelativeLayout.BELOW, titleView.getId());
        subtitleView.setLayoutParams(subtitleParams);
        titleContainer.addView(subtitleView);
        
        addView(titleContainer);
        
        // Create action button
        actionButton = new ImageView(context);
        RelativeLayout.LayoutParams actionParams = new RelativeLayout.LayoutParams(
                dpToPx(40),
                dpToPx(40)
        );
        actionParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        actionParams.addRule(RelativeLayout.CENTER_VERTICAL);
        actionButton.setLayoutParams(actionParams);
        actionButton.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        actionButton.setBackground(createRippleBackground());
        actionButton.setVisibility(View.GONE);
        addView(actionButton);
    }
    
    public void setTitle(String title) {
        titleView.setText(title);
    }
    
    public void setSubtitle(String subtitle) {
        if (subtitle != null && !subtitle.isEmpty()) {
            subtitleView.setText(subtitle);
            subtitleView.setVisibility(View.VISIBLE);
        } else {
            subtitleView.setVisibility(View.GONE);
        }
    }
    
    public void setActionButton(Drawable icon, OnClickListener listener) {
        if (icon != null) {
            actionButton.setImageDrawable(icon);
            actionButton.setOnClickListener(listener);
            actionButton.setVisibility(View.VISIBLE);
        } else {
            actionButton.setVisibility(View.GONE);
        }
    }
    
    public void setLogo(Drawable logo) {
        if (logo != null) {
            logoView.setImageDrawable(logo);
        }
    }
    
    private Drawable createLogoPlaceholder() {
        // Create a simple circular logo placeholder
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(ContextCompat.getColor(getContext(), R.color.white));
        circle.setAlpha(230);
        return circle;
    }
    
    private Drawable createRippleBackground() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return ContextCompat.getDrawable(getContext(), R.drawable.skyfi_ripple_effect);
        } else {
            // Fallback for older versions
            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.OVAL);
            background.setColor(0x1AFFFFFF);
            return background;
        }
    }
    
    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}