package io.gresse.hugo.anecdote.anecdote.list;

import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.Bind;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.util.EventUtils;

/**
 * Display images in a view to be used by RecyclerView as an ViewHolder
 *
 * Created by Hugo Gresse on 08/11/2016.
 */
public class ImageViewHolder extends MixedBaseViewHolder implements View.OnClickListener, RequestListener<String, GlideDrawable> {

    private static final  String TAG         = ImageViewHolder.class.getSimpleName();
    private static final int    RETRY_COUNT = 2;

    private int mRetried;
    @Nullable
    private String mImageUrl;

    @Bind(R.id.imageView)
    ImageView mImageView;



    public ImageViewHolder(View itemView,
                           AdapterListener adapterListener,
                           MixedContentAdapter adapter,
                           int textSize,
                           boolean rowStriping,
                           int rowBackground,
                           int rawStripingBackground) {
        super(itemView, adapterListener, adapter, textSize, rowStriping, rowBackground, rawStripingBackground);

        if (mImageView != null) {
            mImageView.setOnClickListener(this);
        }
    }

    @Override
    public void setData(int position, Anecdote anecdote) {
        super.setData(position, anecdote);
        String log = "setData: url:" + anecdote.media + " text:" + anecdote.text;

        ViewCompat.setTransitionName(mImageView, String.valueOf(position) + "_image");

        reset();
        mImageUrl = anecdote.media;
        loadImage();

        Log.d(TAG, log);
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        reset();
    }

    @Override
    public void onClick(View v) {
        if (!(v instanceof ImageView)) {
            super.onClick(v);
            return;
        }
        if (mAdapterListener != null && mImageView != null) {
            mAdapterListener.onClick(
                    mAdapter.getItem(getAdapterPosition()),
                    mImageView,
                    AdapterListener.ACTION_FULLSCREEN);
        }
    }

    /**
     * Reset glide listener and retry count on view unbind/new data
     */
    private void reset(){
        mRetried = 0;
        Glide.clear(mImageView);
    }

    /**
     * Load the image in the view
     */
    private void loadImage(){
        if(TextUtils.isEmpty(mImageUrl)){
            return;
        }

        Glide.with(mImageView.getContext())
                .load(mImageUrl)
                .fitCenter()
                .error(R.drawable.ic_error_white_24dp)
                .listener(this)
                .into(mImageView);
    }

    /**
     * GLIDE LISTENER
     */

    @Override
    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
        if(mRetried < RETRY_COUNT){
            mRetried ++;
            Log.d(TAG, "Retry " + mImageUrl + " retried?" + mRetried);
            loadImage();
            return true;
        }

        EventUtils.trackError("Image download", e.toString());
        return false;
    }

    @Override
    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
        reset();
        return false;
    }
}
