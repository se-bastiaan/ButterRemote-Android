package eu.se_bastiaan.popcorntimeremote.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.google.gson.internal.LinkedTreeMap;
import com.squareup.okhttp.Call;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.fragments.ConnectionLostFragment;
import eu.se_bastiaan.popcorntimeremote.fragments.LoadingControllerFragment;
import eu.se_bastiaan.popcorntimeremote.fragments.MainControllerFragment;
import eu.se_bastiaan.popcorntimeremote.fragments.MovieControllerFragment;
import eu.se_bastiaan.popcorntimeremote.fragments.PlayerControllerFragment;
import eu.se_bastiaan.popcorntimeremote.fragments.PlayerSelectorDialogFragment;
import eu.se_bastiaan.popcorntimeremote.fragments.SeriesControllerFragment;
import eu.se_bastiaan.popcorntimeremote.fragments.SubtitleSelectorDialogFragment;
import eu.se_bastiaan.popcorntimeremote.rpc.PopcornTimeRpcClient;
import eu.se_bastiaan.popcorntimeremote.utils.ActionBarBackground;

public class ControllerActivity extends ActionBarActivity {

    public static final String KEY_IP = "ipAddress", KEY_PORT = "port", KEY_USERNAME = "username", KEY_PASSWORD = "password", KEY_NAME = "name", KEY_VERSION = "version";

    private Bundle mExtras;
    private PopcornTimeRpcClient mRpc;
    private Handler mHandler;
    private String mCurrentFragment, mTopView;
    private Call mViewstackFuture;

    @InjectView(R.id.frameLayout)
    FrameLayout frameLayout;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_framelayout);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        mExtras = intent.getExtras();

        if(mExtras != null && mExtras.containsKey(KEY_IP) && mExtras.containsKey(KEY_PORT) && mExtras.containsKey(KEY_USERNAME) && mExtras.containsKey(KEY_PASSWORD) && mExtras.containsKey(KEY_NAME)) {
            mRpc = new PopcornTimeRpcClient(mExtras.getString(KEY_IP), mExtras.getString(KEY_PORT), mExtras.getString(KEY_USERNAME), mExtras.getString(KEY_PASSWORD));
            //getSupportActionBar().setTitle(getString(R.string.connected_to) + ": " + mExtras.getString(KEY_NAME));
            getSupportActionBar().setTitle(getString(R.string.app_name));
        } else {
            finish();
        }

        mHandler = new Handler(getMainLooper());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        runViewstackRunnable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSupportFragmentManager().popBackStack();
        if(mViewstackFuture != null)
            mViewstackFuture.cancel();
        mHandler.removeCallbacksAndMessages(null);
    }

    public void setFragment(Fragment fragment, boolean fade) {
        try {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            });

            DialogFragment dialogFragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag("overlay_fragment");
            if (dialogFragment != null) dialogFragment.dismiss();

            fragment.setArguments(mExtras);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            if (fade)
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            fragmentTransaction.replace(R.id.frameLayout, fragment);
            fragmentTransaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(mTopView == null || mTopView.equals("main-browser") || mTopView.equals("no-connection")) {
            super.onBackPressed();
        } else {
            mRpc.back(new PopcornTimeRpcClient.Callback() {
                @Override
                public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
                }
            });
        }
    }

    private void showNoConnection() {
        setFragment(new ConnectionLostFragment(), true);
        mCurrentFragment = mTopView = "no-connection";
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ActionBarBackground.fadeDrawable(ControllerActivity.this, new ColorDrawable(getResources().getColor(R.color.primary)));
            }
        });
    }

    private Runnable mGetViewstackRunnable = new Runnable() {
        @Override
        public void run() {
        mViewstackFuture = mRpc.getViewstack(new PopcornTimeRpcClient.Callback() {
            @Override
            public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
                try {
                    if (e == null && result != null && result.result != null) {
                        LinkedTreeMap<String, Object> map = result.getMapResult();

                        if (map.containsKey("viewstack")) {
                            ArrayList<String> resultList = (ArrayList<String>) map.get("viewstack");
                            mTopView = resultList.get(resultList.size() - 1);
                            Boolean translucentActionBar = false;
                            String shownFragment = mCurrentFragment = mCurrentFragment != null ? mCurrentFragment : "";

                            if (mTopView.equals("player") && !mCurrentFragment.equals("player")) {
                                setFragment(new PlayerControllerFragment(), true);
                                mCurrentFragment = mTopView;
                                translucentActionBar = true;
                            } else if (mTopView.equals("shows-container-contain") && !mCurrentFragment.equals("shows-container-contain")) {
                                setFragment(new SeriesControllerFragment(), true);
                                mCurrentFragment = mTopView;
                            } else if (mTopView.equals("movie-detail") && !mCurrentFragment.equals("movie-detail")) {
                                setFragment(new MovieControllerFragment(), true);
                                mCurrentFragment = mTopView;
                                translucentActionBar = true;
                            } else if (mTopView.equals("app-overlay") && !mCurrentFragment.equals("app-overlay")) {
                                setFragment(new LoadingControllerFragment(), true);
                                mCurrentFragment = mTopView;
                            } else if (!(mTopView.equals("player") || mTopView.equals("shows-container-contain") || mTopView.equals("movie-detail") || mTopView.equals("app-overlay")) && !mCurrentFragment.equals("main")) {
                                setFragment(new MainControllerFragment(), true);
                                mCurrentFragment = "main";
                            }

                            Window window = getWindow();
                            if(translucentActionBar && !shownFragment.equals(mCurrentFragment)) {
                                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                                    window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
                                }
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                    getSupportActionBar().setTitle("");
                                    ActionBarBackground.fadeOut(ControllerActivity.this);
                                    }
                                });
                            } else if(!translucentActionBar && !shownFragment.equals(mCurrentFragment)) {
                                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                                    window.setStatusBarColor(getResources().getColor(R.color.primary_dark));
                                }

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                    getSupportActionBar().setTitle(getString(R.string.app_name));
                                    ActionBarBackground.changeColor(ControllerActivity.this, getResources().getColor(R.color.primary));
                                    }
                                });
                            }
                        }

                        mHandler.postDelayed(mGetViewstackRunnable, 200);
                    } else if (e != null) {
                        e.printStackTrace();
                        showNoConnection();
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        }
    };

    public void runViewstackRunnable() {
        try {
            getSupportFragmentManager().popBackStack();
            mRpc.ping(new PopcornTimeRpcClient.Callback() {
                @Override
                public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
                    if (e == null) {
                        mExtras.putString(KEY_VERSION, mRpc.getVersion());
                        mGetViewstackRunnable.run();
                    } else {
                        e.printStackTrace();
                        showNoConnection();
                    }
                }
            });
        } catch (Exception e) { }
    }
}
