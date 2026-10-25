package io.gresse.hugo.anecdote.anecdote.list;

import androidx.core.view.ViewCompat;
import android.view.View;

import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.databinding.RowAnecdoteExpandBinding;
import io.gresse.hugo.anecdote.databinding.RowAnecdoteVideoBinding;
import io.gresse.hugo.anecdote.view.PlayerContainerView;

/**
 * Display videos in a view to be used by RecyclerView as an ViewHolder
 *
 * Created by Hugo Gresse on 08/11/2016.
 */
public class VideoViewHolder extends MixedBaseViewHolder implements View.OnClickListener {

    private final PlayerContainerView mPlayerView;

    public VideoViewHolder(RowAnecdoteVideoBinding binding,
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

        mPlayerView = binding.playerContainer;
        mPlayerView.setOnClickListener(this);
    }

    @Override
    public void setData(int position, boolean isExpanded, String websiteName, Anecdote anecdote) {
        super.setData(position, isExpanded, websiteName, anecdote);
        if (anecdote.media != null) {
            mPlayerView.setVideoUrl(anecdote.media);
            mPlayerView.setControllerDisplay(false);
        }

        ViewCompat.setTransitionName(mPlayerView, position + "_video");
    }

    @Override
    public void onClick(View v) {
        if (!(v instanceof PlayerContainerView)) {
            super.onClick(v);
            return;
        }
        if (mAdapterListener != null) {
            mAdapterListener.onClick(
                    mAdapter.getItem(getBindingAdapterPosition()),
                    mPlayerView,
                    AdapterListener.ACTION_FULLSCREEN);
        }
    }
}
