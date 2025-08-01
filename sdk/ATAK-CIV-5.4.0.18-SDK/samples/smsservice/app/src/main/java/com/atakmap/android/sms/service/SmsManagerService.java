package com.atakmap.android.sms.service;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import androidx.annotation.Nullable;

/**
 * This service runs completely devoid of all ATAK supplied classes.   It also inherits the
 * permissions from the AndroidManifest because it is a service.
 */
public class SmsManagerService extends Service {

    private static final String TAG = "SmsManagerService";

    private ILogger log;
    private ISmsCallback callback;
    private SmsReceiver smsReceiver = new SmsReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        intentFilter.setPriority(2147483647);
        registerReceiver(smsReceiver,intentFilter);

        if (checkSelfPermission( Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Intent i = new Intent(this, PermissionActivity.class);
            i.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsReceiver);
    }


    /**
     * The remote implementation of the Sms Manager with contains the registration of the logger
     * as well as the registration and of the sms receiver.
     */
    private final ISmsManager.Stub mBinder = new ISmsManager.Stub() {
        @Override
        public void registerLogger(final ILogger log) throws RemoteException {
            SmsManagerService.this.log = log;
            smsReceiver.register(log);
        }

        @Override
        public void registerReceiver(ISmsCallback smsCallback) throws RemoteException {
            SmsManagerService.this.callback = smsCallback;
            smsReceiver.register(callback);
        }

        @Override
        public void sendSMS(String destination, String message) throws RemoteException {
            final SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(destination, null, message, null, null);
            if (log != null) {
                log.d(TAG, "sent the sms", "");
            }
        }
    };

    ;
}
