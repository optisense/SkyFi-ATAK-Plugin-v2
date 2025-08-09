package com.optisense.skyfi.atak.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * A custom CardView that displays a subtle glow effect around its edges.
 * Uses the SkyFi accent color for the glow and integrates with the dark theme.
 */
public class GlowingCardView extends FrameLayout {
    
    private static final float DEFAULT_CORNER_RADIUS = 12f;
    private static final float DEFAULT_GLOW_RADIUS = 16f;
    private static final int DEFAULT_GLOW_COLOR = 0xFF4A90E2; // skyfi_accent
    private static final int DEFAULT_CARD_COLOR = 0xFF1F1F1F;  // card_background
    private static final int GLOW_ANIMATION_DURATION = 2000;
    
    private Paint glowPaint;
    private Paint cardPaint;
    private Paint maskPaint;
    
    private RectF cardRect;
    private RectF glowRect;
    private Path clipPath;
    
    private float cornerRadius = DEFAULT_CORNER_RADIUS;
    private float glowRadius = DEFAULT_GLOW_RADIUS;
    private int glowColor = DEFAULT_GLOW_COLOR;
    private int cardColor = DEFAULT_CARD_COLOR;
    
    private boolean glowEnabled = true;
    private boolean animateGlow = false;
    private float glowIntensity = 0.3f;
    private float animatedGlowIntensity = 0.3f;
    
    private ValueAnimator glowAnimator;
    private Bitmap glowBitmap;
    private Canvas glowCanvas;
    private boolean needsGlowUpdate = true;
    
    public GlowingCardView(Context context) {
        super(context);
        init();
    }
    
    public GlowingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public GlowingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Enable layer for glow effects
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        
        setupPaints();
        setupAnimations();
        
        cardRect = new RectF();
        glowRect = new RectF();
        clipPath = new Path();
        
