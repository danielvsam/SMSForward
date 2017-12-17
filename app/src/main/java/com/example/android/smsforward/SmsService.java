package com.example.android.smsforward;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by DANIEL on 06-Dec-17.
 */


public class SmsService extends Service {

    public BroadcastReceiver smsReceiver;
    public String finalMessage;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.e("SMSService", "Service started");
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
//        smsReceiver = new IncomingSms();
//        registerReceiver(smsReceiver, filter);
    }

    @Override
    public void onDestroy() {
        Log.e("SMSService", "Service stopped");
        unregisterReceiver(smsReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("SMSService", "onStartCommand");
        smsReceiver = new IncomingSms();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, filter);

        Notification smsNotify = new Notification.Builder(this)
                .setContentTitle("SMSForward")
                .setContentText("Forwarding active").build();
        startForeground(100, smsNotify);   //random id
//        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("SMSService", "onTaskRemoved");
        super.onTaskRemoved(rootIntent);
        Intent service = new Intent(this, SmsService.class);
        this.startService(service);
    }

    private class IncomingSms extends BroadcastReceiver {

        public IncomingSms() {
            Log.e("IncomingSms", "IncomingSms Created");
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            Toast.makeText(context, "Message Received!", Toast.LENGTH_SHORT).show();
            Log.e("IncomingSms", "INCOMING SMS");
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

                Log.e("IncomingSms", str);
                finalMessage = str;
                PostTask task = new PostTask();
                task.execute();
            }

        }
    }

    private class PostTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... urls) {
            String messageToSend = finalMessage.replace(" ","+");
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL("https://api.telegram.org/bot489635845:AAGAy5DLIhfmv14QgLhQTAqgrYuvCAs5-MQ/sendMessage?chat_id=507477080&text=" + messageToSend);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                urlConnection.getResponseCode();
            } catch (IOException e) {
                // TODO Handle the IOException
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }
    }
}

