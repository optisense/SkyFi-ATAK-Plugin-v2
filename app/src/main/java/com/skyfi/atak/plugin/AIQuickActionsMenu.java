package com.skyfi.atak.plugin;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * AI Quick Actions Radial Menu for rapid AI analysis
 * Provides context-sensitive AI actions in a circular menu layout
 */
public class AIQuickActionsMenu extends FrameLayout {
    
    private static final String TAG = "AIQuickActionsMenu";
    
    // Menu configuration
    private static final int MAX_MENU_ITEMS = 8;
    private static final float MENU_RADIUS = 120f;
    private static final float ITEM_SIZE = 60f;
    private static final long ANIMATION_DURATION = 300;
    
    // UI Components
    private ImageView centerButton;
    private List<AIActionItem> menuItems;
    private List<AIActionView> actionViews;
    
    // State
    private boolean isMenuOpen = false;
    private boolean isAnimating = false;
    private float centerX, centerY;
    private GeoPoint targetLocation;
    private String selectedArea;
    
    // Listeners
    private OnAIActionSelectedListener actionListener;
    private OnMenuStateChangedListener stateListener;
    
    // Animation
    private AnimatorSet openAnimatorSet;
    private AnimatorSet closeAnimatorSet;
    
    public interface OnAIActionSelectedListener {
        void onActionSelected(AIActionType action, GeoPoint location, String areaId);
    }
    
    public interface OnMenuStateChangedListener {
        void onMenuStateChanged(boolean isOpen);
    }
    
    public enum AIActionType {
        ANALYZE_AREA("Analyze Area", "🔍", "#2196F3", "Perform comprehensive AI analysis of the selected area"),
        DETECT_THREATS("Detect Threats", "🚨", "#FF5722", "Identify potential threats and anomalies"),
        TRACK_MOVEMENT("Track Movement", "📍", "#4CAF50", "Monitor and predict movement patterns"),
        PREDICT_PATTERN("Predict Pattern", "📊", "#9C27B0", "Generate predictive analytics for the area"),
        FIND_ROUTES("Find Routes", "🗺️", "#FF9800", "Optimize routes and identify safe passages"),
        WEATHER_FORECAST("Weather Info", "🌦️", "#03A9F4", "Get AI-powered weather analysis"),
        GENERATE_REPORT("Generate Report", "📋", "#607D8B", "Create comprehensive intelligence report"),
        AI_CHAT("Ask AI", "💬", "#E91E63", "Start natural language conversation with AI");
        
        public final String title;
        public final String icon;
        public final String color;
        public final String description;
        
        AIActionType(String title, String icon, String color, String description) {
            this.title = title;
            this.icon = icon;
            this.color = color;
            this.description = description;
        }
    }
    
    public static class AIActionItem {
        public AIActionType type;
        public boolean isEnabled;
        public String customTitle;
        public int priority; // 1-10, lower is higher priority
        
        public AIActionItem(AIActionType type) {
            this.type = type;
            this.isEnabled = true;
            this.priority = 5;
        }
        
        public AIActionItem(AIActionType type, String customTitle, int priority) {
            this.type = type;
            this.customTitle = customTitle;
            this.isEnabled = true;
            this.priority = priority;
        }
    }
    
    public AIQuickActionsMenu(Context context) {
        super(context);
        init(context);
    }
    
