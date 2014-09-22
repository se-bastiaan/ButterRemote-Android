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

public class JoystickSeriesControllerFragment extends Fragment {

    @InjectView(R.id.joystick)
    JoystickView joystickView;
    @InjectView(R.id.upButton)
    ImageButton upButton;
    @InjectView(R.id.downButton)
    ImageButton downButton;
    @InjectView(R.id.leftButton)
    ImageButton leftButton;
    @InjectView(R.id.rightButton)
    ImageButton rightButton;
    @InjectView(R.id.backButton)
    ImageButton backButton;
    @InjectView(R.id.favouriteButton)
    ImageButton favouriteButton;
    @InjectView(R.id.watchedButton)
    ImageButton watchedButton;

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
            }
        }
    };

    private View.OnClickListener mOnDirectionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            JoystickView.Direction d = JoystickView.Direction.CENTER;
            switch (v.getId()) {
                case R.id.upButton:
                    d = JoystickView.Direction.UP;
                    break;
                case R.id.downButton:
                    d = JoystickView.Direction.DOWN;
                    break;
                case R.id.leftButton:
                    d = JoystickView.Direction.LEFT;
                    break;
                case R.id.rightButton:
                    d = JoystickView.Direction.RIGHT;
                    break;
            }
            joystickView.setDirection(d);
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
                    getClient().nextSeason(mResponseListener);
                    break;
                case LEFT:
                    getClient().prevSeason(mResponseListener);
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

        View v = inflater.inflate(R.layout.fragment_joystick_seriescontroller, container, false);
        ButterKnife.inject(this, v);

        upButton.setOnClickListener(mOnDirectionClickListener);
        downButton.setOnClickListener(mOnDirectionClickListener);
        leftButton.setOnClickListener(mOnDirectionClickListener);
        rightButton.setOnClickListener(mOnDirectionClickListener);

        leftButton.setImageResource(R.drawable.ic_action_prevseason);
        rightButton.setImageResource(R.drawable.ic_action_nextseason);
        leftButton.setContentDescription(getString(R.string.prev_season));
        rightButton.setContentDescription(getString(R.string.next_season));

        backButton.setOnClickListener(mButtonClickListener);
        favouriteButton.setOnClickListener(mButtonClickListener);
        watchedButton.setOnClickListener(mButtonClickListener);

        joystickView.setOnJoystickMoveListener(mOnJoystickMoveListener);
        joystickView.setJoystickImage(R.drawable.ic_action_ok);

        return v;
    }

    private PopcornTimeRpcClient getClient() {
        return ((ControllerActivity) getActivity()).getClient();
    }

}
