package com.example.setting;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static final String PREFS_NAME = "login_prefs";
    private static final String KEY_USER_ID = "saved_id";
    private static final String KEY_USER_PASSWORD = "saved_password";
    private static final String KEY_REMEMBER_ME = "remember_me";

    private static final String TUTORIAL_PREFS_NAME = "tutorial_prefs";

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences getTutorialPreferences(Context context) {
        return context.getSharedPreferences(TUTORIAL_PREFS_NAME, Context.MODE_PRIVATE);
    }

    // User credentials and remember me
    public static void saveUserId(Context context, String userId) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public static void saveUserPassword(Context context, String password) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_PASSWORD, password);
        editor.apply();
    }

    public static void setRememberMe(Context context, boolean rememberMe) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.apply();
    }

    public static String getSavedUserId(Context context) {
        SharedPreferences prefs = getPreferences(context);
        return prefs.getString(KEY_USER_ID, "");
    }

    public static String getSavedUserPassword(Context context) {
        SharedPreferences prefs = getPreferences(context);
        return prefs.getString(KEY_USER_PASSWORD, "");
    }


    public static void clearSavedUserCredentials(Context context) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_PASSWORD);
        editor.apply();
    }

    // Tutorial preferences
    public static void saveTutorialCompleted(Context context, String userId, boolean completed) {
        SharedPreferences prefs = getTutorialPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(userId + "_tutorial_completed", completed);
        editor.apply();
    }

    public static boolean isTutorialCompleted(Context context, String userId) {
        SharedPreferences prefs = getTutorialPreferences(context);
        return prefs.getBoolean(userId + "_tutorial_completed", false);
    }
}
