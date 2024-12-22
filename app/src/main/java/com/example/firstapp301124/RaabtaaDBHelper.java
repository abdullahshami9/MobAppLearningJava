package com.example.firstapp301124;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RaabtaaDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "RaabtaaTest";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USER = "user";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_LINK = "link";
    private static final String KEY_DOB = "dob";
    private static final String KEY_REG_DATE = "registration_date";

    public RaabtaaDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_NAME + " TEXT NOT NULL UNIQUE, " +
                KEY_EMAIL + " TEXT NOT NULL UNIQUE, " +
                KEY_PASSWORD + " TEXT NOT NULL, " +
                KEY_LINK + " TEXT NOT NULL, " +
                KEY_DOB + " TEXT, " +
                KEY_REG_DATE + " TEXT DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    public void addUser(String name, String email, String password, String link, String dob) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_EMAIL, email);
        values.put(KEY_PASSWORD, password);
        values.put(KEY_LINK, link);
        values.put(KEY_DOB, dob);

        try {
            db.insert(TABLE_USER, null, values);
        } catch (Exception e) {
            Log.e("DB_ERROR", "User insertion failed: " + e.getMessage());
        } finally {
            db.close();
        }
    }
}
