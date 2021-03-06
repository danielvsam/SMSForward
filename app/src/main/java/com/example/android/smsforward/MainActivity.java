package com.example.android.smsforward;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback{

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 1;
    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 2;

    private static MainActivity inst;
    private String mPhoneNumber;
    private String mForwardPhoneNumber = null;
    private String mMessage;
    private boolean forwardToSmsActive = false;
    String mPostUrl = "https://api.telegram.org/bot{botToken}/sendMessage?chat_id={chatId}&text=";
    String botToken;
    String chatId;

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    public void MainActivity(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("MainActivity", "Created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            requestSMSPermission();
        }
        Intent service = new Intent(this, SmsService.class);
        this.startService(service);
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
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_SMS},
                MY_PERMISSIONS_REQUEST_READ_SMS);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_SMS},
                MY_PERMISSIONS_REQUEST_RECEIVE_SMS);

    }

    public void forward(String message) {
        Log.e("forward", "forward called");
        mMessage = message;
        if (forwardToSmsActive) {
            Log.e("forward", message);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestSMSPermission();
            } else {
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("+91 "+ mForwardPhoneNumber, null, mMessage, null, null);
                    Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Message failed!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        PostTask task = new PostTask();
        task.execute();
    }


    public void setPhoneNumber(View view) {
        EditText forwardNumber = (EditText) findViewById(R.id.forwardNo);
        mForwardPhoneNumber = forwardNumber.getText().toString();
        Log.e("setPhoneNumber", mForwardPhoneNumber);
    }

    public void setForwardToSmsActive(View view) {
        Button forwardActive = (Button) findViewById(R.id.forwardActivate);
        if(!forwardToSmsActive && mForwardPhoneNumber != null) {
            forwardToSmsActive = true;
            forwardActive.setText("Deactivate Forward To SMS");
        }
        else {
            forwardToSmsActive = false;
            forwardActive.setText("Activate Forward To SMS");
        }
    }

    public void makePostRequest(View v) throws IOException {
        Log.e("forward", "Sending to bot");
        mMessage = "Test Message";
        PostTask task = new PostTask();
        task.execute();
    }

    public void setupTelegram(View v) {
        EditText botTokenEditText = (EditText) findViewById(R.id.botTokenField);
        EditText ChatIdEditText = (EditText) findViewById(R.id.chatIdField);
        botToken = botTokenEditText.getText().toString();
        chatId = ChatIdEditText.getText().toString();
        mPostUrl = mPostUrl.replace("{botToken}","489635845:AAGAy5DLIhfmv14QgLhQTAqgrYuvCAs5-MQ");
        mPostUrl = mPostUrl.replace("{chatId}","507477080");
        Log.e("setupTelegram", mPostUrl);
    }

    private class PostTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... urls) {
            String messageToSend = mMessage.replace(" ","+");
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(mPostUrl + messageToSend);
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
