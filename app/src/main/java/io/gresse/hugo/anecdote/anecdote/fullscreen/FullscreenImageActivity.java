package io.gresse.hugo.anecdote.anecdote.fullscreen;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;

import butterknife.BindView;
import io.gresse.hugo.anecdote.R;

/**
 * Display an image in fullscreen with zooming.
 * <p>
 * Created by Hugo Gresse on 20/06/2017.
 */

public class FullscreenImageActivity extends FullscreenActivity implements OnPhotoTapListener, View.OnLongClickListener {

    @BindView(R.id.imageView)
    public PhotoView mPhotoView;

    RequestListener<String, GlideDrawable> mGlideRequestListener = new RequestListener<String, GlideDrawable>() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target,
                                   boolean isFirstResource) {
            startPostponedEnterTransition(mPhotoView);
            return false;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target,
                                       boolean isFromMemoryCache, boolean isFirstResource) {
            startPostponedEnterTransition(mPhotoView);
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPhotoView.setTransitionName(mMediaTransitionName);
        }

        Glide.with(this)
                .load(mAnecdote.media)
                .listener(mGlideRequestListener)
                .into(mPhotoView);

        mPhotoView.setOnPhotoTapListener(this);
        mPhotoView.setOnLongClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPhotoView != null) {
            Glide.clear(mPhotoView);
        }
    }

    @Override
    int getLayoutRes() {
        return R.layout.activity_fullscreen_image;
    }

    /* **************************
     * Implements PhotoView listeners
     ***************************/

    @Override
    public void onPhotoTap(ImageView view, float x, float y) {
        super.toggleOverlayVisibility();
    }

    @Override
    public boolean onLongClick(View view) {
        super.onContentLongTouch(mAnecdote.media, mPhotoView);
        return true;
    }

}
