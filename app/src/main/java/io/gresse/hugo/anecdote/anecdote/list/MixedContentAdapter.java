package io.gresse.hugo.anecdote.anecdote.list;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.anecdote.model.MediaType;
import io.gresse.hugo.anecdote.databinding.RowAnecdoteBinding;
import io.gresse.hugo.anecdote.databinding.RowAnecdoteImageBinding;
import io.gresse.hugo.anecdote.databinding.RowAnecdoteUnknownBinding;
import io.gresse.hugo.anecdote.databinding.RowAnecdoteVideoBinding;
import io.gresse.hugo.anecdote.databinding.RowLoaderBinding;

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


    private String          mWebsiteName;
    private List<Anecdote>  mAnecdotes;
    private boolean         mDisplayLoader;
    @Nullable
    private AdapterListener mAdapterListener;
    private RowStyle        mRowStyle = new RowStyle(0, false, 0, 0);
    private int mExpandedPosition = -1;

    MixedContentAdapter(@Nullable AdapterListener adapterListener, String websiteName) {
        mWebsiteName = websiteName;
        mAnecdotes = new ArrayList<>();
        mAdapterListener = adapterListener;
        mDisplayLoader = true;
        setHasStableIds(true);
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
                // Prevent recyclerView follow the loading wheel when first items are just added
                mAnecdotes.addAll(quotes);
                this.notifyDataSetChanged();
            }

        } else {
            // Run this on main thread
            new Handler(Looper.getMainLooper()).post(() -> setData(quotes));
        }
    }

    @Override
    public void setLoaderDisplay(boolean displayLoader){
        mDisplayLoader = displayLoader;
        notifyItemChanged(mAnecdotes.size());
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull BaseAnecdoteViewHolder holder) {
        holder.onViewDetached();
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void setTextStyle(int textSize, boolean rowStriping, int colorBackground, int colorBackgroundStripping) {
        mRowStyle = new RowStyle(textSize, rowStriping, colorBackground, colorBackgroundStripping);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BaseAnecdoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return switch (viewType) {
            case VIEW_TYPE_TEXT -> new MixedBaseViewHolder(
                    RowAnecdoteBinding.inflate(inflater, parent, false), mAdapterListener, this, mRowStyle);
            case VIEW_TYPE_IMAGE -> new ImageViewHolder(
                    RowAnecdoteImageBinding.inflate(inflater, parent, false), mAdapterListener, this, mRowStyle);
            case VIEW_TYPE_VIDEO -> new VideoViewHolder(
                    RowAnecdoteVideoBinding.inflate(inflater, parent, false), mAdapterListener, this, mRowStyle);
            case VIEW_TYPE_LOAD -> new LoadViewHolder(
                    RowLoaderBinding.inflate(inflater, parent, false).getRoot());
            default -> new UnknownViewHolder(
                    RowAnecdoteUnknownBinding.inflate(inflater, parent, false), mAdapterListener, this, mRowStyle);
        };
    }

    @Override
    public void onBindViewHolder(@NonNull BaseAnecdoteViewHolder holder, int position) {
        // already done in method below
    }

    @Override
    public void onBindViewHolder(@NonNull BaseAnecdoteViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (position < mAnecdotes.size()) {
            holder.setData(position, mExpandedPosition == position, mWebsiteName, mAnecdotes.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (!mAnecdotes.isEmpty() && !mDisplayLoader) {
            return mAnecdotes.size();
        }
        return mAnecdotes.size() + 1;
    }

    @Override
    public long getItemId(int position) {
        if(position < mAnecdotes.size()){
            return getItem(position).hashCode();
        } else if (position >= mAnecdotes.size()){
            return 2;
        } else {
            return 1;
        }
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
     *
     * @param position the expanded position
     */
    void toggleExpanded(int position) {
        if (mExpandedPosition == position) {
            mExpandedPosition = -1;
            notifyItemChanged(position);
            return;
        }

        // Notify expanded last position
        if (mExpandedPosition >= 0) {
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
        }

        @Override
        public void setData(int position, boolean isExpanded, String websiteName, Anecdote anecdote) {
            // This view is static, no need to change it's data
        }
    }

}
