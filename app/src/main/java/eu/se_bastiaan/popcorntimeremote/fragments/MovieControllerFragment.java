package eu.se_bastiaan.popcorntimeremote.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeIntents;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.gson.internal.LinkedTreeMap;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eu.se_bastiaan.popcorntimeremote.Constants;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.fadingactionbar.FadingActionBarHelper;
import eu.se_bastiaan.popcorntimeremote.graphics.Palette;
import eu.se_bastiaan.popcorntimeremote.graphics.PaletteItem;
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

        Drawable playButtonDrawable = PixelUtils.changeDrawableColor(getActivity(), R.drawable.ic_av_play_button, getResources().getColor(R.color.accent_color));
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

        return v;
    }

    private FutureCallback<PopcornTimeRpcClient.RpcResponse> mSelectionCallback = new FutureCallback<PopcornTimeRpcClient.RpcResponse>() {
        @Override
        public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
            try {
                if (result != null && e == null) {
                    mCurrentMap = result.getMapResult();

                    // movie info
                    String title = (String) mCurrentMap.get("title");
                    String synopsis = (String) mCurrentMap.get("synopsis");
                    String year = Integer.toString(((Double) mCurrentMap.get("year")).intValue());
                    String runtime = Integer.toString(((Double) mCurrentMap.get("runtime")).intValue());
                    String rating = (String) mCurrentMap.get("rating");

                    titleText.setText(title);
                    synopsisText.setText(synopsis);
                    yearText.setText(year);
                    runtimeText.setText(runtime + " " + getString(R.string.minutes));
                    ratingText.setText(rating + "/10");

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

                    Ion.with(getActivity()).load(posterUrl).asBitmap().setCallback(new FutureCallback<Bitmap>() {
                        @Override
                        public void onCompleted(Exception e, final Bitmap bitmap) {
                            if(bitmap != null) {
                                Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
                                        try {
                                            PaletteItem paletteItem = palette.getVibrantColor();
                                            final Integer color;
                                            if(paletteItem != null) {
                                                color = paletteItem.getRgb();
                                            } else {
                                                paletteItem = palette.getMutedColor();
                                                color = paletteItem.getRgb();
                                            }

                                            final ObjectAnimator mainInfoBlockColorFade = ObjectAnimator.ofObject(mainInfoBlock, "backgroundColor", new ArgbEvaluator(), getResources().getColor(R.color.accent_color), color);
                                            mainInfoBlockColorFade.setDuration(500);

                                            Drawable oldDrawable = PixelUtils.changeDrawableColor(getActivity(), R.drawable.ic_av_play_button, getResources().getColor(R.color.accent_color));
                                            mPlayButtonDrawable = PixelUtils.changeDrawableColor(getActivity(), R.drawable.ic_av_play_button, color);
                                            final TransitionDrawable td = new TransitionDrawable(new Drawable[] { oldDrawable, mPlayButtonDrawable });
                                            playButton.setImageDrawable(td);

                                            Ion.with(getActivity()).load(backdropUrl).asBitmap().setCallback(new FutureCallback<Bitmap>() {
                                                @Override
                                                public void onCompleted(Exception e, Bitmap result) {
                                                    coverImage.setImageBitmap(result);

                                                    Animation fadeInAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
                                                    mainInfoBlockColorFade.start();
                                                    td.startTransition(500);
                                                    coverImage.setVisibility(View.VISIBLE);
                                                    coverImage.startAnimation(fadeInAnim);

                                                    mFadingHelper.actionBarBackground(ActionBarBackground.getColoredBackground(getActivity(), color)).initActionBar(getActivity());
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }

                        }
                    });
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
                    .actionBarBackground(R.drawable.ab_solid_pt_remote)
                    .headerLayout(R.layout.fragment_detailheader)
                    .contentLayout(R.layout.fragment_moviecontroller);
            mFadingHelper.initActionBar(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }

        getClient().getSelection(mSelectionCallback);
    }

}
