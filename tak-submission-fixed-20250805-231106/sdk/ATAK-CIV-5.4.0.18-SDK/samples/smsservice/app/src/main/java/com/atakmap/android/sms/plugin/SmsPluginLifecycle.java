
package com.atakmap.android.sensortest.plugin;


import com.atak.plugins.impl.AbstractPlugin;
import gov.tak.api.plugin.IServiceController;
import com.atak.plugins.impl.PluginContextProvider;
import com.atakmap.android.sms.SmsMapComponent;


/**
 *
 * AbstractPluginLifeCycle shipped with
 *     the plugin.
 */
public class SmsPluginLifecycle extends AbstractPlugin {

    private final static String TAG = "SmsPluginLifecycle";

    public SmsPluginLifecycle(IServiceController serviceController) {
        super(serviceController, new SmsMapComponent());
    }

}
