package com.example.sanguineye;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Main3Activity extends AppCompatActivity {

    private static final String TAG = "Main3Activity Log: ";
    // Retrieve stats from DB
    DatabaseHelper databaseHelper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        String appName = getIntent().getStringExtra("APP_NAME");

        TextView appNameDisplay = findViewById(R.id.appName);
        appNameDisplay.setText(appName);
        appNameDisplay.setGravity(Gravity.CENTER);
        appNameDisplay.setTextSize(24);
        appNameDisplay.setTextColor(Color.BLACK);

        List<TextView> daysUsage = new ArrayList<TextView>();
        daysUsage.add((TextView) findViewById(R.id.sunUsage));
        daysUsage.add((TextView) findViewById(R.id.monUsage));
        daysUsage.add((TextView) findViewById(R.id.tueUsage));
        daysUsage.add((TextView) findViewById(R.id.wedUsage));
        daysUsage.add((TextView) findViewById(R.id.thuUsage));
        daysUsage.add((TextView) findViewById(R.id.friUsage));
        daysUsage.add((TextView) findViewById(R.id.satUsage));

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        for (int i = dayOfWeek; i > 0; i--){
            TextView day = daysUsage.get(i - 1);

            Calendar calendarDay = Calendar.getInstance();
            calendarDay.add(Calendar.DAY_OF_WEEK, -(calendar.get(Calendar.DAY_OF_WEEK) - i));
            DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd", Locale.getDefault());
            Date date = calendarDay.getTime();
            String formattedDate = dateFormat.format(date);

            // For testing purposes
//            List<AppUsageRecord> appUsageRecordList = databaseHelper.getAppUsageRecordsByAppName(appName);
//            AlertDialog alertDialog = new AlertDialog.Builder(Main3Activity.this).create();
//            alertDialog.setTitle("Testing");
//            alertDialog.setMessage(appUsageRecordList.get(0).getDate() + ", " + formattedDate);
//            alertDialog.show();

            AppUsageRecord dayUsageRecord = databaseHelper.getAppUsageRecordByAppNameandDate(appName, formattedDate);
            if (dayUsageRecord != null) {
                day.setText(millisecondToAppropriateTime(dayUsageRecord.getTimeSpent()));
            }
            else {
                day.setText("0");
            }
        }
    }

    public void previousActivity(View view){
        this.finish();
    }

    public void setAppUsageLimit(View view){
        String appName = getIntent().getStringExtra("APP_NAME");

        AppUsageLimit appUsageLimit = databaseHelper.getAppUsageLimitByAppName(appName);

        EditText editText = (EditText) findViewById(R.id.appUsageLimit);

        if (editText.getText().toString().equals("") || editText.getText() == null){
            AlertDialog alertDialog = new AlertDialog.Builder(Main3Activity.this).create();
            alertDialog.setTitle("Invalid input");
            alertDialog.setMessage("Provide a new app limit usage in hours");
            alertDialog.show();
        }
        else {
            int usageLimit = Integer.parseInt(editText.getText().toString()) * 60 * 60 * 1000;
            appUsageLimit.setTimeLimit(Integer.toString(usageLimit));
            databaseHelper.addAppUsageLimit(appUsageLimit);

            AlertDialog alertDialog = new AlertDialog.Builder(Main3Activity.this).create();
            alertDialog.setTitle("Success");
            alertDialog.setMessage(String.format(Locale.getDefault(), "New limit (%shrs) has been set for %s", editText.getText().toString(), appName));
            alertDialog.show();
        }
    }

    private String millisecondToAppropriateTime(String time){
        long milliseconds = Long.parseLong(time);
        long seconds = milliseconds/1000;
        if (seconds <= 60){
            return seconds + "s";
        }
        long minutes = seconds/60;
        if (minutes <= 60){
            return minutes + "min";
        }
        Long hours = minutes/60;
        return hours + "hrs";
    }

}
