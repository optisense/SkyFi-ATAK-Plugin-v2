package com.skyfi.atak.plugin;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atakmap.coremap.log.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Tactical AI Assistant overlay that provides contextual recommendations and alerts
 * Displays as a floating, draggable overlay on the map with priority intel updates
 */
public class AIAssistantView extends LinearLayout {
    
    private static final String TAG = "AIAssistantView";
    
    // UI Components
    private TextView headerTitle;
    private TextView lastUpdateTime;
    private RecyclerView recommendationsRecycler;
    private LinearLayout compactView;
    private LinearLayout expandedView;
    private ImageView expandCollapseIcon;
    private TextView priorityCount;
    private ImageView aiStatusIndicator;
    
    // State
    private boolean isExpanded = false;
    private boolean isDragging = false;
    private float initialX, initialY;
    private float initialTouchX, initialTouchY;
    
    // Data
    private List<AIRecommendation> recommendations;
    private RecommendationsAdapter adapter;
    private Handler updateHandler;
    
    // Listeners
    private OnRecommendationActionListener actionListener;
    private OnAssistantStateChangedListener stateListener;
    private GestureDetector gestureDetector;
    
    // Animation
    private ObjectAnimator pulseAnimator;
    
    public interface OnRecommendationActionListener {
        void onRecommendationAction(AIRecommendation recommendation, String action);
    }
    
    public interface OnAssistantStateChangedListener {
        void onStateChanged(boolean expanded, boolean visible);
    }
    
    public static class AIRecommendation {
        public String id;
        public String title;
        public String description;
        public RecommendationType type;
        public int priority; // 1-5, 1 being highest
        public long timestamp;
        public double confidence;
        public String[] actions;
        public Object data; // Additional data for the recommendation
        
        public enum RecommendationType {
            THREAT_DETECTION("🚨", "#FF5722"),
            INTELLIGENCE_UPDATE("📊", "#2196F3"),
            ROUTE_SUGGESTION("🗺️", "#4CAF50"),
            WEATHER_ALERT("🌦️", "#FF9800"),
            TACTICAL_INSIGHT("🎯", "#9C27B0"),
            SYSTEM_NOTIFICATION("ℹ️", "#607D8B");
            
            public final String icon;
            public final String color;
            
            RecommendationType(String icon, String color) {
                this.icon = icon;
                this.color = color;
            }
        }
        
