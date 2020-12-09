package com.example.pushnotifications.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pushnotifications.R;
import com.example.pushnotifications.app.Config;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;


public class MainActivity extends AppCompatActivity {


    TextView txtRegId, txtMessage;
    BroadcastReceiver broadcastReceiver;

    private  static String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FirebaseInstallations.getInstance().getToken(true)
                .addOnSuccessListener(new OnSuccessListener<InstallationTokenResult>() {
                    @Override
                    public void onSuccess(InstallationTokenResult installationTokenResult) {
                        saveSharedPreferencess(installationTokenResult.getToken());
                    }
                });
        txtRegId = (TextView)findViewById(R.id.reg_id);
        txtMessage = (TextView)findViewById(R.id.txt_message);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Config.REGISTRATION_COMPLETE)){
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                    showFirebaseId();
                }else if(intent.getAction().equals(Config.PUSH_NOTIFICATION)){
                    String message = intent.getStringExtra("message");
                    Toast.makeText(getApplicationContext(), "Push Notification: "
                    +message, Toast.LENGTH_LONG).show();
                    txtMessage.setText(message);
                }
            }
        };
showFirebaseId();
    }

@Override
protected  void  onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver
        , new IntentFilter(Config.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver
        ,new IntentFilter(Config.PUSH_NOTIFICATION));
        clearNotification();
}

@Override
protected  void  onPause(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
}

private  void   saveSharedPreferencess(String token){
    SharedPreferences sharedPreferences = getApplicationContext()
            .getSharedPreferences(Config.SHARED_PREF,0);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString("REGID", token);
    editor.commit();
}

private  void  showFirebaseId(){
        SharedPreferences sharedPreferences = getApplicationContext()
                .getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = sharedPreferences.getString("REGID", null);
    Log.e(TAG,"FIREBASE ID: "+regId);
    if (!TextUtils.isEmpty(regId)){
        txtRegId.setText("FIREBASE ID: " +regId);
    }else {
        txtRegId.setText("WE DON'T HAVE ANY ID YET");
    }
}

private  void  clearNotification(){
    NotificationManager notificationManager = (NotificationManager)this
            .getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancelAll();
}
}