package eu.se_bastiaan.popcorntimeremote.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.internal.LinkedTreeMap;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.future.ResponseFuture;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.fragments.ConnectionLostFragment;
import eu.se_bastiaan.popcorntimeremote.fragments.LoadingControllerFragment;
import eu.se_bastiaan.popcorntimeremote.fragments.MainControllerFragment;
import eu.se_bastiaan.popcorntimeremote.fragments.MovieControllerFragment;
import eu.se_bastiaan.popcorntimeremote.fragments.PlayerControllerFragment;
import eu.se_bastiaan.popcorntimeremote.fragments.SeriesControllerFragment;
import eu.se_bastiaan.popcorntimeremote.fragments.SubtitleSelectorDialogFragment;
import eu.se_bastiaan.popcorntimeremote.rpc.PopcornTimeRpcClient;
import eu.se_bastiaan.popcorntimeremote.utils.LogUtils;

public class ControllerActivity extends ActionBarActivity {

    public static final String KEY_IP = "ipAdress", KEY_PORT = "port", KEY_USERNAME = "username", KEY_PASSWORD = "password", KEY_NAME = "name";

    private Bundle mExtras;
    private PopcornTimeRpcClient mRpc;
    private Handler mHandler = new Handler();
    private String mCurrentFragment;
    private ResponseFuture<PopcornTimeRpcClient.RpcResponse> mViewstackFuture;

    @InjectView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_framelayout);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        mExtras = intent.getExtras();

        if(mExtras != null && mExtras.containsKey(KEY_IP) && mExtras.containsKey(KEY_PORT) && mExtras.containsKey(KEY_USERNAME) && mExtras.containsKey(KEY_PASSWORD) && mExtras.containsKey(KEY_NAME)) {
            mRpc = new PopcornTimeRpcClient(this, mExtras.getString(KEY_IP), mExtras.getString(KEY_PORT), mExtras.getString(KEY_USERNAME), mExtras.getString(KEY_PASSWORD));
            getSupportActionBar().setTitle(getString(R.string.connected_to) + ": " + mExtras.getString(KEY_NAME));
        } else {
            finish();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_logo);
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
            mViewstackFuture.cancel(true);
        mHandler.removeCallbacksAndMessages(null);
    }

    public void setFragment(Fragment fragment, boolean fade) {
        try {
            progressBar.setVisibility(View.GONE);

            SubtitleSelectorDialogFragment subsFragment = (SubtitleSelectorDialogFragment) getSupportFragmentManager().findFragmentByTag("subtitle_fragment");
            if (subsFragment != null) subsFragment.dismiss();

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            if (fade)
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            fragmentTransaction.replace(R.id.frameLayout, fragment);
            fragmentTransaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PopcornTimeRpcClient getClient() {
        if(mRpc == null && mExtras != null) mRpc = new PopcornTimeRpcClient(this, mExtras.getString(KEY_IP), mExtras.getString(KEY_PORT), mExtras.getString(KEY_USERNAME), mExtras.getString(KEY_PASSWORD));
        return mRpc;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private Runnable mGetViewstackRunnable = new Runnable() {
        @Override
        public void run() {
        mViewstackFuture = mRpc.getViewstack(new FutureCallback<PopcornTimeRpcClient.RpcResponse>() {
            @Override
            public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
                try {
                    if (e == null && result != null && result.result != null) {
                        LinkedTreeMap<String, Object> map = result.getMapResult();
                        if (map.containsKey("viewstack")) {
                            ArrayList<String> resultList = (ArrayList<String>) map.get("viewstack");
                            String topView = resultList.get(resultList.size() - 1);

                            if (topView.equals("player") && (mCurrentFragment == null || !mCurrentFragment.equals("player"))) {
                                setFragment(new PlayerControllerFragment(), true);
                                mCurrentFragment = topView;
                            } else if (topView.equals("shows-container-contain") && (mCurrentFragment == null || !mCurrentFragment.equals("shows-container-contain"))) {
                                setFragment(new SeriesControllerFragment(), true);
                                mCurrentFragment = topView;
                            } else if (topView.equals("movie-detail") && (mCurrentFragment == null || !mCurrentFragment.equals("movie-detail"))) {
                                setFragment(new MovieControllerFragment(), true);
                                mCurrentFragment = topView;
                            } else if (topView.equals("app-overlay") && (mCurrentFragment == null || !mCurrentFragment.equals("app-overlay"))) {
                                setFragment(new LoadingControllerFragment(), true);
                                mCurrentFragment = topView;
                            } else if (!(topView.equals("player") || topView.equals("shows-container-contain") || topView.equals("movie-detail") || topView.equals("app-overlay")) && (mCurrentFragment == null || !mCurrentFragment.equals("main"))) {
                                setFragment(new MainControllerFragment(), true);
                                mCurrentFragment = "main";
                            }
                        }

                        if(mCurrentFragment.equals("player")) {
                            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
                        } else {
                            getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_solid_pt_remote));
                        }

                        mHandler.postDelayed(mGetViewstackRunnable, 1000);
                    } else if (e != null) {
                        e.printStackTrace();
                        setFragment(new ConnectionLostFragment(), true);
                        mCurrentFragment = "no-connection";
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
            mRpc.ping(new FutureCallback<PopcornTimeRpcClient.RpcResponse>() {
                @Override
                public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
                    if (e == null) {
                        mGetViewstackRunnable.run();
                    } else {
                        e.printStackTrace();
                        setFragment(new ConnectionLostFragment(), true);
                        mCurrentFragment = "no-connection";
                    }
                }
            });
        } catch (Exception e) { }
    }
}
