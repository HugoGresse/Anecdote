package io.gresse.hugo.anecdote.anecdote.fullscreen;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.util.EventUtils;
import uk.co.senab.photoview.PhotoViewAttacher;


/**
 * Display an image in fullscreen. It listen for the end of the fragment shared element transition to intialise the
 * enhanced ImaveView using PhotoView library.
 * <p/>
 * Created by Hugo Gresse on 20/04/16.
 */
public class FullscreenImageFragment extends FullscreenFragment implements PhotoViewAttacher.OnPhotoTapListener, View.OnLongClickListener {

    public static final String TAG             = FullscreenImageFragment.class.getSimpleName();
    public static final String BUNDLE_IMAGEURL = "contentUrl";

    private String mImageUrl;

    @Bind(R.id.imageView)
    public ImageView mImageView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mImageUrl = getArguments().getString(BUNDLE_IMAGEURL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fullscreen_image, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Glide.with(getContext())
                .load(mImageUrl)
                .into(mImageView);

        // No transition compatible
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            createPhotoView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        EventUtils.trackFragmentView(this, null, EventUtils.CONTENT_TYPE_ANECDOTE);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /***************************
     * Private methods
     ***************************/

    private void createPhotoView(){
        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(mImageView);
        photoViewAttacher.setOnPhotoTapListener(this);
        photoViewAttacher.setOnLongClickListener(this);
    }

    /***************************
     * Event
     ***************************/

    @Subscribe
    public void onTransitionEnd(FullscreenEnterTransitionEndEvent event) {
        createPhotoView();
    }

    /***************************
     * implements PhotoViewAttacher.OnPhotoTapListener
     ***************************/

    @Override
    public void onPhotoTap(View view, float x, float y) {
        super.toggleOverlayVisibility();
    }

    @Override
    public boolean onLongClick(View view) {
        super.onContentLongTouch(mImageUrl);
        return true;
    }
}
