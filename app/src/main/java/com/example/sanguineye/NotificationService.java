package com.example.sanguineye;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {

    public static final int notify = 300000;  //interval between two services(Here Service run every 5 Minute)
    private Handler mHandler = new Handler();   //run on another Thread to avoid crash
    private Timer mTimer = null;    //timer handling
    private static final String TAG = "Service Log: ";
    private static final String CHANNEL_ID = "usage_notification";
    private static final int NOTIFICATION_ID = 001;
    // List of installed apps
    List<AppInfo> installedApps = null;
    // Retrieve stats from DB
    DatabaseHelper databaseHelper = new DatabaseHelper(this);

    public int counter=0;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    startMyOwnForeground();
                }
            }, 1000 * 60 * 1);
        }
        else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    startForeground(1, new Notification());
                }
            }, 1000 * 60 * 1);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        installedApps = retreiveInstalledApps();

        // Get current date
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd", Locale.getDefault());
        Date date = new Date();
        String formattedDate = dateFormat.format(date);

        for (int i = 0; i < installedApps.size(); i++){

            // Get appUsageRecord for current app
//            AppUsageRecord appUsageRecord = databaseHelper.getAppUsageRecordByAppNameandDate(installedApps.get(i).appName, formattedDate);

            // App uptime retrieved
            Long appActivityRunningTime = getActivityRunningTime(installedApps.get(i).appPkg, formattedDate);

            // Convert app uptime to hours
            String appActivityRunningTime_Str = null;

            if (appActivityRunningTime == null) {
                appActivityRunningTime_Str = "0";
                continue;
            } else {
                appActivityRunningTime_Str = Long.toString(appActivityRunningTime);
            }

            AppUsageLimit appUsageLimit = databaseHelper.getAppUsageLimitByAppName(installedApps.get(i).appName);
            if (appUsageLimit == null){
                continue;
            }
            appUsageLimit = databaseHelper.getAppUsageLimitByAppName(installedApps.get(i).appName);

            Float appUsageLimitPercentage = Float.parseFloat(appActivityRunningTime_Str)/Float.parseFloat(appUsageLimit.getTimeLimit()) * 100;

            createNotificationChannel();
            displayUsageNotifications(installedApps.get(i).appName, appUsageLimitPercentage);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
//        startTimer();
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stoptimertask();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }



    private Timer timer;
    private TimerTask timerTask;
    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                Log.i("Count", "=========  "+ (counter++));
            }
        };
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
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
//        startForeground(2, builder.setOngoing(true).build());

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
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
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

    //class TimeDisplay for handling task
    class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // display toast
                    Toast.makeText(NotificationService.this, "SanguinEye is watching", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
