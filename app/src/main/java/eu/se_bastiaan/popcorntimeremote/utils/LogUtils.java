package eu.se_bastiaan.popcorntimeremote.utils;

import android.util.Log;

import eu.se_bastiaan.popcorntimeremote.Constants;


public final class LogUtils {

    private static final String LOG_UTILS = "LogUtils";

    private LogUtils() throws InstantiationException {
        throw new InstantiationException("This class is not created for instantiation");
    }

    public static void d(Object message) {
        d(LOG_UTILS, message);
    }

    public static void d(Object tag, Object message) {
        if (Constants.LOG_ENABLED) {
            Log.d(tag.toString(), message.toString());
        }
    }

    public static void v(Object message) {
        v(LOG_UTILS, message);
    }

    public static void v(Object tag, Object message) {
        if (Constants.LOG_ENABLED) {
            Log.v(tag.toString(), message.toString());
        }
    }

    public static void e(Object message) {
        e(LOG_UTILS, message);
    }

    public static void e(Object tag, Object message) {
        if (Constants.LOG_ENABLED) {
            Log.e(tag.toString(), message.toString());
        }
    }

    public static void e(Object tag, Object message, Throwable t) {
        if (Constants.LOG_ENABLED) {
            Log.e(tag.toString(), message.toString(), t);
        }
    }

    public static void w(Object message) {
        w(LOG_UTILS, message);
    }

    public static void w(Object tag, Object message) {
        if (Constants.LOG_ENABLED) {
            Log.w(tag.toString(), message.toString());
        }
    }

}
