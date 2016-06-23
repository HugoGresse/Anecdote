package io.gresse.hugo.anecdote.adapter;

import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.Html;
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
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.model.MediaType;
import io.gresse.hugo.anecdote.util.FabricUtils;
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
    private int                        mExpandedPosition = -1;

    public MixedContentAdapter(@Nullable AnecdoteViewHolderListener anecdoteViewHolderListener, boolean isSinglePage) {
        mAnecdotes = new ArrayList<>();
        mAnecdoteViewHolderListener = anecdoteViewHolderListener;
        mIsSinglePage = isSinglePage;
    }

    @Override
    public void setData(List<Anecdote> quotes) {
        mAnecdotes = quotes;
        notifyDataSetChanged();
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
        if (position + 1 <= mAnecdotes.size()) {
            Anecdote anecdote = mAnecdotes.get(position);
            switch (anecdote.type) {
                case MediaType.TEXT:
                    return VIEW_TYPE_TEXT;
                case MediaType.IMAGE:
                    return VIEW_TYPE_IMAGE;
                case MediaType.VIDEO:
                    return VIEW_TYPE_VIDEO;
                default:
                    FabricUtils.trackError("MixedContentAdapter", "Unknow type: " + anecdote.type);
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

    public abstract class MixedBaseViewHolder extends BaseAnecdoteViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener {

        protected View mItemView;

        @Bind(R.id.contentTextView)
        protected TextView mTextView;

        @Bind(R.id.expandLayout)
        protected LinearLayout mExpandLayout;

        public MixedBaseViewHolder(View itemView) {
            super(itemView);

            mItemView = itemView;
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void setData(int position, Anecdote anecdote, boolean expanded) {
            mTextView.setText(Html.fromHtml(anecdote.text));
            mTextView.setTextSize(mTextSize);

            if (mRowStriping) {
                if (position % 2 == 0) {
                    mItemView.setBackgroundColor(mRowStripingBackground);
                } else {
                    mItemView.setBackgroundColor(mRowBackground);
                }
            }


            if(mExpandLayout == null){
                return;
            }

            if (expanded) {
                mExpandLayout.setVisibility(View.VISIBLE);
                ((ViewGroup.MarginLayoutParams)itemView.getLayoutParams()).topMargin = 50;
                ((ViewGroup.MarginLayoutParams)itemView.getLayoutParams()).bottomMargin = 50;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    itemView.setElevation(8);
                }
            } else {
                mExpandLayout.setVisibility(View.GONE);
                ((ViewGroup.MarginLayoutParams)itemView.getLayoutParams()).topMargin = 0;
                ((ViewGroup.MarginLayoutParams)itemView.getLayoutParams()).bottomMargin = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    itemView.setElevation(0);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mAnecdoteViewHolderListener != null) {
                mAnecdoteViewHolderListener.onLongClick(mAnecdotes.get(getAdapterPosition()));
                return true;
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            if(mExpandedPosition == getAdapterPosition()){
                mExpandedPosition = -1;
                notifyItemChanged(getAdapterPosition());
                return;
            }
            // Notify expanded last position
            notifyItemChanged(mExpandedPosition);
            mExpandedPosition = getAdapterPosition();
            // Notify new element
            notifyItemChanged(mExpandedPosition);
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
                mImageView.setOnLongClickListener(this);
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
        public boolean onLongClick(View v) {
            if (mAnecdoteViewHolderListener != null) {
                mAnecdoteViewHolderListener.onLongClick(mAnecdotes.get(getAdapterPosition()));
                return true;
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            if(!(v instanceof ImageView)){
                super.onClick(v);
                return;
            }
            if (mAnecdoteViewHolderListener != null) {
                if (mImageView != null) {
                    mAnecdoteViewHolderListener.onClick(
                            mAnecdotes.get(getAdapterPosition()),
                            mImageView);
                }
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
                mPlayerView.setOnLongClickListener(this);
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
            if(!(v instanceof PlayerView)){
                super.onClick(v);
                return;
            }
            if (mAnecdoteViewHolderListener != null) {
                mAnecdoteViewHolderListener.onClick(
                        mAnecdotes.get(getAdapterPosition()),
                        mPlayerView);
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
        public void setData(int position, Anecdote anecdote, boolean expanded) {
            super.setData(position, anecdote, expanded);
        }

        @Override
        public void onClick(View v) {
            if(!(v instanceof LinearLayout)){
                super.onClick(v);
                return;
            }
            if (mAnecdoteViewHolderListener != null) {
                Log.d(TAG, "onClick");
                mAnecdoteViewHolderListener.onClick(
                        mAnecdotes.get(getAdapterPosition()),
                        null);
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

        }
    }

}
