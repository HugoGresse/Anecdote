package io.gresse.hugo.anecdote.adapter;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.model.MediaType;
import io.gresse.hugo.anecdote.util.EventUtils;
import io.gresse.hugo.anecdote.view.PlayerView;

/**
 * A generic adapters for all anecdotes
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class MixedContentAdapter extends AnecdoteAdapter {

    public static final String TAG = MixedContentAdapter.class.getSimpleName();

    public static final int VIEW_TYPE_LOAD    = 0;
    public static final int VIEW_TYPE_TEXT    = 1;
    public static final int VIEW_TYPE_IMAGE   = 2;
    public static final int VIEW_TYPE_VIDEO   = 3;
    public static final int VIEW_TYPE_UNKNOWN = 4;


    private List<Anecdote>             mAnecdotes;
    private boolean                    mIsSinglePage;
    @Nullable
    private AnecdoteViewHolderListener mAnecdoteViewHolderListener;
    private int                        mTextSize;
    private boolean                    mRowStriping;
    private int                        mRowBackground;
    private int                        mRowStripingBackground;
    private int mExpandedPosition = -1;

    public MixedContentAdapter(@Nullable AnecdoteViewHolderListener anecdoteViewHolderListener, boolean isSinglePage) {
        mAnecdotes = new ArrayList<>();
        mAnecdoteViewHolderListener = anecdoteViewHolderListener;
        mIsSinglePage = isSinglePage;
    }

    @Override
    public void setData(final List<Anecdote> quotes) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.d(TAG, "setData main thread");
            internalUpdateOnSameThread(quotes);
        } else {
            Log.d(TAG, "setData diff thread");
            // Run this on main thread
            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    internalUpdateOnSameThread(quotes);
                }
            };
            mainHandler.post(runnable);
        }
    }

    /**
     * Notify the adapter of item change on the current thread
     *
     * @param quotes fresh list
     */
    private void internalUpdateOnSameThread(List<Anecdote> quotes) {
        if (quotes.equals(mAnecdotes) && quotes.size() == mAnecdotes.size()) {
            Log.d(TAG, "Same list");
            // List identical, no thing
            return;
        } else {
            Log.d(TAG, "Diff list");
            // number of elements differ
            // Two case: new item added, all item change and count didn't match
            if (quotes.size() > mAnecdotes.size() && mAnecdotes.size() > 2 &&
                    mAnecdotes.get(0).equals(quotes.get(0)) &&
                    mAnecdotes.get(mAnecdotes.size() - 1).equals(quotes.get(mAnecdotes.size() - 1))) {
                Log.d(TAG, "Range inserted : " + mAnecdotes.size() + " to " + quotes.size());
                // First and last item of current Anecdote list and new identical, there is new element
                cloneAnecdoteToCurrent(quotes);
                notifyItemRangeInserted(mAnecdotes.size(), quotes.size() + 1);
            } else {
                Log.d(TAG, "All change");
                cloneAnecdoteToCurrent(quotes);
                // Everything change
                notifyDataSetChanged();
            }
        }
    }

    /**
     * Clone given list instance (shadow, it do not clone inner element)
     *
     * @param quotes list to clone
     */
    private void cloneAnecdoteToCurrent(List<Anecdote> quotes) {
        if (quotes instanceof ArrayList) {
            mAnecdotes = new ArrayList<>(quotes);
        } else {
            mAnecdotes = quotes;
        }
    }

    @Override
    public void setTextStyle(int textSize, boolean rowStriping, int colorBackground, int colorBackgroundStripping) {
        mTextSize = textSize;
        mRowStriping = rowStriping;
        mRowBackground = colorBackground;
        mRowStripingBackground = colorBackgroundStripping;
        notifyDataSetChanged();
    }

    @Override
    public BaseAnecdoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_anecdote_unknown, parent, false);
                return new UnknownViewHolder(v);
            case VIEW_TYPE_TEXT:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_anecdote, parent, false);
                return new TextViewHolder(v);
            case VIEW_TYPE_IMAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_anecdote_image, parent, false);
                return new ImageViewHolder(v);
            case VIEW_TYPE_VIDEO:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_anecdote_video, parent, false);
                return new VideoViewHolder(v);
            case VIEW_TYPE_LOAD:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_loader, parent, false);
                return new LoadViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(BaseAnecdoteViewHolder holder, int position) {
        if (position < mAnecdotes.size()) {
            holder.setData(position, mAnecdotes.get(position), position == mExpandedPosition);
        }
    }

    @Override
    public int getItemCount() {
        if (!mAnecdotes.isEmpty() && mIsSinglePage) {
            return mAnecdotes.size();
        }
        return mAnecdotes.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mAnecdotes.size()) {
            Anecdote anecdote = mAnecdotes.get(position);
            if(TextUtils.isEmpty(anecdote.type)){
                FabricUtils.trackError("MixedContentAdapter", "Unknow type, using TEXT: " + anecdote.type);
                Log.e(TAG, "Unknow type, using TEXT: " + anecdote.type);
                return VIEW_TYPE_TEXT;
            }
            switch (anecdote.type) {
                case MediaType.TEXT:
                    return VIEW_TYPE_TEXT;
                case MediaType.IMAGE:
                    return VIEW_TYPE_IMAGE;
                case MediaType.VIDEO:
                    return VIEW_TYPE_VIDEO;
                default:
                    EventUtils.trackError("MixedContentAdapter", "Unknow type: " + anecdote.type);
                    Log.e(TAG, "Unknow type: " + anecdote.type);
                    return VIEW_TYPE_UNKNOWN;
            }
        } else {
            return VIEW_TYPE_LOAD;
        }
    }

    /***************************
     * ViewHolder
     ***************************/

    public abstract class MixedBaseViewHolder extends BaseAnecdoteViewHolder implements View.OnClickListener {

        protected View mItemView;

        @Bind(R.id.contentTextView)
        protected TextView mTextView;

        @Bind(R.id.expandLayout)
        protected LinearLayout mExpandLayout;

        @Bind(R.id.separator)
        protected View mSeparatorView;

        public MixedBaseViewHolder(View itemView) {
            super(itemView);

            mItemView = itemView;
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void setData(int position, Anecdote anecdote, boolean expanded) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mTextView.setText(Html.fromHtml(anecdote.text, Html.FROM_HTML_MODE_LEGACY));
            } else {
                //noinspection deprecation
                mTextView.setText(Html.fromHtml(anecdote.text));
            }
            mTextView.setTextSize(mTextSize);

            if (mRowStriping) {
                if (position % 2 == 0) {
                    mItemView.setBackgroundColor(mRowStripingBackground);
                } else {
                    mItemView.setBackgroundColor(mRowBackground);
                }
            }


            if (mExpandLayout == null) {
                return;
            }

            if (expanded) {
                mSeparatorView.setVisibility(View.VISIBLE);
                mExpandLayout.setVisibility(View.VISIBLE);
                ((ViewGroup.MarginLayoutParams) itemView.getLayoutParams()).topMargin = 50;
                ((ViewGroup.MarginLayoutParams) itemView.getLayoutParams()).bottomMargin = 50;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    itemView.setElevation(8);
                }
            } else {
                mSeparatorView.setVisibility(View.GONE);
                mExpandLayout.setVisibility(View.GONE);
                ((ViewGroup.MarginLayoutParams) itemView.getLayoutParams()).topMargin = 0;
                ((ViewGroup.MarginLayoutParams) itemView.getLayoutParams()).bottomMargin = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    itemView.setElevation(0);
                }
            }
        }

        @Override
        public void onClick(View v) {
            if (mExpandedPosition == getAdapterPosition()) {
                mExpandedPosition = -1;
                notifyItemChanged(getAdapterPosition());
                return;
            }
            // Notify expanded last position
            notifyItemChanged(mExpandedPosition);
            mExpandedPosition = getAdapterPosition();
            // Notify new element
            notifyItemChanged(mExpandedPosition);
            if (mAnecdoteViewHolderListener != null) {
                mAnecdoteViewHolderListener.onClick(
                        mAnecdotes.get(getAdapterPosition()),
                        itemView,
                        AnecdoteViewHolderListener.ACTION_OPEN_IN_BROWSER_PRELOAD);
            }
        }

        @OnClick(R.id.shareButton)
        public void onShareClick() {
            if (mAnecdoteViewHolderListener != null) {
                mAnecdoteViewHolderListener.onClick(
                        mAnecdotes.get(getAdapterPosition()),
                        itemView,
                        AnecdoteViewHolderListener.ACTION_SHARE);
            }
        }

        @OnClick(R.id.copyButton)
        public void onCopyClick() {
            if (mAnecdoteViewHolderListener != null) {
                mAnecdoteViewHolderListener.onClick(
                        mAnecdotes.get(getAdapterPosition()),
                        itemView,
                        AnecdoteViewHolderListener.ACTION_COPY);
            }
        }

        @OnClick(R.id.openButton)
        public void onOpenClick() {
            if (mAnecdoteViewHolderListener != null) {
                mAnecdoteViewHolderListener.onClick(
                        mAnecdotes.get(getAdapterPosition()),
                        itemView,
                        AnecdoteViewHolderListener.ACTION_OPEN_IN_BROWSER);
            }
        }
    }

    public class TextViewHolder extends MixedBaseViewHolder {

        public TextViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class ImageViewHolder extends MixedBaseViewHolder implements View.OnClickListener {

        @Bind(R.id.imageView)
        protected ImageView mImageView;

        public ImageViewHolder(View itemView) {
            super(itemView);

            if (mImageView != null) {
                mImageView.setOnClickListener(this);
            }
        }

        @Override
        public void setData(int position, Anecdote anecdote, boolean expanded) {
            super.setData(position, anecdote, expanded);
            String log = "setData: url:" + anecdote.media + " text:" + anecdote.text;

            ViewCompat.setTransitionName(mImageView, String.valueOf(position) + "_image");
            Glide.with(mImageView.getContext())
                    .load(anecdote.media)
                    .fitCenter()
                    .into(mImageView);

            Log.d(TAG, log);
        }

        @Override
        public void onClick(View v) {
            if (!(v instanceof ImageView)) {
                super.onClick(v);
                return;
            }
            if (mAnecdoteViewHolderListener != null && mImageView != null) {
                mAnecdoteViewHolderListener.onClick(
                        mAnecdotes.get(getAdapterPosition()),
                        mImageView,
                        AnecdoteViewHolderListener.ACTION_FULLSCREEN);
            }
        }
    }

    public class VideoViewHolder extends MixedBaseViewHolder implements View.OnClickListener {

        @Bind(R.id.exoplayerView)
        protected PlayerView mPlayerView;

        public VideoViewHolder(View itemView) {
            super(itemView);

            if (mPlayerView != null) {
                mPlayerView.setOnClickListener(this);
            }
        }

        @Override
        public void setData(int position, Anecdote anecdote, boolean expanded) {
            super.setData(position, anecdote, expanded);
            if (mPlayerView != null && anecdote.media != null) {
                mPlayerView.setVideoUrl(anecdote.media);
            }
        }

        @Override
        public void onClick(View v) {
            if (!(v instanceof PlayerView)) {
                super.onClick(v);
                return;
            }
            if (mAnecdoteViewHolderListener != null) {
                mAnecdoteViewHolderListener.onClick(
                        mAnecdotes.get(getAdapterPosition()),
                        mPlayerView,
                        AnecdoteViewHolderListener.ACTION_FULLSCREEN);
            }
        }
    }


    public class UnknownViewHolder extends MixedBaseViewHolder implements View.OnClickListener {

        @Bind(R.id.openLinearLayout)
        public LinearLayout mContainerLayout;

        public UnknownViewHolder(View itemView) {
            super(itemView);

            mContainerLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (!(v instanceof LinearLayout)) {
                super.onClick(v);
                return;
            }
            if (mAnecdoteViewHolderListener != null) {
                mAnecdoteViewHolderListener.onClick(
                        mAnecdotes.get(getAdapterPosition()),
                        null, 0);
            }
        }
    }

    public class LoadViewHolder extends BaseAnecdoteViewHolder {

        public LoadViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void setData(int position, Anecdote anecdote, boolean expanded) {
            // This view is static, no need to change it's data
        }
    }

}
