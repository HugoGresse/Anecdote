package io.gresse.hugo.anecdote.anecdote.fullscreen;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.tracking.EventTracker;
import io.gresse.hugo.anecdote.view.PlayerView;

/**
 * Fullscreen video
 * <p/>
 * Created by Hugo Gresse on 24/04/16.
 */
public class FullscreenVideoFragment extends FullscreenFragment {

    public static final String TAG             = FullscreenVideoFragment.class.getSimpleName();
    public static final String BUNDLE_VIDEOURL = "videoUrl";

    private String mVideoUrl;

    @BindView(R.id.playerView)
    public PlayerView mPlayerView;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mVideoUrl = getArguments().getString(BUNDLE_VIDEOURL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fullscreen_video, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!TextUtils.isEmpty(mVideoUrl)) {
            mPlayerView.setVideoUrl(mVideoUrl);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        EventTracker.trackFragmentView(this, null, EventTracker.CONTENT_TYPE_ANECDOTE);
    }

    @OnClick(R.id.playerView)
    public void onClick(){
        super.toggleOverlayVisibility();
    }

    @OnLongClick(R.id.playerView)
    public boolean onLongClick(){
        super.onContentLongTouch(mVideoUrl, null);
        return true;
    }
}
