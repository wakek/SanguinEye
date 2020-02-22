package com.example.sanguineye;

public class AppUsageRecord {
    private int id;
    private String appName;
    private String timeSpent;
    private String date;

    public AppUsageRecord(){

    }

    public AppUsageRecord(String appName, String timeSpent, String date){
        this.appName = appName;
        this.timeSpent = timeSpent;
        this.date = date;
    }

    public AppUsageRecord(int id, String appName, String timeSpent, String date){
        this.id = id;
        this.appName = appName;
        this.timeSpent = timeSpent;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(String timeSpent) {
        this.timeSpent = timeSpent;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