        public AIRecommendation(String id, String title, String description, 
                              RecommendationType type, int priority, String... actions) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.type = type;
            this.priority = priority;
            this.timestamp = System.currentTimeMillis();
            this.confidence = 0.85; // Default confidence
            this.actions = actions;
        }
    }
    
    public AIAssistantView(Context context) {
        super(context);
        init(context);
    }
    
    public AIAssistantView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public AIAssistantView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context) {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.ai_assistant_view, this, true);
        
        recommendations = new ArrayList<>();
        updateHandler = new Handler(Looper.getMainLooper());
        
        initViews();
        setupRecyclerView();
        setupGestureDetector();
        setupAnimations();
        
        // Initialize with sample data
        loadSampleRecommendations();
        
        // Start periodic updates
        startPeriodicUpdates();
    }
    
    private void initViews() {
        headerTitle = findViewById(R.id.ai_assistant_header_title);
        lastUpdateTime = findViewById(R.id.ai_last_update_time);
        recommendationsRecycler = findViewById(R.id.ai_recommendations_recycler);
        compactView = findViewById(R.id.ai_compact_view);
        expandedView = findViewById(R.id.ai_expanded_view);
        expandCollapseIcon = findViewById(R.id.ai_expand_collapse_icon);
        priorityCount = findViewById(R.id.ai_priority_count);
        aiStatusIndicator = findViewById(R.id.ai_status_indicator);
        
        // Set up expand/collapse functionality
        compactView.setOnClickListener(v -> toggleExpanded());
        
        // Set up header click for collapse when expanded
        headerTitle.setOnClickListener(v -> {
            if (isExpanded) toggleExpanded();
        });
        
        updateLastUpdateTime();
    }
    
    private void setupRecyclerView() {
        adapter = new RecommendationsAdapter();
        recommendationsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recommendationsRecycler.setAdapter(adapter);
    }
    
    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
            
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (!isExpanded) {
                    toggleExpanded();
                    return true;
                }
                return false;
            }
        });
    }
    
    private void setupAnimations() {
        // Pulse animation for high priority alerts
        pulseAnimator = ObjectAnimator.ofFloat(aiStatusIndicator, "alpha", 1.0f, 0.3f, 1.0f);
        pulseAnimator.setDuration(1500);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                initialX = getX();
                initialY = getY();
                isDragging = false;
                return true;
                
            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getRawX() - initialTouchX;
                float deltaY = event.getRawY() - initialTouchY;
                
                // Start dragging if moved enough
                if (!isDragging && (Math.abs(deltaX) > 20 || Math.abs(deltaY) > 20)) {
                    isDragging = true;
                }
                
                if (isDragging) {
                    setX(initialX + deltaX);
                    setY(initialY + deltaY);
                }
                return true;
                
            case MotionEvent.ACTION_UP:
                if (isDragging) {
                    // Snap to edges if dragged
                    snapToEdge();
                    isDragging = false;
                }
                return true;
                
            default:
                return super.onTouchEvent(event);
        }
    }
    
    private void snapToEdge() {
        ViewGroup parent = (ViewGroup) getParent();
        if (parent == null) return;
        
        float centerX = getX() + getWidth() / 2f;
        float parentWidth = parent.getWidth();
        
        ObjectAnimator animator;
        if (centerX < parentWidth / 2) {
            // Snap to left edge
            animator = ObjectAnimator.ofFloat(this, "x", getX(), 16f);
        } else {
            // Snap to right edge
            animator = ObjectAnimator.ofFloat(this, "x", getX(), parentWidth - getWidth() - 16f);
        }
        
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }
    
    private void toggleExpanded() {
        isExpanded = !isExpanded;
        
        if (isExpanded) {
            expandedView.setVisibility(View.VISIBLE);
            compactView.setVisibility(View.GONE);
            expandCollapseIcon.setRotation(180f);
        } else {
            expandedView.setVisibility(View.GONE);
            compactView.setVisibility(View.VISIBLE);
            expandCollapseIcon.setRotation(0f);
        }
        
        // Animate the transition
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0.95f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0.95f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "alpha", 0.8f, 1.0f);
        
        scaleX.setDuration(200);
        scaleY.setDuration(200);
        alpha.setDuration(200);
        
        scaleX.start();
        scaleY.start();
        alpha.start();
        
        if (stateListener != null) {
            stateListener.onStateChanged(isExpanded, getVisibility() == View.VISIBLE);
        }
    }
    
    private void loadSampleRecommendations() {
        recommendations.clear();
        
        // High priority threat detection
        recommendations.add(new AIRecommendation(
            "threat_001",
            "Vehicle convoy detected",
            "3 military vehicles moving northeast at 2.1km (87% confidence)",
            AIRecommendation.RecommendationType.THREAT_DETECTION,
            1,
            "View", "Track", "Share"
        ));
        
        // Route suggestion
        recommendations.add(new AIRecommendation(
            "route_001", 
            "Route Alpha compromised",
            "Alternative route suggested via checkpoint Bravo",
            AIRecommendation.RecommendationType.ROUTE_SUGGESTION,
            2,
            "Alt Route", "Risk Assessment"
        ));
        
        // Weather alert
        recommendations.add(new AIRecommendation(
            "weather_001",
            "Weather window closing",
            "Visibility dropping to 2km in next 2 hours",
            AIRecommendation.RecommendationType.WEATHER_ALERT,
            2,
            "Forecast", "Adjust Timeline"
        ));
        
        // Intelligence update
        recommendations.add(new AIRecommendation(
            "intel_001",
            "Population movement detected",
            "Unusual population density shift in sector 4",
            AIRecommendation.RecommendationType.INTELLIGENCE_UPDATE,
            3,
            "Analyze", "Monitor"
        ));
        
        updatePriorityCount();
        adapter.notifyDataSetChanged();
    }
    
    private void updatePriorityCount() {
        int highPriorityCount = 0;
        for (AIRecommendation recommendation : recommendations) {
            if (recommendation.priority <= 2) {
                highPriorityCount++;
            }
        }
        
        priorityCount.setText(String.valueOf(highPriorityCount));
        
        // Start pulse animation for high priority items
        if (highPriorityCount > 0) {
            if (!pulseAnimator.isRunning()) {
                pulseAnimator.start();
            }
        } else {
            pulseAnimator.cancel();
            aiStatusIndicator.setAlpha(1.0f);
        }
    }
    
    private void updateLastUpdateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        lastUpdateTime.setText("Updated: " + sdf.format(new Date()));
    }
    
    private void startPeriodicUpdates() {
        updateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLastUpdateTime();
                // Here you would typically fetch new recommendations from AI service
                updateHandler.postDelayed(this, 30000); // Update every 30 seconds
            }
        }, 30000);
    }
    
    public void addRecommendation(AIRecommendation recommendation) {
        recommendations.add(0, recommendation); // Add to top
        updatePriorityCount();
        adapter.notifyItemInserted(0);
        
        // Auto-expand for high priority items
        if (recommendation.priority == 1 && !isExpanded) {
            toggleExpanded();
        }
    }
    
    public void removeRecommendation(String recommendationId) {
        for (int i = 0; i < recommendations.size(); i++) {
            if (recommendations.get(i).id.equals(recommendationId)) {
                recommendations.remove(i);
                adapter.notifyItemRemoved(i);
                updatePriorityCount();
                break;
            }
        }
    }
    
    public void clearRecommendations() {
        recommendations.clear();
        adapter.notifyDataSetChanged();
        updatePriorityCount();
    }
    
    public void setOnRecommendationActionListener(OnRecommendationActionListener listener) {
        this.actionListener = listener;
    }
    
    public void setOnAssistantStateChangedListener(OnAssistantStateChangedListener listener) {
        this.stateListener = listener;
    }
    
    public boolean isExpanded() {
        return isExpanded;
    }
    
    public int getRecommendationCount() {
        return recommendations.size();
    }
    
    public List<AIRecommendation> getHighPriorityRecommendations() {
        List<AIRecommendation> highPriority = new ArrayList<>();
        for (AIRecommendation rec : recommendations) {
            if (rec.priority <= 2) {
                highPriority.add(rec);
            }
        }
        return highPriority;
    }
    
    // RecyclerView Adapter for recommendations
    private class RecommendationsAdapter extends RecyclerView.Adapter<RecommendationsAdapter.RecommendationViewHolder> {
        
        @Override
        public RecommendationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ai_recommendation_item, parent, false);
            return new RecommendationViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(RecommendationViewHolder holder, int position) {
            AIRecommendation recommendation = recommendations.get(position);
            holder.bind(recommendation);
        }
        
        @Override
        public int getItemCount() {
            return recommendations.size();
        }
        
        class RecommendationViewHolder extends RecyclerView.ViewHolder {
            TextView typeIcon;
            TextView title;
            TextView description;
            TextView timestamp;
            TextView confidence;
            LinearLayout actionsContainer;
            View priorityIndicator;
            
            RecommendationViewHolder(View itemView) {
                super(itemView);
                typeIcon = itemView.findViewById(R.id.recommendation_type_icon);
                title = itemView.findViewById(R.id.recommendation_title);
                description = itemView.findViewById(R.id.recommendation_description);
                timestamp = itemView.findViewById(R.id.recommendation_timestamp);
                confidence = itemView.findViewById(R.id.recommendation_confidence);
                actionsContainer = itemView.findViewById(R.id.recommendation_actions);
                priorityIndicator = itemView.findViewById(R.id.recommendation_priority_indicator);
            }
            
            void bind(AIRecommendation recommendation) {
                typeIcon.setText(recommendation.type.icon);
                title.setText(recommendation.title);
                description.setText(recommendation.description);
                
                // Format timestamp
                long timeDiff = System.currentTimeMillis() - recommendation.timestamp;
                String timeText;
                if (timeDiff < 60000) {
                    timeText = "Just now";
                } else if (timeDiff < 3600000) {
                    timeText = (timeDiff / 60000) + "m ago";
                } else {
                    timeText = (timeDiff / 3600000) + "h ago";
                }
                timestamp.setText(timeText);
                
                confidence.setText(String.format("%.0f%%", recommendation.confidence * 100));
                
                // Set priority indicator color
                int priorityColor;
                switch (recommendation.priority) {
                    case 1:
                        priorityColor = 0xFFFF5722; // Red
                        break;
                    case 2:
                        priorityColor = 0xFFFF9800; // Orange
                        break;
                    case 3:
                        priorityColor = 0xFFFFC107; // Yellow
                        break;
                    default:
                        priorityColor = 0xFF4CAF50; // Green
                }
                priorityIndicator.setBackgroundColor(priorityColor);
                
                // Set up action buttons
                actionsContainer.removeAllViews();
                if (recommendation.actions != null) {
                    for (String action : recommendation.actions) {
                        Button actionButton = new Button(getContext());
                        actionButton.setText(action);
                        actionButton.setTextSize(10);
                        actionButton.setPadding(16, 8, 16, 8);
                        actionButton.setBackground(getContext().getDrawable(R.drawable.skyfi_button_secondary));
                        actionButton.setTextColor(0xFFFFFFFF);
                        
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(4, 0, 4, 0);
                        actionButton.setLayoutParams(params);
                        
                        actionButton.setOnClickListener(v -> {
                            if (actionListener != null) {
                                actionListener.onRecommendationAction(recommendation, action);
                            }
                        });
                        
                        actionsContainer.addView(actionButton);
                    }
                }
            }
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (updateHandler != null) {
            updateHandler.removeCallbacksAndMessages(null);
        }
        if (pulseAnimator != null && pulseAnimator.isRunning()) {
            pulseAnimator.cancel();
        }
    }
}