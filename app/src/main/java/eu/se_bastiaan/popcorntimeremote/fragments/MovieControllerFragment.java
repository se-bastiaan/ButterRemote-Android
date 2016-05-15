package eu.se_bastiaan.popcorntimeremote.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.Bind;
import eu.se_bastiaan.popcorntimeremote.Constants;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.rpc.PopcornTimeRpcClient;
import eu.se_bastiaan.popcorntimeremote.utils.ActionBarBackground;
import eu.se_bastiaan.popcorntimeremote.utils.PixelUtils;
import eu.se_bastiaan.popcorntimeremote.utils.Version;

public class MovieControllerFragment extends BaseControlFragment {

    private static final String OVERLAY_FRAGMENT = "overlay_fragment";
    private Drawable mPlayButtonDrawable;
    private LinkedTreeMap<String, Object> mCurrentMap;
    private Integer mLastScrollLocation = 0, mPaletteColor = R.color.primary, mOpenBarPos, mHeaderHeight, mToolbarHeight, mParallaxHeight;
    private Boolean mTransparentBar = true, mOpenBar = true, mIsFavourited = false;

    View toolbar;
    @Bind(R.id.scrollView)
    ParallaxScrollView scrollView;
    @Bind(R.id.coverImage)
    ImageView coverImage;
    @Bind(R.id.mainInfoBlock)
    RelativeLayout mainInfoBlock;
    @Bind(R.id.playButton)
    ImageButton playButton;
    @Bind(R.id.titleText)
    TextView titleText;
    @Bind(R.id.yearText)
    TextView yearText;
    @Bind(R.id.runtimeText)
    TextView runtimeText;
    @Bind(R.id.ratingText)
    TextView ratingText;
    @Bind(R.id.synopsisText)
    TextView synopsisText;
    @Bind(R.id.favouriteText)
    TextView favouriteText;
    @Bind(R.id.synopsisBlock)
    LinearLayout synopsisBlock;
    @Bind(R.id.qualityBlock)
    LinearLayout qualityBlock;
    @Bind(R.id.favouriteBlock)
    LinearLayout favouriteBlock;
    @Bind(R.id.trailerBlock)
    LinearLayout trailerBlock;
    @Bind(R.id.subtitlesBlock)
    LinearLayout subtitlesBlock;
    @Bind(R.id.playerBlock)
    LinearLayout playerBlock;
    @Bind(R.id.topDivider)
    View topDivider;

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.playButton:
                    getClient().enter(mBlankResponseCallback);
                    break;
                case R.id.qualityBlock:
                    getClient().toggleQuality(mBlankResponseCallback);
                    break;
                case R.id.synopsisBlock:
                    SynopsisDialogFragment synopsisDialogFragment = new SynopsisDialogFragment();
                    Bundle b = new Bundle();
                    b.putString("text", (String) mCurrentMap.get("synopsis"));
                    synopsisDialogFragment.setArguments(b);
                    synopsisDialogFragment.show(getActivity().getSupportFragmentManager(), OVERLAY_FRAGMENT);
                    break;
                case R.id.subtitlesBlock:
                    SubtitleSelectorDialogFragment subtitleFragment = new SubtitleSelectorDialogFragment();
                    subtitleFragment.setArguments(getArguments());
                    subtitleFragment.show(getActivity().getSupportFragmentManager(), OVERLAY_FRAGMENT);
                    break;
                case R.id.playerBlock:
                    PlayerSelectorDialogFragment playerFragment = new PlayerSelectorDialogFragment();
                    playerFragment.setArguments(getArguments());
                    playerFragment.show(getActivity().getSupportFragmentManager(), OVERLAY_FRAGMENT);
                    break;
                case R.id.favouriteBlock:
                    getClient().toggleFavourite(mBlankResponseCallback);
                    mIsFavourited = !mIsFavourited;
                    if(mIsFavourited) {
                        favouriteText.setText(R.string.remove_favourite);
                    } else {
                        favouriteText.setText(R.string.add_favourite);
                    }
                    break;
                case R.id.trailerBlock:
                    getClient().playTrailer(new PopcornTimeRpcClient.Callback() {
                        @Override
                        public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
                            if(e != null) {
                                String videoId = mCurrentMap.get("trailer").toString().replace("http://youtube.com/watch?v=", "");
                                if(videoId != null && getActivity() != null) {
                                    Intent intent = YouTubeStandalonePlayer.createVideoIntent(getActivity(), Constants.YOUTUBE_KEY, videoId, 0, true, true);
                                    startActivity(intent);
                                }
                            }
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    };

    private final ViewTreeObserver.OnScrollChangedListener mOnScrollListener = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();

            if(scrollView.getScrollY() > mHeaderHeight) {
                if (mLastScrollLocation > scrollView.getScrollY()) {
                    // scroll up
                    if ((mOpenBarPos == null || !mOpenBar) && layoutParams.topMargin <= -mToolbarHeight)
                        mOpenBarPos = scrollView.getScrollY() - mToolbarHeight;
                    mOpenBar = true;
                } else if (mLastScrollLocation < scrollView.getScrollY()) {
                    // scroll down
                    if (mOpenBarPos == null || mOpenBar)
                        mOpenBarPos = scrollView.getScrollY();
                    mOpenBar = false;
                }

                if (layoutParams.topMargin <= 0)
                    layoutParams.topMargin = mOpenBarPos - scrollView.getScrollY();

                if (layoutParams.topMargin > 0) {
                    layoutParams.topMargin = 0;
                }
            }

                /* Fade out when over header */
            if(mParallaxHeight - scrollView.getScrollY() < 0) {
                if(mTransparentBar) {
                    mTransparentBar = false;
                    ActionBarBackground.changeColor((AppCompatActivity) getActivity(), mPaletteColor, false);
                }
            } else {
                if(!mTransparentBar) {
                    mTransparentBar = true;
                    ActionBarBackground.fadeOut((AppCompatActivity) getActivity());
                }
            }

            toolbar.setLayoutParams(layoutParams);

            mLastScrollLocation = scrollView.getScrollY();
        }
    };

    private final PopcornTimeRpcClient.Callback mSelectionCallback = new PopcornTimeRpcClient.Callback() {
        @Override
        public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
            try {
                if (result != null && e == null) {
                    mCurrentMap = result.getMapResult();

                    // movie info
                    final String title = (String) mCurrentMap.get("title");
                    final String synopsis = (String) mCurrentMap.get("synopsis");
                    final String year;
                    if(mCurrentMap.get("year") instanceof Double) {
                        year = Integer.toString(((Double) mCurrentMap.get("year")).intValue());
                    } else {
                        year = (String) mCurrentMap.get("year");
                    }
                    final String runtime = Integer.toString(((Double) mCurrentMap.get("runtime")).intValue());
                    String tempRating;
                    try {
                        tempRating = (String) mCurrentMap.get("rating");
                    } catch (ClassCastException ex) {
                        tempRating = Double.toString( (Double) mCurrentMap.get("rating") );
                    }
                    final String rating = tempRating;

                    mIsFavourited = (Boolean) mCurrentMap.get("bookmarked");

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            titleText.setText(title);
                            synopsisText.setText(synopsis);
                            yearText.setText(year);
                            runtimeText.setText(runtime + " " + getString(R.string.minutes));
                            ratingText.setText(rating + "/10");

                            if(mIsFavourited) {
                                favouriteText.setText(R.string.remove_favourite);
                            } else {
                                favouriteText.setText(R.string.add_favourite);
                            }

                            if(!mCurrentMap.containsKey("trailer")) {
                                trailerBlock.setVisibility(View.GONE);
                                topDivider.setVisibility(View.GONE);
                            }
                        }
                    });

                    // poster/color
                    final String posterUrl;
                    if(mCurrentMap.containsKey("image")) {
                        posterUrl = ((String) mCurrentMap.get("image")).replace("-300.jpg", ".jpg");
                    } else {
                        LinkedTreeMap<String, String> images = (LinkedTreeMap<String, String>) mCurrentMap.get("images");
                        posterUrl = images.get("poster").replace("-300.jpg", ".jpg");
                    }

                    final String backdropUrl;
                    if(mCurrentMap.containsKey("backdrop")) {
                        backdropUrl = (String) mCurrentMap.get("backdrop");
                    } else {
                        LinkedTreeMap<String, String> images = (LinkedTreeMap<String, String>) mCurrentMap.get("images");
                        backdropUrl = images.get("fanart");
                    }

                    Bitmap poster = Picasso.with(getActivity()).load(posterUrl).get();
                    Palette palette = Palette.generate(poster);

                    try {
                        int vibrantColor = palette.getVibrantColor(R.color.primary);
                        if (vibrantColor == R.color.primary) {
                            mPaletteColor = palette.getMutedColor(R.color.primary);
                        } else {
                            mPaletteColor = vibrantColor;
                        }

                        final ObjectAnimator mainInfoBlockColorFade = ObjectAnimator.ofObject(mainInfoBlock, "backgroundColor", new ArgbEvaluator(), getResources().getColor(R.color.primary), mPaletteColor);
                        mainInfoBlockColorFade.setDuration(500);
                        Drawable oldDrawable = PixelUtils.changeDrawableColor(getActivity(), R.drawable.ic_av_play_button, getResources().getColor(R.color.primary));
                        mPlayButtonDrawable = PixelUtils.changeDrawableColor(getActivity(), R.drawable.ic_av_play_button, mPaletteColor);
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
                                        if(getActivity() == null) return;
                                        Animation fadeInAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);

                                        mainInfoBlockColorFade.start();
                                        td.startTransition(500);
                                        coverImage.setVisibility(View.VISIBLE);
                                        coverImage.startAnimation(fadeInAnim);
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_moviecontroller, container, false);
        ButterKnife.bind(this, v);

        Drawable playButtonDrawable = PixelUtils.changeDrawableColor(getActivity(), R.drawable.ic_av_play_button, getResources().getColor(R.color.primary));
        if(mPlayButtonDrawable == null) playButton.setImageDrawable(playButtonDrawable);

        playButton.setOnClickListener(mOnClickListener);
        synopsisBlock.setOnClickListener(mOnClickListener);
        trailerBlock.setOnClickListener(mOnClickListener);
        subtitlesBlock.setOnClickListener(mOnClickListener);
        favouriteBlock.setOnClickListener(mOnClickListener);
        qualityBlock.setOnClickListener(mOnClickListener);
        playerBlock.setOnClickListener(mOnClickListener);

        mParallaxHeight = PixelUtils.getPixelsFromDp(getActivity(), 228);
        toolbar = getActionBarView();
        mToolbarHeight = toolbar.getHeight();
        mHeaderHeight = mParallaxHeight - mToolbarHeight;
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(mOnScrollListener);

        if(!Version.compare(getClient().getVersion(), "0.0.0")) {
            playerBlock.setVisibility(View.GONE);
            trailerBlock.setVisibility(View.GONE);
            synopsisBlock.setVisibility(View.GONE);
        }

        getClient().getSelection(mSelectionCallback);

        return v;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        scrollView.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollListener);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
                try {
                    if (layoutParams.topMargin < 0) {
                        int height;
                        if (layoutParams.topMargin < -mToolbarHeight) {
                            height = -mToolbarHeight;
                        } else {
                            height = layoutParams.topMargin;
                        }

                        for (int i = height; i != 1; i++) {
                            layoutParams.topMargin = i;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    toolbar.setLayoutParams(layoutParams);
                                }
                            });
                        }
                    }
                } catch(Exception e) {
                    layoutParams.topMargin = 0;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            toolbar.setLayoutParams(layoutParams);
                        }
                    });
                }
                return null;
            }
        }.execute();
    }

}
