package com.example.android.smsforward;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import static android.R.attr.hideOnContentScroll;
import static android.R.attr.phoneNumber;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback{

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;

    private static MainActivity inst;
    private String mPhoneNumber;
    private String mForwardPhoneNumber = null;
    private String mMessage;
    private boolean forwardToSmsActive = false;
    String urlString = "https://api.telegram.org/bot489635845:AAGAy5DLIhfmv14QgLhQTAqgrYuvCAs5-MQ/sendMessage?chat_id=507477080&text=Hi+Daniel+test";

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            requestSMSPermission();
        }
    }

    public void triggerSend(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            requestSMSPermission();
        } else {
            EditText phoneNumber = (EditText) findViewById(R.id.phoneNo);
            EditText message = (EditText) findViewById(R.id.message);
            mPhoneNumber = "+91 " + phoneNumber.getText().toString();
            mMessage = message.getText().toString();
            try{
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(mPhoneNumber, null, mMessage, null, null);
                Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e) {
                Toast.makeText(this, "Message failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Send SMS permission granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Send SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void requestSMSPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.SEND_SMS},
                MY_PERMISSIONS_REQUEST_SEND_SMS);

    }

    public void forward(String message) {
        if (forwardToSmsActive) {
            Log.e("forward", message);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestSMSPermission();
            } else {
                mMessage = message;
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("+91 "+ mForwardPhoneNumber, null, mMessage, null, null);
                    Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Message failed!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        PostRequest task = new PostRequest();
        task.execute();
    }


    public void setPhoneNumber(View view) {
        EditText forwardNumber = (EditText) findViewById(R.id.forwardNo);
        mForwardPhoneNumber = forwardNumber.getText().toString();
        Log.e("setPhoneNumber", mForwardPhoneNumber);
    }

    public void setForwardToSmsActive(View view) {
        Button forwardActive = (Button) findViewById(R.id.forwardActivate);
        if(forwardToSmsActive == false && mForwardPhoneNumber != null) {
            forwardToSmsActive = true;
            forwardActive.setText("Deactivate Forward To SMS");
        }
        else {
            forwardToSmsActive = false;
            forwardActive.setText("Activate Forward To SMS");
        }
    }

    private class PostRequest extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            // Create URL object
            URL url = createUrl(urlString);

            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
            }
            catch (IOException e) {
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }

        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e("createUrl", "Error with creating URL", exception);
                return null;
            }
            return url;
        }
    }
}


