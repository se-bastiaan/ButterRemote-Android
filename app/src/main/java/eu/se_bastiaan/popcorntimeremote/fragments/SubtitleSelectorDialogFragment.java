package eu.se_bastiaan.popcorntimeremote.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.Bind;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.activities.ControllerActivity;
import eu.se_bastiaan.popcorntimeremote.rpc.PopcornTimeRpcClient;
import eu.se_bastiaan.popcorntimeremote.widget.SubtitleAdapter;

public class SubtitleSelectorDialogFragment extends DialogFragment {

    private ArrayList<String> subsData;
    private PopcornTimeRpcClient mRpc;
    private Bundle mExtras;
    private Handler mHandler;

    @Bind(R.id.listView)
    ListView listView;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    private final PopcornTimeRpcClient.Callback mResponseListener = new PopcornTimeRpcClient.Callback() {
        @Override
        public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
            if(e == null && result != null && result.result != null && result.id == PopcornTimeRpcClient.RequestId.GET_SUBTITLES.ordinal()) {
                subsData = (ArrayList<String>) result.getMapResult().get("subtitles");
                subsData.add(0, "no-subs");
                final SubtitleAdapter adapter = new SubtitleAdapter(getActivity(), subsData);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        listView.setAdapter(adapter);
                    }
                });
            }
        }
    };

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_subtitleselector, null, false);
        ButterKnife.bind(this, view);

        getClient().getSubtitles(mResponseListener);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getClient().setSubtitle(subsData.get(position), mResponseListener);
                dismiss();
            }
        });

        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
            .setView(view)
            .setTitle(R.string.subtitles)
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

}
