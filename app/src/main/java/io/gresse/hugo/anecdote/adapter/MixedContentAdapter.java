package io.gresse.hugo.anecdote.adapter;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.model.RichContent;
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
    public static final int VIEW_TYPE_IMAGE   = 1;
    public static final int VIEW_TYPE_VIDEO   = 2;
    public static final int VIEW_TYPE_UNKNOWN = 3;

    private List<Anecdote>             mAnecdotes;
    private boolean                    mIsSinglePage;
    @Nullable
    private AnecdoteViewHolderListener mAnecdoteViewHolderListener;
    private int                        mTextSize;
    private boolean                    mRowStriping;
    private int                        mRowStripingBackground;

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
    public void setTextStyle(int textSize, boolean rowStriping, int colorBackgroundStripping) {
        mTextSize = textSize;
        mRowStriping = rowStriping;
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
            holder.setData(position, mAnecdotes.get(position));
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
            if (anecdote.mixedContent != null && anecdote.mixedContent.type == RichContent.TYPE_VIDEO) {
                return VIEW_TYPE_VIDEO;
            } else if (anecdote.mixedContent != null && anecdote.mixedContent.type == RichContent.TYPE_IMAGE) {
                return VIEW_TYPE_IMAGE;
            } else if (anecdote.mixedContent != null) {
                FabricUtils.trackError("MixedContentAdapter", "Unknow type: " + anecdote.mixedContent.type);
                Log.e(TAG, "Unknow type: " + anecdote.mixedContent.type);
            }
            return VIEW_TYPE_UNKNOWN;
        } else {
            return VIEW_TYPE_LOAD;
        }
    }

    /***************************
     * ViewHolder
     ***************************/

    public abstract class MixedBaseViewHolder extends BaseAnecdoteViewHolder implements View.OnLongClickListener {

        View mItemView;

        @Bind(R.id.contentTextView)
        TextView mTextView;

        public MixedBaseViewHolder(View itemView) {
            super(itemView);

            mItemView = itemView;
            ButterKnife.bind(this, itemView);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void setData(int position, Anecdote anecdote) {
            mTextView.setText(Html.fromHtml(anecdote.content));
            mTextView.setTextSize(mTextSize);

            if (mRowStriping) {
                if (position % 2 == 0) {
                    mItemView.setBackgroundColor(mRowStripingBackground);
                } else {
                    mItemView.setBackgroundColor(Color.TRANSPARENT);
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

    }

    public class ImageViewHolder extends MixedBaseViewHolder implements View.OnClickListener {

        @Bind(R.id.imageView)
        ImageView mImageView;

        public ImageViewHolder(View itemView) {
            super(itemView);

            if (mImageView != null) {
                mImageView.setOnClickListener(this);
                mImageView.setOnLongClickListener(this);
            }
        }

        @Override
        public void setData(int position, Anecdote anecdote) {
            super.setData(position, anecdote);
            if (anecdote.mixedContent == null) {
                return;
            }
            String log = "setData: url:" + anecdote.mixedContent.contentUrl + " content:" + anecdote.content;

            ViewCompat.setTransitionName(mImageView, String.valueOf(position) + "_image");
            Glide.with(mImageView.getContext())
                    .load(anecdote.mixedContent.contentUrl)
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
        PlayerView mPlayerView;

        public VideoViewHolder(View itemView) {
            super(itemView);

            if (mPlayerView != null) {
                mPlayerView.setOnClickListener(this);
                mPlayerView.setOnLongClickListener(this);
            }
        }

        @Override
        public void setData(int position, Anecdote anecdote) {
            super.setData(position, anecdote);
            if (mPlayerView != null && anecdote.mixedContent != null) {
                mPlayerView.setVideoUrl(anecdote.mixedContent.contentUrl);
            }
        }

        @Override
        public void onClick(View v) {
            if (mAnecdoteViewHolderListener != null) {
                mAnecdoteViewHolderListener.onClick(
                        mAnecdotes.get(getAdapterPosition()),
                        mPlayerView);
            }
        }
    }


    public class UnknownViewHolder extends MixedBaseViewHolder implements View.OnClickListener {

        @Bind(R.id.openLinearLayout)
        public View mContainerLayout;

        public UnknownViewHolder(View itemView) {
            super(itemView);

            mContainerLayout.setOnClickListener(this);
        }

        @Override
        public void setData(int position, Anecdote anecdote) {
            super.setData(position, anecdote);
        }

        @Override
        public void onClick(View v) {
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
        public void setData(int position, Anecdote anecdote) {

        }
    }

}
