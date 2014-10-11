package eu.se_bastiaan.popcorntimeremote.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.google.gson.internal.LinkedTreeMap;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.activities.ControllerActivity;
import eu.se_bastiaan.popcorntimeremote.rpc.PopcornTimeRpcClient;
import eu.se_bastiaan.popcorntimeremote.utils.LogUtils;
import eu.se_bastiaan.popcorntimeremote.utils.Version;
import eu.se_bastiaan.popcorntimeremote.widget.SubtitleAdapter;

public class PlayerControllerFragment extends Fragment {

    private Boolean mPlaying = false, mSeeked = false, mVolumeChanged = false, mFullscreen = false;
    private Integer mCurrentTime, mMax, mVolume;
    private Handler mHandler = new Handler();

    @InjectView(R.id.coverImage)
    ImageView coverImage;
    @InjectView(R.id.backwardButton)
    ImageButton backwardButton;
    @InjectView(R.id.playPauseButton)
    ImageButton playPauseButton;
    @InjectView(R.id.forwardButton)
    ImageButton forwardButton;
    @InjectView(R.id.fullscreenButton)
    ImageButton fullscreenButton;
    @InjectView(R.id.backButton)
    ImageButton backButton;
    @InjectView(R.id.currentProgress)
    SeekBar currentTime;
    @InjectView(R.id.volumeControl)
    SeekBar volumeControl;
    @InjectView(R.id.subsSpinner)
    Spinner subsSpinner;
    @InjectView(R.id.subsSpinnerBlock)
    LinearLayout subsSpinnerBlock;

    private View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.backButton:
                    getClient().back(mResponseListener);
                    break;
                case R.id.fullscreenButton:
                    getClient().toggleFullscreen(mResponseListener);
                    break;
                case R.id.subsButton:
                    SubtitleSelectorDialogFragment fragment = new SubtitleSelectorDialogFragment();
                    fragment.show(getActivity().getSupportFragmentManager(), "subtitle_fragment");
                    break;
                case R.id.playPauseButton:
                    getClient().togglePlay(mResponseListener);
                    mPlaying = !mPlaying;
                    updateViews();
                    break;
                case R.id.forwardButton:
                    getClient().seek(60, mResponseListener);
                    break;
                case R.id.backwardButton:
                    getClient().seek(60, mResponseListener);
                    break;
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnTimeControlChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser) {
                LogUtils.d("JoystickPlayerControllerFragment", progress);
                getClient().seek(progress - mCurrentTime, mResponseListener);
                mCurrentTime = progress;
                mSeeked = true;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }
    };

    private SeekBar.OnSeekBarChangeListener mOnVolumeControlChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser) {
                double volume = (progress / 100.0);
                if (volume == 0) volume = 0.001;
                mVolumeChanged = true;
                mVolume = progress;
                getClient().setVolume(volume, mResponseListener);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }
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

    private Runnable mPlayingRunnable = new Runnable() {
        @Override
        public void run() {
            getClient().getPlaying(new FutureCallback<PopcornTimeRpcClient.RpcResponse>() {
                @Override
                public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
                    try {
                        if (result != null && e == null) {
                            mPlaying = (Boolean) result.getMapResult().get("playing");
                            updateViews();

                            if (mPlaying) {
                                if (mMax == null) {
                                    mMax = ((Double) result.getMapResult().get("duration")).intValue();
                                    currentTime.setMax(mMax);
                                }
                                if (!mSeeked) {
                                    mCurrentTime = ((Double) result.getMapResult().get("currentTime")).intValue();
                                    currentTime.setProgress(mCurrentTime);
                                } else {
                                    mSeeked = false;
                                }
                                if(!mVolumeChanged) {
                                    Double volume = (Double) result.getMapResult().get("volume");
                                    mVolume = (int) (volume * 100.0);
                                    volumeControl.setProgress(mVolume);
                                } else {
                                    mVolumeChanged = false;
                                }
                            }
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }

                    mHandler.postDelayed(mPlayingRunnable, 1000);
                }
            });
        }
    };

    private Runnable mFullscreenRunnable = new Runnable() {
        @Override
        public void run() {
            getClient().getFullscreen(new FutureCallback<PopcornTimeRpcClient.RpcResponse>() {
                @Override
                public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
                    try {
                        if (result != null && e == null) {
                            mFullscreen = (Boolean) result.getMapResult().get("fullscreen");
                            updateViews();
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }

                    mHandler.postDelayed(mFullscreenRunnable, 1000);
                }
            });
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtils.d("JoyStickPlayerControllerFragment", "onCreateView");

        View v = inflater.inflate(R.layout.fragment_playercontroller, container, false);
        ButterKnife.inject(this, v);

        fullscreenButton.setOnClickListener(mButtonClickListener);
        backButton.setOnClickListener(mButtonClickListener);
        forwardButton.setOnClickListener(mButtonClickListener);
        backwardButton.setOnClickListener(mButtonClickListener);
        playPauseButton.setOnClickListener(mButtonClickListener);

        currentTime.setMax(0);
        currentTime.setProgress(0);
        currentTime.setOnSeekBarChangeListener(mOnTimeControlChangeListener);

        getClient().setVolume(1.0, mResponseListener);
        volumeControl.setOnSeekBarChangeListener(mOnVolumeControlChangeListener);

        getClient().getSelection(new FutureCallback<PopcornTimeRpcClient.RpcResponse>() {
            @Override
            public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
                if(result != null && e == null) {
                    LinkedTreeMap<String, Object> mapResult = result.getMapResult();
                    Ion.with(coverImage).load(((String) mapResult.get("image")).replace("-300.jpg", ".jpg"));

                    Set<String> subsSet = (Set<String>) ((LinkedTreeMap<String, String>) result.getMapResult().get("subtitle")).keySet();
                    ArrayList<String> subsData = new ArrayList<String>();
                    subsData.addAll(subsSet);
                    subsData.add(0, "no-subs");
                    SubtitleAdapter adapter = new SubtitleAdapter(getActivity(), subsData);
                    subsSpinner.setAdapter(adapter);
                }
            }
        });

        if(!Version.compare(getClient().getVersion(), "0.0.0")) {
            mPlayingRunnable.run();
            mFullscreenRunnable.run();
            currentTime.setVisibility(View.GONE);
            playPauseButton.setImageResource(R.drawable.ic_action_playpause);
        }

        if(Version.compare(getClient().getVersion(), "0.3.4")) {
            subsSpinnerBlock.setVisibility(View.VISIBLE);
        }

        return v;
    }

    private void updateViews() {
        if(mPlaying) {
            playPauseButton.setImageResource(R.drawable.ic_action_pause);
        } else {
            playPauseButton.setImageResource(R.drawable.ic_action_play);
        }

        if(mFullscreen) {
            fullscreenButton.setImageResource(R.drawable.ic_action_smallscreen);
        } else {
            fullscreenButton.setImageResource(R.drawable.ic_action_fullscreen);
        }
    }

    private PopcornTimeRpcClient getClient() {
        try {
            return ((ControllerActivity) getActivity()).getClient();
        } catch (Exception e) {}
        return new PopcornTimeRpcClient(getActivity(), "0.0.0.0", "8008", "", "");
    }

}
