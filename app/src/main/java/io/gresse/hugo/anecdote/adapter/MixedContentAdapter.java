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
import io.gresse.hugo.anecdote.view.PlayerView;

/**
 * A generic adapters for all anecdotes
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class MixedContentAdapter extends AnecdoteAdapter {

    public static final String TAG = MixedContentAdapter.class.getSimpleName();

    public static final int VIEW_TYPE_LOAD  = 0;
    public static final int VIEW_TYPE_IMAGE = 1;
    public static final int VIEW_TYPE_VIDEO = 2;

    private List<Anecdote>             mAnecdotes;
    @Nullable
    private AnecdoteViewHolderListener mAnecdoteViewHolderListener;
    private int                        mTextSize;
    private boolean                    mRowStriping;
    private int                        mRowStripingBackground;

    public MixedContentAdapter(@Nullable AnecdoteViewHolderListener anecdoteViewHolderListener) {
        mAnecdotes = new ArrayList<>();
        mAnecdoteViewHolderListener = anecdoteViewHolderListener;
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
            case VIEW_TYPE_IMAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_anecdote_image, parent, false);
                return new AnecdoteViewHolder(v);
            case VIEW_TYPE_VIDEO:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_anecdote_video, parent, false);
                return new AnecdoteViewHolder(v);
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
            } else {
                Log.e(TAG, "unknow type");
            }
            return VIEW_TYPE_IMAGE;
        } else {
            return VIEW_TYPE_LOAD;
        }
    }

    /***************************
     * ViewHolder
     ***************************/

    public class AnecdoteViewHolder extends BaseAnecdoteViewHolder implements View.OnLongClickListener, View.OnClickListener {

        View mItemView;

        @Bind(R.id.contentTextView)
        TextView mTextView;

        @Nullable
        @Bind(R.id.imageView)
        ImageView mImageView;

        @Nullable
        @Bind(R.id.exoplayerView)
        PlayerView mPlayerView;

        public AnecdoteViewHolder(View itemView) {
            super(itemView);

            mItemView = itemView;
            ButterKnife.bind(this, itemView);
            itemView.setOnLongClickListener(this);

            if (mPlayerView != null) {
                mPlayerView.setOnClickListener(this);
                mPlayerView.setOnLongClickListener(this);
            }
            if (mImageView != null) {
                mImageView.setOnClickListener(this);
                mImageView.setOnLongClickListener(this);
            }
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

            if (anecdote.mixedContent != null) {
                String log = "setData: " + anecdote.content;
                switch (anecdote.mixedContent.type) {
                    case RichContent.TYPE_IMAGE:
                        if (mImageView != null) {

                            ViewCompat.setTransitionName(mImageView, String.valueOf(position) + "_image");

                            Glide.with(mImageView.getContext())
                                    .load(anecdote.mixedContent.contentUrl)
                                    .fitCenter()
                                    .into(mImageView);

                        }
                        log += " /image: " + anecdote.mixedContent.contentUrl;
                        break;
                    case RichContent.TYPE_VIDEO:
                        if (mPlayerView != null) {
                            mPlayerView.setVideoUrl(anecdote.mixedContent.contentUrl);
                        }
                        log += " /video: " + anecdote.mixedContent.contentUrl;

                        break;
                    default:
                        Log.w(TAG, "Unknown RichContent type");
                        break;
                }

                Log.d(TAG, log);
            } else if (mImageView != null) {
                mImageView.setImageResource(android.R.color.transparent);
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
            if (mAnecdoteViewHolderListener != null) {
                if(mImageView != null){
                    mAnecdoteViewHolderListener.onClick(
                            mAnecdotes.get(getAdapterPosition()),
                            mImageView);
                } else if(mPlayerView != null){
                    mAnecdoteViewHolderListener.onClick(
                            mAnecdotes.get(getAdapterPosition()),
                            mPlayerView);
                }
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
