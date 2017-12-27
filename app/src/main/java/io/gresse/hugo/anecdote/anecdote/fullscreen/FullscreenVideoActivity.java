package io.gresse.hugo.anecdote.anecdote.fullscreen;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.view.PlayerContainerView;

/**
 * Display an a video in fullscreen.
 *
 * TODO : add more control over the video
 *
 * Created by Hugo Gresse on 20/06/2017.
 */

public class FullscreenVideoActivity extends FullscreenActivity {

    @BindView(R.id.playerContainer)
    public PlayerContainerView mPlayerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPlayerView.setTransitionName(mMediaTransitionName);
            startPostponedEnterTransition(mPlayerView);
        }

        if (!TextUtils.isEmpty(mAnecdote.media)) {
            mPlayerView.setVideoUrl(mAnecdote.media);
            mPlayerView.setControllerDisplay(true);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_fullscreen_video;
    }

    @OnClick(R.id.playerContainer)
    public void onClick(){
        super.toggleOverlayVisibility();
    }

    @OnLongClick(R.id.playerContainer)
    public boolean onLongClick(){
        super.onContentLongTouch(mAnecdote.media, null);
        return true;
    }

}
