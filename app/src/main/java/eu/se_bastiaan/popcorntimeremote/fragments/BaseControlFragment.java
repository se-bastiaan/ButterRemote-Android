package eu.se_bastiaan.popcorntimeremote.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.koushikdutta.async.future.FutureCallback;

import eu.se_bastiaan.popcorntimeremote.activities.ControllerActivity;
import eu.se_bastiaan.popcorntimeremote.rpc.PopcornTimeRpcClient;

public abstract class BaseControlFragment extends Fragment {

    private PopcornTimeRpcClient mRpc;
    private Bundle mExtras;

    protected FutureCallback<PopcornTimeRpcClient.RpcResponse> mBlankResponseCallback = new FutureCallback<PopcornTimeRpcClient.RpcResponse>() {
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
        mRpc = new PopcornTimeRpcClient(activity, mExtras.getString(ControllerActivity.KEY_IP), mExtras.getString(ControllerActivity.KEY_PORT), mExtras.getString(ControllerActivity.KEY_USERNAME), mExtras.getString(ControllerActivity.KEY_PASSWORD), mExtras.getString(ControllerActivity.KEY_VERSION));
    }

    protected PopcornTimeRpcClient getClient() {
        if (mRpc == null) {
            mRpc = new PopcornTimeRpcClient(getActivity(), mExtras.getString(ControllerActivity.KEY_IP), mExtras.getString(ControllerActivity.KEY_PORT), mExtras.getString(ControllerActivity.KEY_USERNAME), mExtras.getString(ControllerActivity.KEY_PASSWORD), mExtras.getString(ControllerActivity.KEY_VERSION));
        }
        return mRpc;
    }

}
