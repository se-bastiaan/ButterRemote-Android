package eu.se_bastiaan.popcorntimeremote.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.Bind;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.activities.ControllerActivity;
import eu.se_bastiaan.popcorntimeremote.utils.LogUtils;

public class ConnectionLostFragment extends Fragment {

    @Bind(R.id.retryButton)
    Button retryButton;

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((ControllerActivity) getActivity()).runViewstackRunnable();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_noconnection, container, false);
        ButterKnife.bind(this, v);

        retryButton.setOnClickListener(mOnClickListener);

        return v;
    }

}
