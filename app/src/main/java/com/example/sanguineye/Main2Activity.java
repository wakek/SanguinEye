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

        try {
            for (int i = 0; i < installedApps.size(); i++) {
                // Get current date
                DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd", Locale.getDefault());
                Date date = new Date();
                String formattedDate = dateFormat.format(date);


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

                // App icon
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
                String appActivityRunningTime_Hrs = null;

                if (appActivityRunningTime == null) {
                    appActivityRunningTime_Hrs = "0";
                } else {
                    appActivityRunningTime_Hrs = Long.toString(appActivityRunningTime / (60 * 60 * 1000));
                }

                if (appUsageRecord != null) {    // If previous record exists in DB, update

                    // Update progress text with the new uptime
                    progressTextView.setText(appActivityRunningTime_Hrs + "hrs");

                    // Update records in database
                    appUsageRecord.setTimeSpent(appActivityRunningTime_Hrs);
                    databaseHelper.updateAppUsageRecord(appUsageRecord);
                } else {

                    // Set progress text to new-found uptime
                    progressTextView.setText(appActivityRunningTime_Hrs + "hrs");

                    // Add record to database
                    databaseHelper.addAppUsage(new AppUsageRecord(installedApps.get(i).appName, appActivityRunningTime_Hrs, formattedDate));

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
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("done");
    }

    @Override
    protected void onResume() {
        super.onResume();

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


            for (int j = 0; j < installedApps.size(); j++){

                if (installedApps.get(j).appName.equals(newAppTextView.getText())){
                    // App uptime retrieved
                    Long appActivityRunningTime = getActivityRunningTime(installedApps.get(j).appPkg, formattedDate);

                    // Convert app uptime to hours
                    String appActivityRunningTime_Hrs = Long.toString(appActivityRunningTime/(60 * 60 * 1000));

                    // Get appUsageRecord for current app
                    AppUsageRecord appUsageRecord = databaseHelper.getAppUsageRecordByAppNameandDate(installedApps.get(j).appName, formattedDate);

                    // Update progress text with the new uptime
                    progressTextView.setText(Long.toString(appActivityRunningTime));

                    // Update records in database
                    appUsageRecord.setTimeSpent(appActivityRunningTime_Hrs);
                    databaseHelper.updateAppUsageRecord(appUsageRecord);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        List<PackageInfo> packageList = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packageList.size(); i++){
            PackageInfo packageInfo = packageList.get(i);

            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                String appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                String appPkg = packageInfo.packageName;
                System.out.println("Packages seen: " + appPkg);
                Drawable appIcon = packageInfo.applicationInfo.loadIcon(getPackageManager());

                AppInfo app = new AppInfo();
                app.appName = appName;
                app.appIcon = appIcon;
                app.appPkg = appPkg;

                apps.add(app);
            }
        }
        return apps;
    }
}
