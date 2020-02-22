package com.example.sanguineye;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
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

import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
import com.github.silvestrpredko.dotprogressbar.DotProgressBarBuilder;

import org.w3c.dom.Text;

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
    private static final String TAG = "MainActivity2 Log: ";
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

            // For testing purposes:
//            databaseHelper.addAppUsage(new AppUsageRecord("Amazon Shopping", "4", formattedDate));
//            AppUsageRecord appUsageRecord = databaseHelper.getAppUsageRecordByAppNameandDate("Amazon Shopping", formattedDate);
//            System.out.println("App name" + appUsageRecord.getAppName() + " | Time spent: " + appUsageRecord.getTimeSpent());
//            List<AppUsageRecord> appUsageRecords = databaseHelper.getAppUsageRecordsByAppName(apps.get(i).appName);
//            System.out.println(appUsageRecords.toString());

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
            Long appActivityRunningTime = getActivityRunningTime(installedApps.get(i).appPkg, formattedDate);

            // Convert app uptime to hours
            String appActivityRunningTime_Str = null;

            if (appActivityRunningTime == null) {
                appActivityRunningTime_Str = "0";
            } else {
                appActivityRunningTime_Str = Long.toString(appActivityRunningTime);
            }
            databaseHelper.addAppUsageLimit(new AppUsageLimit(installedApps.get(i).appName, "3600000"));
            AppUsageLimit appUsageLimit = databaseHelper.getAppUsageLimitByAppName(installedApps.get(i).appName);

            if (appUsageRecord != null) {    // If previous record exists in DB, update

                Float appUsageLimitPercentage = Float.parseFloat(appActivityRunningTime_Str)/Float.parseFloat(appUsageLimit.getTimeLimit()) * 100;

                // Set progress text to new-found uptime
                progressTextView.setText(String.format(Locale.getDefault(), "%.2f", appUsageLimitPercentage));

                // Update records in database
                appUsageRecord.setTimeSpent(appActivityRunningTime_Str);
                databaseHelper.updateAppUsageRecord(appUsageRecord);
            } else {

                Float appUsageLimitPercentage = Float.parseFloat(appActivityRunningTime_Str)/Float.parseFloat(appUsageLimit.getTimeLimit()) * 100;

                // Set progress text to new-found uptime
                progressTextView.setText(String.format(Locale.getDefault(), "%.2f", appUsageLimitPercentage));

                // Add record to database
                databaseHelper.addAppUsage(new AppUsageRecord(installedApps.get(i).appName, appActivityRunningTime_Str, formattedDate));
            }
            pbLayout.addView(progressTextView);

            // Adding app icon and app name
            newAppLinearLayout.addView(newAppIcon);
            newAppLinearLayout.addView(newAppTextView);
            newAppLinearLayout.addView(pbLayout);

            LinearLayout.LayoutParams appslayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
            appslayoutParams.setMargins(24, 40, 24, 0);
            appsLinearLayout.addView(newAppLinearLayout, appslayoutParams);
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
                    Long appActivityRunningTime = getActivityRunningTime(installedApps.get(j).appPkg, formattedDate);

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
                        databaseHelper.updateAppUsageRecord(appUsageRecord);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                    progressTextView.setText(String.format(Locale.getDefault(), "%.2f", appUsageLimitPercentage) + "%");
                }
            }
        }
    }

    private Long getActivityRunningTime(String appPkgName, String startDateTime){

        String year = null;
        String month = null;
        String day = null;
        if (startDateTime != null){
            year = startDateTime.split("-")[0];
            month = startDateTime.split("-")[1];
            day = startDateTime.split("-")[2];
        }

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

    class AppInfo {
        private String appName = "";
        private String appPkg = "";
        private Drawable appIcon;
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
