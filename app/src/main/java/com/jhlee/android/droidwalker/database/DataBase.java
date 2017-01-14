package com.jhlee.android.droidwalker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jhlee.android.droidwalker.base.AndroidContext;

/**
 * DroidWalker
 *
 * date 2017-01-14
 * author Jun-hyoung, Lee
 */

public class DataBase extends SQLiteOpenHelper {

    private static final String DB_NAME = "droidwalker_db";
    private static final int DB_VERSION = 1;

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
        db.execSQL("CREATE TABLE " + DB_NAME + " (date INTEGER, steps INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
