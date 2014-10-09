package eu.se_bastiaan.popcorntimeremote.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.koushikdutta.async.future.FutureCallback;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.activities.ControllerActivity;
import eu.se_bastiaan.popcorntimeremote.rpc.PopcornTimeRpcClient;
import eu.se_bastiaan.popcorntimeremote.utils.LogUtils;
import eu.se_bastiaan.popcorntimeremote.widget.JoystickView;

public class MovieControllerFragment extends Fragment {

    @InjectView(R.id.joystick)
    JoystickView joystickView;
    @InjectView(R.id.favouriteButton)
    ImageButton favouriteButton;
    @InjectView(R.id.backButton)
    ImageButton backButton;
    @InjectView(R.id.qualityButton)
    ImageButton qualityButton;
    @InjectView(R.id.subsButton)
    ImageButton subsButton;

    private View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.backButton:
                    getClient().back(mResponseListener);
                    break;
                case R.id.favouriteButton:
                    getClient().toggleFavourite(mResponseListener);
                    break;
                case R.id.watchedButton:
                    getClient().toggleWatched(mResponseListener);
                    break;
                case R.id.subsButton:
                    SubtitleSelectorDialogFragment fragment = new SubtitleSelectorDialogFragment();
                    fragment.show(getActivity().getSupportFragmentManager(), "subtitle_fragment");
                    break;
                case R.id.qualityButton:
                    getClient().toggleQuality(mResponseListener);
                    break;
            }
        }
    };

    private JoystickView.OnJoystickMoveListener mOnJoystickMoveListener = new JoystickView.OnJoystickMoveListener() {
        @Override
        public void onValueChanged(int angle, int power, JoystickView.Direction direction) {
            LogUtils.d("mOnJoystickMoveListener", power);

            switch (direction) {
                case CENTER:
                    getClient().enter(mResponseListener);
                    break;
                case UP:
                    getClient().up(mResponseListener);
                    break;
                case DOWN:
                    getClient().down(mResponseListener);
                    break;
                case RIGHT:
                    getClient().right(mResponseListener);
                    break;
                case LEFT:
                    getClient().left(mResponseListener);
                    break;
            }
        }
    };

    private FutureCallback<PopcornTimeRpcClient.RpcResponse> mResponseListener = new FutureCallback<PopcornTimeRpcClient.RpcResponse>() {
        @Override
        public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
            if(result != null && e != null) {
                LogUtils.d("MainControllerFragment", result.result);
            } else if(e != null) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtils.d("JoyStickMainControllerFragment", "onCreateView");

        View v = inflater.inflate(R.layout.fragment_moviecontroller, container, false);
        ButterKnife.inject(this, v);

        subsButton.setOnClickListener(mButtonClickListener);
        favouriteButton.setOnClickListener(mButtonClickListener);
        qualityButton.setOnClickListener(mButtonClickListener);
        backButton.setOnClickListener(mButtonClickListener);

        joystickView.setOnJoystickMoveListener(mOnJoystickMoveListener);
        joystickView.setJoystickImage(JoystickView.Direction.CENTER, R.drawable.ic_action_playpause);

        return v;
    }

    private PopcornTimeRpcClient getClient() {
        try {
            return ((ControllerActivity) getActivity()).getClient();
        } catch (Exception e) {}
        return new PopcornTimeRpcClient(getActivity(), "0.0.0.0", "8008", "", "");
    }

}