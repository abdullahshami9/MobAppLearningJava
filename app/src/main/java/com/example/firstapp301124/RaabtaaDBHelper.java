package com.example.firstapp301124;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class RaabtaaDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "RaabtaaTest2.db";
    private static final int DATABASE_VERSION = 2;

    // Table and column names
    private static final String TABLE_USER = "users";
    private static final String TABLE_CARTS = "carts";
    private static final String TABLE_CATEGORIES = "categories";
    private static final String TABLE_COLORS = "colors";
    private static final String TABLE_NOTES = "notes";
    private static final String TABLE_ORDER_ITEMS = "order_items";
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_PAYMENTS = "payments";
    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_SOCIAL_LINKS = "social_links";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_UPDATED_AT = "updated_at";
    private static final String KEY_LINK = "link";

    // User table columns
    private static final String KEY_USAGE_TYPE = "usage_type";
    private static final String KEY_HAS_COMPLETED_SURVEY = "has_completed_survey";

    // Carts table columns
    private static final String KEY_PRODUCT_ID = "product_id";
    private static final String KEY_QUANTITY = "quantity";

    // Categories table columns
    private static final String KEY_SLUG = "slug";

    // Colors table columns
    private static final String KEY_VALUE = "value";

    // Notes table columns
    private static final String KEY_COLOR_ID = "color_id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_IS_PINNED = "is_pinned";

    // Order Items table columns
    private static final String KEY_ORDER_ID = "order_id";
    private static final String KEY_PRICE = "price";

    // Orders table columns
    private static final String KEY_TOTAL = "total";
    private static final String KEY_STATUS = "status";

    // Payments table columns
    private static final String KEY_CARD_HOLDER_NAME = "card_holder_name";
    private static final String KEY_CARD_NUMBER = "card_number";
    private static final String KEY_EXPIRY_DATE = "expiry_date";
    private static final String KEY_CVC = "cvc";
    private static final String KEY_IS_DELETED = "is_deleted";

    // Products table columns
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_STOCK = "stock";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_CATEGORY_ID = "category_id";

    // Social Links table columns
    private static final String KEY_PLATFORM = "platform";
    private static final String KEY_URL = "url";
    private static final String KEY_ORDER = "link_order";

    private final Context context;

    // Constructor
    public RaabtaaDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DB_CREATION", "Creating database tables...");
        // Create the users table
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_NAME + " TEXT NOT NULL, " +
                KEY_EMAIL + " TEXT NOT NULL UNIQUE, " +
                KEY_PASSWORD + " TEXT NOT NULL, " +
                KEY_LINK + " TEXT NOT NULL, " +
                KEY_USAGE_TYPE + " TEXT, " +
                KEY_HAS_COMPLETED_SURVEY + " INTEGER DEFAULT 0, " +
                KEY_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                KEY_UPDATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(CREATE_USER_TABLE);
        Log.d("DB_CREATION", "Users table created");

        // Create the carts table
        String CREATE_CARTS_TABLE = "CREATE TABLE " + TABLE_CARTS + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_USER_ID + " INTEGER NOT NULL, " +
                KEY_PRODUCT_ID + " INTEGER NOT NULL, " +
                KEY_QUANTITY + " INTEGER NOT NULL, " +
                KEY_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                KEY_UPDATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + KEY_USER_ID + ") REFERENCES " + TABLE_USER + "(" + KEY_ID + "), " +
                "FOREIGN KEY (" + KEY_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + KEY_ID + "))";
        db.execSQL(CREATE_CARTS_TABLE);

        // Create the categories table
        String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_NAME + " TEXT NOT NULL, " +
                KEY_SLUG + " TEXT NOT NULL UNIQUE, " +
                KEY_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                KEY_UPDATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(CREATE_CATEGORIES_TABLE);

        // Create the colors table
        String CREATE_COLORS_TABLE = "CREATE TABLE " + TABLE_COLORS + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_NAME + " TEXT NOT NULL, " +
                KEY_VALUE + " TEXT NOT NULL, " +
                KEY_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                KEY_UPDATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(CREATE_COLORS_TABLE);

        // Create the notes table
        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_USER_ID + " INTEGER, " +
                KEY_TITLE + " TEXT, " +
                KEY_CONTENT + " TEXT, " +
                KEY_COLOR_ID + " INTEGER, " +
                KEY_IS_PINNED + " INTEGER DEFAULT 0, " +
                KEY_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                KEY_UPDATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + KEY_COLOR_ID + ") REFERENCES " + TABLE_COLORS + "(" + KEY_ID + "))";
        db.execSQL(CREATE_NOTES_TABLE);

        // Create the order_items table
        String CREATE_ORDER_ITEMS_TABLE = "CREATE TABLE " + TABLE_ORDER_ITEMS + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_ORDER_ID + " INTEGER NOT NULL, " +
                KEY_PRODUCT_ID + " INTEGER NOT NULL, " +
                KEY_QUANTITY + " INTEGER NOT NULL, " +
                KEY_PRICE + " REAL NOT NULL, " +
                KEY_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                KEY_UPDATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + KEY_ORDER_ID + ") REFERENCES " + TABLE_ORDERS + "(" + KEY_ID + "), " +
                "FOREIGN KEY (" + KEY_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + KEY_ID + "))";
        db.execSQL(CREATE_ORDER_ITEMS_TABLE);

        // Create the orders table
        String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_USER_ID + " INTEGER NOT NULL, " +
                KEY_TOTAL + " REAL NOT NULL, " +
                KEY_STATUS + " TEXT DEFAULT 'pending', " +
                KEY_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                KEY_UPDATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + KEY_USER_ID + ") REFERENCES " + TABLE_USER + "(" + KEY_ID + "))";
        db.execSQL(CREATE_ORDERS_TABLE);

        // Create the payments table
        String CREATE_PAYMENTS_TABLE = "CREATE TABLE " + TABLE_PAYMENTS + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_USER_ID + " INTEGER NOT NULL, " +
                KEY_CARD_HOLDER_NAME + " TEXT NOT NULL, " +
                KEY_CARD_NUMBER + " TEXT NOT NULL, " +
                KEY_EXPIRY_DATE + " TEXT NOT NULL, " +
                KEY_CVC + " TEXT NOT NULL, " +
                KEY_IS_DELETED + " INTEGER DEFAULT 0, " +
                KEY_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                KEY_UPDATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + KEY_USER_ID + ") REFERENCES " + TABLE_USER + "(" + KEY_ID + "))";
        db.execSQL(CREATE_PAYMENTS_TABLE);

        // Create the products table
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_NAME + " TEXT NOT NULL, " +
                KEY_DESCRIPTION + " TEXT NOT NULL, " +
                KEY_PRICE + " REAL NOT NULL, " +
                KEY_STOCK + " INTEGER NOT NULL, " +
                KEY_IMAGE + " TEXT, " +
                KEY_CATEGORY_ID + " INTEGER NOT NULL, " +
                KEY_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                KEY_UPDATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + KEY_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + KEY_ID + "))";
        db.execSQL(CREATE_PRODUCTS_TABLE);

        // Create the social_links table
        String CREATE_SOCIAL_LINKS_TABLE = "CREATE TABLE " + TABLE_SOCIAL_LINKS + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_USER_ID + " INTEGER NOT NULL, " +
                KEY_PLATFORM + " TEXT NOT NULL, " +
                KEY_URL + " TEXT NOT NULL, " +
                KEY_ORDER + " INTEGER DEFAULT 0, " +
                KEY_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                KEY_UPDATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + KEY_USER_ID + ") REFERENCES " + TABLE_USER + "(" + KEY_ID + "))";
        db.execSQL(CREATE_SOCIAL_LINKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop all tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COLORS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAYMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SOCIAL_LINKS);

        // Recreate the tables
        onCreate(db);
    }

    // Add a new user to the database
    public void addUser(String name, String email, String password, String link, String usageType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_EMAIL, email);
        values.put(KEY_PASSWORD, password);
        values.put(KEY_LINK, link);
        values.put(KEY_USAGE_TYPE, usageType);

        try {
            // Insert the user
            db.insert(TABLE_USER, null, values);
            Toast.makeText(context, "You are Registered", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("DB_ERROR", "User insertion failed: " + e.getMessage());
        } finally {
            // Close the database connection
            db.close();
        }
    }

    // Retrieve a user by email and password
    public User getUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Use parameterized query to prevent SQL Injection
        Cursor cursor = db.rawQuery("SELECT name, email, usage_type FROM " + TABLE_USER + " WHERE email = ? AND password = ?",
                new String[]{email, password});

        if (cursor != null && cursor.moveToFirst()) {
            // Map the returned data to a User object
            String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME));
            String usageType = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USAGE_TYPE));
            cursor.close();

            // Return the User object
            return new User(name, email, usageType);
        }

        if (cursor != null) {
            cursor.close();
        }
        return null;  // No user found
    }

    // Check if a user with the given email already exists
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE email = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Update user details
    public boolean updateUser(String email, String name, String usageType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_USAGE_TYPE, usageType);

        // Update the user
        int rowsAffected = db.update(TABLE_USER, values, KEY_EMAIL + " = ?", new String[]{email});
        db.close();
        return rowsAffected > 0;
    }

    // Delete a user by email
    public boolean deleteUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_USER, KEY_EMAIL + " = ?", new String[]{email});
        db.close();
        return rowsAffected > 0;
    }

    // Add a new note
    public void addNote(int userId, String title, String content, int colorId, int isPinned) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, userId);
        values.put(KEY_TITLE, title);
        values.put(KEY_CONTENT, content);
        values.put(KEY_COLOR_ID, colorId);
        values.put(KEY_IS_PINNED, isPinned);

        try {
            db.insert(TABLE_NOTES, null, values);
            Toast.makeText(context, "Note added successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("DB_ERROR", "Note insertion failed: " + e.getMessage());
        } finally {
            db.close();
        }
    }

    // Update an existing note
    public boolean updateNote(int id, String title, String content, int colorId, int isPinned) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, title);
        values.put(KEY_CONTENT, content);
        values.put(KEY_COLOR_ID, colorId);
        values.put(KEY_IS_PINNED, isPinned);

        int rowsAffected = db.update(TABLE_NOTES, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }

    // Retrieve all notes for a user
    public List<Note> getNotesByUser(int userId) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NOTES + " WHERE " + KEY_USER_ID + " = ?", new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONTENT));
                int colorId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_COLOR_ID));
                int isPinned = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_PINNED));
                String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_AT));
                String updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(KEY_UPDATED_AT));

                notes.add(new Note(id, userId, title, content, colorId, isPinned, createdAt, updatedAt));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return notes;
    }

    // Delete a note by ID
    public boolean deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_NOTES, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }
}