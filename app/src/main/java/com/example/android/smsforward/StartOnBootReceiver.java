package com.example.android.smsforward;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by DANIEL on 17-Dec-17.
 */


public class StartOnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent myIntent = new Intent(context, SmsService.class);
        context.startService(myIntent);
    }
}

