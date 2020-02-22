package com.example.sanguineye;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class Main3Activity extends AppCompatActivity {

    // Retrieve stats from DB
    DatabaseHelper databaseHelper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        final EditText editText = findViewById(R.id.appUsageLimit);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_NEXT){
                    if( editText.getText().toString().trim().equalsIgnoreCase(""))
                        editText.setError("Please enter a valid limit!");
                    else
                        Toast.makeText(getApplicationContext(),"Notnull",Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }

    public void previousActivity(View view){
        this.finish();
    }

    public void setAppUsageLimit(View view){
        String appName = getIntent().getStringExtra("APP_NAME");

        AppUsageLimit appUsageLimit = databaseHelper.getAppUsageLimitByAppName(appName);

        EditText editText = (EditText) findViewById(R.id.appUsageLimit);

        if (editText.getText().equals("") || editText.getText() == null){
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

}
