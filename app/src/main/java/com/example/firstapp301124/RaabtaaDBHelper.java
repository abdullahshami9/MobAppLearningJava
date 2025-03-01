package com.example.firstapp301124;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

public class RaabtaaDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "RaabtaaTest2.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USER = "user";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_LINK = "link";
    private static final String KEY_DOB = "dob";
    private static final String KEY_REG_DATE = "registration_date";

    private final Context context;

    public RaabtaaDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
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
            Toast.makeText(context, "You are Registered", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("DB_ERROR", "User insertion failed: " + e.getMessage());
        } finally {
            db.close();
        }
    }

    public User getUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Use parameterized query to prevent SQL Injection
        Cursor cursor = db.rawQuery("SELECT name, email, link FROM " + TABLE_USER + " WHERE email = ? AND password = ?",
                new String[]{email, password});

        if (cursor != null && cursor.moveToFirst()) {
            // You can map the returned data to your User model
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String link = cursor.getString(cursor.getColumnIndex("link"));
            cursor.close();

            // Return a User object
            return new User(name, email, link);
        }

        cursor.close();
        return null;  // No user found
    }

}
