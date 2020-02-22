package com.example.sanguineye;

public class AppUsageLimit {

    private int id;
    private String appName;
    private String timeLimit;

    public AppUsageLimit(){

    }

    public AppUsageLimit(String appName, String timeLimit){
        this.appName = appName;
        this.timeLimit = timeLimit;
    }

    public AppUsageLimit(int id, String appName, String timeSpent){
        this.id = id;
        this.appName = appName;
        this.timeLimit = timeLimit;
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

    public String getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(String timeLimit) {
        this.timeLimit = timeLimit;
    }
}
