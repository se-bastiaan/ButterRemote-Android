package eu.se_bastiaan.popcorntimeremote.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by Sebastiaan on 11-06-14.
 */
public class PixelUtils {

    public static int getPixelsFromDp(Context context, Integer dp) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static int getPixelsFromSp(Context context, Integer sp) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, r.getDisplayMetrics());
    }

}
