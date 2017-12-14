package com.example.android.smsforward;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Created by DANIEL on 06-Dec-17.
 */

public class IncomingSms extends BroadcastReceiver {
    private String TAG = IncomingSms.class.getSimpleName();

    public IncomingSms() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "INCOMING SMS");
        // Get the data (SMS data) bound to intent
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String str = "";

        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];

            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                str += "SMS from " + msgs[i].getOriginatingAddress() + " : ";
                str += msgs[i].getMessageBody();
                str += "\n";
            }

            Log.e(TAG, str);
            MainActivity inst = MainActivity.instance();
            inst.forward(str);
        }
    }
}

