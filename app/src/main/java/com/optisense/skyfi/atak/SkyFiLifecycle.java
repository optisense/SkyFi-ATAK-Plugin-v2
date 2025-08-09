package com.optisense.skyfi.atak;

import com.atak.plugins.impl.AbstractPlugin;
import com.atak.plugins.impl.PluginContextProvider;
import gov.tak.api.plugin.IServiceController;

/**
 * SkyFi Lifecycle - Main plugin entry point following Meshtastic pattern
 * This approach abstracts away IPlugin interface compatibility issues
 */
public class SkyFiLifecycle extends AbstractPlugin {
    
    public SkyFiLifecycle(IServiceController serviceController) {
        super(serviceController, 
              new SkyFiTool(serviceController.getService(PluginContextProvider.class).getPluginContext()), 
              new SkyFiMapComponent());
    }
}