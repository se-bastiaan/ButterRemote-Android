package eu.se_bastiaan.popcorntimeremote.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.gson.internal.LinkedTreeMap;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eu.se_bastiaan.popcorntimeremote.Constants;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.fadingactionbar.FadingActionBarHelper;
import eu.se_bastiaan.popcorntimeremote.rpc.PopcornTimeRpcClient;
import eu.se_bastiaan.popcorntimeremote.utils.ActionBarBackground;
import eu.se_bastiaan.popcorntimeremote.utils.PixelUtils;
import eu.se_bastiaan.popcorntimeremote.utils.Version;

public class MovieControllerFragment extends BaseControlFragment {

    private FadingActionBarHelper mFadingHelper;
    private Drawable mPlayButtonDrawable;
    private LinkedTreeMap<String, Object> mCurrentMap;

    @InjectView(R.id.coverImage)
    ImageView coverImage;
    @InjectView(R.id.mainInfoBlock)
    RelativeLayout mainInfoBlock;
    @InjectView(R.id.playButton)
    ImageButton playButton;
    @InjectView(R.id.titleText)
    TextView titleText;
    @InjectView(R.id.yearText)
    TextView yearText;
    @InjectView(R.id.runtimeText)
    TextView runtimeText;
    @InjectView(R.id.ratingText)
    TextView ratingText;
    @InjectView(R.id.synopsisText)
    TextView synopsisText;
    @InjectView(R.id.synopsisBlock)
    LinearLayout synopsisBlock;
    @InjectView(R.id.qualityBlock)
    LinearLayout qualityBlock;
    @InjectView(R.id.favouriteBlock)
    LinearLayout favouriteBlock;
    @InjectView(R.id.trailerBlock)
    LinearLayout trailerBlock;
    @InjectView(R.id.subtitlesBlock)
    LinearLayout subtitlesBlock;
    @InjectView(R.id.playerBlock)
    LinearLayout playerBlock;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.playButton:
                    getClient().enter(mBlankResponseCallback);
                    break;
                case R.id.qualityBlock:
                    getClient().toggleQuality(mBlankResponseCallback);
                    break;
                case R.id.subtitlesBlock:
                    SubtitleSelectorDialogFragment subtitleFragment = new SubtitleSelectorDialogFragment();
                    subtitleFragment.setArguments(getArguments());
                    subtitleFragment.show(getActivity().getSupportFragmentManager(), "subtitle_fragment");
                    break;
                case R.id.playerBlock:
                    PlayerSelectorDialogFragment playerFragment = new PlayerSelectorDialogFragment();
                    playerFragment.setArguments(getArguments());
                    playerFragment.show(getActivity().getSupportFragmentManager(), "player_fragment");
                    break;
                case R.id.favouriteBlock:
                    getClient().toggleFavourite(mBlankResponseCallback);
                    break;
                case R.id.trailerBlock:
                    String videoId = mCurrentMap.get("trailer").toString().replace("http://youtube.com/watch?v=", "");
                    Intent intent = YouTubeStandalonePlayer.createVideoIntent(getActivity(), Constants.YOUTUBE_KEY, videoId, 0, true, true);
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = mFadingHelper.createView(inflater);
        ButterKnife.inject(this, v);

        Drawable playButtonDrawable = PixelUtils.changeDrawableColor(getActivity(), R.drawable.ic_av_play_button, getResources().getColor(R.color.primary));
        if(mPlayButtonDrawable == null) playButton.setImageDrawable(playButtonDrawable);

        playButton.setOnClickListener(mOnClickListener);
        trailerBlock.setOnClickListener(mOnClickListener);
        subtitlesBlock.setOnClickListener(mOnClickListener);
        favouriteBlock.setOnClickListener(mOnClickListener);
        qualityBlock.setOnClickListener(mOnClickListener);
        playerBlock.setOnClickListener(mOnClickListener);

        if(!Version.compare(getClient().getVersion(), "0.0.0")) {
            playerBlock.setVisibility(View.GONE);
            trailerBlock.setVisibility(View.GONE);
            synopsisBlock.setVisibility(View.GONE);
        }

