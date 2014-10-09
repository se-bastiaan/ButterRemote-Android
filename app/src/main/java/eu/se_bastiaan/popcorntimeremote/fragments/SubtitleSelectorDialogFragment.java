package eu.se_bastiaan.popcorntimeremote.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.activities.ControllerActivity;
import eu.se_bastiaan.popcorntimeremote.rpc.PopcornTimeRpcClient;
import eu.se_bastiaan.popcorntimeremote.widget.SubtitleAdapter;

public class SubtitleSelectorDialogFragment extends DialogFragment {

    private ArrayList<String> subsData;

    @InjectView(R.id.listView)
    ListView listView;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_subtitleselector, null, false);
        ButterKnife.inject(this, view);

        ((ControllerActivity)getActivity()).getClient().getSubtitles(mResponseListener);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((ControllerActivity)getActivity()).getClient().setSubtitle(subsData.get(position), mResponseListener);
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

    private FutureCallback<PopcornTimeRpcClient.RpcResponse> mResponseListener = new FutureCallback<PopcornTimeRpcClient.RpcResponse>() {
        @Override
        public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
            if(e == null && result != null && result.result != null && result.id == PopcornTimeRpcClient.RequestId.GET_SUBTITLES.ordinal()) {
                subsData = (ArrayList<String>) result.getMapResult().get("subtitles");
                subsData.add(0, "no-subs");
                SubtitleAdapter adapter = new SubtitleAdapter(getActivity(), subsData);
                progressBar.setVisibility(View.GONE);
                listView.setAdapter(adapter);
            }
        }
    };



}
