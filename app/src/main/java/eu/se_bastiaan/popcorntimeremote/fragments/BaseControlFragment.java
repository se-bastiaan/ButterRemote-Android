package eu.se_bastiaan.popcorntimeremote.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;

import eu.se_bastiaan.popcorntimeremote.activities.ControllerActivity;
import eu.se_bastiaan.popcorntimeremote.rpc.PopcornTimeRpcClient;

public abstract class BaseControlFragment extends Fragment {

    private PopcornTimeRpcClient mRpc;
    private Bundle mExtras;
    protected Handler mHandler = new Handler(Looper.getMainLooper());

    protected PopcornTimeRpcClient.Callback mBlankResponseCallback = new PopcornTimeRpcClient.Callback() {
        @Override
        public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
            if(e != null) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mExtras = getArguments();
        mRpc = new PopcornTimeRpcClient(mExtras.getString(ControllerActivity.KEY_IP), mExtras.getString(ControllerActivity.KEY_PORT), mExtras.getString(ControllerActivity.KEY_USERNAME), mExtras.getString(ControllerActivity.KEY_PASSWORD), mExtras.getString(ControllerActivity.KEY_VERSION));
    }

    protected PopcornTimeRpcClient getClient() {
        if (mRpc == null) {
            mRpc = new PopcornTimeRpcClient(mExtras.getString(ControllerActivity.KEY_IP), mExtras.getString(ControllerActivity.KEY_PORT), mExtras.getString(ControllerActivity.KEY_USERNAME), mExtras.getString(ControllerActivity.KEY_PASSWORD), mExtras.getString(ControllerActivity.KEY_VERSION));
        }
        return mRpc;
    }

    protected ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    protected View getActionBarView() {
        Window window = getActivity().getWindow();
        View decorView = window.getDecorView();
        int resId = getResources().getIdentifier("toolbar", "id", getActivity().getPackageName());
        return decorView.findViewById(resId);
    }

}
