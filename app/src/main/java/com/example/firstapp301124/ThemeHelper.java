package com.example.firstapp301124;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {
    private static final String PREF_NAME = "theme_pref";
    private static final String KEY_THEME = "is_dark_theme";

    public static void applyTheme(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isDarkTheme = sharedPref.getBoolean(KEY_THEME, false);

        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static void setTheme(Context context, boolean isDarkTheme) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(KEY_THEME, isDarkTheme);
        editor.apply();
        applyTheme(context);
    }

    public static boolean isDarkTheme(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(KEY_THEME, false);
    }

    public static void toggleTheme(Context context) {
        boolean isDarkTheme = isDarkTheme(context);
        setTheme(context, !isDarkTheme);
    }

    /**
     * Returns the currently applied theme
     * @param context The context to check the theme
     * @return String representing the current theme ("Dark" or "Light")
     */
    public static String getCurrentTheme(Context context) {
        return isDarkTheme(context) ? "Dark" : "Light";
    }
} 