
package com.atakmap.android.hellojni;

import android.content.Context;
import android.content.Intent;

import com.atakmap.android.hellojni.plugin.PluginNativeLoader;

import com.atakmap.android.maps.AbstractMapComponent;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.dropdown.DropDownMapComponent;

import com.atakmap.coremap.log.Log;
import com.atakmap.android.hellojni.plugin.R;

/**
 * This is an example of a MapComponent within the ATAK 
 * ecosphere.   A map component is the building block for all
 * activities within the system.   This defines a concrete 
 * thought or idea. 
 */
public class HelloJNIMapComponent extends AbstractMapComponent {

    public static final String TAG = "HelloJNIMapComponent";

    private Context pluginContext;

    @Override
    public void onStart(final Context context, final MapView view) {
        Log.d(TAG, "onStart");
    }

    @Override
    public void onPause(final Context context, final MapView view) {
        Log.d(TAG, "onPause");
    }

    @Override
    public void onResume(final Context context,
            final MapView view) {
        Log.d(TAG, "onResume");
    }

    @Override
    public void onStop(final Context context,
            final MapView view) {
        Log.d(TAG, "onStop");
    }

    public void onCreate(final Context context, Intent intent,
            final MapView view) {

        // Set the theme.  Otherwise, the plugin will look vastly different
        // than the main ATAK experience.   The theme needs to be set 
        // programatically because the AndroidManifest.xml is not used.
        context.setTheme(R.style.ATAKPluginTheme);

        pluginContext = context;

        // The MapComponent serves as the primary entry point for the plugin, load
        // the JNI library here
        PluginNativeLoader.init(pluginContext);

        // load the JNI library. Note that if the library has one or more
        // dependencies, those dependencies must be explicitly loaded in
        // correct order. Android will not automatically load dependencies
        // even if they are on the system library path
        PluginNativeLoader.loadLibrary("hellojni");
    }


    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        Log.d(TAG, "calling on destroy");

    }
}