    public AIQuickActionsMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public AIQuickActionsMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.ai_quick_actions_menu, this, true);
        
        menuItems = new ArrayList<>();
        actionViews = new ArrayList<>();
        
        initViews();
        setupDefaultActions();
        setupAnimations();
        setupTouchHandling();
    }
    
    private void initViews() {
        centerButton = findViewById(R.id.ai_center_button);
        
        centerButton.setOnClickListener(v -> {
            if (!isAnimating) {
                toggleMenu();
            }
        });
        
        // Set initial position
        post(() -> {
            centerX = getWidth() / 2f;
            centerY = getHeight() / 2f;
        });
    }
    
    private void setupDefaultActions() {
        // Add default AI actions in priority order
        addAction(new AIActionItem(AIActionType.ANALYZE_AREA, null, 1));
        addAction(new AIActionItem(AIActionType.DETECT_THREATS, null, 2));
        addAction(new AIActionItem(AIActionType.TRACK_MOVEMENT, null, 3));
        addAction(new AIActionItem(AIActionType.FIND_ROUTES, null, 4));
        addAction(new AIActionItem(AIActionType.PREDICT_PATTERN, null, 5));
        addAction(new AIActionItem(AIActionType.WEATHER_FORECAST, null, 6));
        addAction(new AIActionItem(AIActionType.AI_CHAT, null, 7));
        addAction(new AIActionItem(AIActionType.GENERATE_REPORT, null, 8));
    }
    
    private void setupAnimations() {
        openAnimatorSet = new AnimatorSet();
        closeAnimatorSet = new AnimatorSet();
    }
    
    private void setupTouchHandling() {
        GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (isMenuOpen) {
                    // Check if tap is outside menu area
                    float distance = (float) Math.sqrt(
                        Math.pow(e.getX() - centerX, 2) + Math.pow(e.getY() - centerY, 2)
                    );
                    
                    if (distance > MENU_RADIUS + ITEM_SIZE) {
                        closeMenu();
                        return true;
                    }
                }
                return false;
            }
        });
        
        setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }
    
    public void addAction(AIActionItem actionItem) {
        if (menuItems.size() >= MAX_MENU_ITEMS) {
            Log.w(TAG, "Maximum menu items reached, cannot add more actions");
            return;
        }
        
        menuItems.add(actionItem);
        createActionView(actionItem, menuItems.size() - 1);
    }
    
    private void createActionView(AIActionItem actionItem, int index) {
        AIActionView actionView = new AIActionView(getContext());
        actionView.setActionItem(actionItem);
        
        // Calculate position
        float angle = (float) (2 * Math.PI * index / menuItems.size()) - (float) Math.PI / 2;
        float x = centerX + MENU_RADIUS * (float) Math.cos(angle);
        float y = centerY + MENU_RADIUS * (float) Math.sin(angle);
        
        LayoutParams params = new LayoutParams((int) ITEM_SIZE, (int) ITEM_SIZE);
        params.leftMargin = (int) (x - ITEM_SIZE / 2);
        params.topMargin = (int) (y - ITEM_SIZE / 2);
        actionView.setLayoutParams(params);
        
        // Initially hidden and scaled down
        actionView.setAlpha(0f);
        actionView.setScaleX(0f);
        actionView.setScaleY(0f);
        actionView.setVisibility(View.INVISIBLE);
        
        actionView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onActionSelected(actionItem.type, targetLocation, selectedArea);
            }
            closeMenu();
        });
        
        addView(actionView);
        actionViews.add(actionView);
    }
    
    public void toggleMenu() {
        if (isMenuOpen) {
            closeMenu();
        } else {
            openMenu();
        }
    }
    
    public void openMenu() {
        if (isAnimating || isMenuOpen) return;
        
        isAnimating = true;
        isMenuOpen = true;
        
        // Show all action views
        for (AIActionView actionView : actionViews) {
            actionView.setVisibility(View.VISIBLE);
        }
        
        // Create open animations
        List<ObjectAnimator> animators = new ArrayList<>();
        
        // Center button rotation
        ObjectAnimator centerRotation = ObjectAnimator.ofFloat(centerButton, "rotation", 0f, 135f);
        centerRotation.setDuration(ANIMATION_DURATION);
        animators.add(centerRotation);
        
        // Action items appear with staggered timing
        for (int i = 0; i < actionViews.size(); i++) {
            AIActionView actionView = actionViews.get(i);
            
            ObjectAnimator alpha = ObjectAnimator.ofFloat(actionView, "alpha", 0f, 1f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(actionView, "scaleX", 0f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(actionView, "scaleY", 0f, 1f);
            
            long delay = i * 50; // Stagger by 50ms
            alpha.setStartDelay(delay);
            scaleX.setStartDelay(delay);
            scaleY.setStartDelay(delay);
            
            alpha.setDuration(ANIMATION_DURATION);
            scaleX.setDuration(ANIMATION_DURATION);
            scaleY.setDuration(ANIMATION_DURATION);
            
            scaleX.setInterpolator(new OvershootInterpolator(1.2f));
            scaleY.setInterpolator(new OvershootInterpolator(1.2f));
            
            animators.add(alpha);
            animators.add(scaleX);
            animators.add(scaleY);
        }
        
        openAnimatorSet = new AnimatorSet();
        openAnimatorSet.playTogether(animators);
        openAnimatorSet.addListener(new AnimatorSet.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {}
            
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isAnimating = false;
                if (stateListener != null) {
                    stateListener.onMenuStateChanged(true);
                }
            }
            
            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
                isAnimating = false;
            }
            
            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {}
        });
        
        openAnimatorSet.start();
    }
    
    public void closeMenu() {
        if (isAnimating || !isMenuOpen) return;
        
        isAnimating = true;
        isMenuOpen = false;
        
        // Create close animations
        List<ObjectAnimator> animators = new ArrayList<>();
        
        // Center button rotation back
        ObjectAnimator centerRotation = ObjectAnimator.ofFloat(centerButton, "rotation", 135f, 0f);
        centerRotation.setDuration(ANIMATION_DURATION);
        animators.add(centerRotation);
        
        // Action items disappear with reverse staggered timing
        for (int i = 0; i < actionViews.size(); i++) {
            AIActionView actionView = actionViews.get(i);
            
            ObjectAnimator alpha = ObjectAnimator.ofFloat(actionView, "alpha", 1f, 0f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(actionView, "scaleX", 1f, 0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(actionView, "scaleY", 1f, 0f);
            
            long delay = (actionViews.size() - i - 1) * 30; // Reverse stagger by 30ms
            alpha.setStartDelay(delay);
            scaleX.setStartDelay(delay);
            scaleY.setStartDelay(delay);
            
            alpha.setDuration(ANIMATION_DURATION - 100);
            scaleX.setDuration(ANIMATION_DURATION - 100);
            scaleY.setDuration(ANIMATION_DURATION - 100);
            
            scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
            
            animators.add(alpha);
            animators.add(scaleX);
            animators.add(scaleY);
        }
        
        closeAnimatorSet = new AnimatorSet();
        closeAnimatorSet.playTogether(animators);
        closeAnimatorSet.addListener(new AnimatorSet.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {}
            
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isAnimating = false;
                // Hide all action views
                for (AIActionView actionView : actionViews) {
                    actionView.setVisibility(View.INVISIBLE);
                }
                if (stateListener != null) {
                    stateListener.onMenuStateChanged(false);
                }
            }
            
            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
                isAnimating = false;
            }
            
            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {}
        });
        
        closeAnimatorSet.start();
    }
    
    public void setTargetLocation(GeoPoint location) {
        this.targetLocation = location;
    }
    
    public void setSelectedArea(String areaId) {
        this.selectedArea = areaId;
    }
    
    public void setOnAIActionSelectedListener(OnAIActionSelectedListener listener) {
        this.actionListener = listener;
    }
    
    public void setOnMenuStateChangedListener(OnMenuStateChangedListener listener) {
        this.stateListener = listener;
    }
    
    public boolean isMenuOpen() {
        return isMenuOpen;
    }
    
    public void removeAction(AIActionType actionType) {
        for (int i = 0; i < menuItems.size(); i++) {
            if (menuItems.get(i).type == actionType) {
                menuItems.remove(i);
                removeView(actionViews.get(i));
                actionViews.remove(i);
                repositionActionViews();
                break;
            }
        }
    }
    
    private void repositionActionViews() {
        for (int i = 0; i < actionViews.size(); i++) {
            AIActionView actionView = actionViews.get(i);
            
            float angle = (float) (2 * Math.PI * i / actionViews.size()) - (float) Math.PI / 2;
            float x = centerX + MENU_RADIUS * (float) Math.cos(angle);
            float y = centerY + MENU_RADIUS * (float) Math.sin(angle);
            
            LayoutParams params = (LayoutParams) actionView.getLayoutParams();
            params.leftMargin = (int) (x - ITEM_SIZE / 2);
            params.topMargin = (int) (y - ITEM_SIZE / 2);
            actionView.setLayoutParams(params);
        }
    }
    
    public void enableAction(AIActionType actionType, boolean enabled) {
        for (AIActionItem item : menuItems) {
            if (item.type == actionType) {
                item.isEnabled = enabled;
                break;
            }
        }
        
        for (AIActionView view : actionViews) {
            if (view.getActionItem().type == actionType) {
                view.setEnabled(enabled);
                view.setAlpha(enabled ? 1.0f : 0.5f);
                break;
            }
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (openAnimatorSet != null && openAnimatorSet.isRunning()) {
            openAnimatorSet.cancel();
        }
        if (closeAnimatorSet != null && closeAnimatorSet.isRunning()) {
            closeAnimatorSet.cancel();
        }
    }
    
    // Custom view for individual action items
    private static class AIActionView extends FrameLayout {
        private AIActionItem actionItem;
        private ImageView iconView;
        private TextView labelView;
        private Paint backgroundPaint;
        private RectF backgroundRect;
        
        public AIActionView(Context context) {
            super(context);
            init(context);
        }
        
        private void init(Context context) {
            LayoutInflater.from(context).inflate(R.layout.ai_action_item, this, true);
            
            iconView = findViewById(R.id.action_icon);
            labelView = findViewById(R.id.action_label);
            
            backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            backgroundRect = new RectF();
        }
        
        public void setActionItem(AIActionItem actionItem) {
            this.actionItem = actionItem;
            
            String title = actionItem.customTitle != null ? 
                          actionItem.customTitle : actionItem.type.title;
            
            iconView.setImageDrawable(getContext().getDrawable(R.drawable.ic_ai_action));
            labelView.setText(title);
            
            // Set background color based on action type
            backgroundPaint.setColor(android.graphics.Color.parseColor(actionItem.type.color));
            
            setContentDescription(actionItem.type.description);
        }
        
        public AIActionItem getActionItem() {
            return actionItem;
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            // Draw circular background
            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;
            float radius = Math.min(getWidth(), getHeight()) / 2f - 4f;
            
            canvas.drawCircle(centerX, centerY, radius, backgroundPaint);
            
            super.onDraw(canvas);
        }
    }
}