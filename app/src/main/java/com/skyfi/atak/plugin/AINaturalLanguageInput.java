package com.skyfi.atak.plugin;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Natural Language Interface component for AI-powered geospatial queries
 * Supports both voice and text input with autocomplete suggestions
 */
public class AINaturalLanguageInput extends LinearLayout {
    
    private static final String TAG = "AINaturalLanguageInput";
    
    // UI Components
    private AutoCompleteTextView textInput;
    private ImageButton voiceButton;
    private ProgressBar listeningIndicator;
    private TextView statusText;
    private LinearLayout suggestionsContainer;
    
    // Voice Recognition
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    private Handler mainHandler;
    
    // Autocomplete suggestions
    private ArrayAdapter<String> autocompleteAdapter;
    private List<String> predefinedQueries;
    
    // Callbacks
    private OnQuerySubmittedListener queryListener;
    private OnVoiceStateChangedListener voiceStateListener;
    
    public interface OnQuerySubmittedListener {
        void onQuerySubmitted(String query, boolean isVoiceInput);
    }
    
    public interface OnVoiceStateChangedListener {
        void onVoiceStateChanged(boolean isListening, String status);
    }
    
    public AINaturalLanguageInput(Context context) {
        super(context);
        init(context);
    }
    
    public AINaturalLanguageInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public AINaturalLanguageInput(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context) {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.ai_natural_language_input, this, true);
        
        mainHandler = new Handler(Looper.getMainLooper());
        
