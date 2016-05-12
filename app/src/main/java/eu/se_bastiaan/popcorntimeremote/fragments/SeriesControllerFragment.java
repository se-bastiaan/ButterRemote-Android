package eu.se_bastiaan.popcorntimeremote.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import butterknife.ButterKnife;
import butterknife.Bind;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.utils.LogUtils;
import eu.se_bastiaan.popcorntimeremote.utils.PixelUtils;
import eu.se_bastiaan.popcorntimeremote.widget.JoystickView;

public class SeriesControllerFragment extends BaseControlFragment {

    @Bind(R.id.joystick)
    JoystickView joystickView;
    @Bind(R.id.favouriteButton)
    ImageButton favouriteButton;
    @Bind(R.id.watchedButton)
    ImageButton watchedButton;
    @Bind(R.id.qualityButton)
    ImageButton qualityButton;

    private View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.favouriteButton:
                    getClient().toggleFavourite(mBlankResponseCallback);
                    break;
                case R.id.watchedButton:
                    getClient().toggleWatched(mBlankResponseCallback);
                    break;
                case R.id.qualityButton:
                    getClient().toggleQuality(mBlankResponseCallback);
                    break;
                default:
                    break;
            }
        }
    };

    private JoystickView.OnJoystickMoveListener mOnJoystickMoveListener = new JoystickView.OnJoystickMoveListener() {
        @Override
        public void onValueChanged(int angle, int power, JoystickView.Direction direction) {
            LogUtils.d("OnJoystickMoveListener", power);

            switch (direction) {
                case CENTER:
                    getClient().enter(mBlankResponseCallback);
                    break;
                case UP:
                    getClient().up(mBlankResponseCallback);
                    break;
                case DOWN:
                    getClient().down(mBlankResponseCallback);
                    break;
                case RIGHT:
                    getClient().nextSeason(mBlankResponseCallback);
                    break;
                case LEFT:
                    getClient().prevSeason(mBlankResponseCallback);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtils.d("JoyStickMainControllerFragment", "onCreateView");

        View v = inflater.inflate(R.layout.fragment_seriescontroller, container, false);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop() + PixelUtils.getStatusBarHeight(getActivity()), v.getPaddingRight(), v.getPaddingBottom());
        }
        ButterKnife.bind(this, v);

        favouriteButton.setOnClickListener(mButtonClickListener);
        watchedButton.setOnClickListener(mButtonClickListener);
        qualityButton.setOnClickListener(mButtonClickListener);

        joystickView.setOnJoystickMoveListener(mOnJoystickMoveListener);
        joystickView.setJoystickImage(JoystickView.Direction.CENTER, R.drawable.ic_action_ok);
        joystickView.setJoystickImage(JoystickView.Direction.LEFT, R.drawable.ic_action_prevseason);
        joystickView.setJoystickImage(JoystickView.Direction.RIGHT, R.drawable.ic_action_nextseason);
        joystickView.setJoystickImage(JoystickView.Direction.UP, R.drawable.ic_action_up);
        joystickView.setJoystickImage(JoystickView.Direction.DOWN, R.drawable.ic_action_down);

        return v;
    }

}
