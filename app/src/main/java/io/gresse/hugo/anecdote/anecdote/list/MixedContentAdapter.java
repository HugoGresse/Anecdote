package io.gresse.hugo.anecdote.anecdote.list;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.anecdote.model.MediaType;
import io.gresse.hugo.anecdote.tracking.EventTracker;

/**
 * A generic adapters for all anecdotes
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
class MixedContentAdapter
        extends RecyclerView.Adapter<AnecdoteAdapter.BaseAnecdoteViewHolder>
        implements AnecdoteAdapter {

    public static final String TAG = MixedContentAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_LOAD    = 0;
    private static final int VIEW_TYPE_TEXT    = 1;
    private static final int VIEW_TYPE_IMAGE   = 2;
    private static final int VIEW_TYPE_VIDEO   = 3;
    private static final int VIEW_TYPE_UNKNOWN = 4;


    private List<Anecdote>  mAnecdotes;
    private boolean         mIsSinglePage;
    @Nullable
    private AdapterListener mAdapterListener;
    private int             mTextSize;
    private boolean         mRowStriping;
    private int             mRowBackground;
    private int             mRowStripingBackground;
    private int mExpandedPosition = -1;

    MixedContentAdapter(@Nullable AdapterListener adapterListener, boolean isSinglePage) {
        mAnecdotes = new ArrayList<>();
        mAdapterListener = adapterListener;
        mIsSinglePage = isSinglePage;
    }

    @Override
    public void setData(final List<Anecdote> quotes) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                    new AnecdoteListDiffCallback(
                            mAnecdotes,
                            quotes)
            );

            if (!mAnecdotes.isEmpty()) {
                mAnecdotes.clear();
                mAnecdotes.addAll(quotes);
                diffResult.dispatchUpdatesTo(this);
            } else {
                // Prevnet recyclerView follow the loading wheel when first items are just added
                mAnecdotes.addAll(quotes);
                this.notifyDataSetChanged();
            }

        } else {
            // Run this on main thread
            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    MixedContentAdapter.this.setData(quotes);
                }
            };
            mainHandler.post(runnable);
        }
    }

    @Override
    public void onViewDetachedFromWindow(BaseAnecdoteViewHolder holder) {
        holder.onViewDetached();
        super.onViewDetachedFromWindow(holder);
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
                return new UnknownViewHolder(v, mAdapterListener, this, mTextSize, mRowStriping, mRowBackground, mRowStripingBackground);
            case VIEW_TYPE_TEXT:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_anecdote, parent, false);
                return new MixedBaseViewHolder(v, mAdapterListener, this, mTextSize, mRowStriping, mRowBackground, mRowStripingBackground);
            case VIEW_TYPE_IMAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_anecdote_image, parent, false);
                return new ImageViewHolder(v, mAdapterListener, this, mTextSize, mRowStriping, mRowBackground, mRowStripingBackground);
            case VIEW_TYPE_VIDEO:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_anecdote_video, parent, false);
                return new VideoViewHolder(v, mAdapterListener, this, mTextSize, mRowStriping, mRowBackground, mRowStripingBackground);
            case VIEW_TYPE_LOAD:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_loader, parent, false);
                return new LoadViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(BaseAnecdoteViewHolder holder, int position) {
        // already done in method below
    }

    @Override
    public void onBindViewHolder(BaseAnecdoteViewHolder holder, int position, List<Object> payloads) {
        if (position < mAnecdotes.size()) {
            holder.setData(position, mExpandedPosition == position, mAnecdotes.get(position));
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
    public int getContentItemCount() {
        return mAnecdotes.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mAnecdotes.size()) {
            Anecdote anecdote = mAnecdotes.get(position);
            if (TextUtils.isEmpty(anecdote.type)) {
                EventTracker.trackError("MixedContentAdapter", "Unknow type, using TEXT: " + anecdote.type);
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
                    EventTracker.trackError("MixedContentAdapter", "Unknow type: " + anecdote.type);
                    Log.e(TAG, "Unknow type: " + anecdote.type);
                    return VIEW_TYPE_UNKNOWN;
            }
        } else {
            return VIEW_TYPE_LOAD;
        }
    }

    @Override
    public Anecdote getItem(int position) {
        return mAnecdotes.get(position);
    }

    /**
     * Set expanded item at the given position, used by viewHolder to prevent the adapter of a new expandedPosition and
     * save it here
     * @param position the expanded position
     */
    public void toggleExpanded(int position){
        if (mExpandedPosition == position) {
            Log.d(TAG, "onClick close current ");
            mExpandedPosition = -1;
            notifyItemChanged(position);
            return;
        }

        // Notify expanded last position
        if(mExpandedPosition >= 0){
            notifyItemChanged(mExpandedPosition);
        }
        mExpandedPosition = position;
        // Notify new element
        notifyItemChanged(mExpandedPosition);
        mExpandedPosition = position;
    }

    private class LoadViewHolder extends BaseAnecdoteViewHolder {

        LoadViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void setData(int position, boolean isExpanded, Anecdote anecdote) {
            // This view is static, no need to change it's data
        }
    }

}
