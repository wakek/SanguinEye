package com.example.sanguineye;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "appUsageManager";
    private static final String TABLE_APPUSAGE = "appUsage";
    private static final String KEY_APPUSAGE_ID = "id";
    private static final String KEY_APPNAME = "appName";
    private static final String KEY_TIMESPENT = "timeSpent";
    private static final String KEY_DATE = "date_of_record";
    private static final String TABLE_APPUSAGELIMITS = "appUsageLimits";
    private static final String KEY_APPUSAGELIMITS_ID = "id";
    private static final String KEY_TIMELIMIT = "timeLimit";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_APPUSAGE_TABLE = "CREATE TABLE " + TABLE_APPUSAGE +
                "(" +
                    KEY_APPUSAGE_ID + " INTEGER PRIMARY KEY," +
                    KEY_APPNAME + " TEXT," +
                    KEY_TIMESPENT + " FLOAT," +
                    KEY_DATE + " DATE" +
                ")";
        String CREATE_APPUSAGE_LIMIT_TABLE = "CREATE TABLE " + TABLE_APPUSAGELIMITS +
                "(" +
                    KEY_APPUSAGELIMITS_ID + " INTEGER PRIMARY KEY," +
                    KEY_APPNAME + " TEXT," +
                    KEY_TIMELIMIT + " FLOAT" +
                ")";
        db.execSQL(CREATE_APPUSAGE_TABLE);
        db.execSQL(CREATE_APPUSAGE_LIMIT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPUSAGE);

        onCreate(db);
    }


    public void addAppUsage(AppUsageRecord appUsageRecord){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(KEY_APPNAME, appUsageRecord.getAppName());
            values.put(KEY_TIMESPENT, Float.parseFloat(appUsageRecord.getTimeSpent()));
            values.put(KEY_DATE, appUsageRecord.getDate());

            db.insertOrThrow(TABLE_APPUSAGE, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add record to database");
        } finally {
            db.endTransaction();
        }
    }

    public AppUsageRecord getAppUsageRecord(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPUSAGE,
                new String[]{KEY_APPUSAGE_ID, KEY_APPNAME, KEY_TIMESPENT, KEY_DATE},
                KEY_APPUSAGE_ID + "=?", new String[]{String.valueOf(id)},
                null, null,null,null);

        AppUsageRecord appUsageRecord = null;
        try {
            if (cursor != null) {
                cursor.moveToFirst();
            }

            appUsageRecord = new AppUsageRecord(
                    Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3));
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add usageRecord to the database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return appUsageRecord;
    }


    public List<AppUsageRecord> getAllAppUsageRecords(){
        List<AppUsageRecord> appUsageRecordList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_APPUSAGE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    AppUsageRecord appUsageRecord = new AppUsageRecord();
                    appUsageRecord.setId(Integer.parseInt(cursor.getString(0)));
                    appUsageRecord.setAppName(cursor.getString(1));
                    appUsageRecord.setTimeSpent(cursor.getString(2));
                    appUsageRecord.setTimeSpent(cursor.getString(3));

                    appUsageRecordList.add(appUsageRecord);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get all usage records from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return appUsageRecordList;
    }


    public List<AppUsageRecord> getAppUsageRecordsByAppName(String appName){
        List<AppUsageRecord> appUsageRecordList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_APPUSAGE + " WHERE " + KEY_APPNAME + "= \"" + appName + "\"";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    AppUsageRecord appUsageRecord = new AppUsageRecord();
                    appUsageRecord.setId(Integer.parseInt(cursor.getString(0)));
                    appUsageRecord.setAppName(cursor.getString(1));
                    appUsageRecord.setTimeSpent(cursor.getString(2));
                    appUsageRecord.setTimeSpent(cursor.getString(3));

                    appUsageRecordList.add(appUsageRecord);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get usage records by appName from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return appUsageRecordList;
    }


    public AppUsageRecord getAppUsageRecordByAppNameandDate(String appName, String appDate){
        String selectQuery = "SELECT * FROM " + TABLE_APPUSAGE + " WHERE " +
                KEY_APPNAME + " = " + " \"" + appName + "\"" +
                " AND " +
                KEY_DATE + " = " + "\"" + appDate + "\"";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        AppUsageRecord appUsageRecord = null;
        try {
            if (cursor != null) {
                cursor.moveToFirst();
            }

            appUsageRecord = new AppUsageRecord(
                    Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3));

        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get usageRecords by appName and Date from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return appUsageRecord;
    }


    public int updateAppUsageRecord(AppUsageRecord appUsageRecord){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_APPNAME, appUsageRecord.getAppName());
        contentValues.put(KEY_TIMESPENT, Float.parseFloat(appUsageRecord.getTimeSpent()));
        contentValues.put(KEY_DATE, appUsageRecord.getAppName());
        return db.update(TABLE_APPUSAGE, contentValues,
                KEY_APPUSAGE_ID + "=?", new String[]{String.valueOf(appUsageRecord.getId())});
    }


    public void deleteAppUsageRecord(AppUsageRecord appUsageRecord){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPUSAGE,
                KEY_APPUSAGE_ID + "=?", new String[]{String.valueOf(appUsageRecord.getId())});
        db.close();
    }

}
