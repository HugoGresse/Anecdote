package io.gresse.hugo.anecdote.anecdote.list;

import android.view.View;
import android.widget.LinearLayout;

import butterknife.Bind;
import io.gresse.hugo.anecdote.R;

/**
 * Display ? in a view to be used by RecyclerView as an ViewHolder
 *
 * Created by Hugo Gresse on 08/11/2016.
 */
public class UnknownViewHolder extends MixedBaseViewHolder implements View.OnClickListener {

    @Bind(R.id.openLinearLayout)
    public LinearLayout mContainerLayout;

    public UnknownViewHolder(View itemView,
                             AdapterListener adapterListener,
                             MixedContentAdapter adapter,
                             int textSize,
                             boolean rowStriping,
                             int rowBackground,
                             int rawStripingBackground) {
        super(itemView, adapterListener, adapter, textSize, rowStriping, rowBackground, rawStripingBackground);

        mContainerLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!(v instanceof LinearLayout)) {
            super.onClick(v);
            return;
        }
        if (mAdapterListener != null) {
            mAdapterListener.onClick(
                    mAdapter.getItem(getAdapterPosition()),
                    null, 0);
        }
    }
}
