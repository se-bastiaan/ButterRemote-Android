package eu.se_bastiaan.popcorntimeremote.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import butterknife.ButterKnife;
import butterknife.Bind;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.utils.LogUtils;
import eu.se_bastiaan.popcorntimeremote.utils.PixelUtils;
import eu.se_bastiaan.popcorntimeremote.widget.ClearableEditText;
import eu.se_bastiaan.popcorntimeremote.widget.JoystickView;

public class MainControllerFragment extends BaseControlFragment {

    @Bind(R.id.joystick)
    JoystickView joystickView;

    @Bind(R.id.searchButton)
    ImageButton searchButton;
    @Bind(R.id.favouriteButton)
    ImageButton favouriteButton;
    @Bind(R.id.tabsButton)
    ImageButton tabsButton;
    @Bind(R.id.searchInputBox)
    LinearLayout searchInputBox;
    @Bind(R.id.searchInput)
    ClearableEditText searchInput;


    private View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.searchButton:
                    if(searchInputBox.getVisibility() == View.VISIBLE) {
                        mClearableEditTextListener.didClearText();
                    } else {
                        searchInputBox.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.favouriteButton:
                    getClient().toggleFavourite(mBlankResponseCallback);
                    break;
                case R.id.tabsButton:
                    getClient().toggleTabs(mBlankResponseCallback);
                    break;
                default:
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
                    getClient().enter(mBlankResponseCallback);
                    break;
                case UP:
                    getClient().up(mBlankResponseCallback);
                    break;
                case DOWN:
                    getClient().down(mBlankResponseCallback);
                    break;
                case RIGHT:
                    getClient().right(mBlankResponseCallback);
                    break;
                case LEFT:
                    getClient().left(mBlankResponseCallback);
                    break;
                default:
                    break;
            }
        }
    };

    private ClearableEditText.Listener mClearableEditTextListener = new ClearableEditText.Listener() {
        @Override
        public void didClearText() {
            searchInputBox.setVisibility(View.GONE);
            getClient().clearSearch(mBlankResponseCallback);
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(searchInput.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    };

    private TextWatcher mClearableEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.toString().equals("")) {
                getClient().clearSearch(mBlankResponseCallback);
                return;
            }
            getClient().filterSearch(s.toString(), mBlankResponseCallback);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtils.d("JoyStickMainControllerFragment", "onCreateView");

        View v = inflater.inflate(R.layout.fragment_maincontroller, container, false);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop() + PixelUtils.getStatusBarHeight(getActivity()), v.getPaddingRight(), v.getPaddingBottom());
        }
        ButterKnife.bind(this, v);

        searchButton.setOnClickListener(mButtonClickListener);
        favouriteButton.setOnClickListener(mButtonClickListener);
        tabsButton.setOnClickListener(mButtonClickListener);

        joystickView.setOnJoystickMoveListener(mOnJoystickMoveListener);
        joystickView.setJoystickImage(JoystickView.Direction.CENTER, R.drawable.ic_action_ok);
        joystickView.setJoystickImage(JoystickView.Direction.RIGHT, R.drawable.ic_action_right);
        joystickView.setJoystickImage(JoystickView.Direction.LEFT, R.drawable.ic_action_left);
        joystickView.setJoystickImage(JoystickView.Direction.UP, R.drawable.ic_action_up);
        joystickView.setJoystickImage(JoystickView.Direction.DOWN, R.drawable.ic_action_down);

        searchInput.setIconAlwaysVisible(true);
        searchInput.setListener(mClearableEditTextListener);
        searchInput.addTextChangedListener(mClearableEditTextWatcher);

        return v;
    }

}
