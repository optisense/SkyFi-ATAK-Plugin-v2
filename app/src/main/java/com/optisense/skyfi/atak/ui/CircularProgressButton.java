package com.optisense.skyfi.atak.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;

/**
 * A custom Button that transforms into a circular progress indicator during loading states.
 * Designed to work seamlessly with the SkyFi dark theme.
 */
public class CircularProgressButton extends Button {
    
    private static final int ANIMATION_DURATION = 300;
    private static final int PROGRESS_ANIMATION_DURATION = 1200;
    private static final float PROGRESS_STROKE_WIDTH = 4f;
    
    // SkyFi theme colors
    private int buttonBackgroundColor = 0xFF4A90E2; // skyfi_accent
    private int progressColor = 0xFFFFFFFF;         // skyfi_primary
    private int progressBackgroundColor = 0x334A90E2; // Transparent accent
    
    private Paint progressPaint;
    private Paint progressBackgroundPaint;
    private RectF progressRect;
    
    private boolean isLoading = false;
    private float progress = 0f;
    private float progressAngle = 0f;
    private int originalWidth, originalHeight;
    private String originalText;
    
    private ValueAnimator progressAnimator;
    private ValueAnimator transformAnimator;
    private AnimatorSet loadingAnimatorSet;
    
    public CircularProgressButton(Context context) {
        super(context);
        init();
    }
    
    public CircularProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CircularProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setupPaints();
        setupAnimations();
        
        // Apply SkyFi theme styling
        setBackgroundColor(buttonBackgroundColor);
        setTextColor(0xFFFFFFFF); // skyfi_text_primary
        
