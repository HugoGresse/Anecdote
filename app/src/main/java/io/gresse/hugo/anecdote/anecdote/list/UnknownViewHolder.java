package io.gresse.hugo.anecdote.anecdote.list;

import android.view.View;
import android.widget.LinearLayout;

import io.gresse.hugo.anecdote.databinding.RowAnecdoteUnknownBinding;

/**
 * Display ? in a view to be used by RecyclerView as an ViewHolder
 *
 * Created by Hugo Gresse on 08/11/2016.
 */
public class UnknownViewHolder extends MixedBaseViewHolder implements View.OnClickListener {

    public UnknownViewHolder(RowAnecdoteUnknownBinding binding,
                             AdapterListener adapterListener,
                             MixedContentAdapter adapter,
                             RowStyle rowStyle) {
        super(binding.getRoot(), binding.contentTextView, null, null, adapterListener, adapter, rowStyle);

        binding.openLinearLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!(v instanceof LinearLayout)) {
            super.onClick(v);
            return;
        }
        if (mAdapterListener != null) {
            mAdapterListener.onClick(
                    mAdapter.getItem(getBindingAdapterPosition()),
                    null, 0);
        }
    }
}
