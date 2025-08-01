
package com.atakmap.android.sms;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.atakmap.android.maps.AbstractMapComponent;
import com.atakmap.android.maps.MapView;

import com.atakmap.android.sms.service.ILogger;
import com.atakmap.android.sms.service.ISmsCallback;
import com.atakmap.android.sms.service.ISmsManager;
import com.atakmap.android.sms.service.SmsManagerService;
import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.log.Log;
import com.atakmap.android.sms.plugin.R;


/**
 * Since this is an example, I will provide a drop down or other user experience in the
 * user interface
 */
public class SmsMapComponent extends AbstractMapComponent {

    private static final String TAG = "SmsMapComponent";

    private Context pluginContext;
    private MapView view;

    private ISmsManager service;

    public void onCreate(final Context context, Intent intent,
            final MapView view) {

        context.setTheme(R.style.ATAKPluginTheme);
        pluginContext = context;
        this.view = view;

        // kick off the service which is running in its own process space and governed by the
        // permissions in the plugins AndroidManifest.xml
        final Intent serviceIntent = new Intent(pluginContext, SmsManagerService.class);
        view.getContext().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        view.getContext().unbindService(connection);
    }

    private final ILogger logger = new ILogger.Stub() {
        @Override
        public void e(String tag, String msg, String exception) {
            if (!FileSystemUtils.isEmpty(exception))
                msg = msg + "\n" + exception;
            Log.e(tag, msg);
        }

        @Override
        public void d(String tag, String msg, String exception) {
            if (!FileSystemUtils.isEmpty(exception))
                msg = msg + "\n" + exception;
            Log.d(tag, msg);
        }
    };

    private final ISmsCallback iSmsCallback = new ISmsCallback.Stub() {
        @Override
        public void receivedSms(String source, String message) throws RemoteException {
            Log.d(TAG, source + " " + message);
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {

            service = ISmsManager.Stub.asInterface(iBinder);
            Log.d(TAG, "connected to the sensor service");
            try {
                // register a logger so that logging text from the service can be added to the
                // ATAK logging subsystem.
                service.registerLogger(logger);
            } catch (RemoteException re) {
                Log.d(TAG, "error registering the remote logging capability", re);
            }

            try {
                // register the remove event listener
                service.registerReceiver(iSmsCallback);
            } catch (RemoteException re) {
                Log.d(TAG, "error registering the remote sensor listener", re);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "disconnected from the sensor service");
        }

    };




}
