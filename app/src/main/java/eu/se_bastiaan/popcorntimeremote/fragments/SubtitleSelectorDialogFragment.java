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
                subsData = (ArrayList<String>) ((ArrayList)result.result).get(0);
                subsData.add(0, "no-subs");
                SubtitleAdapter adapter = new SubtitleAdapter(subsData);
                progressBar.setVisibility(View.GONE);
                listView.setAdapter(adapter);
            }
        }
    };

    class SubtitleAdapter extends BaseAdapter {

        private ArrayList<String> mData;
        private LayoutInflater mInflater;

        class ViewHolder {
            public ViewHolder(View v) {
                ButterKnife.inject(this, v);
            }
            @InjectView(android.R.id.text1)
            TextView text1;
        }

        public SubtitleAdapter(ArrayList<String> data) {
            mData = data;
            mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public String getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null) {
                convertView = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                holder = new ViewHolder(convertView);
                holder.text1.setPadding(32, 0, 0, 0);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String lang = getItem(position);
            if(!lang.equals("no-subs")) {
                Locale locale;
                if(lang.contains("-")) {
                    locale = new Locale(lang.substring(0, 2), lang.substring(3, 5));
                } else {
                    locale = new Locale(lang);
                }
                holder.text1.setText(locale.getDisplayName());
            } else {
                holder.text1.setText(R.string.disable_subs);
            }

            return convertView;
        }
    }

}
