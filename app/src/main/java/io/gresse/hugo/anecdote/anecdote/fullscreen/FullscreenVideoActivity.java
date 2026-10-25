package io.gresse.hugo.anecdote.anecdote.fullscreen;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import io.gresse.hugo.anecdote.databinding.ActivityFullscreenVideoBinding;
import io.gresse.hugo.anecdote.view.PlayerContainerView;

/**
 * Display an a video in fullscreen.
 *
 * TODO : add more control over the video
 *
 * Created by Hugo Gresse on 20/06/2017.
 */

public class FullscreenVideoActivity extends FullscreenActivity {

    private PlayerContainerView mPlayerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isFinishing()) return;

        mPlayerView.setTransitionName(mMediaTransitionName);
        startPostponedEnterTransition(mPlayerView);

        if (!TextUtils.isEmpty(mAnecdote.media)) {
            mPlayerView.setVideoUrl(mAnecdote.media);
            mPlayerView.setControllerDisplay(true);
        }
    }

    @Override
    protected View inflateContentView() {
        ActivityFullscreenVideoBinding binding = ActivityFullscreenVideoBinding.inflate(getLayoutInflater());
        mPlayerView = binding.playerContainer;
        mPlayerView.setOnClickListener(v -> toggleOverlayVisibility());
        mPlayerView.setOnLongClickListener(v -> {
            onContentLongTouch(mAnecdote.media, null);
            return true;
        });
        return binding.getRoot();
    }

}
