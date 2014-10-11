package eu.se_bastiaan.popcorntimeremote.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.google.gson.internal.LinkedTreeMap;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.michaelevans.colorart.library.ColorArt;

import java.util.ArrayList;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.graphics.Palette;
import eu.se_bastiaan.popcorntimeremote.rpc.PopcornTimeRpcClient;
import eu.se_bastiaan.popcorntimeremote.utils.LogUtils;
import eu.se_bastiaan.popcorntimeremote.utils.Version;
import eu.se_bastiaan.popcorntimeremote.widget.SubtitleAdapter;

public class PlayerControllerFragment extends BaseControlFragment {

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
    @InjectView(R.id.slidingPanelTopLayout)
    LinearLayout slidingPanelTopLayout;
    @InjectView(R.id.slidingPanelBottomLayout)
    LinearLayout slidingPanelBottomLayout;
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
                    getClient().back(mBlankResponseCallback);
                    break;
                case R.id.fullscreenButton:
                    getClient().toggleFullscreen(mBlankResponseCallback);
                    break;
                case R.id.subsButton:
                    SubtitleSelectorDialogFragment fragment = new SubtitleSelectorDialogFragment();
                    fragment.show(getActivity().getSupportFragmentManager(), "subtitle_fragment");
                    break;
                case R.id.playPauseButton:
                    getClient().togglePlay(mBlankResponseCallback);
                    mPlaying = !mPlaying;
                    updateViews();
                    break;
                case R.id.forwardButton:
                    getClient().seek(60, mBlankResponseCallback);
                    break;
                case R.id.backwardButton:
                    getClient().seek(-60, mBlankResponseCallback);
                    break;
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnTimeControlChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser) {
                LogUtils.d("JoystickPlayerControllerFragment", progress);
                getClient().seek(progress - mCurrentTime, mBlankResponseCallback);
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
                getClient().setVolume(volume, mBlankResponseCallback);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }
    };

    private FutureCallback<PopcornTimeRpcClient.RpcResponse> mSelectionCallback = new FutureCallback<PopcornTimeRpcClient.RpcResponse>() {
        @Override
        public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
            try {
                if (result != null && e == null) {
                    LinkedTreeMap<String, Object> mapResult = result.getMapResult();
                    String type = null;
                    if(mapResult.containsKey("type")) type = (String) mapResult.get("type");
                    String posterUrl = "";
                    if(type != null && type.equals("movie")) {
                        posterUrl = ((String) mapResult.get("image")).replace("-300.jpg", ".jpg");
                    } else {
                        LinkedTreeMap<String, String> images = (LinkedTreeMap<String, String>) mapResult.get("images");
                        posterUrl = images.get("poster").replace("-300.jpg", ".jpg");
                    }
                    Ion.with(getActivity()).load(posterUrl).asBitmap().setCallback(new FutureCallback<Bitmap>() {
                        @Override
                        public void onCompleted(Exception e, final Bitmap bitmap) {
                            if(bitmap != null) {
                                Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette paramPalette) {
                                        try {
                                            coverImage.setImageBitmap(bitmap);
                                            coverImage.setVisibility(View.VISIBLE);
                                            Animation fadeInAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
                                            coverImage.startAnimation(fadeInAnim);
                                            Integer color = paramPalette.getVibrantColor().getRgb();
                                            slidingPanelTopLayout.setBackgroundColor(color);
                                            slidingPanelBottomLayout.setBackgroundColor(color);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    });

                    Set<String> subsSet = ((LinkedTreeMap<String, String>) result.getMapResult().get("subtitle")).keySet();
                    ArrayList<String> subsData = new ArrayList<String>();
                    subsData.addAll(subsSet);
                    subsData.add(0, "no-subs");
                    SubtitleAdapter adapter = new SubtitleAdapter(getActivity(), subsData);
                    subsSpinner.setAdapter(adapter);
                }
            } catch(Exception exception) { exception.printStackTrace(); }
        }
    };

    private FutureCallback<PopcornTimeRpcClient.RpcResponse> mPlayingCallback = new FutureCallback<PopcornTimeRpcClient.RpcResponse>() {
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
    };

    private FutureCallback<PopcornTimeRpcClient.RpcResponse> mFullscreenCallback  = new FutureCallback<PopcornTimeRpcClient.RpcResponse>() {
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
    };

    private Runnable mPlayingRunnable = new Runnable() {
        @Override
        public void run() {
            getClient().getPlaying(mPlayingCallback);
        }
    };

    private Runnable mFullscreenRunnable = new Runnable() {
        @Override
        public void run() {
            getClient().getFullscreen(mFullscreenCallback);
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

        volumeControl.setOnSeekBarChangeListener(mOnVolumeControlChangeListener);

        getClient().getSelection(mSelectionCallback);

        if(Version.compare(getClient().getVersion(), "0.0.0")) {
            mPlayingRunnable.run();
            mFullscreenRunnable.run();
        } else {
            currentTime.setVisibility(View.GONE);
            playPauseButton.setImageResource(R.drawable.ic_action_playpause);
        }

        if(Version.compare(getClient().getVersion(), "0.3.4")) {
            subsSpinnerBlock.setVisibility(View.VISIBLE);
        }

        return v;
    }

    private void updateViews() {
        if(Version.compare(getClient().getVersion(), "0.0.0")) {
            LogUtils.d("PlayerControllerFragment", "UpdateViews");
            if (mPlaying) {
                playPauseButton.setImageResource(R.drawable.ic_action_pause);
            } else {
                playPauseButton.setImageResource(R.drawable.ic_action_play);
            }

            if (mFullscreen) {
                fullscreenButton.setImageResource(R.drawable.ic_action_smallscreen);
            } else {
                fullscreenButton.setImageResource(R.drawable.ic_action_fullscreen);
            }
        }
    }

}