        progressRect = new RectF();
        originalText = getText() != null ? getText().toString() : "";
    }
    
    private void setupPaints() {
        // Progress circle paint
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(PROGRESS_STROKE_WIDTH);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(progressColor);
        
        // Progress background paint
        progressBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressBackgroundPaint.setStyle(Paint.Style.STROKE);
        progressBackgroundPaint.setStrokeWidth(PROGRESS_STROKE_WIDTH);
        progressBackgroundPaint.setColor(progressBackgroundColor);
    }
    
    private void setupAnimations() {
        // Continuous rotation animation for indeterminate progress
        progressAnimator = ValueAnimator.ofFloat(0f, 360f);
        progressAnimator.setDuration(PROGRESS_ANIMATION_DURATION);
        progressAnimator.setRepeatCount(ValueAnimator.INFINITE);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progressAngle = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        if (originalWidth == 0) {
            originalWidth = w;
            originalHeight = h;
        }
        
        updateProgressRect();
    }
    
    private void updateProgressRect() {
        int size = Math.min(getWidth(), getHeight());
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = (size - (int) PROGRESS_STROKE_WIDTH * 2) / 2;
        
        progressRect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        );
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (isLoading) {
            drawProgress(canvas);
        }
    }
    
    private void drawProgress(Canvas canvas) {
        updateProgressRect();
        
        // Draw progress background circle
        canvas.drawCircle(
            progressRect.centerX(),
            progressRect.centerY(),
            progressRect.width() / 2,
            progressBackgroundPaint
        );
        
        if (progress > 0) {
            // Determinate progress
            float sweepAngle = 360f * progress;
            canvas.drawArc(progressRect, -90f, sweepAngle, false, progressPaint);
        } else {
            // Indeterminate progress with rotating arc
            float startAngle = progressAngle - 90f;
            float sweepAngle = 120f;
            canvas.drawArc(progressRect, startAngle, sweepAngle, false, progressPaint);
        }
    }
    
    /**
     * Start loading state with indeterminate progress
     */
    public void startLoading() {
        startLoading(-1f);
    }
    
    /**
     * Start loading state
     * @param initialProgress Initial progress value (0f-1f) or -1f for indeterminate
     */
    public void startLoading(float initialProgress) {
        if (isLoading) return;
        
        isLoading = true;
        progress = Math.max(-1f, Math.min(1f, initialProgress));
        
        // Hide text and transform button
        originalText = getText() != null ? getText().toString() : "";
        setText("");
        setEnabled(false);
        
        // Animate transformation to circular shape
        animateToCircular();
        
        // Start progress animation if indeterminate
        if (progress < 0) {
            progressAnimator.start();
        } else {
            invalidate();
        }
    }
    
    /**
     * Stop loading state and restore original button
     */
    public void stopLoading() {
        if (!isLoading) return;
        
        isLoading = false;
        progress = 0f;
        
        // Stop animations
        if (progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }
        if (transformAnimator != null && transformAnimator.isRunning()) {
            transformAnimator.cancel();
        }
        
        // Animate back to original shape
        animateToOriginal();
    }
    
    /**
     * Update progress for determinate loading
     * @param progress Progress value between 0f and 1f
     */
    public void setProgress(float progress) {
        if (!isLoading) return;
        
        this.progress = Math.max(0f, Math.min(1f, progress));
        
        // Switch to determinate mode
        if (progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }
        
        invalidate();
    }
    
    private void animateToCircular() {
        int targetSize = Math.min(originalWidth, originalHeight);
        
        ObjectAnimator widthAnimator = ObjectAnimator.ofInt(this, "width", getWidth(), targetSize);
        ObjectAnimator heightAnimator = ObjectAnimator.ofInt(this, "height", getHeight(), targetSize);
        
        transformAnimator = ValueAnimator.ofFloat(0f, 1f);
        transformAnimator.setDuration(ANIMATION_DURATION);
        transformAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        transformAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = (Float) animation.getAnimatedValue();
                
                // Animate size transformation
                int currentWidth = (int) (originalWidth + (Math.min(originalWidth, originalHeight) - originalWidth) * fraction);
                int currentHeight = (int) (originalHeight + (Math.min(originalWidth, originalHeight) - originalHeight) * fraction);
                
                getLayoutParams().width = currentWidth;
                getLayoutParams().height = currentHeight;
                requestLayout();
                
                invalidate();
            }
        });
        
        transformAnimator.start();
    }
    
    private void animateToOriginal() {
        if (transformAnimator != null && transformAnimator.isRunning()) {
            transformAnimator.cancel();
        }
        
        transformAnimator = ValueAnimator.ofFloat(1f, 0f);
        transformAnimator.setDuration(ANIMATION_DURATION);
        transformAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        transformAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = (Float) animation.getAnimatedValue();
                
                // Animate size transformation back to original
                int currentWidth = (int) (originalWidth + (Math.min(originalWidth, originalHeight) - originalWidth) * fraction);
                int currentHeight = (int) (originalHeight + (Math.min(originalWidth, originalHeight) - originalHeight) * fraction);
                
                getLayoutParams().width = currentWidth;
                getLayoutParams().height = currentHeight;
                requestLayout();
                
                // Restore text when almost complete
                if (fraction < 0.3f && getText().toString().isEmpty()) {
                    setText(originalText);
                    setEnabled(true);
                }
                
                invalidate();
            }
        });
        
        transformAnimator.start();
    }
    
    /**
     * Check if button is in loading state
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return isLoading;
    }
    
    /**
     * Get current progress value
     * @return Progress value (0f-1f) or -1f for indeterminate
     */
    public float getProgress() {
        return progress;
    }
    
    /**
     * Set custom progress color
     * @param color Progress indicator color
     */
    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }
    
    /**
     * Set custom progress background color
     * @param color Progress background color
     */
    public void setProgressBackgroundColor(int color) {
        this.progressBackgroundColor = color;
        progressBackgroundPaint.setColor(color);
        invalidate();
    }
    
    /**
     * Set custom button background color
     * @param color Button background color
     */
    public void setButtonBackgroundColor(int color) {
        this.buttonBackgroundColor = color;
        setBackgroundColor(color);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        
        // Clean up animations
        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }
        if (transformAnimator != null && transformAnimator.isRunning()) {
            transformAnimator.cancel();
        }
    }
}