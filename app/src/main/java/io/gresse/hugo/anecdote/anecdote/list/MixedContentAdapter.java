package io.gresse.hugo.anecdote.anecdote.list;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
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
import io.gresse.hugo.anecdote.util.EventUtils;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.anecdote.model.MediaType;

/**
 * A generic adapters for all anecdotes
 * <p/>
 * Created by Hugo Gresse on 13/02/16.
 */
public class MixedContentAdapter
        extends RecyclerView.Adapter<AnecdoteAdapter.BaseAnecdoteViewHolder>
        implements AnecdoteAdapter {

    public static final String TAG = MixedContentAdapter.class.getSimpleName();

    public static final int VIEW_TYPE_LOAD    = 0;
    public static final int VIEW_TYPE_TEXT    = 1;
    public static final int VIEW_TYPE_IMAGE   = 2;
    public static final int VIEW_TYPE_VIDEO   = 3;
    public static final int VIEW_TYPE_UNKNOWN = 4;


    private List<Anecdote>  mAnecdotes;
    private boolean         mIsSinglePage;
    @Nullable
    private AdapterListener mAdapterListener;
    private int             mTextSize;
    private boolean         mRowStriping;
    private int             mRowBackground;
    private int             mRowStripingBackground;

    public MixedContentAdapter(@Nullable AdapterListener adapterListener, boolean isSinglePage) {
        mAnecdotes = new ArrayList<>();
        mAdapterListener = adapterListener;
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

    @Override
    public void onViewDetachedFromWindow(BaseAnecdoteViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onViewDetached();
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
        if (position < mAnecdotes.size()) {
            Anecdote anecdote = mAnecdotes.get(position);
            if (TextUtils.isEmpty(anecdote.type)) {
                EventUtils.trackError("MixedContentAdapter", "Unknow type, using TEXT: " + anecdote.type);
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

    @Override
    public Anecdote getItem(int position) {
        return mAnecdotes.get(position);
    }

    private class LoadViewHolder extends BaseAnecdoteViewHolder {

        LoadViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void setData(int position, Anecdote anecdote) {
            // This view is static, no need to change it's data
        }
    }

}
