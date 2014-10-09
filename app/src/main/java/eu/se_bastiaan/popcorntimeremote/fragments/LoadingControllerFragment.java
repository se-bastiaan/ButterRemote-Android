package eu.se_bastiaan.popcorntimeremote.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.utils.LogUtils;

public class LoadingControllerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtils.d("JoyStickMainControllerFragment", "onCreateView");

        View v = inflater.inflate(R.layout.fragment_loadingcontroller, container, false);
        ButterKnife.inject(this, v);

        return v;
    }

}