        getClient().getSelection(mSelectionCallback);

        return v;
    }

    private PopcornTimeRpcClient.Callback mSelectionCallback = new PopcornTimeRpcClient.Callback() {
        @Override
        public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
            try {
                if (result != null && e == null) {
                    mCurrentMap = result.getMapResult();

                    // movie info
                    final String title = (String) mCurrentMap.get("title");
                    final String synopsis = (String) mCurrentMap.get("synopsis");
                    final String year = Integer.toString(((Double) mCurrentMap.get("year")).intValue());
                    final String runtime = Integer.toString(((Double) mCurrentMap.get("runtime")).intValue());
                    final String rating = (String) mCurrentMap.get("rating");

                    mHandler.post(new Runnable() {
                          @Override
                          public void run() {
                              titleText.setText(title);
                              synopsisText.setText(synopsis);
                              yearText.setText(year);
                              runtimeText.setText(runtime + " " + getString(R.string.minutes));
                              ratingText.setText(rating + "/10");
                          }
                    });

                    // poster/color
                    String type = null;
                    if(mCurrentMap.containsKey("type")) type = (String) mCurrentMap.get("type");
                    final String posterUrl;
                    if(type != null && type.equals("movie")) {
                        posterUrl = ((String) mCurrentMap.get("image")).replace("-300.jpg", ".jpg");
                    } else {
                        LinkedTreeMap<String, String> images = (LinkedTreeMap<String, String>) mCurrentMap.get("images");
                        posterUrl = images.get("poster").replace("-300.jpg", ".jpg");
                    }

                    final String backdropUrl;
                    if(type != null && type.equals("movie")) {
                        backdropUrl = (String) mCurrentMap.get("backdrop");
                    } else {
                        LinkedTreeMap<String, String> images = (LinkedTreeMap<String, String>) mCurrentMap.get("images");
                        backdropUrl = images.get("fanart");
                    }

                    Bitmap poster = Picasso.with(getActivity()).load(posterUrl).get();
                    Palette palette = Palette.generate(poster);

                    try {
                        int vibrantColor = palette.getVibrantColor(R.color.primary);
                        final int color;
                        if (vibrantColor == R.color.primary) {
                            color = palette.getMutedColor(R.color.primary);
                        } else {
                            color = vibrantColor;
                        }

                        final ObjectAnimator mainInfoBlockColorFade = ObjectAnimator.ofObject(mainInfoBlock, "backgroundColor", new ArgbEvaluator(), getResources().getColor(R.color.primary), color);
                        mainInfoBlockColorFade.setDuration(500);
                        Drawable oldDrawable = PixelUtils.changeDrawableColor(getActivity(), R.drawable.ic_av_play_button, getResources().getColor(R.color.primary));
                        mPlayButtonDrawable = PixelUtils.changeDrawableColor(getActivity(), R.drawable.ic_av_play_button, color);
                        final TransitionDrawable td = new TransitionDrawable(new Drawable[]{oldDrawable, mPlayButtonDrawable});

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                playButton.setImageDrawable(td);
                            }
                        });

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Picasso.with(getActivity()).load(backdropUrl).into(coverImage, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Animation fadeInAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);

                                        mainInfoBlockColorFade.start();
                                        td.startTransition(500);
                                        coverImage.setVisibility(View.VISIBLE);
                                        coverImage.startAnimation(fadeInAnim);

                                        mFadingHelper.actionBarBackground(ActionBarBackground.getColoredBackground(color)).initActionBar(getActivity());
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            } catch(Exception exception) {
                exception.printStackTrace();
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mFadingHelper = new FadingActionBarHelper()
                    .actionBarBackground(ActionBarBackground.getColoredBackground(getResources().getColor(R.color.primary)))
                    .headerLayout(R.layout.fragment_detailheader)
                    .contentLayout(R.layout.fragment_moviecontroller);
            mFadingHelper.initActionBar(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
