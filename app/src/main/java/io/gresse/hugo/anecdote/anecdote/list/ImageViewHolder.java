package io.gresse.hugo.anecdote.anecdote.list;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.MediaContextDialog;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.databinding.RowAnecdoteExpandBinding;
import io.gresse.hugo.anecdote.databinding.RowAnecdoteImageBinding;
import io.gresse.hugo.anecdote.view.CustomImageView;

/**
 * Display images in a view to be used by RecyclerView as an ViewHolder
 *
 * TODO : improve blank image sometines, see https://github.com/bumptech/glide/issues/550
 *
 * Created by Hugo Gresse on 08/11/2016.
 */
public class ImageViewHolder extends MixedBaseViewHolder implements View.OnClickListener, RequestListener<Drawable>, View.OnLongClickListener {

    private static final  String TAG         = ImageViewHolder.class.getSimpleName();
    private static final int    RETRY_COUNT = 2;

    private int mRetried;
    @Nullable
    private String mImageUrl;

    private final CustomImageView mImageView;

    public ImageViewHolder(RowAnecdoteImageBinding binding,
                           AdapterListener adapterListener,
                           MixedContentAdapter adapter,
                           RowStyle rowStyle) {
        super(binding.getRoot(),
                binding.contentTextView,
                binding.separator,
                RowAnecdoteExpandBinding.bind(binding.getRoot()),
                adapterListener,
                adapter,
                rowStyle);

        mImageView = binding.imageView;
        mImageView.setOnClickListener(this);
        mImageView.setOnLongClickListener(this);
    }

    @Override
    public void setData(int position, boolean isExpanded, String websiteName, Anecdote anecdote) {
        super.setData(position, isExpanded, websiteName, anecdote);

        ViewCompat.setTransitionName(mImageView, position + "_image");

        reset();
        mImageUrl = anecdote.media;
        loadImage();
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        // We do not reset the view here, see http://stackoverflow.com/a/40073839/1377145
    }

    @Override
    public void onClick(View v) {
        if (!(v instanceof ImageView)) {
            super.onClick(v);
            return;
        }
        if (mAdapterListener != null) {
            mAdapterListener.onClick(
                    mAdapter.getItem(getBindingAdapterPosition()),
                    mImageView,
                    AdapterListener.ACTION_FULLSCREEN);
        }
    }

    /**
     * Reset glide listener and retry count on view unbind/new data
     */
    private void reset(){
        mRetried = 0;
        Glide.with(mImageView).clear(mImageView);
    }

    /**
     * Load the image in the view
     */
    private void loadImage(){
        if(TextUtils.isEmpty(mImageUrl)){
            return;
        }

        Glide.with(mImageView)
                .load(mImageUrl)
                .listener(this)
                .error(R.drawable.ic_error_white_24dp)
                .into(mImageView);

        mImageView.setOnLongClickListener(view -> {
            MediaContextDialog.openDialog(mImageView.getContext(), mWebsiteName, mCurrentAnecdote, mImageUrl, mImageView);
            return true;
        });
    }

    /**
     * GLIDE LISTENER
     */

    @Override
    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model,
                                @NonNull Target<Drawable> target, boolean isFirstResource) {
        if(mRetried < RETRY_COUNT && mImageUrl != null){
            mRetried ++;
            Log.d(TAG, "Retry " + mImageUrl + " retried?" + mRetried);
            loadImage();
            return true;
        }

        return false;
    }

    @Override
    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target,
                                   @NonNull DataSource dataSource, boolean isFirstResource) {
        return false;
    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }
}
