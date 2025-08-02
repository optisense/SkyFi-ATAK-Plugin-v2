package com.skyfi.atak.plugin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

/**
 * Dialog that prompts users to search in current region vs worldwide,
 * similar to Apple Maps' "Search in this region?" prompt.
 */
public class RegionPromptDialog {
    
    public interface RegionPromptCallback {
        void onRegionSelected();
        void onWorldSelected();
    }
    
    private final Context context;
    private final AORFilterManager aorFilterManager;
    
    public RegionPromptDialog(Context context) {
        this.context = context;
        this.aorFilterManager = AORFilterManager.getInstance(context);
    }
    
    /**
     * Show the region prompt dialog if it should be shown
     */
    public void showIfNeeded(RegionPromptCallback callback) {
        if (!aorFilterManager.shouldShowRegionPrompt()) {
            // User has chosen not to see this prompt
            return;
        }
        
        show(callback);
    }
    
    /**
     * Show the region prompt dialog
     */
    public void show(RegionPromptCallback callback) {
        // Create custom layout with checkbox
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.region_prompt_dialog, null);
        
        CheckBox dontAskAgainCheckBox = dialogView.findViewById(R.id.dont_ask_again_checkbox);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.search_in_region_title)
                .setMessage(R.string.search_in_region_message)
                .setView(dialogView)
                .setPositiveButton(R.string.search_in_region_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Set filter to region mode
                        aorFilterManager.setFilterMode(AORFilterManager.FilterMode.REGION);
                        
                        // Update "don't ask again" preference if checked
                        if (dontAskAgainCheckBox.isChecked()) {
                            aorFilterManager.setShowRegionPrompt(false);
                        }
                        
                        if (callback != null) {
                            callback.onRegionSelected();
                        }
                        
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.search_in_region_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Keep filter in world mode
                        aorFilterManager.setFilterMode(AORFilterManager.FilterMode.WORLD);
                        
                        // Update "don't ask again" preference if checked
                        if (dontAskAgainCheckBox.isChecked()) {
                            aorFilterManager.setShowRegionPrompt(false);
                        }
                        
                        if (callback != null) {
                            callback.onWorldSelected();
                        }
                        
                        dialog.dismiss();
                    }
                })
                .setCancelable(true);
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}