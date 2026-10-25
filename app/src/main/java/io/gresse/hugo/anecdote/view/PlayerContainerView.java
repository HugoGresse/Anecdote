package io.gresse.hugo.anecdote.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.BundleCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

/**
 * Container for a Media3 {@link PlayerView}, playing a muted looping video.
 *
 * Created by Hugo Gresse on 23/06/2017.
 */
public class PlayerContainerView extends FrameLayout implements Player.Listener {

    public static final String TAG = "PlayerContainerView";

    private static final String BUNDLE_URL            = "url";
    private static final String BUNDLE_INSTANCE_STATE = "instanceState";

    private PlayerView mPlayerView;
    @Nullable
    private ExoPlayer  mPlayer;
    @Nullable
    private String     mVideoUrl;

    public PlayerContainerView(@NonNull Context context) {
        super(context);
    }

    public PlayerContainerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerContainerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPlayerView = (PlayerView) getChildAt(0);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releasePlayer();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(BUNDLE_INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putString(BUNDLE_URL, mVideoUrl);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof Bundle bundle)) {
            super.onRestoreInstanceState(state);
            return;
        }

        String videoUrl = bundle.getString(BUNDLE_URL);
        if (videoUrl != null) {
            setVideoUrl(videoUrl);
        }
        super.onRestoreInstanceState(BundleCompat.getParcelable(bundle, BUNDLE_INSTANCE_STATE, Parcelable.class));
    }

    public void setControllerDisplay(boolean displayController) {
        mPlayerView.setUseController(displayController);
    }

    public void setVideoUrl(String url) {
        mVideoUrl = url;
        if (mPlayer == null) {
            mPlayer = new ExoPlayer.Builder(getContext()).build();
            mPlayer.addListener(this);
            mPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            mPlayer.setVolume(0);
            mPlayerView.setPlayer(mPlayer);
        }

        mPlayer.setMediaItem(MediaItem.fromUri(url));
        mPlayer.prepare();
        mPlayerView.requestFocus();
        mPlayer.setPlayWhenReady(true);
    }

    private void releasePlayer() {
        if (mPlayer == null) return;
        mPlayerView.setPlayer(null);
        mPlayer.release();
        mPlayer = null;
    }

    @Override
    public void onPlayerError(@NonNull PlaybackException error) {
        Log.w(TAG, "Player error for " + mVideoUrl, error);
        Toast.makeText(getContext(), "Player Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
