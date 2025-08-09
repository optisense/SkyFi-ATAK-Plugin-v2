package com.optisense.skyfi.atak.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

/**
 * A custom TextView that applies gradient colors to text with animation support.
 * Integrates with the SkyFi dark theme for modern UI effects.
 */
public class GradientTextView extends TextView {
    
    private static final int DEFAULT_ANIMATION_DURATION = 2000;
    
    private LinearGradient gradient;
    private Matrix gradientMatrix;
    private Paint textPaint;
    private ValueAnimator animator;
    
    private int[] gradientColors = {
        0xFFFFFFFF, // White (skyfi_primary)
        0xFF4A90E2, // Blue (skyfi_accent)
        0xFFB3B3B3, // Light gray (skyfi_text_secondary)
        0xFFFFFFFF  // Back to white
    };
    
    private float[] gradientPositions = {0f, 0.3f, 0.7f, 1f};
    private float animationOffset = 0f;
    private boolean isAnimating = false;
    
    public GradientTextView(Context context) {
        super(context);
        init();
    }
    
    public GradientTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public GradientTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        gradientMatrix = new Matrix();
        textPaint = getPaint();
        setupAnimator();
    }
    
    private void setupAnimator() {
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(DEFAULT_ANIMATION_DURATION);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animationOffset = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        if (w > 0) {
            gradient = new LinearGradient(
                -w, 0f, w * 2, 0f,
                gradientColors,
                gradientPositions,
                Shader.TileMode.CLAMP
            );
            textPaint.setShader(gradient);
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (gradient != null && isAnimating) {
            // Apply animation transformation
            float translateX = getWidth() * (animationOffset - 0.5f) * 2f;
            gradientMatrix.setTranslate(translateX, 0f);
            gradient.setLocalMatrix(gradientMatrix);
        }
        
        super.onDraw(canvas);
    }
    
    /**
     * Start the gradient animation
     */
    public void startAnimation() {
        if (!isAnimating) {
            isAnimating = true;
            animator.start();
        }
    }
    
    /**
     * Stop the gradient animation
     */
    public void stopAnimation() {
        if (isAnimating) {
            isAnimating = false;
            animator.cancel();
            animationOffset = 0f;
            
            // Reset gradient to default position
            if (gradient != null) {
                gradientMatrix.setTranslate(0f, 0f);
                gradient.setLocalMatrix(gradientMatrix);
            }
            invalidate();
        }
    }
    
    /**
     * Set custom gradient colors
     * @param colors Array of color values (minimum 2 colors)
     */
    public void setGradientColors(int[] colors) {
        if (colors != null && colors.length >= 2) {
            this.gradientColors = colors;
            
            // Auto-generate positions if not matching
            if (gradientPositions.length != colors.length) {
                gradientPositions = new float[colors.length];
                for (int i = 0; i < colors.length; i++) {
                    gradientPositions[i] = (float) i / (colors.length - 1);
                }
            }
            
            // Recreate gradient
            if (getWidth() > 0) {
                gradient = new LinearGradient(
                    -getWidth(), 0f, getWidth() * 2, 0f,
                    gradientColors,
                    gradientPositions,
                    Shader.TileMode.CLAMP
                );
                textPaint.setShader(gradient);
                invalidate();
            }
        }
    }
    
    /**
     * Set custom gradient colors with positions
     * @param colors Array of color values
     * @param positions Array of position values (0f to 1f)
     */
    public void setGradientColors(int[] colors, float[] positions) {
        if (colors != null && positions != null && 
            colors.length >= 2 && colors.length == positions.length) {
            this.gradientColors = colors;
            this.gradientPositions = positions;
            
            // Recreate gradient
            if (getWidth() > 0) {
                gradient = new LinearGradient(
                    -getWidth(), 0f, getWidth() * 2, 0f,
                    gradientColors,
                    gradientPositions,
                    Shader.TileMode.CLAMP
                );
                textPaint.setShader(gradient);
                invalidate();
            }
        }
    }
    
    /**
     * Set animation duration
     * @param duration Duration in milliseconds
     */
    public void setAnimationDuration(int duration) {
        animator.setDuration(duration);
    }
    
    /**
     * Check if gradient animation is running
     * @return true if animating, false otherwise
     */
    public boolean isAnimating() {
        return isAnimating;
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Auto-start animation if configured
        if (isAnimating && !animator.isRunning()) {
            animator.start();
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Clean up animation
        if (animator.isRunning()) {
            animator.cancel();
        }
    }
}