package eu.se_bastiaan.popcorntimeremote.utils;

import android.content.Context;

import eu.se_bastiaan.popcorntimeremote.Constants;
import eu.se_bastiaan.popcorntimeremote.widget.ObscuredSharedPreferences;

public final class PrefUtils {

    private PrefUtils() throws InstantiationException {
        throw new InstantiationException("This class is not created for instantiation");
    }

    // Main functions below

    public static void clear(Context context) {
        getPrefs(context).edit().clear().apply();
    }

    public static void save(Context context, String key, String value) {
        getPrefs(context).edit().putString(key, value).apply();
    }

    public static String get(Context context, String key, String defaultValue) {
        return getPrefs(context).getString(key, defaultValue);
    }

    public static void save(Context context, String key, Boolean value) {
        getPrefs(context).edit().putBoolean(key, value).apply();
    }

    public static Boolean get(Context context, String key, Boolean defaultValue) {
        return getPrefs(context).getBoolean(key, defaultValue);
    }

    public static Boolean contains(Context context, String key) {
        return getPrefs(context).contains(key);
    }

    public static ObscuredSharedPreferences getPrefs(Context context) {
        return new ObscuredSharedPreferences(context, context.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE));
    }

}
