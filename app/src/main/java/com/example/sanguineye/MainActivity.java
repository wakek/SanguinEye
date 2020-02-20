package com.example.sanguineye;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<String> quotes = new ArrayList<>();
        quotes.add("\"The opportunity to step away from everything and take a break is something that shouldn\'t be squandered\" ― Harper Reed");
        quotes.add("\"Do something nice for yourself today. Find some quiet, sit in stillness, breathe. Put your problems on pause. You deserve a break.\" ― Akiroq Brost");

        int rand_int = new Random().nextInt(quotes.size());
        TextView quote_textview = (TextView)findViewById(R.id.quote);
        quote_textview.setText(quotes.get(rand_int));

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
            }
        }, 5000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP){
            Intent intent = new Intent(MainActivity.this, Main2Activity.class);
            return true;
        }
        return false;
    }
}
