package io.gresse.hugo.anecdote.anecdote.fullscreen;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;

import io.gresse.hugo.anecdote.databinding.ActivityFullscreenImageBinding;

/**
 * Display an image in fullscreen with zooming.
 * <p>
 * Created by Hugo Gresse on 20/06/2017.
 */

public class FullscreenImageActivity extends FullscreenActivity implements OnPhotoTapListener, View.OnLongClickListener {

    private PhotoView mPhotoView;

    private final RequestListener<Drawable> mGlideRequestListener = new RequestListener<>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model,
                                    @NonNull Target<Drawable> target, boolean isFirstResource) {
            startPostponedEnterTransition(mPhotoView);
            return false;
        }

        @Override
        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target,
                                       @NonNull DataSource dataSource, boolean isFirstResource) {
            startPostponedEnterTransition(mPhotoView);
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isFinishing()) return;

        mPhotoView.setTransitionName(mMediaTransitionName);

        Glide.with(this)
                .load(mAnecdote.media)
                .listener(mGlideRequestListener)
                .into(mPhotoView);

        mPhotoView.setOnPhotoTapListener(this);
        mPhotoView.setOnLongClickListener(this);
    }

    @Override
    protected View inflateContentView() {
        ActivityFullscreenImageBinding binding = ActivityFullscreenImageBinding.inflate(getLayoutInflater());
        mPhotoView = binding.imageView;
        return binding.getRoot();
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