        initViews();
        setupAutoComplete();
        setupVoiceRecognition();
        setupListeners();
    }
    
    private void initViews() {
        textInput = findViewById(R.id.ai_text_input);
        voiceButton = findViewById(R.id.ai_voice_button);
        listeningIndicator = findViewById(R.id.ai_listening_indicator);
        statusText = findViewById(R.id.ai_status_text);
        suggestionsContainer = findViewById(R.id.ai_suggestions_container);
    }
    
    private void setupAutoComplete() {
        predefinedQueries = Arrays.asList(
            "Show me all vehicles within 500 meters",
            "Identify damaged buildings in this area",
            "Track movement patterns in the last 24 hours",
            "Find safe routes to extraction point",
            "Detect military vehicles in sector 7",
            "Analyze population density patterns",
            "Show threat probability in this region",
            "Find helicopter landing zones nearby",
            "Identify infrastructure damage",
            "Track convoy movements northbound",
            "Show weather forecast for next 6 hours",
            "Find buildings suitable for overwatch",
            "Analyze terrain for vehicle accessibility",
            "Detect changes since last imagery",
            "Show elevation profile for planned route"
        );
        
        autocompleteAdapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_dropdown_item_1line, predefinedQueries);
        textInput.setAdapter(autocompleteAdapter);
        textInput.setThreshold(3); // Start suggesting after 3 characters
        
        // Add quick suggestion buttons
        addQuickSuggestions();
    }
    
    private void addQuickSuggestions() {
        String[] quickSuggestions = {
            "Analyze Area",
            "Detect Threats", 
            "Track Movement",
            "Find Routes"
        };
        
        for (String suggestion : quickSuggestions) {
            TextView suggestionView = new TextView(getContext());
            suggestionView.setText(suggestion);
            suggestionView.setPadding(16, 8, 16, 8);
            suggestionView.setBackground(getContext().getDrawable(R.drawable.skyfi_button_secondary));
            suggestionView.setTextColor(0xFFFFFFFF);
            suggestionView.setClickable(true);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 4, 8, 4);
            suggestionView.setLayoutParams(params);
            
            suggestionView.setOnClickListener(v -> {
                String expandedQuery = expandQuickSuggestion(suggestion);
                textInput.setText(expandedQuery);
                submitQuery(expandedQuery, false);
            });
            
            suggestionsContainer.addView(suggestionView);
        }
    }
    
    private String expandQuickSuggestion(String suggestion) {
        switch (suggestion) {
            case "Analyze Area":
                return "Analyze the current map area for objects of interest";
            case "Detect Threats":
                return "Detect potential threats in the visible area";
            case "Track Movement":
                return "Track movement patterns in this region";
            case "Find Routes":
                return "Find optimal routes to the marked destination";
            default:
                return suggestion;
        }
    }
    
    private void setupVoiceRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(getContext())) {
            voiceButton.setVisibility(View.GONE);
            Log.w(TAG, "Speech recognition not available");
            return;
        }
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                updateVoiceStatus("Listening...", true);
            }
            
            @Override
            public void onBeginningOfSpeech() {
                updateVoiceStatus("Speaking detected", true);
            }
            
            @Override
            public void onRmsChanged(float rmsdB) {
                // Update visual indicator based on sound levels
                animateListeningIndicator(rmsdB);
            }
            
            @Override
            public void onBufferReceived(byte[] buffer) {}
            
            @Override
            public void onEndOfSpeech() {
                updateVoiceStatus("Processing...", true);
            }
            
            @Override
            public void onError(int error) {
                String errorMessage = getSpeechErrorMessage(error);
                updateVoiceStatus("Error: " + errorMessage, false);
                stopListening();
            }
            
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String recognizedText = matches.get(0);
                    textInput.setText(recognizedText);
                    updateVoiceStatus("Voice input received", false);
                    submitQuery(recognizedText, true);
                }
                stopListening();
            }
            
            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    textInput.setText(matches.get(0));
                }
            }
            
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }
    
    private void setupListeners() {
        // Voice button touch listener for press-and-hold functionality
        voiceButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startListening();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopListening();
                    return true;
            }
            return false;
        });
        
        // Text input listener
        textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear status when user starts typing
                if (s.length() > 0 && !isListening) {
                    statusText.setText("");
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Submit on enter key
        textInput.setOnEditorActionListener((v, actionId, event) -> {
            String query = textInput.getText().toString().trim();
            if (!query.isEmpty()) {
                submitQuery(query, false);
                return true;
            }
            return false;
        });
    }
    
    private void startListening() {
        if (checkMicrophonePermission() && !isListening) {
            isListening = true;
            
            Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            
            speechRecognizer.startListening(recognizerIntent);
            
            // Visual feedback
            voiceButton.setImageDrawable(getContext().getDrawable(R.drawable.ic_mic_recording));
            listeningIndicator.setVisibility(View.VISIBLE);
            startListeningAnimation();
        }
    }
    
    private void stopListening() {
        if (isListening) {
            isListening = false;
            speechRecognizer.stopListening();
            
            // Reset visual state
            voiceButton.setImageDrawable(getContext().getDrawable(R.drawable.ic_mic));
            listeningIndicator.setVisibility(View.GONE);
            stopListeningAnimation();
            
            if (voiceStateListener != null) {
                voiceStateListener.onVoiceStateChanged(false, "");
            }
        }
    }
    
    private void submitQuery(String query, boolean isVoiceInput) {
        if (query.trim().isEmpty()) return;
        
        // Clear text input after submission
        textInput.setText("");
        
        // Show processing status
        statusText.setText("Processing query...");
        statusText.setTextColor(0xFF2196F3);
        
        // Notify listener
        if (queryListener != null) {
            queryListener.onQuerySubmitted(query, isVoiceInput);
        }
        
        // Clear status after delay
        mainHandler.postDelayed(() -> statusText.setText(""), 3000);
    }
    
    private void updateVoiceStatus(String status, boolean isActive) {
        mainHandler.post(() -> {
            statusText.setText(status);
            statusText.setTextColor(isActive ? 0xFF4CAF50 : 0xFFFF5722);
            
            if (voiceStateListener != null) {
                voiceStateListener.onVoiceStateChanged(isActive, status);
            }
        });
    }
    
    private void startListeningAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(voiceButton, "scaleX", 1.0f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(voiceButton, "scaleY", 1.0f, 1.2f, 1.0f);
        
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        
        scaleX.start();
        scaleY.start();
    }
    
    private void stopListeningAnimation() {
        voiceButton.clearAnimation();
        voiceButton.setScaleX(1.0f);
        voiceButton.setScaleY(1.0f);
    }
    
    private void animateListeningIndicator(float rmsdB) {
        // Convert RMS dB to visual feedback (0-100)
        float normalizedLevel = Math.max(0, Math.min(100, (rmsdB + 2) * 10));
        listeningIndicator.setProgress((int) normalizedLevel);
    }
    
    private boolean checkMicrophonePermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
            
            Toast.makeText(getContext(), "Microphone permission required for voice input", 
                Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    
    private String getSpeechErrorMessage(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No speech input detected";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            default:
                return "Unknown error";
        }
    }
    
    public void setOnQuerySubmittedListener(OnQuerySubmittedListener listener) {
        this.queryListener = listener;
    }
    
    public void setOnVoiceStateChangedListener(OnVoiceStateChangedListener listener) {
        this.voiceStateListener = listener;
    }
    
    public void setQuery(String query) {
        textInput.setText(query);
    }
    
    public void clearInput() {
        textInput.setText("");
        statusText.setText("");
    }
    
    public boolean isListeningActive() {
        return isListening;
    }
    
    public void showProcessingStatus(String message) {
        statusText.setText(message);
        statusText.setTextColor(0xFF2196F3);
    }
    
    public void showSuccessStatus(String message) {
        statusText.setText(message);
        statusText.setTextColor(0xFF4CAF50);
    }
    
    public void showErrorStatus(String message) {
        statusText.setText(message);
        statusText.setTextColor(0xFFFF5722);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}