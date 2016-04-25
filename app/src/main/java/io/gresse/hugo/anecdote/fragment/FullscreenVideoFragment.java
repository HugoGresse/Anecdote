package io.gresse.hugo.anecdote.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.event.ChangeFullscreenEvent;
import io.gresse.hugo.anecdote.util.FabricUtils;
import io.gresse.hugo.anecdote.view.PlayerView;

/**
 * Fullscreen video
 *
 * Created by Hugo Gresse on 24/04/16.
 */
public class FullscreenVideoFragment extends Fragment {

    public static final String TAG             = FullscreenVideoFragment.class.getSimpleName();
    public static final String BUNDLE_VIDEOURL = "videoUrl";

    private String mVideoUrl;

    @Bind(R.id.playerView)
    public PlayerView mPlayerView;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BusProvider.getInstance().post(new ChangeFullscreenEvent(true));

        if (getArguments() != null) {
            mVideoUrl = getArguments().getString(BUNDLE_VIDEOURL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_fullscreen_video, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(!TextUtils.isEmpty(mVideoUrl)){
            mPlayerView.setVideoUrl(mVideoUrl);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        BusProvider.getInstance().post(new ChangeFullscreenEvent(false));
    }

    @Override
    public void onResume() {
        super.onResume();

        BusProvider.getInstance().register(this);
        FabricUtils.trackFragmentView(this, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }
}
