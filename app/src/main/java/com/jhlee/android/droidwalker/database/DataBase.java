package com.jhlee.android.droidwalker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jhlee.android.droidwalker.base.AndroidContext;
import com.jhlee.android.droidwalker.model.DailyWalkSet;
import com.jhlee.android.droidwalker.model.WalkSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * DroidWalker
 *
 * date 2017-01-14
 * author Jun-hyoung, Lee
 */

public class DataBase extends SQLiteOpenHelper {

    private static final String DB_NAME = "droidwalker_db";
    private static final int DB_VERSION = 1;

    private static final String KEY_DATE = "date";
    private static final String KEY_STEPS = "steps";
    private static final String KEY_DISTANCE = "distance";

    private static DataBase sInstance;

    private DataBase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized DataBase instance() {
        if (sInstance == null) {
            sInstance = new DataBase(AndroidContext.instance().get());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DB_NAME + " (date INTEGER, steps INTEGER, distance INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void add(long date, int steps, int distance) {
        getWritableDatabase().beginTransaction();

        try {
            Cursor cursor = getReadableDatabase().query(DB_NAME, new String[]{KEY_DATE}, "date = ?",
                    new String[]{String.valueOf(date)}, null, null, null);
            if (cursor.getCount() == 0 && steps >= 0) {
                ContentValues values = new ContentValues();
                values.put(KEY_DATE, date);
                values.put(KEY_STEPS, steps);
                values.put(KEY_DISTANCE, distance);
                getWritableDatabase().insert(DB_NAME, null, values);
            }
            cursor.close();
            getWritableDatabase().setTransactionSuccessful();

        } finally {
            getWritableDatabase().endTransaction();
        }
    }

    public void updateSteps(long date, int steps) {
        ContentValues values = new ContentValues();
        values.put(KEY_STEPS, steps);
        getWritableDatabase().update(DB_NAME, values, KEY_DATE + " = ?", new String[]{String.valueOf(date)});
    }

    public int getSteps(long date) {
        Cursor cursor = getReadableDatabase().query(DB_NAME, new String[]{KEY_STEPS}, "date = ?",
                new String[]{String.valueOf(date)}, null, null, null);
        cursor.moveToFirst();

        int steps = (cursor.getCount() == 0) ? -1 : cursor.getInt(0);

        cursor.close();
        return steps;
    }

    public WalkSet getWalkSet(long date) {
        Cursor cursor = getReadableDatabase().query(DB_NAME, new String[]{KEY_STEPS, KEY_DISTANCE}, "date = ?",
                new String[]{String.valueOf(date)}, null, null, null);
        cursor.moveToFirst();

        WalkSet walkSet = null;
        if (cursor.getCount() > 0) {
            walkSet = new WalkSet(cursor.getInt(0), cursor.getInt(1), null /*not used*/);
        }
        return walkSet;
    }


    public void updateDistance(long date, int distance) {
        ContentValues values = new ContentValues();
        values.put(KEY_DISTANCE, distance);
        getWritableDatabase().update(DB_NAME, values, KEY_DATE + " = ?", new String[]{String.valueOf(date)});
    }

    public int getDistance(long date) {
        Cursor cursor = getReadableDatabase().query(DB_NAME, new String[]{KEY_DISTANCE}, "date = ?",
                new String[]{String.valueOf(date)}, null, null, null);
        cursor.moveToFirst();

        int distance = (cursor.getCount() == 0) ? -1 : cursor.getInt(0);

        cursor.close();
        return distance;
    }

    public List<DailyWalkSet> getRecords() {
        List<DailyWalkSet> data = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(DB_NAME, new String[]{KEY_DATE, KEY_STEPS, KEY_DISTANCE}, null, null, null, null, null);
        if (cursor == null) {
            return data;
        }

        while (cursor.moveToNext()) {
            DailyWalkSet dataSet = new DailyWalkSet(
                    cursor.getLong(cursor.getColumnIndex(KEY_DATE)),
                    cursor.getInt(cursor.getColumnIndex(KEY_STEPS)),
                    cursor.getInt(cursor.getColumnIndex(KEY_DISTANCE))
            );
            data.add(dataSet);
        }

        cursor.close();
        return data;
    }


    public static long getToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static String getTimeString(long date) {
        return new SimpleDateFormat("yyyy/MM/dd").format(date);
    }
}
