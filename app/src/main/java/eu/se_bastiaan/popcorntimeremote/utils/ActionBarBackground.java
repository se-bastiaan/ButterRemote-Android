package eu.se_bastiaan.popcorntimeremote.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import eu.se_bastiaan.popcorntimeremote.R;

public class ActionBarBackground {

    private Drawable mOldBackground;
    private ActionBarActivity mActivity;
    private ActionBar mActionBar;
    private int mNewColor;
    private Handler mHandler = new Handler();

    public ActionBarBackground(ActionBarActivity actionBarActivity) {
        mNewColor = Color.parseColor("#FFFFFF");
        init(actionBarActivity);
    }

    public ActionBarBackground(ActionBarActivity actionBarActivity, int newColor) {
        mNewColor = newColor;
        init(actionBarActivity);
    }

    private void init(ActionBarActivity actionBarActivity) {
        mActionBar = actionBarActivity.getSupportActionBar();
        mActivity = actionBarActivity;

        final int actionBarId = getResources().getIdentifier("action_bar", "id", "android");
        final View actionBar = actionBarActivity.findViewById(actionBarId);
        mOldBackground = actionBar.getBackground();
    }

    private Resources getResources() {
        return mActivity.getResources();
    }

    private void changeColor() {
        fadeBackground(mOldBackground, getColoredBackground(mActivity, mNewColor));
    }

    private void fadeBackground(Drawable newDrawable) {
        fadeBackground(mOldBackground, newDrawable);
    }

    private void fadeBackground(Drawable oldDrawable, Drawable newDrawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            if (oldDrawable == null) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    newDrawable.setCallback(drawableCallback);
                } else {
                    mActionBar.setBackgroundDrawable(newDrawable);
                }

            } else {

                TransitionDrawable td = new TransitionDrawable(new Drawable[] { oldDrawable, newDrawable });

                // workaround for broken ActionBarContainer drawable handling on
                // pre-API 17 builds
                // https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    td.setCallback(drawableCallback);
                } else {
                    mActionBar.setBackgroundDrawable(td);
                }

                td.startTransition(500);

            }

            mOldBackground = newDrawable;

            // http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(true);

        }
    }

    private Drawable.Callback drawableCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            mActionBar.setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            mHandler.postAtTime(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            mHandler.removeCallbacks(what);
        }
    };

    public static Drawable getColoredBackground(Context context, int color) {
        Drawable colorDrawable = new ColorDrawable(color);
        Drawable bottomDrawable = context.getResources().getDrawable(R.drawable.actionbar_bottom);
        LayerDrawable ld = new LayerDrawable(new Drawable[]{colorDrawable, bottomDrawable});
        return ld;
    }

    public static ActionBarBackground changeColor(ActionBarActivity activity, int newColor) {
        ActionBarBackground abColor = new ActionBarBackground(activity, newColor);
        abColor.changeColor();
        return abColor;
    }

    public static ActionBarBackground fade(ActionBarActivity activity, Drawable oldDrawable, Drawable newDrawable) {
        ActionBarBackground abColor = new ActionBarBackground(activity);
        abColor.fadeBackground(oldDrawable, newDrawable);
        return abColor;
    }

    public static ActionBarBackground fadeDrawable(ActionBarActivity activity, Drawable newDrawable) {
        ActionBarBackground abColor = new ActionBarBackground(activity);
        abColor.fadeBackground(newDrawable);
        return abColor;
    }

}
