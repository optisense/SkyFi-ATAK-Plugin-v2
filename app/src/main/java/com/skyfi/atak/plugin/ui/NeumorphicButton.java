package com.skyfi.atak.plugin.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;

/**
 * A custom Button that implements neumorphic design with soft shadows.
 * Provides smooth press animations that work with the SkyFi dark theme.
 */
public class NeumorphicButton extends Button {
    
    private static final int ANIMATION_DURATION = 150;
    private static final float ELEVATION_NORMAL = 8f;
    private static final float ELEVATION_PRESSED = 2f;
    private static final float CORNER_RADIUS = 12f;
    
    // SkyFi theme colors
    private int backgroundColor = 0xFF1A1A1A; // skyfi_surface
    private int highlightColor = 0xFF2A2A2A;  // card_hover
    private int shadowColorLight = 0x1AFFFFFF; // Light shadow for top-left
    private int shadowColorDark = 0x4D000000;  // Dark shadow for bottom-right
    
    private Paint shadowPaint;
    private RectF buttonRect;
    private float currentElevation = ELEVATION_NORMAL;
    private boolean isPressed = false;
    
    private AnimatorSet pressAnimator;
    private AnimatorSet releaseAnimator;
    
    public NeumorphicButton(Context context) {
        super(context);
        init();
    }
    
    public NeumorphicButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public NeumorphicButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Disable default background
        setBackground(null);
        
        // Setup shadow paint
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonRect = new RectF();
        
        // Setup animations
        setupAnimations();
        
        // Set text appearance for dark theme
        setTextColor(0xFFFFFFFF); // skyfi_text_primary
        setTypeface(getTypeface(), android.graphics.Typeface.NORMAL);
        
        // Enable click effects
        setClickable(true);
        setFocusable(true);
    }
    
    private void setupAnimations() {
        // Press animation
        ObjectAnimator pressElevation = ObjectAnimator.ofFloat(this, "currentElevation", ELEVATION_NORMAL, ELEVATION_PRESSED);
        ObjectAnimator pressScaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 0.98f);
        ObjectAnimator pressScaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 0.98f);
        
        pressAnimator = new AnimatorSet();
        pressAnimator.playTogether(pressElevation, pressScaleX, pressScaleY);
        pressAnimator.setDuration(ANIMATION_DURATION);
        pressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        // Release animation
        ObjectAnimator releaseElevation = ObjectAnimator.ofFloat(this, "currentElevation", ELEVATION_PRESSED, ELEVATION_NORMAL);
        ObjectAnimator releaseScaleX = ObjectAnimator.ofFloat(this, "scaleX", 0.98f, 1f);
        ObjectAnimator releaseScaleY = ObjectAnimator.ofFloat(this, "scaleY", 0.98f, 1f);
        
        releaseAnimator = new AnimatorSet();
        releaseAnimator.playTogether(releaseElevation, releaseScaleX, releaseScaleY);
        releaseAnimator.setDuration(ANIMATION_DURATION);
        releaseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isPressed) {
                    isPressed = true;
                    animatePress();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isPressed) {
                    isPressed = false;
                    animateRelease();
                }
                break;
        }
        return super.onTouchEvent(event);
    }
    
    private void animatePress() {
        if (releaseAnimator.isRunning()) {
            releaseAnimator.cancel();
        }
        pressAnimator.start();
    }
    
    private void animateRelease() {
        if (pressAnimator.isRunning()) {
            pressAnimator.cancel();
        }
        releaseAnimator.start();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        float padding = currentElevation;
        buttonRect.set(
            padding,
            padding,
            w - padding,
            h - padding
        );
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        drawNeumorphicBackground(canvas);
        super.onDraw(canvas);
    }
    
    private void drawNeumorphicBackground(Canvas canvas) {
        float padding = currentElevation;
        buttonRect.set(
            padding,
            padding,
            getWidth() - padding,
            getHeight() - padding
        );
        
        // Draw dark shadow (bottom-right)
        shadowPaint.setColor(shadowColorDark);
        shadowPaint.setShadowLayer(currentElevation, currentElevation * 0.5f, currentElevation * 0.5f, shadowColorDark);
        canvas.drawRoundRect(buttonRect, CORNER_RADIUS, CORNER_RADIUS, shadowPaint);
        
        // Draw light shadow (top-left)
        shadowPaint.setColor(shadowColorLight);
        shadowPaint.setShadowLayer(currentElevation, -currentElevation * 0.3f, -currentElevation * 0.3f, shadowColorLight);
        canvas.drawRoundRect(buttonRect, CORNER_RADIUS, CORNER_RADIUS, shadowPaint);
        
        // Draw button background
        shadowPaint.setColor(isPressed ? highlightColor : backgroundColor);
        shadowPaint.clearShadowLayer();
        canvas.drawRoundRect(buttonRect, CORNER_RADIUS, CORNER_RADIUS, shadowPaint);
        
        // Add subtle inner highlight for depth
        if (!isPressed) {
            shadowPaint.setColor(0x0DFFFFFF);
            RectF innerRect = new RectF(
                buttonRect.left + 2,
                buttonRect.top + 1,
                buttonRect.right - 2,
                buttonRect.bottom - 2
            );
            canvas.drawRoundRect(innerRect, CORNER_RADIUS - 1, CORNER_RADIUS - 1, shadowPaint);
        }
    }
    
    // Property setter for animation
    public void setCurrentElevation(float elevation) {
        this.currentElevation = elevation;
        invalidate();
    }
    
    public float getCurrentElevation() {
        return currentElevation;
    }
    
    /**
     * Set custom background color for the neumorphic button
     * @param color Background color
     */
    public void setNeumorphicBackgroundColor(int color) {
        this.backgroundColor = color;
        
        // Calculate highlight color (slightly lighter)
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = Math.min(1.0f, hsv[2] + 0.1f); // Increase brightness by 10%
        this.highlightColor = Color.HSVToColor(hsv);
        
        invalidate();
    }
    
    /**
     * Set shadow colors for the neumorphic effect
     * @param lightColor Light shadow color (top-left)
     * @param darkColor Dark shadow color (bottom-right)
     */
    public void setShadowColors(int lightColor, int darkColor) {
        this.shadowColorLight = lightColor;
        this.shadowColorDark = darkColor;
        invalidate();
    }
    
    /**
     * Enable or disable the layer for hardware acceleration (recommended for smooth shadows)
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setLayerType(View.LAYER_TYPE_SOFTWARE, null); // Required for shadow effects
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        
        if (enabled) {
            setAlpha(1.0f);
            setTextColor(0xFFFFFFFF); // skyfi_text_primary
        } else {
            setAlpha(0.5f);
            setTextColor(0xFF666666); // skyfi_text_disabled
        }
    }
}