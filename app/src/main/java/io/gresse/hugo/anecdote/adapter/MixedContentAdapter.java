package io.gresse.hugo.anecdote.adapter;

import android.graphics.Color;
import android.support.annotation.Nullable;
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

/**
 * A generic adapters for all anecdotes
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class MixedContentAdapter extends AnecdoteAdapter {

    public static final String TAG = MixedContentAdapter.class.getSimpleName();

    public static final int VIEW_TYPE_LOAD    = 0;
    public static final int VIEW_TYPE_CONTENT = 1;

    private List<Anecdote>     mAnecdotes;
    @Nullable
    private ViewHolderListener mViewHolderListener;
    private int                mTextSize;
    private boolean            mRowStriping;
    private int                mRowStripingBackground;

    public MixedContentAdapter(@Nullable ViewHolderListener viewHolderListener) {
        mAnecdotes = new ArrayList<>();
        mViewHolderListener = viewHolderListener;
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
            case VIEW_TYPE_CONTENT:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_anecdote_image, parent, false);
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
            return VIEW_TYPE_CONTENT;
        } else {
            return VIEW_TYPE_LOAD;
        }
    }

    /***************************
     * ViewHolder
     ***************************/

    public class AnecdoteViewHolder extends BaseAnecdoteViewHolder implements View.OnLongClickListener {

        View mItemView;

        @Bind(R.id.contentTextView)
        TextView mTextView;

        @Bind(R.id.imageView)
        ImageView mImageView;

        public AnecdoteViewHolder(View itemView) {
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

            if (anecdote.mixedContent != null) {
                Glide.with(mImageView.getContext())
                        .load(anecdote.mixedContent.contentUrl)
                        .fitCenter()
                        .into(mImageView);

                Log.d(TAG, "Image uri: " + anecdote.mixedContent.contentUrl);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mViewHolderListener != null) {
                mViewHolderListener.onLongClick(mAnecdotes.get(getAdapterPosition()));
                return true;
            }
            return false;
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
