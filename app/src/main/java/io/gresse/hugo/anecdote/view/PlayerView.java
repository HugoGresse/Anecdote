package io.gresse.hugo.anecdote.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import io.gresse.hugo.simpleexoplayer.MediaFile;
import io.gresse.hugo.simpleexoplayer.player.SimpleExoPlayer;
import io.gresse.hugo.simpleexoplayer.player.SimpleExoPlayerListener;
import io.gresse.hugo.simpleexoplayer.util.Utils;
import io.gresse.hugo.simpleexoplayer.view.AspectRatioTextureView;


/**
 * Wrap SimpleExoPlayer
 *
 * Created by Hugo Gresse on 18/04/16.
 */
public class PlayerView extends AspectRatioTextureView implements SimpleExoPlayerListener {

    public static final String TAG = PlayerView.class.getSimpleName();

    public static final String BUNDLE_STATE_PLAYING = "playing";

    private SimpleExoPlayer         mSimpleExoPlayer;
    private MediaFile               mMediaFile;
    private boolean                 mAutoPlay;
    private boolean                 mPlayInBackground;
    private boolean                 mPreLoad;
    private boolean                 mIsAttachedToWindow;

    public PlayerView(Context context) {
        super(context);
        init(null, 0, 0);
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, 0);
    }

    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        final TypedArray a = getContext().obtainStyledAttributes(attrs, io.gresse.hugo.simpleexoplayer.R.styleable.ExoplayerView, defStyleAttr, 0);
        mAutoPlay = a.getBoolean(io.gresse.hugo.simpleexoplayer.R.styleable.ExoplayerView_autoPlay, true);
        mPreLoad = a.getBoolean(io.gresse.hugo.simpleexoplayer.R.styleable.ExoplayerView_preload, true);
        mPlayInBackground = a.getBoolean(io.gresse.hugo.simpleexoplayer.R.styleable.ExoplayerView_playInBackground, false);
        String videoUrl = a.getString(io.gresse.hugo.simpleexoplayer.R.styleable.ExoplayerView_videoUrl);
        a.recycle();

        if (!TextUtils.isEmpty(videoUrl)) {
            setVideoUrl(videoUrl);
        }

        maybeCreatePlayer();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        Log.i(TAG, "onAttachedToWindow");
        mIsAttachedToWindow = true;

        if (mSimpleExoPlayer == null && !maybeCreatePlayer()) {
            return;
        }

        mSimpleExoPlayer.attach(getContext(), this, 0, getId());

        if (mAutoPlay && !mSimpleExoPlayer.isAutoPlay()) {
            mSimpleExoPlayer.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
        mIsAttachedToWindow = false;
        if(mSimpleExoPlayer != null && !mSimpleExoPlayer.isReleased()){
            mSimpleExoPlayer.release();
            mSimpleExoPlayer = null;
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.i(TAG, "onSaveInstanceState");

        Bundle bundle = new Bundle();

        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putBoolean(BUNDLE_STATE_PLAYING, mSimpleExoPlayer.isPlaying());

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.i(TAG, "onRestoreInstanceState");
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            boolean wasPlaying = bundle.getBoolean(BUNDLE_STATE_PLAYING);

            if (mSimpleExoPlayer == null && !maybeCreatePlayer()) {
                return;
            }

            if (wasPlaying) {
                mSimpleExoPlayer.start();
            }

            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
            return;
        }

        super.onRestoreInstanceState(state);
    }
    /***************************
     * Getter/Setter
     ***************************/

    /**
     * Get the current video url
     *
     * @return the video url
     */
    @Nullable
    public String getVideoUrl() {
        return mMediaFile.mediaFileURL;
    }

    /**
     * Set the video url to load and get the correct mime type from it
     *
     * @param videoUrl the video url, local or remote
     */
    public void setVideoUrl(@NonNull String videoUrl) {
        mMediaFile = new MediaFile(videoUrl);
        mMediaFile.type = Utils.getMimeType(videoUrl);
        if (mSimpleExoPlayer != null) {
            mSimpleExoPlayer.release();
        }
        if(maybeCreatePlayer() && mIsAttachedToWindow){
            mSimpleExoPlayer.attach(getContext(), this, 0, getId());

            if (mAutoPlay && !mSimpleExoPlayer.isAutoPlay()) {
                mSimpleExoPlayer.start();
            }
        }
    }

    /**
     * Get the current player, if any
     */
    @Nullable
    public SimpleExoPlayer getPlayer() {
        return mSimpleExoPlayer;
    }

    /**
     * Set the current view player
     */
    public void setPlayer(@Nullable SimpleExoPlayer simpleExoPlayer) {
        mSimpleExoPlayer = simpleExoPlayer;
    }

    /***************************
     * Private methods
     ***************************/

    /**
     * Create the player based on current class members
     *
     * @return true if player created, false either
     */
    private boolean maybeCreatePlayer() {
        if (mMediaFile == null) {
            Log.d(TAG, "No video to play");
            return false;
        }

        mSimpleExoPlayer = new SimpleExoPlayer(getContext(), mMediaFile, this);
        mSimpleExoPlayer.init();

        if (mPreLoad) {
            mSimpleExoPlayer.preLoad();
        }

        if (mPlayInBackground) {
            mSimpleExoPlayer.setAllowPlayInBackground(true);
        }
        return true;
    }


    /***************************
     * Implement SimpleExoPlayerListener
     ***************************/

    @Override
    public void playerIsLoaded() {

    }

    @Override
    public void playerViewAttached() {

    }

    @Override
    public void playerError(Exception e) {

    }

    @Override
    public void playerWillStartPlaying() {

    }

    @Override
    public void playerStartPlaying() {

    }

    @Override
    public void playerSurfaceDestroyedShouldPause() {

    }

    @Override
    public void playerFinishPlaying() {
        mSimpleExoPlayer.restart();
    }

    @Override
    public void playerPublishProgress(long milliSecond) {

    }
}