        // Set default padding for glow effect
        setPadding(
            (int) glowRadius,
            (int) glowRadius,
            (int) glowRadius,
            (int) glowRadius
        );
    }
    
    private void setupPaints() {
        // Glow paint
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        
        // Card paint
        cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardPaint.setStyle(Paint.Style.FILL);
        cardPaint.setColor(cardColor);
        
        // Mask paint for inner shadow cutout
        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }
    
    private void setupAnimations() {
        glowAnimator = ValueAnimator.ofFloat(0.1f, 1.0f);
        glowAnimator.setDuration(GLOW_ANIMATION_DURATION);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.setRepeatMode(ValueAnimator.REVERSE);
        glowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        glowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animationValue = (Float) animation.getAnimatedValue();
                animatedGlowIntensity = glowIntensity * animationValue;
                needsGlowUpdate = true;
                invalidate();
            }
        });
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Update card and glow rectangles
        cardRect.set(
            glowRadius,
            glowRadius,
            w - glowRadius,
            h - glowRadius
        );
        
        glowRect.set(
            0,
            0,
            w,
            h
        );
        
        // Create clipping path for card shape
        clipPath.reset();
        clipPath.addRoundRect(cardRect, cornerRadius, cornerRadius, Path.Direction.CW);
        
        needsGlowUpdate = true;
        
        // Recreate glow bitmap
        if (glowBitmap != null) {
            glowBitmap.recycle();
        }
        if (w > 0 && h > 0) {
            glowBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            glowCanvas = new Canvas(glowBitmap);
        }
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (glowEnabled && glowBitmap != null) {
            if (needsGlowUpdate) {
                updateGlowBitmap();
                needsGlowUpdate = false;
            }
            
            // Draw the glow effect
            canvas.drawBitmap(glowBitmap, 0, 0, null);
        } else {
            // Draw card background without glow
            canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, cardPaint);
        }
        
        // Clip child views to card bounds
        canvas.save();
        canvas.clipPath(clipPath);
        super.dispatchDraw(canvas);
        canvas.restore();
    }
    
    private void updateGlowBitmap() {
        if (glowCanvas == null) return;
        
        // Clear the glow bitmap
        glowCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        
        // Calculate glow intensity and color
        float currentIntensity = animateGlow ? animatedGlowIntensity : glowIntensity;
        int glowAlpha = (int) (255 * currentIntensity);
        int currentGlowColor = (glowColor & 0x00FFFFFF) | (glowAlpha << 24);
        
        // Set glow properties
        glowPaint.setColor(currentGlowColor);
        glowPaint.setShadowLayer(glowRadius, 0, 0, currentGlowColor);
        
        // Draw glow background (larger than card)
        glowCanvas.drawRoundRect(
            glowRect.left + glowRadius * 0.3f,
            glowRect.top + glowRadius * 0.3f,
            glowRect.right - glowRadius * 0.3f,
            glowRect.bottom - glowRadius * 0.3f,
            cornerRadius + glowRadius * 0.5f,
            cornerRadius + glowRadius * 0.5f,
            glowPaint
        );
        
        // Cut out the inner card area to create glow effect only on edges
        glowCanvas.drawRoundRect(
            cardRect,
            cornerRadius,
            cornerRadius,
            maskPaint
        );
        
        // Draw the card background
        cardPaint.setColor(cardColor);
        glowCanvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, cardPaint);
    }
    
    /**
     * Enable or disable the glow effect
     * @param enabled true to enable glow, false to disable
     */
    public void setGlowEnabled(boolean enabled) {
        this.glowEnabled = enabled;
        needsGlowUpdate = true;
        invalidate();
    }
    
    /**
     * Set the glow color
     * @param color Glow color
     */
    public void setGlowColor(int color) {
        this.glowColor = color;
        needsGlowUpdate = true;
        invalidate();
    }
    
    /**
     * Set the glow radius
     * @param radius Glow radius in pixels
     */
    public void setGlowRadius(float radius) {
        this.glowRadius = radius;
        
        // Update padding to accommodate new glow radius
        setPadding(
            (int) glowRadius,
            (int) glowRadius,
            (int) glowRadius,
            (int) glowRadius
        );
        
        needsGlowUpdate = true;
        requestLayout();
    }
    
    /**
     * Set the glow intensity
     * @param intensity Intensity value between 0f and 1f
     */
    public void setGlowIntensity(float intensity) {
        this.glowIntensity = Math.max(0f, Math.min(1f, intensity));
        needsGlowUpdate = true;
        invalidate();
    }
    
    /**
     * Set the corner radius for the card
     * @param radius Corner radius in pixels
     */
    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        
        // Update clip path
        if (cardRect != null && !cardRect.isEmpty()) {
            clipPath.reset();
            clipPath.addRoundRect(cardRect, cornerRadius, cornerRadius, Path.Direction.CW);
        }
        
        needsGlowUpdate = true;
        invalidate();
    }
    
    /**
     * Set the card background color
     * @param color Card background color
     */
    public void setCardBackgroundColor(int color) {
        this.cardColor = color;
        needsGlowUpdate = true;
        invalidate();
    }
    
    /**
     * Enable or disable glow animation
     * @param animate true to animate glow, false for static glow
     */
    public void setAnimateGlow(boolean animate) {
        this.animateGlow = animate;
        
        if (animate && !glowAnimator.isRunning()) {
            glowAnimator.start();
        } else if (!animate && glowAnimator.isRunning()) {
            glowAnimator.cancel();
            animatedGlowIntensity = glowIntensity;
            needsGlowUpdate = true;
            invalidate();
        }
    }
    
    /**
     * Check if glow is enabled
     * @return true if glow is enabled, false otherwise
     */
    public boolean isGlowEnabled() {
        return glowEnabled;
    }
    
    /**
     * Check if glow animation is enabled
     * @return true if glow animation is enabled, false otherwise
     */
    public boolean isAnimateGlow() {
        return animateGlow;
    }
    
    /**
     * Get current glow intensity
     * @return Current glow intensity (0f-1f)
     */
    public float getGlowIntensity() {
        return glowIntensity;
    }
    
    /**
     * Get current glow radius
     * @return Current glow radius in pixels
     */
    public float getGlowRadius() {
        return glowRadius;
    }
    
    /**
     * Get current corner radius
     * @return Current corner radius in pixels
     */
    public float getCornerRadius() {
        return cornerRadius;
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        
        if (animateGlow && !glowAnimator.isRunning()) {
            glowAnimator.start();
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        
        // Clean up animations and bitmaps
        if (glowAnimator.isRunning()) {
            glowAnimator.cancel();
        }
        
        if (glowBitmap != null && !glowBitmap.isRecycled()) {
            glowBitmap.recycle();
            glowBitmap = null;
        }
        
        glowCanvas = null;
    }
}