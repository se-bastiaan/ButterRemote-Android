package eu.se_bastiaan.popcorntimeremote.fragments;

import android.support.v4.app.Fragment;

import com.koushikdutta.async.future.FutureCallback;

import eu.se_bastiaan.popcorntimeremote.activities.ControllerActivity;
import eu.se_bastiaan.popcorntimeremote.rpc.PopcornTimeRpcClient;

public abstract class BaseControlFragment extends Fragment {

    protected FutureCallback<PopcornTimeRpcClient.RpcResponse> mBlankResponseCallback = new FutureCallback<PopcornTimeRpcClient.RpcResponse>() {
        @Override
        public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
            if(e != null) {
                e.printStackTrace();
            }
        }
    };

    protected PopcornTimeRpcClient getClient() {
        try {
            return ((ControllerActivity) getActivity()).getClient();
        } catch (Exception e) { e.printStackTrace(); }
        return new PopcornTimeRpcClient(getActivity(), "0.0.0.0", "8008", "", "");
    }

}
