package eu.se_bastiaan.popcorntimeremote.intro;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.se_bastiaan.popcorntimeremote.R;

public class SlideTwo extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_introslide_two, container, false);
        return v;
    }
}