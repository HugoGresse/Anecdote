package io.gresse.hugo.anecdote.anecdote.fullscreen;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.view.PlayerView;

/**
 * Display an a video in fullscreen.
 *
 * TODO : add more control over the video
 *
 * Created by Hugo Gresse on 20/06/2017.
 */

public class FullscreenVideoActivity extends FullscreenActivity {

    @BindView(R.id.playerView)
    public PlayerView mPlayerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPlayerView.setTransitionName(mMediaTransitionName);
            startPostponedEnterTransition(mPlayerView);
        }

        if (!TextUtils.isEmpty(mAnecdote.media)) {
            mPlayerView.setVideoUrl(mAnecdote.media);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_fullscreen_video;
    }

    @OnClick(R.id.playerView)
    public void onClick(){
        super.toggleOverlayVisibility();
    }

    @OnLongClick(R.id.playerView)
    public boolean onLongClick(){
        super.onContentLongTouch(mAnecdote.media, null);
        return true;
    }

}
