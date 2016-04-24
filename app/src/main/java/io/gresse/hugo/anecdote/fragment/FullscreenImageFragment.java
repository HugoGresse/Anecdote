package io.gresse.hugo.anecdote.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.event.BusProvider;
import io.gresse.hugo.anecdote.event.ChangeFullscreenEvent;
import io.gresse.hugo.anecdote.event.EnterTransitionEndEvent;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Display an image in fullscreen. It listen for the end of the fragment shared element transition to intialise the
 * enhanced ImaveView using PhotoView library.
 * <p/>
 * Created by Hugo Gresse on 20/04/16.
 */
public class FullscreenImageFragment extends Fragment {

    public static final String TAG             = FullscreenImageFragment.class.getSimpleName();
    public static final String BUNDLE_IMAGEURL = "imageUrl";

    private String mImageUrl;
    PhotoViewAttacher mAttacher;

    @Bind(R.id.imageView)
    public ImageView mImageView;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BusProvider.getInstance().post(new ChangeFullscreenEvent(true));

        if (getArguments() != null) {
            mImageUrl = getArguments().getString(BUNDLE_IMAGEURL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_fullscreen_image, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Glide.with(getContext())
                .load(mImageUrl)
                .fitCenter()
                .into(mImageView);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }


    /***************************
     * Event
     ***************************/

    @Subscribe
    public void onTransitionEnd(EnterTransitionEndEvent event) {
        new PhotoViewAttacher(mImageView);
    }

}
