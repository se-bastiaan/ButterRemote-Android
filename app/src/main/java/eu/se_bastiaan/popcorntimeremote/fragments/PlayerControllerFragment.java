package eu.se_bastiaan.popcorntimeremote.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.google.gson.internal.LinkedTreeMap;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.rpc.PopcornTimeRpcClient;
import eu.se_bastiaan.popcorntimeremote.utils.LogUtils;
import eu.se_bastiaan.popcorntimeremote.utils.PrefUtils;
import eu.se_bastiaan.popcorntimeremote.utils.Version;

public class PlayerControllerFragment extends BaseControlFragment {

    private Boolean mPlaying = false, mSeeked = false, mVolumeChanged = false, mFullscreen = false;
    private Integer mCurrentTime, mMax, mVolume;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @InjectView(R.id.slidingLayout)
    SlidingUpPanelLayout slidingLayout;
    @InjectView(R.id.coverImage)
    ImageView coverImage;
    @InjectView(R.id.backwardButton)
    ImageButton backwardButton;
    @InjectView(R.id.playPauseButton)
    ImageButton playPauseButton;
    @InjectView(R.id.forwardButton)
    ImageButton forwardButton;
    @InjectView(R.id.slidingPanelTopLayout)
    LinearLayout slidingPanelTopLayout;
    @InjectView(R.id.currentProgress)
    SeekBar currentTime;
    @InjectView(R.id.volumeControl)
    SeekBar volumeControl;
    @InjectView(R.id.fullscreenBlock)
    LinearLayout fullscreenBlock;
    @InjectView(R.id.fullscreenBlockImage)
    ImageView fullscreenBlockImage;
    @InjectView(R.id.subtitlesBlock)
    LinearLayout subtitlesBlock;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.fullscreenBlock:
                    getClient().toggleFullscreen(mBlankResponseCallback);
                    break;
                case R.id.subtitlesBlock:
                    SubtitleSelectorDialogFragment subtitleFragment = new SubtitleSelectorDialogFragment();
                    subtitleFragment.setArguments(getArguments());
                    subtitleFragment.show(getActivity().getSupportFragmentManager(), "overlay_fragment");
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
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            getClient().seek(progress - mCurrentTime, mBlankResponseCallback);
            mCurrentTime = progress;
            mSeeked = true;
        }
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

    private PopcornTimeRpcClient.Callback mSelectionCallback = new PopcornTimeRpcClient.Callback() {
        @Override
        public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
            try {
                if (result != null && e == null) {
                    LinkedTreeMap<String, Object> mapResult = result.getMapResult();
                    String posterUrl = "";
                    if(mapResult.containsKey("image")) {
                        posterUrl = ((String) mapResult.get("image")).replace("-300.jpg", ".jpg");
                    } else {
                        LinkedTreeMap<String, String> images = (LinkedTreeMap<String, String>) mapResult.get("images");
                        posterUrl = images.get("poster").replace("-300.jpg", ".jpg");
                    }

                    final Bitmap bitmap = Picasso.with(getActivity()).load(posterUrl).get();
                    Palette palette = Palette.generate(bitmap);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            coverImage.setImageBitmap(bitmap);
                        }
                    });

                    int vibrantColor = palette.getVibrantColor(R.color.primary);
                    final int color;
                    if (vibrantColor == R.color.primary) {
                        color = palette.getMutedColor(R.color.primary);
                    } else {
                        color = vibrantColor;
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Animation fadeInAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
                            ObjectAnimator slidingPanelTopLayoutColorFade = ObjectAnimator.ofObject(slidingPanelTopLayout, "backgroundColor", new ArgbEvaluator(), getResources().getColor(R.color.primary), color);
                            slidingPanelTopLayoutColorFade.setDuration(500);

                            slidingPanelTopLayoutColorFade.start();
                            coverImage.setVisibility(View.VISIBLE);
                            coverImage.startAnimation(fadeInAnim);
                        }
                    });
                }
            } catch(Exception exception) {
                exception.printStackTrace();
            }
        }
    };

    private PopcornTimeRpcClient.Callback mPlayingCallback = new PopcornTimeRpcClient.Callback() {
        @Override
        public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
            try {
                if (result != null && e == null) {
                    mPlaying = (Boolean) result.getMapResult().get("playing");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateViews();
                        }
                    });

                    if (mPlaying) {
                        LinkedTreeMap<String, Object> mapResult = result.getMapResult();
                        if (mMax == null) {
                            mMax = ((Double) mapResult.get("duration")).intValue();
                            currentTime.setMax(mMax);
                        }
                        if (!mSeeked) {
                            mCurrentTime = ((Double) mapResult.get("currentTime")).intValue();
                            currentTime.setProgress(mCurrentTime);
                        } else {
                            mSeeked = false;
                        }
                        if(!mVolumeChanged) {
                            Double volume = (Double) mapResult.get("volume");
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

    private PopcornTimeRpcClient.Callback mFullscreenCallback  = new PopcornTimeRpcClient.Callback() {
        @Override
        public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
            try {
                if (result != null && e == null) {
                    mFullscreen = (Boolean) result.getMapResult().get("fullscreen");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateViews();
                        }
                    });
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

        fullscreenBlock.setOnClickListener(mOnClickListener);
        forwardButton.setOnClickListener(mOnClickListener);
        backwardButton.setOnClickListener(mOnClickListener);
        playPauseButton.setOnClickListener(mOnClickListener);
        subtitlesBlock.setOnClickListener(mOnClickListener);

        currentTime.setMax(0);
        currentTime.setProgress(0);
        currentTime.setOnSeekBarChangeListener(mOnTimeControlChangeListener);

        volumeControl.setOnSeekBarChangeListener(mOnVolumeControlChangeListener);

        getClient().getSelection(mSelectionCallback);

        LogUtils.d("Version", getClient().getVersion());

        if(Version.compare(getClient().getVersion(), "0.0.0")) {
            mPlayingRunnable.run();
            mFullscreenRunnable.run();
        } else {
            currentTime.setVisibility(View.GONE);
            playPauseButton.setImageResource(R.drawable.ic_av_playpause);
        }

        if(Version.compare(getClient().getVersion(), "0.3.4")) {
            subtitlesBlock.setVisibility(View.VISIBLE);
        }

        if(!PrefUtils.contains(getActivity(), "learned_panel")) {
            slidingLayout.expandPanel();
            PrefUtils.save(getActivity(), "learned_panel", true);
        }

        return v;
    }

    private void updateViews() {
        if(Version.compare(getClient().getVersion(), "0.0.0")) {
            if (mPlaying) {
                playPauseButton.setImageResource(R.drawable.ic_av_pause);
            } else {
                playPauseButton.setImageResource(R.drawable.ic_av_play);
            }

            if (mFullscreen) {
                fullscreenBlockImage.setImageResource(R.drawable.ic_av_small_screen);
            } else {
                fullscreenBlockImage.setImageResource(R.drawable.ic_av_full_screen);
            }
        }
    }

}
