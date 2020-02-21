package com.example.sanguineye;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.text.Layout;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {

    public final static String GOOGLE_URL = "https://play.google.com/store/apps/details?id=";
    public static final String ERROR = "error";
    ListView appListView;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        LinearLayout appsLinearLayout = (LinearLayout)findViewById(R.id.appLinearLayout2);

        ArrayList<AppInfo> apps =  retreiveInstalledApps();

        for (int i = 0; i < apps.size(); i++){
            LinearLayout newAppLinearLayout = new LinearLayout(this);
            newAppLinearLayout.setVerticalGravity(Gravity.CENTER);
            newAppLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            newAppLinearLayout.setBackgroundResource(R.drawable.layout_bg);

            ImageView newAppIcon = new ImageView(this);
            Drawable appIconDrawable = new ScaleDrawable(apps.get(i).appIcon, 0, 50, 50).getDrawable();
            appIconDrawable.setBounds(0, 0, 50, 50);
            newAppIcon.setImageDrawable(appIconDrawable);


            TextView newAppTextView = new TextView(this);
            newAppTextView.setText(apps.get(i).appName);
            newAppTextView.setTextSize(15);
            newAppTextView.setPadding(20,0,0,0);
            newAppTextView.setTextColor(Color.parseColor("#6B6A6A"));

            newAppLinearLayout.addView(newAppIcon);
            newAppLinearLayout.addView(newAppTextView);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
            layoutParams.setMargins(24, 0, 24, 40);
            appsLinearLayout.addView(newAppLinearLayout, layoutParams);
        }

    }

    class AppInfo {
        private String appName = "";
        private Drawable appIcon;
    }

    public ArrayList<AppInfo> retreiveInstalledApps(){
        ArrayList<AppInfo> apps = new ArrayList<>();
        List<PackageInfo> packageList = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packageList.size(); i++){
            PackageInfo packageInfo = packageList.get(i);

            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                String appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                Drawable appIcon = packageInfo.applicationInfo.loadIcon(getPackageManager());

                AppInfo app = new AppInfo();
                app.appName = appName;
                app.appIcon = appIcon;

                apps.add(app);
            }
        }
        return apps;
    }
}
