package io.gresse.hugo.anecdote.view;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

/**
 * Container for {@link SimpleExoPlayerView}
 *
 * Created by Hugo Gresse on 23/06/2017.
 */

public class PlayerContainerView extends FrameLayout implements ExoPlayer.EventListener {

    public static final String TAG = "PlayerContainerView";

    public static final String BUNDLE_URL = "url";

    private SimpleExoPlayerView mPlayerView;
    private SimpleExoPlayer     mPlayer;
    private boolean             mIsPlaying;
    private TrackSelector       mTrackSelector;
    private String              mVideoUrl;

    public PlayerContainerView(@NonNull Context context) {
        super(context);
    }

    public PlayerContainerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerContainerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PlayerContainerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPlayerView = (SimpleExoPlayerView) getChildAt(0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onViewAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            mIsPlaying = false;
            mTrackSelector = null;
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.i(TAG, "onSaveInstanceState");

        Bundle bundle = new Bundle();

        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putString(BUNDLE_URL, mVideoUrl);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.i(TAG, "onRestoreInstanceState");
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mVideoUrl = bundle.getString(BUNDLE_URL);

            if (mVideoUrl != null) {
                setVideoUrl(mVideoUrl);
            }

            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
            return;
        }

        super.onRestoreInstanceState(state);
    }

    public void setControllerDisplay(boolean displayController){
        if(mPlayerView != null){
            mPlayerView.setUseController(displayController);
        }
    }

    public void setVideoUrl(String url) {
        mVideoUrl = url;
        boolean needNewPlayer = mPlayer == null;
        if (needNewPlayer) {
            TrackSelection.Factory trackSelectionFactory = new FixedTrackSelection.Factory();
            mTrackSelector = new DefaultTrackSelector(trackSelectionFactory);

            mPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), mTrackSelector);
            mPlayer.addListener(this);

            mPlayerView.setPlayer(mPlayer);
            mPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        }

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(),
                Util.getUserAgent(getContext(), "unknown"), null);
        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource(Uri.parse(url),
                dataSourceFactory, extractorsFactory, null, null);
        LoopingMediaSource loopingMediaSource = new LoopingMediaSource(videoSource);
        // Prepare the player with the source.
        mPlayer.prepare(loopingMediaSource);
        mPlayer.setVolume(0);
        mPlayerView.requestFocus();
        mPlayer.setPlayWhenReady(true); // autoplay
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    /* *************************
     * Implements ExoPlayer.EventListener
     ***************************/

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case ExoPlayer.STATE_READY:
            case ExoPlayer.STATE_IDLE:
            case ExoPlayer.STATE_BUFFERING:
                mIsPlaying = true;
                break;
            case ExoPlayer.STATE_ENDED:
                mIsPlaying = false;
                break;
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Toast.makeText(getContext(), "Player Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
}
