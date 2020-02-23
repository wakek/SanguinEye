package com.example.sanguineye;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class Main2Activity extends AppCompatActivity {

    public final static String GOOGLE_URL = "https://play.google.com/store/apps/details?id=";
    public static final String ERROR = "error";
    private static final String TAG = "Main2Activity Log: ";
    private static final String CHANNEL_ID = "usage_notification";
    private static final int NOTIFICATION_ID = 001;
    ListView appListView;
    Intent intent;

    // List of installed apps
    List<AppInfo> installedApps = null;
    // Retrieve stats from DB
    DatabaseHelper databaseHelper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
//        createNotificationChannel();

        boolean granted = false;
        AppOpsManager appOps = (AppOpsManager) this
                .getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), this.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            granted = (this.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }


        // Get list of installed apps
        installedApps = retreiveInstalledApps();

        LinearLayout appsLinearLayout = (LinearLayout)findViewById(R.id.appLinearLayout2);

        for (int i = 0; i < installedApps.size(); i++) {
            // Get current date
            DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd", Locale.getDefault());
            Date date = new Date();
            String formattedDate = dateFormat.format(date);

            System.out.println("Retrieving: " + installedApps.get(i).appName + ", " + installedApps.get(i).appPkg);

            // Individual linear layout for apps
            LinearLayout newAppLinearLayout = new LinearLayout(this);
            newAppLinearLayout.setVerticalGravity(Gravity.CENTER);
            newAppLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            newAppLinearLayout.setBackgroundResource(R.drawable.layout_bg);

            // Display App icon
            ImageView newAppIcon = new ImageView(this);
            Drawable appIconDrawable = new ScaleDrawable(installedApps.get(i).appIcon, 0, 50, 50).getDrawable();
            appIconDrawable.setBounds(0, 0, 50, 50);
            newAppIcon.setImageDrawable(appIconDrawable);

            // Display App name
            TextView newAppTextView = new TextView(this);
            newAppTextView.setText(installedApps.get(i).appName);
            newAppTextView.setTextSize(15);
            newAppTextView.setPadding(20, 0, 0, 0);
            newAppTextView.setTextColor(Color.parseColor("#6B6A6A"));

            // Side layout for usage stat
            LinearLayout pbLayout = new LinearLayout(this);
            LinearLayout.LayoutParams pblayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            pbLayout.setLayoutParams(pblayoutParams);
            pbLayout.setVerticalGravity(Gravity.CENTER);
            pbLayout.setHorizontalGravity(Gravity.RIGHT);
            pbLayout.setOrientation(LinearLayout.HORIZONTAL);


            // Text for usage stat
            TextView progressTextView = new TextView(this);
            progressTextView.setPadding(0, 0, 40, 0);

            // Get appUsageRecord for current app
            AppUsageRecord appUsageRecord = databaseHelper.getAppUsageRecordByAppNameandDate(installedApps.get(i).appName, formattedDate);

            // App uptime retrieved
            Long appActivityRunningTime = getActivityRunningTime(installedApps.get(i).appPkg);

            // Convert app uptime to hours
            String appActivityRunningTime_Str = null;

            if (appActivityRunningTime == null) {
                appActivityRunningTime_Str = "0";
            } else {
                appActivityRunningTime_Str = Long.toString(appActivityRunningTime);
            }

            AppUsageLimit appUsageLimit = databaseHelper.getAppUsageLimitByAppName(installedApps.get(i).appName);
            if (appUsageLimit == null){
                databaseHelper.addAppUsageLimit(new AppUsageLimit(installedApps.get(i).appName, "3600000"));
            }
            appUsageLimit = databaseHelper.getAppUsageLimitByAppName(installedApps.get(i).appName);

            Float appUsageLimitPercentage = null;
            if (appUsageRecord != null) {    // If previous record exists in DB, update

                appUsageLimitPercentage = Float.parseFloat(appActivityRunningTime_Str)/Float.parseFloat(appUsageLimit.getTimeLimit()) * 100;

                // Set progress text to new-found uptime
                progressTextView.setText(String.format(Locale.getDefault(), "%.2f", appUsageLimitPercentage));

                // Update records in database
                appUsageRecord.setTimeSpent(appActivityRunningTime_Str);
                databaseHelper.updateAppUsageRecord(appUsageRecord);
            } else {

                appUsageLimitPercentage = Float.parseFloat(appActivityRunningTime_Str)/Float.parseFloat(appUsageLimit.getTimeLimit()) * 100;

                // Set progress text to new-found uptime
                progressTextView.setText(String.format(Locale.getDefault(), "%.2f", appUsageLimitPercentage));

                // Add record to database
                databaseHelper.addAppUsage(new AppUsageRecord(installedApps.get(i).appName, appActivityRunningTime_Str, formattedDate));
            }
            pbLayout.addView(progressTextView);
//            displayUsageNotifications(installedApps.get(i).appName, appUsageLimitPercentage);

            // Adding app icon and app name
            newAppLinearLayout.addView(newAppIcon);
            newAppLinearLayout.addView(newAppTextView);
            newAppLinearLayout.addView(pbLayout);
            newAppLinearLayout.setClickable(true);
            final int appIndex = i;
            newAppLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToSetLimitActivity(installedApps.get(appIndex).appName);
                }
            });

            LinearLayout.LayoutParams appslayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
            appslayoutParams.setMargins(24, 40, 24, 0);
            appsLinearLayout.addView(newAppLinearLayout, appslayoutParams);
        }
        boolean mboolean = false;

        SharedPreferences settings = getSharedPreferences("PREFS_NAME", 0);
        mboolean = settings.getBoolean("FIRST_RUN", false);
        if (!mboolean) {
            // do the thing for the first time
            AlertDialog alertDialog = new AlertDialog.Builder(Main2Activity.this).create();
            alertDialog.setTitle("Restart");
            alertDialog.setMessage("Restart app to load usage data.");
            alertDialog.show();
            settings = getSharedPreferences("PREFS_NAME", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("FIRST_RUN", true);
            editor.commit();
        } else {
            // other time your app loads
        }
    }

    public void goToSetLimitActivity(String appName){
        Intent intent = new Intent(getBaseContext(), Main3Activity.class);
        intent.putExtra("APP_NAME", appName);
        startActivity(intent);
    }

    public void displayUsageNotifications(String appName, Float usagePercentage){
        if (usagePercentage < 100){
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_priority_notification)
                .setContentTitle(String.format(Locale.getDefault(), "%s: Usage limit hit", appName))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (usagePercentage >= 100 && usagePercentage <= 110){
            builder.setContentText("App usage limit exceeded. Let's take a break.");
        }
        else if (usagePercentage > 110) {
            builder.setContentText("App usage limit exceeded. Geez, take a break, sheesh.");
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());

        System.out.println("notification");
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Usage";
            String description = "A channel of freedom";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshStats();
    }

    @Override
    protected void onPause() {
        super.onPause();

        refreshStats();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void refreshStats(){
        LinearLayout appsLinearLayout = (LinearLayout)findViewById(R.id.appLinearLayout2);

        // Get current date
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd", Locale.getDefault());
        Date date = new Date();
        String formattedDate = dateFormat.format(date);

        for (int i = 0; i < appsLinearLayout.getChildCount(); i++){
            LinearLayout appLinearLayout = (LinearLayout) appsLinearLayout.getChildAt(i);

            TextView newAppTextView = (TextView) appLinearLayout.getChildAt(1);

            LinearLayout pbLayout = (LinearLayout) appLinearLayout.getChildAt(2);

            TextView progressTextView = (TextView) pbLayout.getChildAt(0);

            System.out.println(newAppTextView.getText());

            for (int j = 0; j < installedApps.size(); j++){

                if (installedApps.get(j).appName.equals(newAppTextView.getText())){
                    // App uptime retrieved
                    Long appActivityRunningTime = getActivityRunningTime(installedApps.get(j).appPkg);

                    // Convert app uptime to hours
                    String appActivityRunningTime_Str = null;

                    if (appActivityRunningTime == null) {
                        appActivityRunningTime_Str = "0";
                    } else {
                        appActivityRunningTime_Str = Long.toString(appActivityRunningTime);
                    }

                    // Get appUsageRecord for current app
                    AppUsageRecord appUsageRecord = databaseHelper.getAppUsageRecordByAppNameandDate(installedApps.get(j).appName, formattedDate);

                    AppUsageLimit appUsageLimit = databaseHelper.getAppUsageLimitByAppName(installedApps.get(i).appName);

                    Float appUsageLimitPercentage = Float.parseFloat(appActivityRunningTime_Str)/Float.parseFloat(appUsageLimit.getTimeLimit()) * 100;

                    // Update records in database
                    try {
                        appUsageRecord.setTimeSpent(appActivityRunningTime_Str);
//                        appUsageRecord.setDate(formattedDate);
                        databaseHelper.updateAppUsageRecord(appUsageRecord);
//                        createNotificationChannel();
//                        displayUsageNotifications(installedApps.get(j).appName, appUsageLimitPercentage);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                    progressTextView.setText(String.format(Locale.getDefault(), "%.2f", appUsageLimitPercentage) + "%");
                }
            }
        }
    }

    private Long getActivityRunningTime(String appPkgName){

        Calendar startDate = new GregorianCalendar();
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);

        Calendar endDate = new GregorianCalendar();

        long startTime = startDate.getTimeInMillis();
        long endTime = endDate.getTimeInMillis();

        UsageStatsManager usageStatsManager = (UsageStatsManager)this.getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        for (UsageStats us : queryUsageStats) {
            System.out.println(us.getPackageName() + " vs. " + appPkgName);
            Log.d(TAG, us.getPackageName() + " = " + us.getTotalTimeInForeground());
            if (us.getPackageName().equals(appPkgName)){
                return us.getTotalTimeInForeground();
            }
        }
        return null;
    }

    public List<AppInfo> retreiveInstalledApps(){
        List<AppInfo> apps = new ArrayList<>();

        Calendar startDate = new GregorianCalendar();
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);

        Calendar endDate = new GregorianCalendar();

        long startTime = startDate.getTimeInMillis();
        long endTime = endDate.getTimeInMillis();

        UsageStatsManager usageStatsManager = (UsageStatsManager)this.getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, endTime);

        final PackageManager pm = this.getPackageManager();
        List<PackageInfo> list = pm.getInstalledPackages(0);

        List<String> systemApps = new ArrayList<>();

        for(PackageInfo pi : list) {
            ApplicationInfo ai = null;
            try {
                ai = pm.getApplicationInfo(pi.packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                systemApps.add(pi.packageName);
            }
        }

        for (UsageStats us : queryUsageStats) {

            Drawable appIcon = null;
            ApplicationInfo ai = null;
            try {
                appIcon = getPackageManager().getApplicationIcon(us.getPackageName());
                ai = pm.getApplicationInfo( us.getPackageName(), 0);
            }
            catch (PackageManager.NameNotFoundException e)
            {
                e.printStackTrace();
                continue;
            }

            String appName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");

            if (systemApps.contains(us.getPackageName())){
                continue;
            }

            AppInfo app = new AppInfo();
            app.appName = appName;
            app.appIcon = appIcon;
            app.appPkg = us.getPackageName();
            apps.add(app);
        }
        return apps;
    }
}
