package io.gresse.hugo.anecdote.anecdote.list;

import android.view.View;

import butterknife.Bind;
import io.gresse.hugo.anecdote.R;
import io.gresse.hugo.anecdote.anecdote.model.Anecdote;
import io.gresse.hugo.anecdote.view.PlayerView;

/**
 * Display videos in a view to be used by RecyclerView as an ViewHolder
 *
 * Created by Hugo Gresse on 08/11/2016.
 */
public class VideoViewHolder extends MixedBaseViewHolder implements View.OnClickListener {

    @Bind(R.id.exoplayerView)
    PlayerView mPlayerView;

    public VideoViewHolder(View itemView,
                           AdapterListener adapterListener,
                           MixedContentAdapter adapter,
                           int textSize,
                           boolean rowStriping,
                           int rowBackground,
                           int rawStripingBackground) {
        super(itemView, adapterListener, adapter, textSize, rowStriping, rowBackground, rawStripingBackground);

        if (mPlayerView != null) {
            mPlayerView.setOnClickListener(this);
        }
    }

    @Override
    public void setData(int position, Anecdote anecdote) {
        super.setData(position, anecdote);
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
        if (mAdapterListener != null) {
            mAdapterListener.onClick(
                    mAdapter.getItem(getAdapterPosition()),
                    mPlayerView,
                    AdapterListener.ACTION_FULLSCREEN);
        }
    }
}
