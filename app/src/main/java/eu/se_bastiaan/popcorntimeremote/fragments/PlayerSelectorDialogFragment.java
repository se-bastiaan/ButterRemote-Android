package eu.se_bastiaan.popcorntimeremote.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.activities.ControllerActivity;
import eu.se_bastiaan.popcorntimeremote.rpc.PopcornTimeRpcClient;
import eu.se_bastiaan.popcorntimeremote.widget.PlayerAdapter;

public class PlayerSelectorDialogFragment extends DialogFragment {

    private ArrayList<LinkedTreeMap<String, String>> playerData;
    private PopcornTimeRpcClient mRpc;
    private Bundle mExtras;

    @InjectView(R.id.listView)
    ListView listView;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;

    protected PopcornTimeRpcClient getClient() {
        if(mExtras == null) {
            mExtras = getArguments();
        }
        if (mRpc == null) {
            mRpc = new PopcornTimeRpcClient(mExtras.getString(ControllerActivity.KEY_IP), mExtras.getString(ControllerActivity.KEY_PORT), mExtras.getString(ControllerActivity.KEY_USERNAME), mExtras.getString(ControllerActivity.KEY_PASSWORD), mExtras.getString(ControllerActivity.KEY_VERSION));
        }
        return mRpc;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_subtitleselector, null, false);
        ButterKnife.inject(this, view);

        getClient().getPlayers(mResponseListener);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getClient().setPlayer(playerData.get(position).get("id"), mResponseListener);
                dismiss();
            }
        });

        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.player)
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }
                );

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private PopcornTimeRpcClient.Callback mResponseListener = new PopcornTimeRpcClient.Callback() {
        @Override
        public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
            if(e == null && result != null && result.result != null && result.id == PopcornTimeRpcClient.RequestId.GET_PLAYERS.ordinal()) {
                playerData = (ArrayList<LinkedTreeMap<String, String>>) result.getMapResult().get("players");
                PlayerAdapter adapter = new PlayerAdapter(getActivity(), playerData);
                progressBar.setVisibility(View.GONE);
                listView.setAdapter(adapter);
            }
        }
    };



}

