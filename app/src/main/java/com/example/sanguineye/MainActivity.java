package com.example.sanguineye;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Intent notificationServiceIntent;
    private NotificationService notificationService;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationService = new NotificationService();
        notificationServiceIntent = new Intent(this, NotificationService.class);
        if (!notificationServiceIsRunning(notificationService.getClass())){
            startService(notificationServiceIntent);
        }

        List<String> quotes = new ArrayList<>();
        quotes.add("\"The opportunity to step away from everything and take a break is something that shouldn\'t be squandered.\" ― Harper Reed");
        quotes.add("\"Do something nice for yourself today. Find some quiet, sit in stillness, breathe. Put your problems on pause. You deserve a break.\" ― Akiroq Brost");
        quotes.add("\"When things are not happening as planned just stop worrying and take an unplanned break to regain yourself.\"― Giridhar Alwar");

        int rand_int = new Random().nextInt(quotes.size());
        TextView quote_textview = findViewById(R.id.quote);
        quote_textview.setText(quotes.get(rand_int));

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent timed_intent = new Intent(MainActivity.this, Main2Activity.class);
                try {
                    startActivity(timed_intent);
                    finish();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, 5000);
    }

    private boolean notificationServiceIsRunning(Class<?> notificationServiceClass){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo: activityManager.getRunningServices(Integer.MAX_VALUE)){
            if (notificationServiceClass.getClass().equals(serviceInfo.service.getClassName())){
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }

    @Override
    protected void onDestroy() {
//        stopService(notificationServiceIntent);
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP){
            Intent touch_intent = new Intent(MainActivity.this, Main2Activity.class);
            startActivity(touch_intent);
            return true;
        }
        return false;
    }
}
