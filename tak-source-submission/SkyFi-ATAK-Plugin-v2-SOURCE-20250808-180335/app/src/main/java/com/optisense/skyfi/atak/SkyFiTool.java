package com.optisense.skyfi.atak;
import com.skyfi.atak.plugin.R;

import android.content.Context;
import com.atak.plugins.impl.AbstractPluginTool;
import gov.tak.api.util.Disposable;

/**
 * SkyFi Tool - Following Meshtastic pattern for tool registration
 */
public class SkyFiTool extends AbstractPluginTool implements Disposable {

    public SkyFiTool(Context context) {
        super(context,
                context.getString(R.string.app_name),
                context.getString(R.string.app_name),
                context.getResources().getDrawable(R.drawable.ic_launcher),
                "com.optisense.skyfi.atak.SHOW_PLUGIN");
        
        // Initialize plugin native loader if you have one
        PluginNativeLoader.init(context);
    }

    @Override
    public void dispose() {
        // Cleanup when tool is disposed
    }
}